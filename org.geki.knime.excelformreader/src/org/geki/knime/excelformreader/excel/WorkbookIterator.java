package org.geki.knime.excelformreader.excel;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.geki.knime.excelformreader.ExcelFormReaderSettings.SheetFilterMode;
import org.geki.knime.excelformreader.ExcelFormReaderSettings.SheetSelection;
import org.geki.knime.excelformreader.domain.ReadingMode;
import org.knime.core.node.NodeLogger;

public class WorkbookIterator implements Iterator<WorkbookIterator.Entry>, Closeable {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(WorkbookIterator.class);

    public static final class Entry {
        public final Path filePath;
        public final String sheetName;
        public final Sheet sheet;
        public final Workbook workbook;

        Entry(final Path filePath, final String sheetName,
              final Sheet sheet, final Workbook workbook) {
            this.filePath = filePath;
            this.sheetName = sheetName;
            this.sheet = sheet;
            this.workbook = workbook;
        }
    }

    private final List<Path>      files;
    private final boolean         processManySheets;
    private final SheetSelection  sheetSelection;
    private final String          sheetName;
    private final int             sheetPosition;
    private final SheetFilterMode sheetFilterMode;
    private final Set<String>     sheetFilterNames;
    private final boolean         includeHiddenSheets;

    private int     fileIndex        = 0;
    private Workbook currentWorkbook = null;
    private Path    currentFilePath  = null;
    private final Deque<Integer> pendingSheetIndices = new ArrayDeque<>();

    // Pre-fetch state
    private Entry   cachedNext = null;
    private boolean fetched    = false;

    public WorkbookIterator(final Path rootPath,
                             final ReadingMode mode,
                             final boolean processManySheets,
                             final SheetSelection sheetSelection,
                             final String sheetName,
                             final int sheetPosition,
                             final SheetFilterMode sheetFilterMode,
                             final Set<String> sheetFilterNames,
                             final boolean includeHiddenSheets,
                             final boolean recursive,
                             final boolean includeHiddenFiles,
                             final boolean includeHiddenFolders,
                             final boolean filterByExtension,
                             final Set<String> fileExtensions) throws IOException {
        this.processManySheets  = processManySheets;
        this.sheetSelection     = (sheetSelection != null) ? sheetSelection : SheetSelection.FIRST;
        this.sheetName          = (sheetName != null) ? sheetName : "";
        this.sheetPosition      = sheetPosition;
        this.sheetFilterMode    = (sheetFilterMode != null) ? sheetFilterMode : SheetFilterMode.ALL;
        this.sheetFilterNames   = (sheetFilterNames != null) ? sheetFilterNames : Collections.emptySet();
        this.includeHiddenSheets = includeHiddenSheets;

        if (mode == ReadingMode.SINGLE_FILE) {
            this.files = new ArrayList<>(Collections.singletonList(rootPath));
        } else {
            if (recursive) {
                try (Stream<Path> stream = Files.walk(rootPath)) {
                    this.files = stream
                        .filter(p -> !Files.isDirectory(p))
                        .filter(p -> matchesExtension(p, filterByExtension, fileExtensions))
                        .filter(p -> includeHiddenFiles || !isHiddenSafe(p))
                        .filter(p -> includeHiddenFolders || !isInHiddenDirectory(p, rootPath))
                        .sorted()
                        .collect(Collectors.toList());
                }
            } else {
                try (Stream<Path> stream = Files.list(rootPath)) {
                    this.files = stream
                        .filter(Files::isRegularFile)
                        .filter(p -> matchesExtension(p, filterByExtension, fileExtensions))
                        .filter(p -> includeHiddenFiles || !isHiddenSafe(p))
                        .sorted()
                        .collect(Collectors.toList());
                }
            }
        }

        ensureFetched();
    }

    @Override
    public boolean hasNext() {
        ensureFetched();
        return cachedNext != null;
    }

    @Override
    public Entry next() {
        ensureFetched();
        if (cachedNext == null) {
            throw new NoSuchElementException("No more workbook entries");
        }
        final Entry result = cachedNext;
        cachedNext = null;
        fetched = false;
        return result;
    }

    @Override
    public void close() throws IOException {
        closeCurrentWorkbook();
    }

    private void ensureFetched() {
        if (!fetched) {
            cachedNext = findNext();
            fetched = true;
        }
    }

    // Advances internal state and returns the next valid entry, or null when exhausted.
    // The workbook is closed here only after all its pending sheets have been yielded,
    // so any entry's workbook is still open when the caller receives it.
    private Entry findNext() {
        while (true) {
            if (currentWorkbook != null) {
                if (!pendingSheetIndices.isEmpty()) {
                    final int idx = pendingSheetIndices.poll();
                    final Sheet sheet = currentWorkbook.getSheetAt(idx);
                    return new Entry(currentFilePath, sheet.getSheetName(), sheet, currentWorkbook);
                }
                // All pending sheets yielded — safe to close now because the last entry
                // from this workbook was already returned by the previous next() call.
                closeCurrentWorkbook();
            }

            if (fileIndex >= files.size()) {
                return null;
            }

            final Path file = files.get(fileIndex++);
            try {
                currentWorkbook = WorkbookFactory.create(file.toFile(), null, true);
                currentFilePath = file;
                pendingSheetIndices.clear();
                computePendingSheets();
            } catch (final IOException e) {
                LOGGER.warn("Cannot open workbook '" + file + "': " + e.getMessage());
            }
        }
    }

    private void computePendingSheets() {
        if (!processManySheets) {
            // Intent A: select exactly one sheet by SheetSelection
            switch (sheetSelection) {
                case FIRST:
                    for (int i = 0; i < currentWorkbook.getNumberOfSheets(); i++) {
                        final Sheet s = currentWorkbook.getSheetAt(i);
                        if (shouldIncludeSheet(s, currentWorkbook) && hasAnyRows(s)) {
                            pendingSheetIndices.add(i);
                            return;
                        }
                    }
                    LOGGER.warn("No suitable sheet found in '" + currentFilePath + "'");
                    break;

                case BY_NAME:
                    final Sheet named = currentWorkbook.getSheet(this.sheetName);
                    if (named == null) {
                        LOGGER.warn("Sheet '" + this.sheetName + "' not found in '"
                            + currentFilePath + "'");
                    } else if (!shouldIncludeSheet(named, currentWorkbook)) {
                        LOGGER.warn("Sheet '" + this.sheetName + "' is excluded in '"
                            + currentFilePath + "'");
                    } else {
                        pendingSheetIndices.add(currentWorkbook.getSheetIndex(this.sheetName));
                    }
                    break;

                case BY_POSITION:
                    if (sheetPosition >= currentWorkbook.getNumberOfSheets()) {
                        LOGGER.warn("Sheet position " + sheetPosition + " out of range in '"
                            + currentFilePath + "' (workbook has "
                            + currentWorkbook.getNumberOfSheets() + " sheet(s))");
                    } else {
                        final Sheet atPos = currentWorkbook.getSheetAt(sheetPosition);
                        if (!shouldIncludeSheet(atPos, currentWorkbook)) {
                            LOGGER.warn("Sheet at position " + sheetPosition
                                + " is excluded in '" + currentFilePath + "'");
                        } else {
                            pendingSheetIndices.add(sheetPosition);
                        }
                    }
                    break;

                default:
                    break;
            }
        } else {
            // Intent B: yield all sheets that pass shouldIncludeSheet()
            // (used for FOLDER mode and SINGLE_FILE "process many sheets")
            for (int i = 0; i < currentWorkbook.getNumberOfSheets(); i++) {
                final Sheet s = currentWorkbook.getSheetAt(i);
                if (shouldIncludeSheet(s, currentWorkbook)) {
                    pendingSheetIndices.add(i);
                }
            }
        }
    }

    private boolean shouldIncludeSheet(final Sheet sheet, final Workbook workbook) {
        final String name = sheet.getSheetName();

        if (!includeHiddenSheets) {
            final int idx = workbook.getSheetIndex(name);
            if (workbook.isSheetHidden(idx)) {
                return false;
            }
        }

        final String nameLower = name.trim().toLowerCase();
        final Set<String> filterNamesLower = sheetFilterNames.stream()
            .map(s -> s.trim().toLowerCase())
            .collect(Collectors.toSet());

        switch (sheetFilterMode) {
            case ALL:       return true;
            case BLACKLIST: return !filterNamesLower.contains(nameLower);
            case WHITELIST: return filterNamesLower.contains(nameLower);
            default:        return true;
        }
    }

    private static boolean hasAnyRows(final Sheet sheet) {
        final int max = Math.min(10, sheet.getLastRowNum() + 1);
        for (int r = 0; r < max; r++) {
            if (sheet.getRow(r) != null) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchesExtension(final Path path,
                                             final boolean filterByExtension,
                                             final Set<String> extensions) {
        if (!filterByExtension || extensions.isEmpty()) {
            return true;
        }
        final String name = path.getFileName().toString().toLowerCase();
        final int dot = name.lastIndexOf('.');
        return dot >= 0 && extensions.contains(name.substring(dot + 1));
    }

    private static boolean isHiddenSafe(final Path path) {
        try {
            return Files.isHidden(path);
        } catch (final IOException e) {
            return false;
        }
    }

    // Returns true if any directory component between rootPath (exclusive) and
    // path (exclusive) is hidden. Used to skip files inside hidden subdirectories.
    private static boolean isInHiddenDirectory(final Path path, final Path rootPath) {
        Path parent = path.getParent();
        while (parent != null && !parent.equals(rootPath)) {
            if (isHiddenSafe(parent)) {
                return true;
            }
            parent = parent.getParent();
        }
        return false;
    }

    private void closeCurrentWorkbook() {
        if (currentWorkbook != null) {
            try {
                currentWorkbook.close();
            } catch (final IOException e) {
                LOGGER.warn("Failed to close workbook '" + currentFilePath + "': " + e.getMessage());
            } finally {
                currentWorkbook = null;
                currentFilePath = null;
            }
        }
    }
}

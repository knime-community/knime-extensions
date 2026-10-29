package org.geki.knime.excelformreader.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.geki.knime.excelformreader.ExcelFormReaderSettings.SheetFilterMode;
import org.geki.knime.excelformreader.ExcelFormReaderSettings.SheetSelection;
import org.geki.knime.excelformreader.domain.ReadingMode;
import org.geki.knime.excelformreader.excel.WorkbookIterator;
import org.geki.knime.excelformreader.excel.WorkbookIterator.Entry;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class WorkbookIteratorTest {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    // --- fixture helpers ---

    private static final class SheetSpec {
        final String name;
        final boolean hasData;
        final boolean hidden;

        private SheetSpec(final String name, final boolean hasData, final boolean hidden) {
            this.name = name;
            this.hasData = hasData;
            this.hidden = hidden;
        }

        static SheetSpec data(final String name) { return new SheetSpec(name, true, false); }
        static SheetSpec blank(final String name) { return new SheetSpec(name, false, false); }
        static SheetSpec hiddenData(final String name) { return new SheetSpec(name, true, true); }
    }

    private Path writeWorkbook(final Path file, final SheetSpec... specs) throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            for (final SheetSpec spec : specs) {
                wb.createSheet(spec.name);
                if (spec.hasData) {
                    final Row r = wb.getSheet(spec.name).createRow(0);
                    r.createCell(0).setCellValue("data");
                }
                if (spec.hidden) {
                    wb.setSheetHidden(wb.getSheetIndex(spec.name), true);
                }
            }
            try (OutputStream os = Files.newOutputStream(file)) {
                wb.write(os);
            }
        }
        return file;
    }

    private Path writeCorruptFile(final Path file) throws IOException {
        Files.write(file, "not a real workbook".getBytes());
        return file;
    }

    /** Mutable holder for the WorkbookIterator's many constructor parameters, with sane defaults. */
    private static final class Params {
        Path rootPath;
        ReadingMode mode = ReadingMode.SINGLE_FILE;
        boolean processManySheets = false;
        SheetSelection sheetSelection = SheetSelection.FIRST;
        String sheetName = "";
        int sheetPosition = 0;
        SheetFilterMode sheetFilterMode = SheetFilterMode.ALL;
        Set<String> sheetFilterNames = Collections.emptySet();
        boolean includeHiddenSheets = false;
        boolean recursive = false;
        boolean includeHiddenFiles = false;
        boolean includeHiddenFolders = false;
        boolean filterByExtension = true;
        Set<String> fileExtensions = Collections.singleton("xlsx");
    }

    private WorkbookIterator build(final Params p) throws IOException {
        return new WorkbookIterator(p.rootPath, p.mode, p.processManySheets, p.sheetSelection,
            p.sheetName, p.sheetPosition, p.sheetFilterMode, p.sheetFilterNames,
            p.includeHiddenSheets, p.recursive, p.includeHiddenFiles, p.includeHiddenFolders,
            p.filterByExtension, p.fileExtensions);
    }

    private List<String> collectSheetNames(final WorkbookIterator it) {
        final List<String> names = new ArrayList<>();
        while (it.hasNext()) {
            names.add(it.next().sheetName);
        }
        return names;
    }

    // --- SINGLE_FILE + FIRST ---

    @Test
    public void testSingleFile_first_skipsBlankLeadingSheets() throws IOException {
        final Path file = writeWorkbook(tmp.getRoot().toPath().resolve("wb.xlsx"),
            SheetSpec.blank("Empty"), SheetSpec.data("HasData"));
        final Params p = new Params();
        p.rootPath = file;
        p.sheetSelection = SheetSelection.FIRST;
        final WorkbookIterator it = build(p);
        assertTrue(it.hasNext());
        final Entry e = it.next();
        assertEquals("HasData", e.sheetName);
        assertFalse(it.hasNext());
    }

    // --- SINGLE_FILE + BY_NAME ---

    @Test
    public void testSingleFile_byName_found() throws IOException {
        final Path file = writeWorkbook(tmp.getRoot().toPath().resolve("wb.xlsx"),
            SheetSpec.data("Alpha"), SheetSpec.data("Beta"));
        final Params p = new Params();
        p.rootPath = file;
        p.sheetSelection = SheetSelection.BY_NAME;
        p.sheetName = "Beta";
        final WorkbookIterator it = build(p);
        assertTrue(it.hasNext());
        assertEquals("Beta", it.next().sheetName);
        assertFalse(it.hasNext());
    }

    @Test
    public void testSingleFile_byName_notFound_skipsSilently() throws IOException {
        final Path file = writeWorkbook(tmp.getRoot().toPath().resolve("wb.xlsx"),
            SheetSpec.data("Alpha"));
        final Params p = new Params();
        p.rootPath = file;
        p.sheetSelection = SheetSelection.BY_NAME;
        p.sheetName = "DoesNotExist";
        final WorkbookIterator it = build(p);
        assertFalse(it.hasNext());
    }

    @Test
    public void testSingleFile_byName_excludedByWhitelist_skipsSilently() throws IOException {
        final Path file = writeWorkbook(tmp.getRoot().toPath().resolve("wb.xlsx"),
            SheetSpec.data("Alpha"));
        final Params p = new Params();
        p.rootPath = file;
        p.sheetSelection = SheetSelection.BY_NAME;
        p.sheetName = "Alpha";
        p.sheetFilterMode = SheetFilterMode.WHITELIST;
        p.sheetFilterNames = Collections.singleton("SomeOtherSheet");
        final WorkbookIterator it = build(p);
        assertFalse(it.hasNext());
    }

    // --- SINGLE_FILE + BY_POSITION ---

    @Test
    public void testSingleFile_byPosition_valid() throws IOException {
        final Path file = writeWorkbook(tmp.getRoot().toPath().resolve("wb.xlsx"),
            SheetSpec.data("Alpha"), SheetSpec.data("Beta"));
        final Params p = new Params();
        p.rootPath = file;
        p.sheetSelection = SheetSelection.BY_POSITION;
        p.sheetPosition = 1;
        final WorkbookIterator it = build(p);
        assertTrue(it.hasNext());
        assertEquals("Beta", it.next().sheetName);
    }

    @Test
    public void testSingleFile_byPosition_outOfRange_skipsSilently() throws IOException {
        final Path file = writeWorkbook(tmp.getRoot().toPath().resolve("wb.xlsx"),
            SheetSpec.data("Alpha"));
        final Params p = new Params();
        p.rootPath = file;
        p.sheetSelection = SheetSelection.BY_POSITION;
        p.sheetPosition = 5;
        final WorkbookIterator it = build(p);
        assertFalse(it.hasNext());
    }

    // --- SINGLE_FILE + process many sheets ---

    @Test
    public void testSingleFile_processManySheets_yieldsAllInOrder() throws IOException {
        final Path file = writeWorkbook(tmp.getRoot().toPath().resolve("wb.xlsx"),
            SheetSpec.data("Alpha"), SheetSpec.data("Beta"), SheetSpec.data("Gamma"));
        final Params p = new Params();
        p.rootPath = file;
        p.processManySheets = true;
        final WorkbookIterator it = build(p);
        assertEquals(Arrays.asList("Alpha", "Beta", "Gamma"), collectSheetNames(it));
    }

    // --- hidden sheet filtering ---

    @Test
    public void testHiddenSheet_excludedByDefault() throws IOException {
        final Path file = writeWorkbook(tmp.getRoot().toPath().resolve("wb.xlsx"),
            SheetSpec.hiddenData("Hidden"), SheetSpec.data("Visible"));
        final Params p = new Params();
        p.rootPath = file;
        p.processManySheets = true;
        p.includeHiddenSheets = false;
        final WorkbookIterator it = build(p);
        assertEquals(Collections.singletonList("Visible"), collectSheetNames(it));
    }

    @Test
    public void testHiddenSheet_includedWhenFlagSet() throws IOException {
        final Path file = writeWorkbook(tmp.getRoot().toPath().resolve("wb.xlsx"),
            SheetSpec.hiddenData("Hidden"), SheetSpec.data("Visible"));
        final Params p = new Params();
        p.rootPath = file;
        p.processManySheets = true;
        p.includeHiddenSheets = true;
        final WorkbookIterator it = build(p);
        assertEquals(new LinkedHashSet<>(Arrays.asList("Hidden", "Visible")),
            new LinkedHashSet<>(collectSheetNames(it)));
    }

    // --- sheet filter mode: whitelist / blacklist, case-insensitive + trimmed ---

    @Test
    public void testSheetFilterMode_whitelist_caseInsensitiveTrimmed() throws IOException {
        final Path file = writeWorkbook(tmp.getRoot().toPath().resolve("wb.xlsx"),
            SheetSpec.data("Alpha"), SheetSpec.data("Beta"));
        final Params p = new Params();
        p.rootPath = file;
        p.processManySheets = true;
        p.sheetFilterMode = SheetFilterMode.WHITELIST;
        p.sheetFilterNames = Collections.singleton("  alpha  ");
        final WorkbookIterator it = build(p);
        assertEquals(Collections.singletonList("Alpha"), collectSheetNames(it));
    }

    @Test
    public void testSheetFilterMode_blacklist() throws IOException {
        final Path file = writeWorkbook(tmp.getRoot().toPath().resolve("wb.xlsx"),
            SheetSpec.data("Alpha"), SheetSpec.data("Beta"));
        final Params p = new Params();
        p.rootPath = file;
        p.processManySheets = true;
        p.sheetFilterMode = SheetFilterMode.BLACKLIST;
        p.sheetFilterNames = Collections.singleton("Alpha");
        final WorkbookIterator it = build(p);
        assertEquals(Collections.singletonList("Beta"), collectSheetNames(it));
    }

    // --- FOLDER mode ---

    @Test
    public void testFolder_nonRecursive_onlyTopLevelFiles() throws IOException {
        final Path root = tmp.getRoot().toPath();
        writeWorkbook(root.resolve("top.xlsx"), SheetSpec.data("S1"));
        final Path subDir = Files.createDirectory(root.resolve("sub"));
        writeWorkbook(subDir.resolve("nested.xlsx"), SheetSpec.data("S2"));

        final Params p = new Params();
        p.rootPath = root;
        p.mode = ReadingMode.FOLDER;
        p.processManySheets = true;
        p.recursive = false;
        final WorkbookIterator it = build(p);
        assertEquals(Collections.singletonList("S1"), collectSheetNames(it));
    }

    @Test
    public void testFolder_recursive_findsNestedFiles() throws IOException {
        final Path root = tmp.getRoot().toPath();
        writeWorkbook(root.resolve("top.xlsx"), SheetSpec.data("S1"));
        final Path subDir = Files.createDirectory(root.resolve("sub"));
        writeWorkbook(subDir.resolve("nested.xlsx"), SheetSpec.data("S2"));

        final Params p = new Params();
        p.rootPath = root;
        p.mode = ReadingMode.FOLDER;
        p.processManySheets = true;
        p.recursive = true;
        final WorkbookIterator it = build(p);
        assertEquals(new LinkedHashSet<>(Arrays.asList("S1", "S2")),
            new LinkedHashSet<>(collectSheetNames(it)));
    }

    @Test
    public void testFolder_extensionFilter_matchesByFilenameOnly() throws IOException {
        final Path root = tmp.getRoot().toPath();
        writeWorkbook(root.resolve("keep.xlsx"), SheetSpec.data("Kept"));
        // Same valid xlsx bytes, but named with a non-matching extension.
        writeWorkbook(root.resolve("skip.csv"), SheetSpec.data("Skipped"));

        final Params p = new Params();
        p.rootPath = root;
        p.mode = ReadingMode.FOLDER;
        p.processManySheets = true;
        p.filterByExtension = true;
        p.fileExtensions = Collections.singleton("xlsx");
        final WorkbookIterator it = build(p);
        assertEquals(Collections.singletonList("Kept"), collectSheetNames(it));
    }

    @Test
    public void testFolder_hiddenFile_excludedByDefault() throws IOException {
        final Path root = tmp.getRoot().toPath();
        writeWorkbook(root.resolve("visible.xlsx"), SheetSpec.data("Visible"));
        writeWorkbook(root.resolve(".hidden.xlsx"), SheetSpec.data("HiddenFile"));

        final Params p = new Params();
        p.rootPath = root;
        p.mode = ReadingMode.FOLDER;
        p.processManySheets = true;
        p.includeHiddenFiles = false;
        final WorkbookIterator it = build(p);
        assertEquals(Collections.singletonList("Visible"), collectSheetNames(it));
    }

    @Test
    public void testFolder_hiddenFile_includedWhenFlagSet() throws IOException {
        final Path root = tmp.getRoot().toPath();
        writeWorkbook(root.resolve("visible.xlsx"), SheetSpec.data("Visible"));
        writeWorkbook(root.resolve(".hidden.xlsx"), SheetSpec.data("HiddenFile"));

        final Params p = new Params();
        p.rootPath = root;
        p.mode = ReadingMode.FOLDER;
        p.processManySheets = true;
        p.includeHiddenFiles = true;
        final WorkbookIterator it = build(p);
        assertEquals(new LinkedHashSet<>(Arrays.asList("Visible", "HiddenFile")),
            new LinkedHashSet<>(collectSheetNames(it)));
    }

    @Test
    public void testFolder_hiddenSubdirectory_excludedByDefault_recursive() throws IOException {
        final Path root = tmp.getRoot().toPath();
        writeWorkbook(root.resolve("visible.xlsx"), SheetSpec.data("Visible"));
        final Path hiddenDir = Files.createDirectory(root.resolve(".hiddenDir"));
        writeWorkbook(hiddenDir.resolve("inside.xlsx"), SheetSpec.data("InsideHidden"));

        final Params p = new Params();
        p.rootPath = root;
        p.mode = ReadingMode.FOLDER;
        p.processManySheets = true;
        p.recursive = true;
        p.includeHiddenFolders = false;
        final WorkbookIterator it = build(p);
        assertEquals(Collections.singletonList("Visible"), collectSheetNames(it));
    }

    @Test
    public void testFolder_hiddenSubdirectory_includedWhenFlagSet_recursive() throws IOException {
        final Path root = tmp.getRoot().toPath();
        writeWorkbook(root.resolve("visible.xlsx"), SheetSpec.data("Visible"));
        final Path hiddenDir = Files.createDirectory(root.resolve(".hiddenDir"));
        writeWorkbook(hiddenDir.resolve("inside.xlsx"), SheetSpec.data("InsideHidden"));

        final Params p = new Params();
        p.rootPath = root;
        p.mode = ReadingMode.FOLDER;
        p.processManySheets = true;
        p.recursive = true;
        p.includeHiddenFolders = true;
        final WorkbookIterator it = build(p);
        assertEquals(new LinkedHashSet<>(Arrays.asList("Visible", "InsideHidden")),
            new LinkedHashSet<>(collectSheetNames(it)));
    }

    @Test
    public void testFolder_nestedHiddenAncestor_excludedByDefault_recursive() throws IOException {
        // Hidden directory two levels up from the file - exercises the parent-chain walk.
        final Path root = tmp.getRoot().toPath();
        final Path hiddenDir = Files.createDirectory(root.resolve(".hiddenDir"));
        final Path nested = Files.createDirectory(hiddenDir.resolve("nested"));
        writeWorkbook(nested.resolve("deep.xlsx"), SheetSpec.data("Deep"));
        writeWorkbook(root.resolve("visible.xlsx"), SheetSpec.data("Visible"));

        final Params p = new Params();
        p.rootPath = root;
        p.mode = ReadingMode.FOLDER;
        p.processManySheets = true;
        p.recursive = true;
        p.includeHiddenFolders = false;
        final WorkbookIterator it = build(p);
        assertEquals(Collections.singletonList("Visible"), collectSheetNames(it));
    }

    @Test
    public void testFolder_corruptFile_skippedButIterationContinues() throws IOException {
        final Path root = tmp.getRoot().toPath();
        writeCorruptFile(root.resolve("bad.xlsx"));
        writeWorkbook(root.resolve("good.xlsx"), SheetSpec.data("Good"));

        final Params p = new Params();
        p.rootPath = root;
        p.mode = ReadingMode.FOLDER;
        p.processManySheets = true;
        final WorkbookIterator it = build(p);
        assertEquals(Collections.singletonList("Good"), collectSheetNames(it));
    }

    @Test
    public void testFolder_empty_hasNextFalseImmediately() throws IOException {
        final Params p = new Params();
        p.rootPath = tmp.getRoot().toPath();
        p.mode = ReadingMode.FOLDER;
        p.processManySheets = true;
        final WorkbookIterator it = build(p);
        assertFalse(it.hasNext());
    }

    // --- workbook lifecycle ---

    @Test
    public void testWorkbookLifecycle_sameWorkbookAcrossSheetsOfOneFile() throws IOException {
        final Path file = writeWorkbook(tmp.getRoot().toPath().resolve("wb.xlsx"),
            SheetSpec.data("Alpha"), SheetSpec.data("Beta"));
        final Params p = new Params();
        p.rootPath = file;
        p.processManySheets = true;
        final WorkbookIterator it = build(p);

        final Entry first = it.next();
        assertEquals("data", first.sheet.getRow(0).getCell(0).getStringCellValue());
        final Entry second = it.next();
        assertSame("Both sheets of the same file must share the same open workbook",
            first.workbook, second.workbook);
        // The first entry's sheet must still be readable after fetching the second entry.
        assertEquals("data", first.sheet.getRow(0).getCell(0).getStringCellValue());
    }

    @Test
    public void testClose_midIteration_doesNotThrow() throws IOException {
        final Path file = writeWorkbook(tmp.getRoot().toPath().resolve("wb.xlsx"),
            SheetSpec.data("Alpha"), SheetSpec.data("Beta"));
        final Params p = new Params();
        p.rootPath = file;
        p.processManySheets = true;
        final WorkbookIterator it = build(p);
        it.next();
        it.close();
    }
}

package org.geki.knime.excelformreader;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

public class ExcelFormReaderSettings {

    // ── Settings keys ────────────────────────────────────────────────────────

    private static final String CFG_INPUT_MODE                = "cfg_inputMode";
    private static final String CFG_FILE_PATH                 = "cfg_filePath";
    private static final String CFG_FILE_SHEET_SELECTION      = "cfg_fileSheetSelection";
    private static final String CFG_FILE_SHEET_NAME           = "cfg_fileSheetName";
    private static final String CFG_FILE_SHEET_POSITION       = "cfg_fileSheetPosition";
    private static final String CFG_FILE_HIDDEN_SHEETS        = "cfg_fileHiddenSheets";
    private static final String CFG_FILE_SHEET_FILTER_MODE    = "cfg_fileSheetFilterMode";
    private static final String CFG_FILE_SHEET_FILTER_NAMES   = "cfg_fileSheetFilterNames";
    private static final String CFG_FOLDER_PATH               = "cfg_folderPath";
    private static final String CFG_RECURSIVE                 = "cfg_recursive";
    private static final String CFG_INCLUDE_HIDDEN_FOLDERS    = "cfg_includeHiddenFolders";
    private static final String CFG_FILTER_BY_EXTENSION       = "cfg_filterByExtension";
    private static final String CFG_FILE_EXTENSIONS           = "cfg_fileExtensions";
    private static final String CFG_INCLUDE_HIDDEN_FILES      = "cfg_includeHiddenFiles";
    private static final String CFG_FOLDER_HIDDEN_SHEETS      = "cfg_folderHiddenSheets";
    private static final String CFG_FOLDER_SHEET_FILTER_MODE  = "cfg_folderSheetFilterMode";
    private static final String CFG_FOLDER_SHEET_FILTER_NAMES = "cfg_folderSheetFilterNames";
    private static final String CFG_OUTPUT_FORMAT             = "cfg_outputFormat";
    private static final String CFG_INCLUDE_SOURCE_FILENAME   = "cfg_includeSourceFilename";
    private static final String CFG_INCLUDE_SHEET_NAME        = "cfg_includeSheetName";
    private static final String CFG_ON_MISSING_CELL              = "cfg_onMissingCell";
    private static final String CFG_ON_BAD_VALUE                = "cfg_onBadValue";
    private static final String CFG_FILE_MANY_SHEETS            = "cfg_fileManySheets";
    private static final String CFG_FILE_SINGLE_HIDDEN_SHEETS   = "cfg_fileSingleHiddenSheets";
    private static final String CFG_FOLDER_MANY_SHEETS          = "cfg_folderManySheets";
    private static final String CFG_FOLDER_SHEET_SELECTION      = "cfg_folderSheetSelection";
    private static final String CFG_FOLDER_SHEET_NAME           = "cfg_folderSheetName";
    private static final String CFG_FOLDER_SHEET_POSITION       = "cfg_folderSheetPosition";
    private static final String CFG_FOLDER_SINGLE_HIDDEN_SHEETS = "cfg_folderSingleHiddenSheets";
    private static final String CFG_INCLUDE_LABEL_FIELDS        = "cfg_includeLabelFields";
    private static final String CFG_OUTPUT_LABEL_PORT           = "cfg_outputLabelPort";

    // ── Enums ────────────────────────────────────────────────────────────────

    public enum InputMode {
        SINGLE_FILE("Single File"),
        FOLDER("Folder");

        private final String label;
        InputMode(final String label) { this.label = label; }
        public String getLabel() { return label; }
        public static InputMode fromString(final String s) {
            for (final InputMode v : values()) {
                if (v.name().equalsIgnoreCase(s)) { return v; }
            }
            return SINGLE_FILE;
        }
    }

    public enum SheetSelection {
        FIRST("First"),
        BY_NAME("By name"),
        BY_POSITION("By position");

        private final String label;
        SheetSelection(final String label) { this.label = label; }
        public String getLabel() { return label; }
        public static SheetSelection fromString(final String s) {
            for (final SheetSelection v : values()) {
                if (v.name().equalsIgnoreCase(s)) { return v; }
            }
            return FIRST;
        }
    }

    public enum SheetFilterMode {
        ALL("All"),
        BLACKLIST("Blacklist"),
        WHITELIST("Whitelist");

        private final String label;
        SheetFilterMode(final String label) { this.label = label; }
        public String getLabel() { return label; }
        public static SheetFilterMode fromString(final String s) {
            for (final SheetFilterMode v : values()) {
                if (v.name().equalsIgnoreCase(s)) { return v; }
            }
            return ALL;
        }
    }

    public enum OutputFormat {
        WIDE("Wide"),
        LONG("Long");

        private final String label;
        OutputFormat(final String label) { this.label = label; }
        public String getLabel() { return label; }
        public static OutputFormat fromString(final String s) {
            for (final OutputFormat v : values()) {
                if (v.name().equalsIgnoreCase(s)) { return v; }
            }
            return WIDE;
        }
    }

    public enum ErrorHandling {
        WARN("Warn and insert missing value"),
        FAIL("Fail");

        private final String label;
        ErrorHandling(final String label) { this.label = label; }
        public String getLabel() { return label; }
        public static ErrorHandling fromString(final String s) {
            for (final ErrorHandling v : values()) {
                if (v.name().equalsIgnoreCase(s)) { return v; }
            }
            return WARN;
        }
    }

    // ── Settings models ──────────────────────────────────────────────────────

    private final SettingsModelString  m_inputMode              = new SettingsModelString(CFG_INPUT_MODE, "SINGLE_FILE");
    private final SettingsModelString  m_filePath               = new SettingsModelString(CFG_FILE_PATH, "");
    private final SettingsModelString  m_fileSheetSelection     = new SettingsModelString(CFG_FILE_SHEET_SELECTION, "FIRST");
    private final SettingsModelString  m_fileSheetName          = new SettingsModelString(CFG_FILE_SHEET_NAME, "");
    private final SettingsModelInteger m_fileSheetPosition      = new SettingsModelInteger(CFG_FILE_SHEET_POSITION, 0);
    private final SettingsModelBoolean m_fileHiddenSheets       = new SettingsModelBoolean(CFG_FILE_HIDDEN_SHEETS, false);
    private final SettingsModelString  m_fileSheetFilterMode    = new SettingsModelString(CFG_FILE_SHEET_FILTER_MODE, "ALL");
    private final SettingsModelString  m_fileSheetFilterNames   = new SettingsModelString(CFG_FILE_SHEET_FILTER_NAMES, "");
    private final SettingsModelString  m_folderPath             = new SettingsModelString(CFG_FOLDER_PATH, "");
    private final SettingsModelBoolean m_recursive              = new SettingsModelBoolean(CFG_RECURSIVE, false);
    private final SettingsModelBoolean m_includeHiddenFolders   = new SettingsModelBoolean(CFG_INCLUDE_HIDDEN_FOLDERS, false);
    private final SettingsModelBoolean m_filterByExtension      = new SettingsModelBoolean(CFG_FILTER_BY_EXTENSION, true);
    private final SettingsModelString  m_fileExtensions         = new SettingsModelString(CFG_FILE_EXTENSIONS, "xlsx");
    private final SettingsModelBoolean m_includeHiddenFiles     = new SettingsModelBoolean(CFG_INCLUDE_HIDDEN_FILES, false);
    private final SettingsModelBoolean m_folderHiddenSheets     = new SettingsModelBoolean(CFG_FOLDER_HIDDEN_SHEETS, false);
    private final SettingsModelString  m_folderSheetFilterMode  = new SettingsModelString(CFG_FOLDER_SHEET_FILTER_MODE, "ALL");
    private final SettingsModelString  m_folderSheetFilterNames = new SettingsModelString(CFG_FOLDER_SHEET_FILTER_NAMES, "");
    private final SettingsModelString  m_outputFormat           = new SettingsModelString(CFG_OUTPUT_FORMAT, "WIDE");
    private final SettingsModelBoolean m_includeSourceFilename  = new SettingsModelBoolean(CFG_INCLUDE_SOURCE_FILENAME, true);
    private final SettingsModelBoolean m_includeSheetName       = new SettingsModelBoolean(CFG_INCLUDE_SHEET_NAME, true);
    private final SettingsModelString  m_onMissingCell          = new SettingsModelString(CFG_ON_MISSING_CELL, "WARN");
    private final SettingsModelString  m_onBadValue             = new SettingsModelString(CFG_ON_BAD_VALUE, "WARN");
    private final SettingsModelBoolean m_fileManySheets          = new SettingsModelBoolean(CFG_FILE_MANY_SHEETS, false);
    private final SettingsModelBoolean m_fileSingleHiddenSheets  = new SettingsModelBoolean(CFG_FILE_SINGLE_HIDDEN_SHEETS, false);
    private final SettingsModelBoolean m_folderManySheets         = new SettingsModelBoolean(CFG_FOLDER_MANY_SHEETS, false);
    private final SettingsModelString  m_folderSheetSelection     = new SettingsModelString(CFG_FOLDER_SHEET_SELECTION, "FIRST");
    private final SettingsModelString  m_folderSheetName          = new SettingsModelString(CFG_FOLDER_SHEET_NAME, "");
    private final SettingsModelInteger m_folderSheetPosition      = new SettingsModelInteger(CFG_FOLDER_SHEET_POSITION, 0);
    private final SettingsModelBoolean m_folderSingleHiddenSheets = new SettingsModelBoolean(CFG_FOLDER_SINGLE_HIDDEN_SHEETS, false);
    private final SettingsModelBoolean m_includeLabelFields       = new SettingsModelBoolean(CFG_INCLUDE_LABEL_FIELDS, false);
    private final SettingsModelBoolean m_outputLabelPort          = new SettingsModelBoolean(CFG_OUTPUT_LABEL_PORT, true);

    // ── Typed getters ────────────────────────────────────────────────────────

    public InputMode getInputMode() {
        return InputMode.fromString(m_inputMode.getStringValue());
    }

    public String getFilePath() { return m_filePath.getStringValue(); }

    public SheetSelection getFileSheetSelection() {
        return SheetSelection.fromString(m_fileSheetSelection.getStringValue());
    }

    public String getFileSheetName() { return m_fileSheetName.getStringValue(); }

    public int getFileSheetPosition() { return m_fileSheetPosition.getIntValue(); }

    public boolean isFileIncludeHiddenSheets() { return m_fileHiddenSheets.getBooleanValue(); }

    public SheetFilterMode getFileSheetFilterMode() {
        return SheetFilterMode.fromString(m_fileSheetFilterMode.getStringValue());
    }

    public Set<String> getFileSheetFilterNames() {
        return splitToSet(m_fileSheetFilterNames.getStringValue());
    }

    public String getFolderPath() { return m_folderPath.getStringValue(); }

    public boolean isRecursive() { return m_recursive.getBooleanValue(); }

    public boolean isIncludeHiddenFolders() { return m_includeHiddenFolders.getBooleanValue(); }

    public boolean isFilterByExtension() { return m_filterByExtension.getBooleanValue(); }

    public Set<String> getFileExtensions() {
        return splitToSet(m_fileExtensions.getStringValue());
    }

    public boolean isIncludeHiddenFiles() { return m_includeHiddenFiles.getBooleanValue(); }

    public boolean isFolderIncludeHiddenSheets() { return m_folderHiddenSheets.getBooleanValue(); }

    public SheetFilterMode getFolderSheetFilterMode() {
        return SheetFilterMode.fromString(m_folderSheetFilterMode.getStringValue());
    }

    public Set<String> getFolderSheetFilterNames() {
        return splitToSet(m_folderSheetFilterNames.getStringValue());
    }

    public OutputFormat getOutputFormat() {
        return OutputFormat.fromString(m_outputFormat.getStringValue());
    }

    public boolean isIncludeSourceFilename() { return m_includeSourceFilename.getBooleanValue(); }

    public boolean isIncludeSheetName() { return m_includeSheetName.getBooleanValue(); }

    public ErrorHandling getOnMissingCell() {
        return ErrorHandling.fromString(m_onMissingCell.getStringValue());
    }

    public ErrorHandling getOnBadValue() {
        return ErrorHandling.fromString(m_onBadValue.getStringValue());
    }

    public boolean getFileManySheets() { return m_fileManySheets.getBooleanValue(); }
    public boolean isFileSingleIncludeHiddenSheets() { return m_fileSingleHiddenSheets.getBooleanValue(); }

    public boolean getFolderManySheets() { return m_folderManySheets.getBooleanValue(); }
    public SheetSelection getFolderSheetSelection() { return SheetSelection.fromString(m_folderSheetSelection.getStringValue()); }
    public String getFolderSheetName() { return m_folderSheetName.getStringValue(); }
    public int getFolderSheetPosition() { return m_folderSheetPosition.getIntValue(); }
    public boolean isFolderSingleIncludeHiddenSheets() { return m_folderSingleHiddenSheets.getBooleanValue(); }
    public boolean isIncludeLabelFields()               { return m_includeLabelFields.getBooleanValue(); }
    public boolean isOutputLabelPort()                  { return m_outputLabelPort.getBooleanValue(); }

    // ── Raw model getters ─────────────────────────────────────────────────────

    public SettingsModelString  getInputModeModel()              { return m_inputMode; }
    public SettingsModelString  getFilePathModel()               { return m_filePath; }
    public SettingsModelString  getFileSheetSelectionModel()     { return m_fileSheetSelection; }
    public SettingsModelString  getFileSheetNameModel()          { return m_fileSheetName; }
    public SettingsModelInteger getFileSheetPositionModel()      { return m_fileSheetPosition; }
    public SettingsModelBoolean getFileHiddenSheetsModel()       { return m_fileHiddenSheets; }
    public SettingsModelString  getFileSheetFilterModeModel()    { return m_fileSheetFilterMode; }
    public SettingsModelString  getFileSheetFilterNamesModel()   { return m_fileSheetFilterNames; }
    public SettingsModelString  getFolderPathModel()             { return m_folderPath; }
    public SettingsModelBoolean getRecursiveModel()              { return m_recursive; }
    public SettingsModelBoolean getIncludeHiddenFoldersModel()   { return m_includeHiddenFolders; }
    public SettingsModelBoolean getFilterByExtensionModel()      { return m_filterByExtension; }
    public SettingsModelString  getFileExtensionsModel()         { return m_fileExtensions; }
    public SettingsModelBoolean getIncludeHiddenFilesModel()     { return m_includeHiddenFiles; }
    public SettingsModelBoolean getFolderHiddenSheetsModel()     { return m_folderHiddenSheets; }
    public SettingsModelString  getFolderSheetFilterModeModel()  { return m_folderSheetFilterMode; }
    public SettingsModelString  getFolderSheetFilterNamesModel() { return m_folderSheetFilterNames; }
    public SettingsModelString  getOutputFormatModel()           { return m_outputFormat; }
    public SettingsModelBoolean getIncludeSourceFilenameModel()  { return m_includeSourceFilename; }
    public SettingsModelBoolean getIncludeSheetNameModel()       { return m_includeSheetName; }
    public SettingsModelString  getOnMissingCellModel()            { return m_onMissingCell; }
    public SettingsModelString  getOnBadValueModel()               { return m_onBadValue; }
    public SettingsModelBoolean getFileManySheeetsModel()          { return m_fileManySheets; }
    public SettingsModelBoolean getFileSingleHiddenSheetsModel()   { return m_fileSingleHiddenSheets; }
    public SettingsModelBoolean getFolderManySheetsModel()          { return m_folderManySheets; }
    public SettingsModelString  getFolderSheetSelectionModel()      { return m_folderSheetSelection; }
    public SettingsModelString  getFolderSheetNameModel()           { return m_folderSheetName; }
    public SettingsModelInteger getFolderSheetPositionModel()       { return m_folderSheetPosition; }
    public SettingsModelBoolean getFolderSingleHiddenSheetsModel()  { return m_folderSingleHiddenSheets; }
    public SettingsModelBoolean getIncludeLabelFieldsModel()        { return m_includeLabelFields; }
    public SettingsModelBoolean getOutputLabelPortModel()           { return m_outputLabelPort; }

    // ── Persistence ───────────────────────────────────────────────────────────

    public void saveSettings(final NodeSettingsWO settings) {
        m_inputMode.saveSettingsTo(settings);
        m_filePath.saveSettingsTo(settings);
        m_fileSheetSelection.saveSettingsTo(settings);
        m_fileSheetName.saveSettingsTo(settings);
        m_fileSheetPosition.saveSettingsTo(settings);
        m_fileHiddenSheets.saveSettingsTo(settings);
        m_fileSheetFilterMode.saveSettingsTo(settings);
        m_fileSheetFilterNames.saveSettingsTo(settings);
        m_folderPath.saveSettingsTo(settings);
        m_recursive.saveSettingsTo(settings);
        m_includeHiddenFolders.saveSettingsTo(settings);
        m_filterByExtension.saveSettingsTo(settings);
        m_fileExtensions.saveSettingsTo(settings);
        m_includeHiddenFiles.saveSettingsTo(settings);
        m_folderHiddenSheets.saveSettingsTo(settings);
        m_folderSheetFilterMode.saveSettingsTo(settings);
        m_folderSheetFilterNames.saveSettingsTo(settings);
        m_outputFormat.saveSettingsTo(settings);
        m_includeSourceFilename.saveSettingsTo(settings);
        m_includeSheetName.saveSettingsTo(settings);
        m_onMissingCell.saveSettingsTo(settings);
        m_onBadValue.saveSettingsTo(settings);
        m_fileManySheets.saveSettingsTo(settings);
        m_fileSingleHiddenSheets.saveSettingsTo(settings);
        m_folderManySheets.saveSettingsTo(settings);
        m_folderSheetSelection.saveSettingsTo(settings);
        m_folderSheetName.saveSettingsTo(settings);
        m_folderSheetPosition.saveSettingsTo(settings);
        m_folderSingleHiddenSheets.saveSettingsTo(settings);
        m_includeLabelFields.saveSettingsTo(settings);
        m_outputLabelPort.saveSettingsTo(settings);
    }

    public void loadSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_inputMode.loadSettingsFrom(settings);
        m_filePath.loadSettingsFrom(settings);
        m_fileSheetSelection.loadSettingsFrom(settings);
        m_fileSheetName.loadSettingsFrom(settings);
        m_fileSheetPosition.loadSettingsFrom(settings);
        m_fileHiddenSheets.loadSettingsFrom(settings);
        m_fileSheetFilterMode.loadSettingsFrom(settings);
        m_fileSheetFilterNames.loadSettingsFrom(settings);
        m_folderPath.loadSettingsFrom(settings);
        m_recursive.loadSettingsFrom(settings);
        m_includeHiddenFolders.loadSettingsFrom(settings);
        m_filterByExtension.loadSettingsFrom(settings);
        m_fileExtensions.loadSettingsFrom(settings);
        m_includeHiddenFiles.loadSettingsFrom(settings);
        m_folderHiddenSheets.loadSettingsFrom(settings);
        m_folderSheetFilterMode.loadSettingsFrom(settings);
        m_folderSheetFilterNames.loadSettingsFrom(settings);
        m_outputFormat.loadSettingsFrom(settings);
        m_includeSourceFilename.loadSettingsFrom(settings);
        m_includeSheetName.loadSettingsFrom(settings);
        m_onMissingCell.loadSettingsFrom(settings);
        m_onBadValue.loadSettingsFrom(settings);
        m_fileManySheets.loadSettingsFrom(settings);
        m_fileSingleHiddenSheets.loadSettingsFrom(settings);
        m_folderManySheets.loadSettingsFrom(settings);
        m_folderSheetSelection.loadSettingsFrom(settings);
        m_folderSheetName.loadSettingsFrom(settings);
        m_folderSheetPosition.loadSettingsFrom(settings);
        m_folderSingleHiddenSheets.loadSettingsFrom(settings);
        m_includeLabelFields.loadSettingsFrom(settings);
        m_outputLabelPort.loadSettingsFrom(settings);
    }

    public void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_inputMode.validateSettings(settings);
        m_filePath.validateSettings(settings);
        m_fileSheetSelection.validateSettings(settings);
        m_fileSheetName.validateSettings(settings);
        m_fileSheetPosition.validateSettings(settings);
        m_fileHiddenSheets.validateSettings(settings);
        m_fileSheetFilterMode.validateSettings(settings);
        m_fileSheetFilterNames.validateSettings(settings);
        m_folderPath.validateSettings(settings);
        m_recursive.validateSettings(settings);
        m_includeHiddenFolders.validateSettings(settings);
        m_filterByExtension.validateSettings(settings);
        m_fileExtensions.validateSettings(settings);
        m_includeHiddenFiles.validateSettings(settings);
        m_folderHiddenSheets.validateSettings(settings);
        m_folderSheetFilterMode.validateSettings(settings);
        m_folderSheetFilterNames.validateSettings(settings);
        m_outputFormat.validateSettings(settings);
        m_includeSourceFilename.validateSettings(settings);
        m_includeSheetName.validateSettings(settings);
        m_onMissingCell.validateSettings(settings);
        m_onBadValue.validateSettings(settings);
        m_fileManySheets.validateSettings(settings);
        m_fileSingleHiddenSheets.validateSettings(settings);
        m_folderManySheets.validateSettings(settings);
        m_folderSheetSelection.validateSettings(settings);
        m_folderSheetName.validateSettings(settings);
        m_folderSheetPosition.validateSettings(settings);
        m_folderSingleHiddenSheets.validateSettings(settings);
        m_includeLabelFields.validateSettings(settings);
        m_outputLabelPort.validateSettings(settings);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private static Set<String> splitToSet(final String s) {
        return Arrays.stream(s.split(","))
            .map(t -> t.trim().toLowerCase())
            .filter(t -> !t.isEmpty())
            .collect(Collectors.toSet());
    }
}

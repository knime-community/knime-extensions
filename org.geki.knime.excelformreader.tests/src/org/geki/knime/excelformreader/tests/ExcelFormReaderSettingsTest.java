package org.geki.knime.excelformreader.tests;

import static org.junit.Assert.assertEquals;

import org.geki.knime.excelformreader.ExcelFormReaderSettings;
import org.geki.knime.excelformreader.ExcelFormReaderSettings.ErrorHandling;
import org.geki.knime.excelformreader.ExcelFormReaderSettings.InputMode;
import org.geki.knime.excelformreader.ExcelFormReaderSettings.OutputFormat;
import org.geki.knime.excelformreader.ExcelFormReaderSettings.SheetFilterMode;
import org.geki.knime.excelformreader.ExcelFormReaderSettings.SheetSelection;
import org.junit.Test;
import org.knime.core.node.NodeSettings;

public class ExcelFormReaderSettingsTest {

    // --- InputMode ---

    @Test
    public void testInputMode_fromString_validCaseInsensitive() {
        assertEquals(InputMode.SINGLE_FILE, InputMode.fromString("single_file"));
        assertEquals(InputMode.FOLDER, InputMode.fromString("FOLDER"));
    }

    @Test
    public void testInputMode_fromString_unrecognized_silentlyDefaults() {
        // Unlike ReadingMode.fromString (which throws), these settings enums never throw -
        // an unrecognized or null value quietly falls back to the first/default constant.
        assertEquals(InputMode.SINGLE_FILE, InputMode.fromString("not_a_mode"));
        assertEquals(InputMode.SINGLE_FILE, InputMode.fromString(null));
    }

    // --- SheetSelection ---

    @Test
    public void testSheetSelection_fromString_validCaseInsensitive() {
        assertEquals(SheetSelection.BY_NAME, SheetSelection.fromString("by_name"));
        assertEquals(SheetSelection.BY_POSITION, SheetSelection.fromString("BY_POSITION"));
    }

    @Test
    public void testSheetSelection_fromString_unrecognized_silentlyDefaultsToFirst() {
        assertEquals(SheetSelection.FIRST, SheetSelection.fromString("bogus"));
        assertEquals(SheetSelection.FIRST, SheetSelection.fromString(null));
    }

    // --- SheetFilterMode ---

    @Test
    public void testSheetFilterMode_fromString_validCaseInsensitive() {
        assertEquals(SheetFilterMode.WHITELIST, SheetFilterMode.fromString("whitelist"));
        assertEquals(SheetFilterMode.BLACKLIST, SheetFilterMode.fromString("BLACKLIST"));
    }

    @Test
    public void testSheetFilterMode_fromString_unrecognized_silentlyDefaultsToAll() {
        assertEquals(SheetFilterMode.ALL, SheetFilterMode.fromString("bogus"));
        assertEquals(SheetFilterMode.ALL, SheetFilterMode.fromString(null));
    }

    // --- OutputFormat ---

    @Test
    public void testOutputFormat_fromString_validCaseInsensitive() {
        assertEquals(OutputFormat.LONG, OutputFormat.fromString("long"));
        assertEquals(OutputFormat.WIDE, OutputFormat.fromString("WIDE"));
    }

    @Test
    public void testOutputFormat_fromString_unrecognized_silentlyDefaultsToWide() {
        assertEquals(OutputFormat.WIDE, OutputFormat.fromString("bogus"));
        assertEquals(OutputFormat.WIDE, OutputFormat.fromString(null));
    }

    // --- ErrorHandling ---

    @Test
    public void testErrorHandling_fromString_validCaseInsensitive() {
        assertEquals(ErrorHandling.FAIL, ErrorHandling.fromString("fail"));
        assertEquals(ErrorHandling.WARN, ErrorHandling.fromString("WARN"));
    }

    @Test
    public void testErrorHandling_fromString_unrecognized_silentlyDefaultsToWarn() {
        assertEquals(ErrorHandling.WARN, ErrorHandling.fromString("bogus"));
        assertEquals(ErrorHandling.WARN, ErrorHandling.fromString(null));
    }

    // --- save/load round trip (spot check: one String, one Boolean, one Integer field) ---

    @Test
    public void testSaveLoad_roundTrip() throws Exception {
        final ExcelFormReaderSettings settings = new ExcelFormReaderSettings();
        settings.getFilePathModel().setStringValue("/some/path.xlsx");
        settings.getRecursiveModel().setBooleanValue(true);
        settings.getFileSheetPositionModel().setIntValue(3);

        final NodeSettings ns = new NodeSettings("test");
        settings.saveSettings(ns);

        final ExcelFormReaderSettings loaded = new ExcelFormReaderSettings();
        loaded.loadSettings(ns);

        assertEquals("/some/path.xlsx", loaded.getFilePath());
        assertEquals(true, loaded.isRecursive());
        assertEquals(3, loaded.getFileSheetPosition());
    }
}

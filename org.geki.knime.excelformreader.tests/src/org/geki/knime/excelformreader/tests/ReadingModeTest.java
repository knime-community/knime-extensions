package org.geki.knime.excelformreader.tests;

import static org.junit.Assert.assertEquals;

import org.geki.knime.excelformreader.domain.ReadingMode;
import org.junit.Test;

public class ReadingModeTest {

    // --- fromString ---

    @Test
    public void testFromString_singleFile() {
        assertEquals(ReadingMode.SINGLE_FILE, ReadingMode.fromString("Single File"));
    }

    @Test
    public void testFromString_folder() {
        assertEquals(ReadingMode.FOLDER, ReadingMode.fromString("Folder"));
    }

    @Test
    public void testFromString_caseInsensitive() {
        assertEquals(ReadingMode.SINGLE_FILE, ReadingMode.fromString("single file"));
        assertEquals(ReadingMode.FOLDER, ReadingMode.fromString("FOLDER"));
    }

    @Test
    public void testFromString_trimmed() {
        assertEquals(ReadingMode.FOLDER, ReadingMode.fromString("  Folder  "));
    }

    // --- invalid inputs ---
    // Note: unlike ExcelFormReaderSettings's nested enums (which silently default to a
    // fallback value on unrecognized input), ReadingMode.fromString throws instead.

    @Test(expected = IllegalArgumentException.class)
    public void testFromString_null_throws() {
        ReadingMode.fromString(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromString_unknown_throws() {
        ReadingMode.fromString("Not A Mode");
    }
}

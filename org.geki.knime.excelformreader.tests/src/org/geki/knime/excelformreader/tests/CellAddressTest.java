package org.geki.knime.excelformreader.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.geki.knime.excelformreader.domain.CellAddress;
import org.junit.Test;

public class CellAddressTest {

    // --- single cell ---

    @Test
    public void testParseSingleCell_A1() {
        final CellAddress addr = CellAddress.parse("A1");
        assertEquals(0, addr.getStartCol());
        assertEquals(0, addr.getStartRow());
        assertEquals(0, addr.getEndCol());
        assertEquals(0, addr.getEndRow());
        assertTrue(addr.isSingleCell());
        assertFalse(addr.isRange());
    }

    @Test
    public void testParseSingleCell_C4() {
        final CellAddress addr = CellAddress.parse("C4");
        assertEquals(2, addr.getStartCol());
        assertEquals(3, addr.getStartRow());
        assertTrue(addr.isSingleCell());
    }

    @Test
    public void testParseSingleCell_E14() {
        final CellAddress addr = CellAddress.parse("E14");
        assertEquals(4, addr.getStartCol());
        assertEquals(13, addr.getStartRow());
        assertTrue(addr.isSingleCell());
    }

    @Test
    public void testParseSingleCell_F11() {
        final CellAddress addr = CellAddress.parse("F11");
        assertEquals(5, addr.getStartCol());
        assertEquals(10, addr.getStartRow());
        assertTrue(addr.isSingleCell());
    }

    @Test
    public void testParseSingleCell_lowercase() {
        final CellAddress addr = CellAddress.parse("c4");
        assertEquals(2, addr.getStartCol());
        assertEquals(3, addr.getStartRow());
        assertTrue(addr.isSingleCell());
    }

    @Test
    public void testParseSingleCell_whitespace() {
        final CellAddress addr = CellAddress.parse("  C4  ");
        assertEquals(2, addr.getStartCol());
        assertEquals(3, addr.getStartRow());
        assertTrue(addr.isSingleCell());
    }

    // --- range ---

    @Test
    public void testParseRange_B10_D15() {
        final CellAddress addr = CellAddress.parse("B10:D15");
        assertEquals(1, addr.getStartCol());
        assertEquals(9, addr.getStartRow());
        assertEquals(3, addr.getEndCol());
        assertEquals(14, addr.getEndRow());
        assertTrue(addr.isRange());
        assertFalse(addr.isSingleCell());
    }

    @Test
    public void testParseRange_singleRow() {
        final CellAddress addr = CellAddress.parse("A1:C1");
        assertEquals(0, addr.getStartRow());
        assertEquals(0, addr.getEndRow());
        assertEquals(0, addr.getStartCol());
        assertEquals(2, addr.getEndCol());
        assertTrue(addr.isRange());
    }

    @Test
    public void testParseRange_singleColumn() {
        final CellAddress addr = CellAddress.parse("A1:A5");
        assertEquals(0, addr.getStartCol());
        assertEquals(0, addr.getEndCol());
        assertEquals(0, addr.getStartRow());
        assertEquals(4, addr.getEndRow());
        assertTrue(addr.isRange());
    }

    // --- toString ---

    @Test
    public void testToString_singleCell() {
        assertEquals("C4", CellAddress.parse("C4").toString());
    }

    @Test
    public void testToString_range() {
        assertEquals("B10:D15", CellAddress.parse("B10:D15").toString());
    }

    @Test
    public void testToString_A1() {
        assertEquals("A1", CellAddress.parse("A1").toString());
    }

    // --- invalid inputs ---

    @Test(expected = IllegalArgumentException.class)
    public void testParseInvalid_null() {
        CellAddress.parse(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseInvalid_empty() {
        CellAddress.parse("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseInvalid_blank() {
        CellAddress.parse("   ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseInvalid_columnOnly() {
        CellAddress.parse("NOTACELL");
    }
}

package org.geki.knime.excelformreader.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.geki.knime.excelformreader.domain.FieldMapping;
import org.junit.Test;

public class FieldMappingTest {

    // --- valid construction ---

    @Test
    public void testConstruct_dataField() {
        final FieldMapping fm = new FieldMapping("Score", "C4", "data", "int");
        assertEquals("Score", fm.getName());
        assertEquals("C4", fm.getAddress().toString());
        assertEquals("data", fm.getContentType());
        assertEquals("int", fm.getDataType());
        assertTrue(fm.isData());
        assertFalse(fm.isLabel());
    }

    @Test
    public void testConstruct_labelField() {
        final FieldMapping fm = new FieldMapping("Assessor", "B2", "label", "string");
        assertEquals("label", fm.getContentType());
        assertTrue(fm.isLabel());
        assertFalse(fm.isData());
    }

    @Test
    public void testConstruct_nullDataType_defaultsToString() {
        final FieldMapping fm = new FieldMapping("Field", "A1", "data", null);
        assertEquals("string", fm.getDataType());
    }

    @Test
    public void testConstruct_blankDataType_defaultsToString() {
        final FieldMapping fm = new FieldMapping("Field", "A1", "data", "  ");
        assertEquals("string", fm.getDataType());
    }

    @Test
    public void testConstruct_nullContentType_defaultsToData() {
        final FieldMapping fm = new FieldMapping("Field", "A1", null, "string");
        assertEquals("data", fm.getContentType());
        assertTrue(fm.isData());
    }

    @Test
    public void testConstruct_blankContentType_defaultsToData() {
        final FieldMapping fm = new FieldMapping("Field", "A1", "", "string");
        assertEquals("data", fm.getContentType());
    }

    @Test
    public void testConstruct_contentType_caseInsensitive() {
        final FieldMapping fm = new FieldMapping("Field", "A1", "LABEL", "string");
        assertEquals("label", fm.getContentType());
        assertTrue(fm.isLabel());
    }

    @Test
    public void testConstruct_allDataTypes() {
        for (final String dt : new String[]{"string", "int", "double", "date", "boolean"}) {
            final FieldMapping fm = new FieldMapping("F", "A1", "data", dt);
            assertEquals(dt, fm.getDataType());
        }
    }

    @Test
    public void testConstruct_nameIsTrimmed() {
        final FieldMapping fm = new FieldMapping("  MyField  ", "A1", "data", "string");
        assertEquals("MyField", fm.getName());
    }

    // --- invalid construction ---

    @Test(expected = IllegalArgumentException.class)
    public void testConstruct_nullName_throws() {
        new FieldMapping(null, "A1", "data", "string");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstruct_blankName_throws() {
        new FieldMapping("   ", "A1", "data", "string");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstruct_invalidCellRange_throws() {
        new FieldMapping("Field", "NOTACELL", "data", "string");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstruct_invalidDataType_throws() {
        new FieldMapping("Field", "A1", "data", "richtext");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstruct_invalidContentType_throws() {
        new FieldMapping("Field", "A1", "metadata", "string");
    }
}

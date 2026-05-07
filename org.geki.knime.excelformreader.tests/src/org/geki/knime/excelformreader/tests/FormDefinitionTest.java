package org.geki.knime.excelformreader.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;

import org.geki.knime.excelformreader.domain.FieldMapping;
import org.geki.knime.excelformreader.domain.FormDefinition;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class FormDefinitionTest {

    private FieldMapping dataField1;
    private FieldMapping dataField2;
    private FieldMapping labelField1;
    private FieldMapping labelField2;

    @Before
    public void setUp() {
        dataField1 = new FieldMapping("Score", "C4", "data", "int");
        dataField2 = new FieldMapping("Comment", "D5", "data", "string");
        labelField1 = new FieldMapping("Assessor", "B2", "label", "string");
        labelField2 = new FieldMapping("Date", "B3", "label", "date");
    }

    // --- constructor ---

    @Test
    public void testConstruct_valid() {
        final FormDefinition fd = new FormDefinition(Arrays.asList(dataField1, labelField1));
        assertNotNull(fd);
        assertEquals(2, fd.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstruct_nullList_throws() {
        new FormDefinition(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstruct_emptyList_throws() {
        new FormDefinition(java.util.Collections.emptyList());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testConstruct_listIsUnmodifiable() {
        final FormDefinition fd = new FormDefinition(Arrays.asList(dataField1));
        fd.getFields().add(dataField2);
    }

    // --- filtering methods ---

    @Test
    public void testGetDataFields() {
        final FormDefinition fd = new FormDefinition(
            Arrays.asList(labelField1, dataField1, labelField2, dataField2));
        final List<FieldMapping> data = fd.getDataFields();
        assertEquals(2, data.size());
        assertEquals("Score", data.get(0).getName());
        assertEquals("Comment", data.get(1).getName());
    }

    @Test
    public void testGetLabelFields() {
        final FormDefinition fd = new FormDefinition(
            Arrays.asList(labelField1, dataField1, labelField2, dataField2));
        final List<FieldMapping> labels = fd.getLabelFields();
        assertEquals(2, labels.size());
        assertEquals("Assessor", labels.get(0).getName());
        assertEquals("Date", labels.get(1).getName());
    }

    @Test
    public void testGetDataFields_allData() {
        final FormDefinition fd = new FormDefinition(Arrays.asList(dataField1, dataField2));
        assertEquals(2, fd.getDataFields().size());
        assertEquals(0, fd.getLabelFields().size());
    }

    @Test
    public void testGetLabelFields_allLabels() {
        final FormDefinition fd = new FormDefinition(Arrays.asList(labelField1, labelField2));
        assertEquals(0, fd.getDataFields().size());
        assertEquals(2, fd.getLabelFields().size());
    }

    @Test
    public void testGetFields_orderPreserved() {
        final FormDefinition fd = new FormDefinition(
            Arrays.asList(dataField2, labelField2, dataField1, labelField1));
        final List<FieldMapping> fields = fd.getFields();
        assertEquals("Comment", fields.get(0).getName());
        assertEquals("Date", fields.get(1).getName());
        assertEquals("Score", fields.get(2).getName());
        assertEquals("Assessor", fields.get(3).getName());
    }

    // --- fromDataTable ---

    @Ignore("fromDataTable requires a live KNIME BufferedDataTable — covered by integration tests")
    @Test
    public void testFromDataTable_placeholder() {
    }
}

package org.geki.knime.excelformreader.tests;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.geki.knime.excelformreader.domain.FieldMapping;
import org.geki.knime.excelformreader.domain.FormDefinition;
import org.geki.knime.excelformreader.output.OutputSpecFactory;
import org.junit.Test;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.def.BooleanCell;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.LongCell;
import org.knime.core.data.def.StringCell;

public class OutputSpecFactoryTest {

    private FormDefinition oneDataOneLabelField() {
        return new FormDefinition(Arrays.asList(
            new FieldMapping("Score", "A1", "data", "int"),
            new FieldMapping("Assessor", "B1", "label", "string")));
    }

    // --- createWideSpec: source_file / sheet_name columns ---

    @Test
    public void testWideSpec_sourceAndSheetColumns_presentWhenFlagsTrue() {
        final DataTableSpec spec = OutputSpecFactory.createWideSpec(
            oneDataOneLabelField(), true, true, false, false, false);
        assertEquals("source_file", spec.getColumnSpec(0).getName());
        assertEquals("sheet_name", spec.getColumnSpec(1).getName());
        assertEquals("Score", spec.getColumnSpec(2).getName());
    }

    @Test
    public void testWideSpec_sourceAndSheetColumns_absentWhenFlagsFalse() {
        final DataTableSpec spec = OutputSpecFactory.createWideSpec(
            oneDataOneLabelField(), false, false, false, false, false);
        assertEquals("Score", spec.getColumnSpec(0).getName());
        assertEquals(1, spec.getNumColumns());
    }

    // --- createWideSpec: label field inclusion ---

    @Test
    public void testWideSpec_includeLabelFieldsFalse_onlyDataFields() {
        final DataTableSpec spec = OutputSpecFactory.createWideSpec(
            oneDataOneLabelField(), false, false, false, false, false);
        assertEquals(1, spec.getNumColumns());
        assertEquals("Score", spec.getColumnSpec(0).getName());
    }

    @Test
    public void testWideSpec_includeLabelFieldsTrue_allFields() {
        final DataTableSpec spec = OutputSpecFactory.createWideSpec(
            oneDataOneLabelField(), false, false, true, false, false);
        assertEquals(2, spec.getNumColumns());
        assertEquals("Score", spec.getColumnSpec(0).getName());
        assertEquals("Assessor", spec.getColumnSpec(1).getName());
    }

    // --- createWideSpec: per-field metadata companion columns ---

    @Test
    public void testWideSpec_formatConditionColumn_addedPerField() {
        final DataTableSpec spec = OutputSpecFactory.createWideSpec(
            oneDataOneLabelField(), false, false, false, true, false);
        assertEquals(2, spec.getNumColumns());
        assertEquals("Score", spec.getColumnSpec(0).getName());
        assertEquals("Score (Format Condition Operator)", spec.getColumnSpec(1).getName());
    }

    @Test
    public void testWideSpec_validationTypeColumn_addedPerField() {
        final DataTableSpec spec = OutputSpecFactory.createWideSpec(
            oneDataOneLabelField(), false, false, false, false, true);
        assertEquals(2, spec.getNumColumns());
        assertEquals("Score (Validation Type)", spec.getColumnSpec(1).getName());
    }

    @Test
    public void testWideSpec_bothMetadataColumns_orderedAfterValue() {
        final DataTableSpec spec = OutputSpecFactory.createWideSpec(
            oneDataOneLabelField(), false, false, false, true, true);
        assertEquals(3, spec.getNumColumns());
        assertEquals("Score", spec.getColumnSpec(0).getName());
        assertEquals("Score (Format Condition Operator)", spec.getColumnSpec(1).getName());
        assertEquals("Score (Validation Type)", spec.getColumnSpec(2).getName());
    }

    // --- createWideSpec: dataType -> KNIME DataType mapping ---

    @Test
    public void testWideSpec_dataTypeMapping_allTypes() {
        final FormDefinition def = new FormDefinition(Arrays.asList(
            new FieldMapping("S", "A1", "data", "string"),
            new FieldMapping("I", "A2", "data", "int"),
            new FieldMapping("D", "A3", "data", "double"),
            new FieldMapping("Dt", "A4", "data", "date"),
            new FieldMapping("B", "A5", "data", "boolean")));
        final DataTableSpec spec = OutputSpecFactory.createWideSpec(def, false, false, false, false, false);
        assertEquals(StringCell.TYPE, spec.getColumnSpec("S").getType());
        assertEquals(LongCell.TYPE, spec.getColumnSpec("I").getType());
        assertEquals(DoubleCell.TYPE, spec.getColumnSpec("D").getType());
        // "date" fields are stringified (CellValueConverter emits an ISO string), so the
        // column type is StringCell, not a KNIME date/time type.
        assertEquals(StringCell.TYPE, spec.getColumnSpec("Dt").getType());
        assertEquals(BooleanCell.TYPE, spec.getColumnSpec("B").getType());
    }

    // --- createLongSpec ---

    @Test
    public void testLongSpec_fixedColumns_noOptionalFlags() {
        final DataTableSpec spec = OutputSpecFactory.createLongSpec(false, false, false, false);
        assertEquals(2, spec.getNumColumns());
        assertEquals("field_name", spec.getColumnSpec(0).getName());
        assertEquals("value", spec.getColumnSpec(1).getName());
    }

    @Test
    public void testLongSpec_allFlagsOn() {
        final DataTableSpec spec = OutputSpecFactory.createLongSpec(true, true, true, true);
        assertEquals(6, spec.getNumColumns());
        assertEquals("source_file", spec.getColumnSpec(0).getName());
        assertEquals("sheet_name", spec.getColumnSpec(1).getName());
        assertEquals("field_name", spec.getColumnSpec(2).getName());
        assertEquals("value", spec.getColumnSpec(3).getName());
        assertEquals("Format Condition Operator", spec.getColumnSpec(4).getName());
        assertEquals("Validation Type", spec.getColumnSpec(5).getName());
    }

    // --- createLabelSpec ---

    @Test
    public void testLabelSpec_fixedColumns_noOptionalFlags() {
        final DataTableSpec spec = OutputSpecFactory.createLabelSpec(false, false, false, false);
        assertEquals(3, spec.getNumColumns());
        assertEquals("Name", spec.getColumnSpec(0).getName());
        assertEquals("Cell Range", spec.getColumnSpec(1).getName());
        assertEquals("Cell Content", spec.getColumnSpec(2).getName());
    }

    @Test
    public void testLabelSpec_allFlagsOn() {
        final DataTableSpec spec = OutputSpecFactory.createLabelSpec(true, true, true, true);
        assertEquals(7, spec.getNumColumns());
        assertEquals("Source File", spec.getColumnSpec(0).getName());
        assertEquals("Sheet Name", spec.getColumnSpec(1).getName());
        assertEquals("Name", spec.getColumnSpec(2).getName());
        assertEquals("Cell Range", spec.getColumnSpec(3).getName());
        assertEquals("Cell Content", spec.getColumnSpec(4).getName());
        assertEquals("Format Condition Operator", spec.getColumnSpec(5).getName());
        assertEquals("Validation Type", spec.getColumnSpec(6).getName());
    }
}

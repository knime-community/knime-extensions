package org.geki.knime.excelformreader.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geki.knime.excelformreader.domain.FieldMapping;
import org.geki.knime.excelformreader.domain.FormDefinition;
import org.geki.knime.excelformreader.excel.ExcelFormExtractor.CellExtractionResult;
import org.geki.knime.excelformreader.output.LongOutputBuilder;
import org.geki.knime.excelformreader.output.OutputSpecFactory;
import org.junit.Test;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.StringCell;

public class LongOutputBuilderTest {

    private FormDefinition twoFields() {
        return new FormDefinition(Arrays.asList(
            new FieldMapping("First", "A1", "data", "string"),
            new FieldMapping("Second", "A2", "data", "double")));
    }

    @Test
    public void testBuildRows_oneRowPerField() {
        final FormDefinition def = twoFields();
        final DataTableSpec spec = OutputSpecFactory.createLongSpec(false, false, false, false);
        final LongOutputBuilder builder = new LongOutputBuilder(spec, false, false, false, false, false);
        final Map<String, CellExtractionResult> results = new HashMap<>();
        results.put("First", new CellExtractionResult(new StringCell("A"), DataType.getMissingCell(), DataType.getMissingCell()));
        results.put("Second", new CellExtractionResult(new DoubleCell(3.14), DataType.getMissingCell(), DataType.getMissingCell()));

        final List<DataRow> rows = builder.buildRows(null, null, results, def, 0);
        assertEquals(2, rows.size());
        assertEquals("First", rows.get(0).getCell(0).toString());
        assertEquals("A", rows.get(0).getCell(1).toString());
        assertEquals("Second", rows.get(1).getCell(0).toString());
    }

    @Test
    public void testBuildRows_valueStringified_typeLossRegression() {
        // Regression: unlike WideOutputBuilder (which preserves the typed DataCell), the
        // long-format value column always stringifies via toString(), losing DoubleCell-ness.
        final FormDefinition def = new FormDefinition(
            java.util.Collections.singletonList(new FieldMapping("Second", "A1", "data", "double")));
        final DataTableSpec spec = OutputSpecFactory.createLongSpec(false, false, false, false);
        final LongOutputBuilder builder = new LongOutputBuilder(spec, false, false, false, false, false);
        final Map<String, CellExtractionResult> results = new HashMap<>();
        results.put("Second", new CellExtractionResult(new DoubleCell(3.14), DataType.getMissingCell(), DataType.getMissingCell()));

        final List<DataRow> rows = builder.buildRows(null, null, results, def, 0);
        assertEquals(StringCell.class, rows.get(0).getCell(1).getClass());
        assertEquals(new DoubleCell(3.14).toString(), rows.get(0).getCell(1).toString());
    }

    @Test
    public void testBuildRows_missingValue_staysMissing_notStringified() {
        final FormDefinition def = new FormDefinition(
            java.util.Collections.singletonList(new FieldMapping("First", "A1", "data", "string")));
        final DataTableSpec spec = OutputSpecFactory.createLongSpec(false, false, false, false);
        final LongOutputBuilder builder = new LongOutputBuilder(spec, false, false, false, false, false);

        final List<DataRow> rows = builder.buildRows(null, null, new HashMap<>(), def, 0);
        assertTrue(rows.get(0).getCell(1).isMissing());
    }

    @Test
    public void testBuildRows_rowKeys_includeBaseOffset() {
        final FormDefinition def = twoFields();
        final DataTableSpec spec = OutputSpecFactory.createLongSpec(false, false, false, false);
        final LongOutputBuilder builder = new LongOutputBuilder(spec, false, false, false, false, false);

        final List<DataRow> rows = builder.buildRows(null, null, new HashMap<>(), def, 5);
        assertEquals("Row5", rows.get(0).getKey().getString());
        assertEquals("Row6", rows.get(1).getKey().getString());
    }

    @Test
    public void testBuildRows_sourceAndSheetColumns_populated() {
        final FormDefinition def = new FormDefinition(
            java.util.Collections.singletonList(new FieldMapping("First", "A1", "data", "string")));
        final DataTableSpec spec = OutputSpecFactory.createLongSpec(true, true, false, false);
        final LongOutputBuilder builder = new LongOutputBuilder(spec, true, true, false, false, false);

        final List<DataRow> rows = builder.buildRows("file.xlsx", "Sheet1", new HashMap<>(), def, 0);
        assertEquals("file.xlsx", rows.get(0).getCell(0).toString());
        assertEquals("Sheet1", rows.get(0).getCell(1).toString());
    }

    @Test
    public void testBuildRows_includeLabelFieldsFalse_excludesLabelFields() {
        final FormDefinition def = new FormDefinition(Arrays.asList(
            new FieldMapping("First", "A1", "data", "string"),
            new FieldMapping("Assessor", "B1", "label", "string")));
        final DataTableSpec spec = OutputSpecFactory.createLongSpec(false, false, false, false);
        final LongOutputBuilder builder = new LongOutputBuilder(spec, false, false, false, false, false);

        final List<DataRow> rows = builder.buildRows(null, null, new HashMap<>(), def, 0);
        assertEquals(1, rows.size());
        assertEquals("First", rows.get(0).getCell(0).toString());
    }
}

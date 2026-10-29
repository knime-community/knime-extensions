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
import org.geki.knime.excelformreader.output.LabelOutputBuilder;
import org.geki.knime.excelformreader.output.OutputSpecFactory;
import org.junit.Test;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.StringCell;

public class LabelOutputBuilderTest {

    private FormDefinition dataAndLabelFields() {
        return new FormDefinition(Arrays.asList(
            new FieldMapping("Score", "A1", "data", "int"),
            new FieldMapping("Assessor", "B1", "label", "string")));
    }

    @Test
    public void testBuildRows_onlyLabelFieldsIncluded() {
        final FormDefinition def = dataAndLabelFields();
        final DataTableSpec spec = OutputSpecFactory.createLabelSpec(false, false, false, false);
        final LabelOutputBuilder builder = new LabelOutputBuilder(spec, false, false, false, false);
        final Map<String, CellExtractionResult> results = new HashMap<>();
        results.put("Assessor", new CellExtractionResult(new StringCell("Jane"), DataType.getMissingCell(), DataType.getMissingCell()));

        final List<DataRow> rows = builder.buildRows(null, null, results, def, 0);
        assertEquals(1, rows.size());
        assertEquals("Assessor", rows.get(0).getCell(0).toString());
        assertEquals("B1", rows.get(0).getCell(1).toString());
        assertEquals("Jane", rows.get(0).getCell(2).toString());
    }

    @Test
    public void testBuildRows_cellContentStringified_typeLossRegression() {
        final FormDefinition def = new FormDefinition(
            java.util.Collections.singletonList(new FieldMapping("Score", "A1", "label", "double")));
        final DataTableSpec spec = OutputSpecFactory.createLabelSpec(false, false, false, false);
        final LabelOutputBuilder builder = new LabelOutputBuilder(spec, false, false, false, false);
        final Map<String, CellExtractionResult> results = new HashMap<>();
        results.put("Score", new CellExtractionResult(new DoubleCell(3.14), DataType.getMissingCell(), DataType.getMissingCell()));

        final List<DataRow> rows = builder.buildRows(null, null, results, def, 0);
        assertEquals(StringCell.class, rows.get(0).getCell(2).getClass());
        assertEquals(new DoubleCell(3.14).toString(), rows.get(0).getCell(2).toString());
    }

    @Test
    public void testBuildRows_missingResult_cellContentMissing() {
        final FormDefinition def = new FormDefinition(
            java.util.Collections.singletonList(new FieldMapping("Assessor", "B1", "label", "string")));
        final DataTableSpec spec = OutputSpecFactory.createLabelSpec(false, false, false, false);
        final LabelOutputBuilder builder = new LabelOutputBuilder(spec, false, false, false, false);

        final List<DataRow> rows = builder.buildRows(null, null, new HashMap<>(), def, 0);
        assertTrue(rows.get(0).getCell(2).isMissing());
    }

    @Test
    public void testBuildRows_rowKeyPrefix_labelRow() {
        final FormDefinition def = dataAndLabelFields();
        final DataTableSpec spec = OutputSpecFactory.createLabelSpec(false, false, false, false);
        final LabelOutputBuilder builder = new LabelOutputBuilder(spec, false, false, false, false);

        final List<DataRow> rows = builder.buildRows(null, null, new HashMap<>(), def, 3);
        assertEquals("LabelRow3", rows.get(0).getKey().getString());
    }

    @Test
    public void testBuildRows_metadataColumns_populated() {
        final FormDefinition def = new FormDefinition(
            java.util.Collections.singletonList(new FieldMapping("Assessor", "B1", "label", "string")));
        final DataTableSpec spec = OutputSpecFactory.createLabelSpec(false, false, true, true);
        final LabelOutputBuilder builder = new LabelOutputBuilder(spec, false, false, true, true);
        final Map<String, CellExtractionResult> results = new HashMap<>();
        results.put("Assessor", new CellExtractionResult(
            new StringCell("Jane"), new StringCell("EQUAL"), new StringCell("LIST")));

        final List<DataRow> rows = builder.buildRows(null, null, results, def, 0);
        assertEquals("EQUAL", rows.get(0).getCell(3).toString());
        assertEquals("LIST", rows.get(0).getCell(4).toString());
    }
}

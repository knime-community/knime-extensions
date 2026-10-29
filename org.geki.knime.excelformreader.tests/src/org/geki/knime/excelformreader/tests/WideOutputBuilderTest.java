package org.geki.knime.excelformreader.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.geki.knime.excelformreader.domain.FieldMapping;
import org.geki.knime.excelformreader.domain.FormDefinition;
import org.geki.knime.excelformreader.excel.ExcelFormExtractor.CellExtractionResult;
import org.geki.knime.excelformreader.output.OutputSpecFactory;
import org.geki.knime.excelformreader.output.WideOutputBuilder;
import org.junit.Test;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.def.StringCell;

public class WideOutputBuilderTest {

    private FormDefinition oneField() {
        return new FormDefinition(
            Collections.singletonList(new FieldMapping("Score", "A1", "data", "string")));
    }

    private CellExtractionResult result(final String value) {
        return new CellExtractionResult(new StringCell(value), DataType.getMissingCell(), DataType.getMissingCell());
    }

    @Test
    public void testBuildRow_allColumnsPresent_valuesInOrder() {
        final FormDefinition def = oneField();
        final DataTableSpec spec = OutputSpecFactory.createWideSpec(def, true, true, false, false, false);
        final WideOutputBuilder builder = new WideOutputBuilder(spec, true, true, false, false, false);
        final Map<String, CellExtractionResult> results = new HashMap<>();
        results.put("Score", result("42"));

        final DataRow row = builder.buildRow("file.xlsx", "Sheet1", results, def, 0);
        assertEquals("file.xlsx", row.getCell(0).toString());
        assertEquals("Sheet1", row.getCell(1).toString());
        assertEquals("42", row.getCell(2).toString());
    }

    @Test
    public void testBuildRow_fieldMissingFromResultsMap_producesMissingCell() {
        final FormDefinition def = oneField();
        final DataTableSpec spec = OutputSpecFactory.createWideSpec(def, false, false, false, false, false);
        final WideOutputBuilder builder = new WideOutputBuilder(spec, false, false, false, false, false);

        final DataRow row = builder.buildRow(null, null, new HashMap<>(), def, 0);
        assertTrue(row.getCell(0).isMissing());
    }

    @Test
    public void testBuildRow_nullResultsMap_allFieldsMissing() {
        final FormDefinition def = oneField();
        final DataTableSpec spec = OutputSpecFactory.createWideSpec(def, false, false, false, false, false);
        final WideOutputBuilder builder = new WideOutputBuilder(spec, false, false, false, false, false);

        final DataRow row = builder.buildRow(null, null, null, def, 0);
        assertTrue(row.getCell(0).isMissing());
    }

    @Test
    public void testBuildRow_nullSourceFileAndSheetName_defaultToEmptyString() {
        final FormDefinition def = oneField();
        final DataTableSpec spec = OutputSpecFactory.createWideSpec(def, true, true, false, false, false);
        final WideOutputBuilder builder = new WideOutputBuilder(spec, true, true, false, false, false);

        final DataRow row = builder.buildRow(null, null, new HashMap<>(), def, 0);
        assertEquals("", row.getCell(0).toString());
        assertEquals("", row.getCell(1).toString());
    }

    @Test
    public void testBuildRow_metadataColumns_populatedFromResult() {
        final FormDefinition def = oneField();
        final DataTableSpec spec = OutputSpecFactory.createWideSpec(def, false, false, false, true, true);
        final WideOutputBuilder builder = new WideOutputBuilder(spec, false, false, false, true, true);
        final Map<String, CellExtractionResult> results = new HashMap<>();
        results.put("Score", new CellExtractionResult(
            new StringCell("42"), new StringCell("EQUAL"), new StringCell("INTEGER")));

        final DataRow row = builder.buildRow(null, null, results, def, 0);
        assertEquals("42", row.getCell(0).toString());
        assertEquals("EQUAL", row.getCell(1).toString());
        assertEquals("INTEGER", row.getCell(2).toString());
    }

    @Test
    public void testBuildRow_rowKey_includesRowIndex() {
        final FormDefinition def = oneField();
        final DataTableSpec spec = OutputSpecFactory.createWideSpec(def, false, false, false, false, false);
        final WideOutputBuilder builder = new WideOutputBuilder(spec, false, false, false, false, false);

        final DataRow row = builder.buildRow(null, null, new HashMap<>(), def, 7);
        assertEquals("Row7", row.getKey().getString());
    }

    @Test
    public void testBuildRow_includeLabelFields_addsLabelColumn() {
        final FormDefinition def = new FormDefinition(Arrays.asList(
            new FieldMapping("Score", "A1", "data", "string"),
            new FieldMapping("Assessor", "B1", "label", "string")));
        final DataTableSpec spec = OutputSpecFactory.createWideSpec(def, false, false, true, false, false);
        final WideOutputBuilder builder = new WideOutputBuilder(spec, false, false, true, false, false);
        final Map<String, CellExtractionResult> results = new HashMap<>();
        results.put("Score", result("42"));
        results.put("Assessor", result("Jane"));

        final DataRow row = builder.buildRow(null, null, results, def, 0);
        assertEquals(2, row.getNumCells());
        assertEquals("Jane", row.getCell(1).toString());
    }
}

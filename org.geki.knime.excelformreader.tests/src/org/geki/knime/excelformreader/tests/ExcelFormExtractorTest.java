package org.geki.knime.excelformreader.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.geki.knime.excelformreader.ExcelFormReaderSettings;
import org.geki.knime.excelformreader.ExcelFormReaderSettings.ErrorHandling;
import org.geki.knime.excelformreader.domain.FieldMapping;
import org.geki.knime.excelformreader.domain.FormDefinition;
import org.geki.knime.excelformreader.excel.ExcelFormExtractor;
import org.geki.knime.excelformreader.excel.ExcelFormExtractor.CellExtractionResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.knime.core.data.DataCell;

public class ExcelFormExtractorTest {

    private XSSFWorkbook wb;
    private XSSFSheet sheet;

    @Before
    public void setUp() {
        wb = new XSSFWorkbook();
        sheet = wb.createSheet("TestSheet");
    }

    @After
    public void tearDown() throws Exception {
        wb.close();
    }

    private ExcelFormExtractor extractor(final ErrorHandling onMissingCell) {
        final ExcelFormReaderSettings settings = new ExcelFormReaderSettings();
        settings.getOnMissingCellModel().setStringValue(onMissingCell.name());
        return new ExcelFormExtractor(settings);
    }

    private Cell setCell(final int row, final int col, final Object value) {
        Row r = sheet.getRow(row);
        if (r == null) {
            r = sheet.createRow(row);
        }
        final Cell c = r.createCell(col);
        if (value instanceof String) {
            c.setCellValue((String) value);
        } else if (value instanceof Double) {
            c.setCellValue((Double) value);
        } else if (value instanceof Boolean) {
            c.setCellValue((Boolean) value);
        }
        return c;
    }

    // --- single cell: happy path per data type ---

    @Test
    public void testSingleCell_string_extractsValue() {
        setCell(3, 2, "Hello"); // C4
        final FormDefinition def = new FormDefinition(
            Collections.singletonList(new FieldMapping("Field", "C4", "data", "string")));
        final Map<String, CellExtractionResult> result = extractor(ErrorHandling.WARN).extract(sheet, def, wb);
        assertEquals("Hello", result.get("Field").value.toString());
    }

    @Test
    public void testSingleCell_int_extractsValue() {
        setCell(0, 0, 42.0); // A1
        final FormDefinition def = new FormDefinition(
            Collections.singletonList(new FieldMapping("Field", "A1", "data", "int")));
        final Map<String, CellExtractionResult> result = extractor(ErrorHandling.WARN).extract(sheet, def, wb);
        assertEquals("42", result.get("Field").value.toString());
    }

    @Test
    public void testSingleCell_double_extractsValue() {
        setCell(0, 0, 3.14);
        final FormDefinition def = new FormDefinition(
            Collections.singletonList(new FieldMapping("Field", "A1", "data", "double")));
        final Map<String, CellExtractionResult> result = extractor(ErrorHandling.WARN).extract(sheet, def, wb);
        assertTrue(result.get("Field").value.toString().startsWith("3.14"));
    }

    @Test
    public void testSingleCell_boolean_extractsValue() {
        setCell(0, 0, true);
        final FormDefinition def = new FormDefinition(
            Collections.singletonList(new FieldMapping("Field", "A1", "data", "boolean")));
        final Map<String, CellExtractionResult> result = extractor(ErrorHandling.WARN).extract(sheet, def, wb);
        assertEquals("true", result.get("Field").value.toString());
    }

    @Test
    public void testSingleCell_date_extractsValue() {
        final Row r = sheet.createRow(0);
        final Cell c = r.createCell(0);
        final Calendar cal = Calendar.getInstance();
        cal.set(2025, 11, 31, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        c.setCellValue(cal.getTime());
        final CellStyle style = wb.createCellStyle();
        final CreationHelper ch = wb.getCreationHelper();
        style.setDataFormat(ch.createDataFormat().getFormat("yyyy-mm-dd"));
        c.setCellStyle(style);

        final FormDefinition def = new FormDefinition(
            Collections.singletonList(new FieldMapping("Field", "A1", "data", "date")));
        final Map<String, CellExtractionResult> result = extractor(ErrorHandling.WARN).extract(sheet, def, wb);
        assertEquals("2025-12-31", result.get("Field").value.toString());
    }

    // --- missing cell handling ---

    @Test
    public void testMissingRow_warn_returnsMissingResult() {
        // No rows created at all - row for address is null
        final FormDefinition def = new FormDefinition(
            Collections.singletonList(new FieldMapping("Field", "A1", "data", "string")));
        final Map<String, CellExtractionResult> result = extractor(ErrorHandling.WARN).extract(sheet, def, wb);
        final CellExtractionResult r = result.get("Field");
        assertTrue(r.value.isMissing());
        assertTrue(r.formatConditionOperator.isMissing());
        assertTrue(r.validationType.isMissing());
    }

    @Test
    public void testMissingCell_warn_returnsMissingResult() {
        sheet.createRow(0); // row exists, but no cell at column 0
        final FormDefinition def = new FormDefinition(
            Collections.singletonList(new FieldMapping("Field", "A1", "data", "string")));
        final Map<String, CellExtractionResult> result = extractor(ErrorHandling.WARN).extract(sheet, def, wb);
        assertTrue(result.get("Field").value.isMissing());
    }

    @Test
    public void testMissingRow_fail_throws() {
        final FormDefinition def = new FormDefinition(
            Collections.singletonList(new FieldMapping("Field", "A1", "data", "string")));
        try {
            extractor(ErrorHandling.FAIL).extract(sheet, def, wb);
            fail("Expected RuntimeException");
        } catch (final RuntimeException e) {
            assertTrue(e.getMessage().contains("Field"));
        }
    }

    @Test
    public void testMissingCell_fail_throws() {
        sheet.createRow(0);
        final FormDefinition def = new FormDefinition(
            Collections.singletonList(new FieldMapping("Field", "A1", "data", "string")));
        try {
            extractor(ErrorHandling.FAIL).extract(sheet, def, wb);
            fail("Expected RuntimeException");
        } catch (final RuntimeException e) {
            assertTrue(e.getMessage().contains("Field"));
        }
    }

    // --- range extraction ---

    @Test
    public void testRange_multiCell_joinsWithDelimiter() {
        setCell(0, 0, "Alpha");
        setCell(0, 1, "Beta");
        setCell(1, 0, "Gamma");
        final FormDefinition def = new FormDefinition(
            Collections.singletonList(new FieldMapping("Field", "A1:B2", "data", "string")));
        final Map<String, CellExtractionResult> result = extractor(ErrorHandling.WARN).extract(sheet, def, wb);
        assertEquals("Alpha, Beta, Gamma", result.get("Field").value.toString());
    }

    @Test
    public void testRange_declaredAsInt_stillReturnsStringCell() {
        // Regression: range fields always coerce to string, regardless of declared dataType.
        setCell(0, 0, 1.0);
        setCell(0, 1, 2.0);
        final FormDefinition def = new FormDefinition(
            Collections.singletonList(new FieldMapping("Field", "A1:B1", "data", "int")));
        final Map<String, CellExtractionResult> result = extractor(ErrorHandling.WARN).extract(sheet, def, wb);
        final DataCell value = result.get("Field").value;
        assertFalse(value.isMissing());
        assertEquals("1, 2", value.toString());
        assertEquals(org.knime.core.data.def.StringCell.class, value.getClass());
    }

    @Test
    public void testRange_withBlankCellsInterspersed_skipsBlanks() {
        setCell(0, 0, "Alpha");
        // (0,1) intentionally left absent
        setCell(1, 0, "Gamma");
        final FormDefinition def = new FormDefinition(
            Collections.singletonList(new FieldMapping("Field", "A1:B2", "data", "string")));
        final Map<String, CellExtractionResult> result = extractor(ErrorHandling.WARN).extract(sheet, def, wb);
        assertEquals("Alpha, Gamma", result.get("Field").value.toString());
    }

    @Test
    public void testRange_allBlank_returnsMissing() {
        final FormDefinition def = new FormDefinition(
            Collections.singletonList(new FieldMapping("Field", "A1:B2", "data", "string")));
        final Map<String, CellExtractionResult> result = extractor(ErrorHandling.WARN).extract(sheet, def, wb);
        assertTrue(result.get("Field").value.isMissing());
    }

    // --- metadata wiring ---

    @Test
    public void testSingleCell_metadataWired() {
        setCell(0, 0, "X");
        final FormDefinition def = new FormDefinition(
            Collections.singletonList(new FieldMapping("Field", "A1", "data", "string")));
        final Map<String, CellExtractionResult> result = extractor(ErrorHandling.WARN).extract(sheet, def, wb);
        final CellExtractionResult r = result.get("Field");
        // No conditional formatting/validation configured -> both missing, but must not be null.
        assertTrue(r.formatConditionOperator.isMissing());
        assertTrue(r.validationType.isMissing());
    }

    @Test
    public void testExtract_multipleFields_preservesOrderAndAllPresent() {
        setCell(0, 0, "A");
        setCell(1, 1, "B");
        final FormDefinition def = new FormDefinition(Arrays.asList(
            new FieldMapping("First", "A1", "data", "string"),
            new FieldMapping("Second", "B2", "data", "string")));
        final Map<String, CellExtractionResult> result = extractor(ErrorHandling.WARN).extract(sheet, def, wb);
        assertEquals(2, result.size());
        assertEquals("A", result.get("First").value.toString());
        assertEquals("B", result.get("Second").value.toString());
    }
}

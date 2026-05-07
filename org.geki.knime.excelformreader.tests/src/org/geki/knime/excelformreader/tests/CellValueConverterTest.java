package org.geki.knime.excelformreader.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.geki.knime.excelformreader.excel.CellValueConverter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.knime.core.data.DataCell;

public class CellValueConverterTest {

    private Workbook wb;
    private Sheet sheet;
    private FormulaEvaluator evaluator;

    @Before
    public void setUp() {
        wb = new XSSFWorkbook();
        sheet = wb.createSheet("Test");
        evaluator = wb.getCreationHelper().createFormulaEvaluator();
    }

    @After
    public void tearDown() throws Exception {
        wb.close();
    }

    private Cell createCell(final int row, final int col, final Object value) {
        final Row r = sheet.createRow(row);
        final Cell c = r.createCell(col);
        if (value instanceof String) {
            c.setCellValue((String) value);
        } else if (value instanceof Double) {
            c.setCellValue((Double) value);
        } else if (value instanceof Boolean) {
            c.setCellValue((Boolean) value);
        } else if (value instanceof java.util.Date) {
            c.setCellValue((java.util.Date) value);
        }
        return c;
    }

    // --- null / blank ---

    @Test
    public void testNullCell_returnsMissing() {
        final DataCell result = CellValueConverter.convert(null, "string", evaluator);
        assertTrue(result.isMissing());
    }

    @Test
    public void testBlankCell_returnsMissing() {
        final Row r = sheet.createRow(0);
        final Cell c = r.createCell(0);
        c.setBlank();
        final DataCell result = CellValueConverter.convert(c, "string", evaluator);
        assertTrue(result.isMissing());
    }

    // --- string ---

    @Test
    public void testStringCell_returnsStringCell() {
        final Cell c = createCell(0, 0, "Hello World");
        final DataCell result = CellValueConverter.convert(c, "string", evaluator);
        assertFalse(result.isMissing());
        assertEquals("Hello World", result.toString());
    }

    @Test
    public void testStringCell_emptyString_returnsMissing() {
        final Cell c = createCell(0, 0, "");
        final DataCell result = CellValueConverter.convert(c, "string", evaluator);
        assertTrue(result.isMissing());
    }

    // --- int ---

    @Test
    public void testIntCell_returnsLongCell() {
        final Cell c = createCell(0, 0, 42.0);
        final DataCell result = CellValueConverter.convert(c, "int", evaluator);
        assertFalse(result.isMissing());
        assertEquals("42", result.toString());
    }

    @Test
    public void testIntCell_negativeValue() {
        final Cell c = createCell(0, 0, -17.0);
        final DataCell result = CellValueConverter.convert(c, "int", evaluator);
        assertEquals("-17", result.toString());
    }

    @Test
    public void testIntCell_zero() {
        final Cell c = createCell(0, 0, 0.0);
        final DataCell result = CellValueConverter.convert(c, "int", evaluator);
        assertEquals("0", result.toString());
    }

    // --- double ---

    @Test
    public void testDoubleCell_returnsDoubleCell() {
        final Cell c = createCell(0, 0, 3.14);
        final DataCell result = CellValueConverter.convert(c, "double", evaluator);
        assertFalse(result.isMissing());
        assertTrue(result.toString().startsWith("3.14"));
    }

    @Test
    public void testDoubleCell_wholeNumber() {
        final Cell c = createCell(0, 0, 100.0);
        final DataCell result = CellValueConverter.convert(c, "double", evaluator);
        assertEquals("100.0", result.toString());
    }

    // --- boolean ---

    @Test
    public void testBooleanCell_true() {
        final Row r = sheet.createRow(0);
        final Cell c = r.createCell(0);
        c.setCellValue(true);
        final DataCell result = CellValueConverter.convert(c, "boolean", evaluator);
        assertFalse(result.isMissing());
        assertEquals("true", result.toString());
    }

    @Test
    public void testBooleanCell_false() {
        final Row r = sheet.createRow(0);
        final Cell c = r.createCell(0);
        c.setCellValue(false);
        final DataCell result = CellValueConverter.convert(c, "boolean", evaluator);
        assertEquals("false", result.toString());
    }

    @Test
    public void testBooleanCell_fromString_true() {
        final Cell c = createCell(0, 0, "yes");
        final DataCell result = CellValueConverter.convert(c, "boolean", evaluator);
        assertFalse(result.isMissing());
    }

    @Test
    public void testBooleanCell_fromString_false() {
        final Cell c = createCell(0, 0, "no");
        final DataCell result = CellValueConverter.convert(c, "boolean", evaluator);
        assertFalse(result.isMissing());
    }

    // --- date ---

    @Test
    public void testDateCell_returnsISOString() {
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
        final DataCell result = CellValueConverter.convert(c, "date", evaluator);
        assertFalse(result.isMissing());
        assertEquals("2025-12-31", result.toString());
    }

    // --- unknown type ---

    @Test
    public void testUnknownDataType_fallsBackToString() {
        final Cell c = createCell(0, 0, "SomeValue");
        final DataCell result = CellValueConverter.convert(c, "unknown_type", evaluator);
        assertFalse(result.isMissing());
        assertEquals("SomeValue", result.toString());
    }

    // --- formula evaluation ---

    @Test
    public void testFormulaCell_evaluatedToString() {
        final Row r = sheet.createRow(0);
        final Cell c = r.createCell(0);
        c.setCellFormula("CONCATENATE(\"Hello\",\" \",\"World\")");
        final DataCell result = CellValueConverter.convert(c, "string", evaluator);
        assertFalse(result.isMissing());
        assertEquals("Hello World", result.toString());
    }

    @Test
    public void testFormulaCell_evaluatedToNumber() {
        final Row r = sheet.createRow(0);
        final Cell c = r.createCell(0);
        c.setCellFormula("1+1");
        final DataCell result = CellValueConverter.convert(c, "int", evaluator);
        assertEquals("2", result.toString());
    }
}

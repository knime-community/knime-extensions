package org.geki.knime.excelformreader.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.poi.ss.usermodel.ComparisonOperator;
import org.apache.poi.ss.usermodel.ConditionalFormattingRule;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.SheetConditionalFormatting;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.geki.knime.excelformreader.domain.CellAddress;
import org.geki.knime.excelformreader.excel.CellMetadataReader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.knime.core.data.DataCell;

public class CellMetadataReaderTest {

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

    // --- format condition operator ---

    @Test
    public void testNoConditionalFormatting_returnsMissing() {
        final CellAddress addr = CellAddress.parse("A1");
        final DataCell result = CellMetadataReader.readFormatConditionOperator(
            sheet, addr, ", ", wb);
        assertTrue(result.isMissing());
    }

    @Test
    public void testWithConditionalFormatting_returnsOperator() {
        final SheetConditionalFormatting scf = sheet.getSheetConditionalFormatting();
        final ConditionalFormattingRule rule =
            scf.createConditionalFormattingRule(ComparisonOperator.EQUAL, "0");
        final CellRangeAddress[] regions = { CellRangeAddress.valueOf("A1") };
        scf.addConditionalFormatting(regions, rule);

        final CellAddress addr = CellAddress.parse("A1");
        final DataCell result = CellMetadataReader.readFormatConditionOperator(
            sheet, addr, ", ", wb);
        assertFalse(result.isMissing());
        assertTrue(result.toString().length() > 0);
    }

    @Test
    public void testCellOutsideFormattingRange_returnsMissing() {
        final SheetConditionalFormatting scf = sheet.getSheetConditionalFormatting();
        final ConditionalFormattingRule rule =
            scf.createConditionalFormattingRule(ComparisonOperator.EQUAL, "0");
        final CellRangeAddress[] regions = { CellRangeAddress.valueOf("A1") };
        scf.addConditionalFormatting(regions, rule);

        final CellAddress addr = CellAddress.parse("B2");
        final DataCell result = CellMetadataReader.readFormatConditionOperator(
            sheet, addr, ", ", wb);
        assertTrue(result.isMissing());
    }

    // --- validation type ---

    @Test
    public void testNoValidation_returnsMissing() {
        final CellAddress addr = CellAddress.parse("A1");
        final DataCell result = CellMetadataReader.readValidationType(
            sheet, addr, ", ", wb);
        assertTrue(result.isMissing());
    }

    @Test
    public void testWithListValidation_inline_returnsListWithOptions() {
        final DataValidationHelper dvHelper = sheet.getDataValidationHelper();
        final DataValidationConstraint constraint =
            dvHelper.createExplicitListConstraint(
                new String[]{"Option1", "Option2", "Option3"});
        final CellRangeAddressList addressList = new CellRangeAddressList(0, 0, 0, 0);
        final DataValidation validation = dvHelper.createValidation(constraint, addressList);
        sheet.addValidationData(validation);

        final CellAddress addr = CellAddress.parse("A1");
        final DataCell result = CellMetadataReader.readValidationType(
            sheet, addr, ", ", wb);
        assertFalse(result.isMissing());
        final String val = result.toString();
        assertTrue(val.startsWith("LIST"));
        assertTrue(val.contains("Option1"));
        assertTrue(val.contains("Option2"));
        assertTrue(val.contains("Option3"));
    }

    @Test
    public void testWithIntegerValidation_returnsINTEGER() {
        final DataValidationHelper dvHelper = sheet.getDataValidationHelper();
        final DataValidationConstraint constraint =
            dvHelper.createIntegerConstraint(
                DataValidationConstraint.OperatorType.BETWEEN, "1", "100");
        final CellRangeAddressList addressList = new CellRangeAddressList(0, 0, 0, 0);
        final DataValidation validation = dvHelper.createValidation(constraint, addressList);
        sheet.addValidationData(validation);

        final CellAddress addr = CellAddress.parse("A1");
        final DataCell result = CellMetadataReader.readValidationType(
            sheet, addr, ", ", wb);
        assertFalse(result.isMissing());
        assertEquals("INTEGER", result.toString());
    }

    @Test
    public void testCellOutsideValidationRange_returnsMissing() {
        final DataValidationHelper dvHelper = sheet.getDataValidationHelper();
        final DataValidationConstraint constraint =
            dvHelper.createExplicitListConstraint(new String[]{"A", "B"});
        final CellRangeAddressList addressList = new CellRangeAddressList(0, 0, 0, 0);
        final DataValidation validation = dvHelper.createValidation(constraint, addressList);
        sheet.addValidationData(validation);

        final CellAddress addr = CellAddress.parse("B2");
        final DataCell result = CellMetadataReader.readValidationType(
            sheet, addr, ", ", wb);
        assertTrue(result.isMissing());
    }

    @Test
    public void testWithRangeBasedListValidation_resolvesValues() {
        final Row r = sheet.createRow(10);
        r.createCell(0).setCellValue("Alpha");
        r.createCell(1).setCellValue("Beta");
        final Row r2 = sheet.createRow(11);
        r2.createCell(0).setCellValue("Gamma");

        final DataValidationHelper dvHelper = sheet.getDataValidationHelper();
        final DataValidationConstraint constraint =
            dvHelper.createFormulaListConstraint("$A$11:$A$12");
        final CellRangeAddressList addressList = new CellRangeAddressList(0, 0, 0, 0);
        final DataValidation validation = dvHelper.createValidation(constraint, addressList);
        sheet.addValidationData(validation);

        final CellAddress addr = CellAddress.parse("A1");
        final DataCell result = CellMetadataReader.readValidationType(
            sheet, addr, ", ", wb);
        assertFalse(result.isMissing());
        final String val = result.toString();
        assertTrue(val.startsWith("LIST"));
        assertTrue(val.contains("Alpha"));
        assertTrue(val.contains("Gamma"));
    }
}

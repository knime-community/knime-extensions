package org.geki.knime.excelformreader.excel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.geki.knime.excelformreader.ExcelFormReaderSettings;
import org.geki.knime.excelformreader.domain.CellAddress;
import org.geki.knime.excelformreader.domain.FieldMapping;
import org.geki.knime.excelformreader.domain.FormDefinition;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.NodeLogger;

public class ExcelFormExtractor {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(ExcelFormExtractor.class);

    private static final String RANGE_DELIMITER = ", ";

    private final ExcelFormReaderSettings settings;

    public ExcelFormExtractor(final ExcelFormReaderSettings settings) {
        this.settings = settings;
    }

    public static final class CellExtractionResult {
        public final DataCell value;
        public final DataCell formatConditionOperator;
        public final DataCell validationType;

        public CellExtractionResult(final DataCell value,
                                    final DataCell formatConditionOperator,
                                    final DataCell validationType) {
            this.value = value;
            this.formatConditionOperator = formatConditionOperator;
            this.validationType = validationType;
        }
    }

    public Map<String, CellExtractionResult> extract(final Sheet sheet,
                                                      final FormDefinition definition,
                                                      final Workbook workbook) {
        final FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
        final Map<String, CellExtractionResult> result = new LinkedHashMap<>();

        for (final FieldMapping mapping : definition.getFields()) {
            if (mapping.getAddress().isSingleCell()) {
                result.put(mapping.getName(), extractSingleCell(sheet, mapping, evaluator));
            } else {
                result.put(mapping.getName(), extractRange(sheet, mapping, evaluator));
            }
        }

        return result;
    }

    private CellExtractionResult extractSingleCell(final Sheet sheet, final FieldMapping mapping,
                                                    final FormulaEvaluator evaluator) {
        final CellAddress address = mapping.getAddress();
        final Row row = sheet.getRow(address.getStartRow());
        if (row == null) {
            return handleMissingCell(mapping, sheet);
        }
        final Cell cell = row.getCell(address.getStartCol());
        if (cell == null) {
            return handleMissingCell(mapping, sheet);
        }
        final DataCell value = CellValueConverter.convert(cell, mapping.getDataType(), evaluator);
        final DataCell fco = CellMetadataReader.readFormatConditionOperator(sheet, address, RANGE_DELIMITER);
        final DataCell vt = CellMetadataReader.readValidationType(sheet, address, RANGE_DELIMITER);
        return new CellExtractionResult(value, fco, vt);
    }

    private CellExtractionResult extractRange(final Sheet sheet, final FieldMapping mapping,
                                               final FormulaEvaluator evaluator) {
        final CellAddress address = mapping.getAddress();
        final List<String> values = new ArrayList<>();

        for (int r = address.getStartRow(); r <= address.getEndRow(); r++) {
            final Row row = sheet.getRow(r);
            if (row == null) {
                continue;
            }
            for (int c = address.getStartCol(); c <= address.getEndCol(); c++) {
                final Cell cell = row.getCell(c);
                final DataCell converted = CellValueConverter.convert(cell, "string", evaluator);
                if (!converted.isMissing()) {
                    values.add(((StringCell) converted).getStringValue());
                }
            }
        }

        final DataCell value = values.isEmpty()
            ? DataType.getMissingCell()
            : new StringCell(String.join(RANGE_DELIMITER, values));
        final DataCell fco = CellMetadataReader.readFormatConditionOperator(sheet, address, RANGE_DELIMITER);
        final DataCell vt = CellMetadataReader.readValidationType(sheet, address, RANGE_DELIMITER);
        return new CellExtractionResult(value, fco, vt);
    }

    private CellExtractionResult handleMissingCell(final FieldMapping mapping, final Sheet sheet) {
        final CellAddress addr = mapping.getAddress();
        final String addrStr = addr.isSingleCell()
            ? "row " + addr.getStartRow() + ", col " + addr.getStartCol()
            : "rows " + addr.getStartRow() + "-" + addr.getEndRow()
              + ", cols " + addr.getStartCol() + "-" + addr.getEndCol();
        final String msg = "Cell not found for field '" + mapping.getName()
            + "' at address '" + addrStr + "' in sheet '" + sheet.getSheetName() + "'";

        if (settings.getOnMissingCell() == ExcelFormReaderSettings.ErrorHandling.FAIL) {
            throw new RuntimeException(msg);
        }
        LOGGER.warn(msg);
        return new CellExtractionResult(
            DataType.getMissingCell(),
            DataType.getMissingCell(),
            DataType.getMissingCell());
    }
}

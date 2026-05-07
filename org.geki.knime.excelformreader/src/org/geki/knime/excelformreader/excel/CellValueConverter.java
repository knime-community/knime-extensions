package org.geki.knime.excelformreader.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.data.def.BooleanCell;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.LongCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.NodeLogger;

public class CellValueConverter {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(CellValueConverter.class);

    CellValueConverter() {}

    public static DataCell convert(final Cell cell, final String dataType,
                                   final FormulaEvaluator evaluator) {
        if (cell == null) {
            return DataType.getMissingCell();
        }

        CellValue cellValue;
        try {
            cellValue = evaluator.evaluate(cell);
        } catch (final Exception e) {
            LOGGER.warn("Failed to evaluate formula at row " + cell.getRowIndex()
                + ", col " + cell.getColumnIndex() + ": " + e.getMessage());
            return DataType.getMissingCell();
        }

        if (cellValue == null || cellValue.getCellType() == CellType.BLANK) {
            return DataType.getMissingCell();
        }

        final String normalizedType = (dataType == null) ? "string" : dataType.trim().toLowerCase();

        switch (normalizedType) {
            case "string":  return convertToString(cell, evaluator);
            case "int":     return convertToInt(cellValue);
            case "double":  return convertToDouble(cellValue);
            case "boolean": return convertToBoolean(cellValue);
            case "date":    return convertToDate(cell, cellValue);
            default:
                LOGGER.warn("Unknown dataType '" + dataType + "', falling back to string");
                return convertToString(cell, evaluator);
        }
    }

    private static DataCell convertToString(final Cell cell, final FormulaEvaluator evaluator) {
        final String value = new DataFormatter().formatCellValue(cell, evaluator);
        if (value == null || value.isEmpty()) {
            return DataType.getMissingCell();
        }
        return new StringCell(value);
    }

    private static DataCell convertToInt(final CellValue cellValue) {
        if (cellValue.getCellType() == CellType.NUMERIC) {
            return new LongCell((long) cellValue.getNumberValue());
        }
        if (cellValue.getCellType() == CellType.STRING) {
            try {
                return new LongCell(Long.parseLong(cellValue.getStringValue().trim()));
            } catch (final NumberFormatException e) {
                LOGGER.warn("Cannot parse int value: '" + cellValue.getStringValue() + "'");
            }
        }
        return DataType.getMissingCell();
    }

    private static DataCell convertToDouble(final CellValue cellValue) {
        if (cellValue.getCellType() == CellType.NUMERIC) {
            return new DoubleCell(cellValue.getNumberValue());
        }
        if (cellValue.getCellType() == CellType.STRING) {
            try {
                return new DoubleCell(Double.parseDouble(cellValue.getStringValue().trim()));
            } catch (final NumberFormatException e) {
                LOGGER.warn("Cannot parse double value: '" + cellValue.getStringValue() + "'");
            }
        }
        return DataType.getMissingCell();
    }

    private static DataCell convertToBoolean(final CellValue cellValue) {
        if (cellValue.getCellType() == CellType.BOOLEAN) {
            return cellValue.getBooleanValue() ? BooleanCell.TRUE : BooleanCell.FALSE;
        }
        if (cellValue.getCellType() == CellType.STRING) {
            final String val = cellValue.getStringValue().trim().toLowerCase();
            if ("true".equals(val) || "yes".equals(val) || "1".equals(val)) {
                return BooleanCell.TRUE;
            }
            if ("false".equals(val) || "no".equals(val) || "0".equals(val)) {
                return BooleanCell.FALSE;
            }
        }
        return DataType.getMissingCell();
    }

    private static DataCell convertToDate(final Cell cell, final CellValue cellValue) {
        if (cellValue.getCellType() != CellType.NUMERIC) {
            LOGGER.warn("Cell at row " + cell.getRowIndex() + ", col " + cell.getColumnIndex()
                + " is not numeric, cannot extract date");
            return DataType.getMissingCell();
        }
        if (!DateUtil.isCellDateFormatted(cell)) {
            LOGGER.warn("Cell at row " + cell.getRowIndex() + ", col " + cell.getColumnIndex()
                + " is not date-formatted");
            return DataType.getMissingCell();
        }
        try {
            final java.time.LocalDateTime ldt = cell.getLocalDateTimeCellValue();
            return new StringCell(ldt.toLocalDate().toString());
        } catch (final Exception e) {
            LOGGER.warn("Cannot extract date from cell at row " + cell.getRowIndex()
                + ", col " + cell.getColumnIndex() + ": " + e.getMessage());
            return DataType.getMissingCell();
        }
    }
}

package org.geki.knime.excelformreader.excel;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.ComparisonOperator;
import org.apache.poi.ss.usermodel.ConditionType;
import org.apache.poi.ss.usermodel.ConditionalFormatting;
import org.apache.poi.ss.usermodel.ConditionalFormattingRule;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.geki.knime.excelformreader.domain.CellAddress;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.NodeLogger;

public class CellMetadataReader {

    private static final NodeLogger LOGGER =
        NodeLogger.getLogger(CellMetadataReader.class);

    private CellMetadataReader() {}

    /**
     * Returns the conditional formatting operator(s) applied to the cell(s)
     * described by address. For CELL_VALUE_IS rules the comparison operator
     * name is returned; for other rule types the condition type name is used.
     * Multiple distinct values are joined with rangeDelimiter.
     * Returns MissingCell when no matching rules exist.
     */
    public static DataCell readFormatConditionOperator(
            final Sheet sheet,
            final CellAddress address,
            final String rangeDelimiter) {
        try {
            final int numCF = sheet.getSheetConditionalFormatting()
                .getNumConditionalFormattings();
            final List<int[]> coords = getCellCoordinates(address);
            final List<String> results = new ArrayList<>();

            for (final int[] coord : coords) {
                final int row = coord[0];
                final int col = coord[1];
                final List<String> operators = new ArrayList<>();

                for (int i = 0; i < numCF; i++) {
                    final ConditionalFormatting cf =
                        sheet.getSheetConditionalFormatting()
                             .getConditionalFormattingAt(i);
                    for (final CellRangeAddress cfRange : cf.getFormattingRanges()) {
                        if (cfRange.isInRange(row, col)) {
                            for (int r = 0; r < cf.getNumberOfRules(); r++) {
                                final ConditionalFormattingRule rule = cf.getRule(r);
                                final String opName = formatConditionName(rule);
                                if (!operators.contains(opName)) {
                                    operators.add(opName);
                                }
                            }
                            break;
                        }
                    }
                }

                if (!operators.isEmpty()) {
                    results.add(String.join(rangeDelimiter, operators));
                }
            }

            if (results.isEmpty()) {
                return DataType.getMissingCell();
            }
            return new StringCell(String.join(rangeDelimiter, results));

        } catch (final Exception e) {
            LOGGER.warn("Could not read conditional formatting: " + e.getMessage());
            return DataType.getMissingCell();
        }
    }

    /**
     * Returns the data validation type(s) applied to the cell(s) described
     * by address. Multiple distinct values are joined with rangeDelimiter.
     * Returns MissingCell when no matching validation rules exist.
     */
    public static DataCell readValidationType(
            final Sheet sheet,
            final CellAddress address,
            final String rangeDelimiter) {
        try {
            final List<? extends DataValidation> validations =
                sheet.getDataValidations();
            final List<int[]> coords = getCellCoordinates(address);
            final List<String> results = new ArrayList<>();

            for (final int[] coord : coords) {
                final int row = coord[0];
                final int col = coord[1];
                final List<String> types = new ArrayList<>();

                for (final DataValidation dv : validations) {
                    final CellRangeAddressList regions = dv.getRegions();
                    for (final CellRangeAddress region :
                            regions.getCellRangeAddresses()) {
                        if (region.isInRange(row, col)) {
                            final String typeName = validationTypeName(
                                dv.getValidationConstraint().getValidationType());
                            if (!types.contains(typeName)) {
                                types.add(typeName);
                            }
                            break;
                        }
                    }
                }

                if (!types.isEmpty()) {
                    results.add(String.join(rangeDelimiter, types));
                }
            }

            if (results.isEmpty()) {
                return DataType.getMissingCell();
            }
            return new StringCell(String.join(rangeDelimiter, results));

        } catch (final Exception e) {
            LOGGER.warn("Could not read data validation: " + e.getMessage());
            return DataType.getMissingCell();
        }
    }

    private static String formatConditionName(final ConditionalFormattingRule rule) {
        final ConditionType type = rule.getConditionType();
        if (type != null && type.id == ConditionType.CELL_VALUE_IS.id) {
            return comparisonOperatorName(rule.getComparisonOperation());
        }
        return type != null ? type.toString() : "UNKNOWN";
    }

    private static String comparisonOperatorName(final byte op) {
        if (op == ComparisonOperator.BETWEEN)     return "BETWEEN";
        if (op == ComparisonOperator.NOT_BETWEEN) return "NOT_BETWEEN";
        if (op == ComparisonOperator.EQUAL)       return "EQUAL";
        if (op == ComparisonOperator.NOT_EQUAL)   return "NOT_EQUAL";
        if (op == ComparisonOperator.GT)          return "GT";
        if (op == ComparisonOperator.LT)          return "LT";
        if (op == ComparisonOperator.GE)          return "GE";
        if (op == ComparisonOperator.LE)          return "LE";
        return "NO_COMPARISON";
    }

    private static String validationTypeName(final int type) {
        switch (type) {
            case DataValidationConstraint.ValidationType.ANY:         return "ANY";
            case DataValidationConstraint.ValidationType.INTEGER:     return "INTEGER";
            case DataValidationConstraint.ValidationType.DECIMAL:     return "DECIMAL";
            case DataValidationConstraint.ValidationType.LIST:        return "LIST";
            case DataValidationConstraint.ValidationType.DATE:        return "DATE";
            case DataValidationConstraint.ValidationType.TIME:        return "TIME";
            case DataValidationConstraint.ValidationType.TEXT_LENGTH: return "TEXT_LENGTH";
            case DataValidationConstraint.ValidationType.FORMULA:     return "FORMULA";
            default: return "UNKNOWN(" + type + ")";
        }
    }

    private static List<int[]> getCellCoordinates(final CellAddress address) {
        final List<int[]> coords = new ArrayList<>();
        for (int r = address.getStartRow(); r <= address.getEndRow(); r++) {
            for (int c = address.getStartCol(); c <= address.getEndCol(); c++) {
                coords.add(new int[]{r, c});
            }
        }
        return coords;
    }
}

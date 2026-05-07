package org.geki.knime.excelformreader.output;

import java.util.Map;

import org.geki.knime.excelformreader.domain.FieldMapping;
import org.geki.knime.excelformreader.domain.FormDefinition;
import org.geki.knime.excelformreader.excel.ExcelFormExtractor.CellExtractionResult;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;

public class WideOutputBuilder {

    private final DataTableSpec spec;
    private final boolean includeSourceFilename;
    private final boolean includeSheetName;
    private final boolean includeLabelFields;
    private final boolean includeFormatCondition;
    private final boolean includeValidationType;

    public WideOutputBuilder(final DataTableSpec spec,
                              final boolean includeSourceFilename,
                              final boolean includeSheetName,
                              final boolean includeLabelFields,
                              final boolean includeFormatCondition,
                              final boolean includeValidationType) {
        this.spec = spec;
        this.includeSourceFilename = includeSourceFilename;
        this.includeSheetName = includeSheetName;
        this.includeLabelFields = includeLabelFields;
        this.includeFormatCondition = includeFormatCondition;
        this.includeValidationType = includeValidationType;
    }

    public DataRow buildRow(final String sourceFile,
                             final String sheetName,
                             final Map<String, CellExtractionResult> results,
                             final FormDefinition definition,
                             final long rowIndex) {
        final DataCell[] cells = new DataCell[spec.getNumColumns()];
        int i = 0;

        if (includeSourceFilename) {
            cells[i++] = new StringCell(sourceFile != null ? sourceFile : "");
        }
        if (includeSheetName) {
            cells[i++] = new StringCell(sheetName != null ? sheetName : "");
        }

        final Iterable<FieldMapping> fields = includeLabelFields
            ? definition.getFields()
            : definition.getDataFields();

        for (final FieldMapping mapping : fields) {
            final CellExtractionResult result = results != null
                ? results.get(mapping.getName())
                : null;

            cells[i++] = result != null ? result.value : DataType.getMissingCell();

            if (includeFormatCondition) {
                cells[i++] = result != null
                    ? result.formatConditionOperator
                    : DataType.getMissingCell();
            }
            if (includeValidationType) {
                cells[i++] = result != null
                    ? result.validationType
                    : DataType.getMissingCell();
            }
        }

        while (i < cells.length) {
            cells[i++] = DataType.getMissingCell();
        }

        return new DefaultRow(new RowKey("Row" + rowIndex), cells);
    }
}

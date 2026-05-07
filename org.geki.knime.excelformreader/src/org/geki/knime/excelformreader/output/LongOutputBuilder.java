package org.geki.knime.excelformreader.output;

import java.util.ArrayList;
import java.util.List;
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

public class LongOutputBuilder {

    private final DataTableSpec spec;
    private final boolean includeSourceFilename;
    private final boolean includeSheetName;
    private final boolean includeLabelFields;
    private final boolean includeFormatCondition;
    private final boolean includeValidationType;

    public LongOutputBuilder(final DataTableSpec spec,
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

    public List<DataRow> buildRows(final String sourceFile,
                                    final String sheetName,
                                    final Map<String, CellExtractionResult> results,
                                    final FormDefinition definition,
                                    final long rowIndexBase) {
        final List<DataRow> rows = new ArrayList<>();
        int i = 0;

        final Iterable<FieldMapping> fields = includeLabelFields
            ? definition.getFields()
            : definition.getDataFields();

        for (final FieldMapping mapping : fields) {
            final DataCell[] cells = new DataCell[spec.getNumColumns()];
            int col = 0;

            if (includeSourceFilename) {
                cells[col++] = new StringCell(sourceFile != null ? sourceFile : "");
            }
            if (includeSheetName) {
                cells[col++] = new StringCell(sheetName != null ? sheetName : "");
            }

            final CellExtractionResult result = results != null
                ? results.get(mapping.getName())
                : null;

            cells[col++] = new StringCell(mapping.getName());

            final DataCell rawValue = result != null
                ? result.value
                : DataType.getMissingCell();
            cells[col++] = rawValue.isMissing()
                ? DataType.getMissingCell()
                : new StringCell(rawValue.toString());

            if (includeFormatCondition) {
                cells[col++] = result != null
                    ? result.formatConditionOperator
                    : DataType.getMissingCell();
            }
            if (includeValidationType) {
                cells[col++] = result != null
                    ? result.validationType
                    : DataType.getMissingCell();
            }

            while (col < cells.length) {
                cells[col++] = DataType.getMissingCell();
            }

            rows.add(new DefaultRow(new RowKey("Row" + (rowIndexBase + i)), cells));
            i++;
        }

        return rows;
    }
}

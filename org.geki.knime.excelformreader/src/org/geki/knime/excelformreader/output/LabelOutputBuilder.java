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

public class LabelOutputBuilder {

    private final DataTableSpec spec;
    private final boolean includeSourceFilename;
    private final boolean includeSheetName;
    private final boolean includeFormatCondition;
    private final boolean includeValidationType;

    public LabelOutputBuilder(final DataTableSpec spec,
                               final boolean includeSourceFilename,
                               final boolean includeSheetName,
                               final boolean includeFormatCondition,
                               final boolean includeValidationType) {
        this.spec = spec;
        this.includeSourceFilename = includeSourceFilename;
        this.includeSheetName = includeSheetName;
        this.includeFormatCondition = includeFormatCondition;
        this.includeValidationType = includeValidationType;
    }

    public List<DataRow> buildRows(final String sourceFile,
                                    final String sheetName,
                                    final Map<String, CellExtractionResult> results,
                                    final FormDefinition definition,
                                    final long rowIndexBase) {
        final List<DataRow> rows = new ArrayList<>();
        final List<FieldMapping> labelFields = definition.getLabelFields();

        for (int i = 0; i < labelFields.size(); i++) {
            final FieldMapping mapping = labelFields.get(i);
            final DataCell[] cells = new DataCell[spec.getNumColumns()];
            int col = 0;

            if (includeSourceFilename) {
                cells[col++] = new StringCell(sourceFile != null ? sourceFile : "");
            }
            if (includeSheetName) {
                cells[col++] = new StringCell(sheetName != null ? sheetName : "");
            }

            cells[col++] = new StringCell(mapping.getName());
            cells[col++] = new StringCell(mapping.getAddress().toString());

            final CellExtractionResult result = results != null
                ? results.get(mapping.getName())
                : null;
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

            rows.add(new DefaultRow(new RowKey("LabelRow" + (rowIndexBase + i)), cells));
        }

        return rows;
    }
}

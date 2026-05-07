package org.geki.knime.excelformreader.output;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.geki.knime.excelformreader.domain.FieldMapping;
import org.geki.knime.excelformreader.domain.FormDefinition;
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

    public LabelOutputBuilder(final DataTableSpec spec,
                               final boolean includeSourceFilename,
                               final boolean includeSheetName) {
        this.spec = spec;
        this.includeSourceFilename = includeSourceFilename;
        this.includeSheetName = includeSheetName;
    }

    public List<DataRow> buildRows(final String sourceFile,
                                    final String sheetName,
                                    final Map<String, DataCell> extractedValues,
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

            final DataCell raw = (extractedValues != null)
                ? extractedValues.get(mapping.getName())
                : null;
            cells[col++] = (raw == null || raw.isMissing())
                ? DataType.getMissingCell()
                : new StringCell(raw.toString());

            rows.add(new DefaultRow(new RowKey("LabelRow" + (rowIndexBase + i)), cells));
        }

        return rows;
    }
}

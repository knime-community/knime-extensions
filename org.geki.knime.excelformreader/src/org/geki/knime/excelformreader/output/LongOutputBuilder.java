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

public class LongOutputBuilder {

    private final DataTableSpec spec;
    private final boolean includeSourceFilename;
    private final boolean includeSheetName;

    public LongOutputBuilder(final DataTableSpec spec,
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
        int i = 0;

        for (final FieldMapping mapping : definition.getFields()) {
            final DataCell[] cells = new DataCell[spec.getNumColumns()];
            int col = 0;

            if (includeSourceFilename) {
                cells[col++] = new StringCell(sourceFile != null ? sourceFile : "");
            }
            if (includeSheetName) {
                cells[col++] = new StringCell(sheetName != null ? sheetName : "");
            }

            cells[col++] = new StringCell(mapping.getName());

            final DataCell raw = (extractedValues != null)
                ? extractedValues.get(mapping.getName())
                : null;
            if (raw == null || raw.isMissing()) {
                cells[col++] = DataType.getMissingCell();
            } else {
                cells[col++] = new StringCell(raw.toString());
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

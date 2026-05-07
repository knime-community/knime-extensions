package org.geki.knime.excelformreader.output;

import java.util.ArrayList;
import java.util.List;

import org.geki.knime.excelformreader.domain.FieldMapping;
import org.geki.knime.excelformreader.domain.FormDefinition;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.def.BooleanCell;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.LongCell;
import org.knime.core.data.def.StringCell;

public class OutputSpecFactory {

    private OutputSpecFactory() {}

    public static DataTableSpec createWideSpec(final FormDefinition definition,
                                                final boolean includeSourceFilename,
                                                final boolean includeSheetName,
                                                final boolean includeLabelFields) {
        final List<DataColumnSpec> cols = new ArrayList<>();

        if (includeSourceFilename) {
            cols.add(new DataColumnSpecCreator("source_file", StringCell.TYPE).createSpec());
        }
        if (includeSheetName) {
            cols.add(new DataColumnSpecCreator("sheet_name", StringCell.TYPE).createSpec());
        }

        final List<FieldMapping> fields = includeLabelFields
            ? definition.getFields()
            : definition.getDataFields();

        for (final FieldMapping mapping : fields) {
            cols.add(new DataColumnSpecCreator(
                mapping.getName(),
                dataTypeFromString(mapping.getDataType())).createSpec());
        }

        return new DataTableSpec(cols.toArray(new DataColumnSpec[0]));
    }

    public static DataTableSpec createLongSpec(final boolean includeSourceFilename,
                                                final boolean includeSheetName) {
        final List<DataColumnSpec> cols = new ArrayList<>();

        if (includeSourceFilename) {
            cols.add(new DataColumnSpecCreator("source_file", StringCell.TYPE).createSpec());
        }
        if (includeSheetName) {
            cols.add(new DataColumnSpecCreator("sheet_name", StringCell.TYPE).createSpec());
        }

        cols.add(new DataColumnSpecCreator("field_name", StringCell.TYPE).createSpec());
        cols.add(new DataColumnSpecCreator("value", StringCell.TYPE).createSpec());

        return new DataTableSpec(cols.toArray(new DataColumnSpec[0]));
    }

    public static DataTableSpec createLabelSpec(final boolean includeSourceFilename,
                                                 final boolean includeSheetName) {
        final List<DataColumnSpec> cols = new ArrayList<>();

        if (includeSourceFilename) {
            cols.add(new DataColumnSpecCreator("Source File", StringCell.TYPE).createSpec());
        }
        if (includeSheetName) {
            cols.add(new DataColumnSpecCreator("Sheet Name", StringCell.TYPE).createSpec());
        }

        cols.add(new DataColumnSpecCreator("Name", StringCell.TYPE).createSpec());
        cols.add(new DataColumnSpecCreator("Cell Range", StringCell.TYPE).createSpec());
        cols.add(new DataColumnSpecCreator("Cell Content", StringCell.TYPE).createSpec());

        return new DataTableSpec(cols.toArray(new DataColumnSpec[0]));
    }

    private static DataType dataTypeFromString(final String dataType) {
        if (dataType == null) {
            return StringCell.TYPE;
        }
        switch (dataType.trim().toLowerCase()) {
            case "int":     return LongCell.TYPE;
            case "double":  return DoubleCell.TYPE;
            case "boolean": return BooleanCell.TYPE;
            case "date":    return StringCell.TYPE;
            case "string":
            default:        return StringCell.TYPE;
        }
    }
}

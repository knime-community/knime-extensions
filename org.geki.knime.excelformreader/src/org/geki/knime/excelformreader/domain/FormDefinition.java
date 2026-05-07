package org.geki.knime.excelformreader.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;

public class FormDefinition {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(FormDefinition.class);

    private final List<FieldMapping> fields;

    public FormDefinition(final List<FieldMapping> fields) {
        if (fields == null || fields.isEmpty()) {
            throw new IllegalArgumentException("fields must not be null or empty");
        }
        this.fields = Collections.unmodifiableList(new ArrayList<>(fields));
    }

    public List<FieldMapping> getFields() { return fields; }
    public int size() { return fields.size(); }

    public List<FieldMapping> getDataFields() {
        return fields.stream().filter(FieldMapping::isData).collect(Collectors.toList());
    }

    public List<FieldMapping> getLabelFields() {
        return fields.stream().filter(FieldMapping::isLabel).collect(Collectors.toList());
    }

    // Private no-arg constructor for the configure()-time sentinel (empty fields list).
    private FormDefinition() {
        this.fields = Collections.emptyList();
    }

    /**
     * Validates that the required columns are present in the spec and returns a sentinel
     * FormDefinition with no fields. Used at configure() time when no row data is available.
     */
    public static FormDefinition fromSpec(final DataTableSpec spec)
            throws InvalidSettingsException {
        if (findColumnIndex(spec, "Name") < 0) {
            throw new InvalidSettingsException(
                "Form definition table is missing required column 'Name'");
        }
        if (findColumnIndex(spec, "Cell Range") < 0) {
            throw new InvalidSettingsException(
                "Form definition table is missing required column 'Cell Range'");
        }
        return new FormDefinition();
    }

    public static FormDefinition fromDataTable(final BufferedDataTable table)
            throws InvalidSettingsException {
        final DataTableSpec spec = table.getDataTableSpec();

        final int nameIdx = findColumnIndex(spec, "Name");
        if (nameIdx < 0) {
            throw new InvalidSettingsException(
                "Form definition table is missing required column 'Name'");
        }
        final int cellRangeIdx = findColumnIndex(spec, "Cell Range");
        if (cellRangeIdx < 0) {
            throw new InvalidSettingsException(
                "Form definition table is missing required column 'Cell Range'");
        }
        final int contentTypeIdx = findColumnIndex(spec, "Content Type");
        final int dataTypeIdx = findColumnIndex(spec, "Data Type");

        final List<FieldMapping> mappings = new ArrayList<>();
        for (final DataRow row : table) {
            final DataCell nameCell = row.getCell(nameIdx);
            final DataCell cellRangeCell = row.getCell(cellRangeIdx);

            if (nameCell.isMissing() || cellRangeCell.isMissing()) {
                LOGGER.warn("Skipping row " + row.getKey() + ": Name or Cell Range is missing");
                continue;
            }

            final String name = nameCell.toString();
            final String cellRange = cellRangeCell.toString();
            final String contentType = (contentTypeIdx >= 0 && !row.getCell(contentTypeIdx).isMissing())
                ? row.getCell(contentTypeIdx).toString() : null;
            final String dataType = (dataTypeIdx >= 0 && !row.getCell(dataTypeIdx).isMissing())
                ? row.getCell(dataTypeIdx).toString() : null;

            try {
                mappings.add(new FieldMapping(name, cellRange, contentType, dataType));
            } catch (final IllegalArgumentException e) {
                LOGGER.warn("Skipping row " + row.getKey() + ": " + e.getMessage());
            }
        }

        if (mappings.isEmpty()) {
            throw new InvalidSettingsException(
                "Form definition table contains no valid field mappings");
        }

        return new FormDefinition(mappings);
    }

    private static int findColumnIndex(final DataTableSpec spec, final String columnName) {
        for (int i = 0; i < spec.getNumColumns(); i++) {
            if (spec.getColumnSpec(i).getName().equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        return -1;
    }
}

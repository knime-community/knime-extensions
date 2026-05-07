package org.geki.knime.excelformreader.domain;

import java.util.Set;

public class FieldMapping {

    private static final Set<String> VALID_DATA_TYPES = Set.of("string", "int", "double", "date", "boolean");
    private static final Set<String> VALID_CONTENT_TYPES = Set.of("data", "label");

    private final String name;
    private final CellAddress address;
    private final String contentType;
    private final String dataType;

    public FieldMapping(final String name,
                        final String cellRange,
                        final String contentType,
                        final String dataType) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("name must not be null or blank");
        }
        this.name = name.trim();
        this.address = CellAddress.parse(cellRange);

        final String normalizedContent = (contentType == null || contentType.trim().isEmpty())
            ? "data"
            : contentType.trim().toLowerCase();
        if (!VALID_CONTENT_TYPES.contains(normalizedContent)) {
            throw new IllegalArgumentException(
                "Invalid content type: '" + contentType + "'. Must be one of: " + VALID_CONTENT_TYPES);
        }
        this.contentType = normalizedContent;

        final String normalizedType = (dataType == null || dataType.trim().isEmpty())
            ? "string"
            : dataType.trim().toLowerCase();
        if (!VALID_DATA_TYPES.contains(normalizedType)) {
            throw new IllegalArgumentException(
                "Invalid data type: '" + dataType + "'. Must be one of: " + VALID_DATA_TYPES);
        }
        this.dataType = normalizedType;
    }

    public String getName() { return name; }
    public CellAddress getAddress() { return address; }
    public String getContentType() { return contentType; }
    public String getDataType() { return dataType; }

    public boolean isLabel() { return "label".equals(contentType); }
    public boolean isData() { return "data".equals(contentType); }
}

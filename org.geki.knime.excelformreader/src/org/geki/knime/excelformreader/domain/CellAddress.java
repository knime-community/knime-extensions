package org.geki.knime.excelformreader.domain;

import org.apache.poi.ss.util.CellReference;

public class CellAddress {

    private final int startCol;
    private final int startRow;
    private final int endCol;
    private final int endRow;
    private final boolean isRange;

    private CellAddress(final int startCol, final int startRow,
                        final int endCol, final int endRow, final boolean isRange) {
        this.startCol = startCol;
        this.startRow = startRow;
        this.endCol = endCol;
        this.endRow = endRow;
        this.isRange = isRange;
    }

    public static CellAddress parse(final String address) {
        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("Cell address must not be null or blank");
        }
        final String normalized = address.trim().toUpperCase();
        try {
            if (normalized.contains(":")) {
                final String[] parts = normalized.split(":", 2);
                final CellReference start = new CellReference(parts[0].trim());
                final CellReference end = new CellReference(parts[1].trim());
                return new CellAddress(start.getCol(), start.getRow(), end.getCol(), end.getRow(), true);
            } else {
                final CellReference ref = new CellReference(normalized);
                final int col = ref.getCol();
                final int row = ref.getRow();
                return new CellAddress(col, row, col, row, false);
            }
        } catch (final RuntimeException e) {
            throw new IllegalArgumentException("Invalid cell address: '" + address + "'", e);
        }
    }

    public int getStartCol() { return startCol; }
    public int getStartRow() { return startRow; }
    public int getEndCol() { return endCol; }
    public int getEndRow() { return endRow; }
    public boolean isRange() { return isRange; }
    public boolean isSingleCell() { return !isRange; }

    @Override
    public String toString() {
        final String start = new CellReference(startRow, startCol).formatAsString();
        if (!isRange) {
            return start;
        }
        return start + ":" + new CellReference(endRow, endCol).formatAsString();
    }
}

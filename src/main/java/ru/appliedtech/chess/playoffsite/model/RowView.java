package ru.appliedtech.chess.playoffsite.model;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

public class RowView<T extends CellView> {
    private final List<T> cells;

    public RowView(List<T> cells) {
        this.cells = cells != null ? new ArrayList<>(cells) : emptyList();
    }

    public List<T> getCells() {
        return unmodifiableList(cells);
    }
}

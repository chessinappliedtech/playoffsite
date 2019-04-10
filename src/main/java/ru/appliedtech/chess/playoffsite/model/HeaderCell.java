package ru.appliedtech.chess.playoffsite.model;

public class HeaderCell extends CellView {
    public HeaderCell(String value, int colspan, int rowspan) {
        super(value, colspan, rowspan);
    }

    public HeaderCell(String value) {
        this(value, 1, 1);
    }
}

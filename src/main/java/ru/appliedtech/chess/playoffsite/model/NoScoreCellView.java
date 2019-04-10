package ru.appliedtech.chess.playoffsite.model;

public class NoScoreCellView extends CellView {
    public NoScoreCellView() {
        this(1, 1);
    }

    public NoScoreCellView(int colspan, int rowspan) {
        super("", colspan, rowspan);
    }
}

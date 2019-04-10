package ru.appliedtech.chess.playoffsite.model;

import java.math.BigDecimal;

public class RatingCellView extends CellView {
    public RatingCellView(BigDecimal value) {
        this(value, 1, 1);
    }

    public RatingCellView(BigDecimal value, int colspan, int rowspan) {
        super(String.valueOf(value), colspan, rowspan);
    }
}

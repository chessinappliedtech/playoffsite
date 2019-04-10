package ru.appliedtech.chess.playoffsite.model;

import java.math.BigDecimal;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;

public class ScoreCellView extends CellView {
    public ScoreCellView(BigDecimal value) {
        this(value, 1, 1);
    }

    public ScoreCellView(BigDecimal value, int colspan, int rowspan) {
        super(scoreToString(value), colspan, rowspan);
    }


    public static String scoreToString(BigDecimal value) {
        String result;
        BigDecimal wholePart = new BigDecimal(value.intValue());
        String wholePartString = wholePart.toBigInteger().toString();
        if (isWhole(value)) {
            result = wholePartString;
        } else if (hasHalf(value)) {
            boolean zero = wholePart.compareTo(ZERO) == 0;
            result = (zero ? "" : wholePartString) + "&#189;";
        } else if (hasFourth(value)) {
            boolean zero = wholePart.compareTo(ZERO) == 0;
            result = (zero ? "" : wholePartString) + "&#188;";
        } else if (hasThreeQuarters(value)) {
            boolean zero = wholePart.compareTo(ZERO) == 0;
            result = (zero ? "" : wholePartString) + "&#190;";
        } else {
            result = value.toString();
        }
        return result;
    }

    private static boolean isWhole(BigDecimal value) {
        return value.remainder(ONE).compareTo(ZERO) == 0;
    }

    private static boolean hasHalf(BigDecimal value) {
        return value.remainder(ONE).compareTo(new BigDecimal(0.5)) == 0;
    }

    private static boolean hasFourth(BigDecimal value) {
        return value.remainder(ONE).compareTo(new BigDecimal(0.25)) == 0;
    }

    private static boolean hasThreeQuarters(BigDecimal value) {
        return value.remainder(ONE).compareTo(new BigDecimal(0.75)) == 0;
    }
}

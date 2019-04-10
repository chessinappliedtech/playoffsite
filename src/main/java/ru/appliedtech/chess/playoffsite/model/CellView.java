package ru.appliedtech.chess.playoffsite.model;

public class CellView {
    private final String value;
    private final String link;
    private final int colspan;
    private final int rowspan;
    private String style;

    public CellView(String value) {
        this(value, 1, 1);
    }

    public CellView(String value, int colspan, int rowspan) {
        this(value, null, colspan, rowspan);
    }

    public CellView(String value, String link, int colspan, int rowspan) {
        this.value = value;
        this.link = link;
        this.colspan = colspan;
        this.rowspan = rowspan;
    }

    public String getValue() {
        return value;
    }

    public int getColspan() {
        return colspan;
    }

    public int getRowspan() {
        return rowspan;
    }

    public String getLink() {
        return link;
    }

    @Override
    public String toString() {
        return value;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String getStyle() {
        return style;
    }
}

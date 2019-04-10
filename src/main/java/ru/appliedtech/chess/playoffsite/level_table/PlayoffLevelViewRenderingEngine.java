package ru.appliedtech.chess.playoffsite.level_table;

import java.io.IOException;
import java.io.Writer;

public interface PlayoffLevelViewRenderingEngine {
    void render(PlayoffLevelTableView playoffLevelTableView, Writer writer) throws IOException;
}

package ru.appliedtech.chess.playoffsite.level_table;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.Writer;

public class PlayoffLevelViewHtmlRenderingEngine implements PlayoffLevelViewRenderingEngine {
    private final Configuration templatesConfiguration;

    public PlayoffLevelViewHtmlRenderingEngine(Configuration templatesConfiguration) {
        this.templatesConfiguration = templatesConfiguration;
    }

    @Override
    public void render(PlayoffLevelTableView playoffLevelTableView, Writer writer) throws IOException {
        Template template = templatesConfiguration.getTemplate("playoffLevelTable.ftl");
        try {
            template.process(playoffLevelTableView, writer);
            writer.flush();
        } catch (TemplateException e) {
            throw new IOException(e);
        }
    }
}

package ru.appliedtech.chess.playoffsite.level_table;

import ru.appliedtech.chess.Color;
import ru.appliedtech.chess.Player;
import ru.appliedtech.chess.TimeControlType;
import ru.appliedtech.chess.TournamentDescription;
import ru.appliedtech.chess.playoff.MatchScore;
import ru.appliedtech.chess.playoff.PlayoffLevel;
import ru.appliedtech.chess.playoff.PlayoffLevelTable;
import ru.appliedtech.chess.playoffsite.model.*;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static ru.appliedtech.chess.playoffsite.model.ScoreCellView.scoreToString;

public class PlayoffLevelTableView {
    private final ResourceBundle resourceBundle;
    private final TournamentDescription tournamentDescription;
    private final HeaderRowView headerRowView;
    private final List<PlayerRowView> playerRowViews;
    private final String levelDescription;

    public PlayoffLevelTableView(Locale locale,
                                 PlayoffLevelTable playoffLevelTable,
                                 TournamentDescription tournamentDescription) {
        this.resourceBundle = ResourceBundle.getBundle("resources", locale);
        this.headerRowView = createHeaderRowView(playoffLevelTable);
        this.playerRowViews = createPlayerRowViews(playoffLevelTable);
        this.levelDescription = constructLevelDescription(playoffLevelTable.getLevel());
        this.tournamentDescription = tournamentDescription;
    }

    private String constructLevelDescription(PlayoffLevel level) {
        return resourceBundle.getString("playoff.table.view." + level.name().toLowerCase());
    }

    public String getTournamentTitle() {
        return tournamentDescription.getTournamentTitle();
    }

    public HeaderRowView getHeaderRowView() {
        return headerRowView;
    }

    public List<PlayerRowView> getPlayerRowViews() {
        return playerRowViews;
    }

    public String getLevelDescription() {
        return levelDescription;
    }

    private HeaderRowView createHeaderRowView(PlayoffLevelTable playoffLevelTable) {
        List<HeaderCell> headerCells = new ArrayList<>();

        headerCells.add(new HeaderCell(resourceBundle.getString("playoff.table.view.header.player")));
        for (int i = 0; i < playoffLevelTable.getLevelSetup().getClassicRounds(); i++) {
            headerCells.add(new HeaderCell(
                    resourceBundle.getString("playoff.table.view.header.classic_round") + (i + 1)));
        }
        for (int i = 0; i < playoffLevelTable.getLevelSetup().getRapidRounds(); i++) {
            headerCells.add(new HeaderCell(
                    resourceBundle.getString("playoff.table.view.header.rapid_round") + (i + 1)));
        }
        for (int i = 0; i < playoffLevelTable.getLevelSetup().getBlitzRounds(); i++) {
            headerCells.add(new HeaderCell(
                    resourceBundle.getString("playoff.table.view.header.blitz_round") + (i + 1)));
        }
        if (playoffLevelTable.getLevelSetup().hasArmageddon()) {
            headerCells.add(new HeaderCell(
                    resourceBundle.getString("playoff.table.view.header.armageddon")));
        }
        headerCells.add(new HeaderCell(
                resourceBundle.getString("playoff.table.view.header.total_score")));
        headerCells.add(new HeaderCell(
                resourceBundle.getString("playoff.table.view.header.win")));
        return new HeaderRowView(headerCells);
    }

    private List<PlayerRowView> createPlayerRowViews(PlayoffLevelTable playoffLevelTable) {
        List<PlayerRowView> rowViews = new ArrayList<>();
        if (playoffLevelTable.arePlayersAssigned()) {
            rowViews.add(new PlayerRowView(createPlayerCells(playoffLevelTable, playoffLevelTable.getPlayer1())));
            rowViews.add(new PlayerRowView(createPlayerCells(playoffLevelTable, playoffLevelTable.getPlayer2())));
        } else {
            rowViews.add(new PlayerRowView(createEmptyPlayerCells(playoffLevelTable, playoffLevelTable.getPlayer1())));
            rowViews.add(new PlayerRowView(createEmptyPlayerCells(playoffLevelTable, playoffLevelTable.getPlayer2())));
        }
        return rowViews;
    }

    private List<CellView> createPlayerCells(PlayoffLevelTable playoffLevelTable, Player player) {
        List<CellView> cells = new ArrayList<>();
        String playerName = MessageFormat.format("{0} {1}",
                player.getFirstName(), player.getLastName());
        CellView playerCell = new CellView(playerName);
        playerCell.setStyle("text-center fixed-square-player");
        cells.add(playerCell);
        String playerId = player.getId();
        List<BigDecimal> classicScores = playoffLevelTable.getGameScores(playerId, TimeControlType.CLASSIC);
        for (int i = 0; i < playoffLevelTable.getLevelSetup().getClassicRounds(); i++) {
            CellView scoreCell;
            if (i < classicScores.size()) {
                scoreCell = new ScoreCellView(classicScores.get(i));
            } else {
                scoreCell = new NoScoreCellView();
            }
            scoreCell.setStyle("text-center fixed-square-score");
            cells.add(scoreCell);
        }
        List<BigDecimal> rapidScores = playoffLevelTable.getGameScores(playerId, TimeControlType.RAPID);
        for (int i = 0; i < playoffLevelTable.getLevelSetup().getRapidRounds(); i++) {
            CellView scoreCell;
            if (i < rapidScores.size()) {
                scoreCell = new ScoreCellView(rapidScores.get(i));
            } else {
                scoreCell = new NoScoreCellView();
            }
            scoreCell.setStyle("text-center fixed-square-score");
            cells.add(scoreCell);
        }
        List<BigDecimal> blitzScores = playoffLevelTable.getGameScores(playerId, TimeControlType.BLITZ);
        for (int i = 0; i < playoffLevelTable.getLevelSetup().getBlitzRounds(); i++) {
            CellView scoreCell;
            if (i < blitzScores.size()) {
                scoreCell = new ScoreCellView(blitzScores.get(i));
            } else {
                scoreCell = new NoScoreCellView();
            }
            scoreCell.setStyle("text-center fixed-square-score");
            cells.add(scoreCell);
        }
        String armageddonColorMark = playoffLevelTable.getArmageddonColor(playerId)
                .map(c -> c == Color.white
                        ? resourceBundle.getString("playoff.table.view.armageddon.white")
                        : resourceBundle.getString("playoff.table.view.armageddon.black"))
                .map(m -> "(" + m +")")
                .orElse("");
        CellView armageddonScoreCell = playoffLevelTable.getArmageddonScore(playerId)
                .map(score -> new CellView(scoreToString(score) + " " + armageddonColorMark))
                .orElse(new NoScoreCellView());
        armageddonScoreCell.setStyle("text-center fixed-square-score");
        cells.add(armageddonScoreCell);
        ScoreCellView totalScoreCell = new ScoreCellView(playoffLevelTable.getTotalScore(playerId));
        totalScoreCell.setStyle("text-center fixed-square-score");
        cells.add(totalScoreCell);
        CellView winCell;
        if (playoffLevelTable.isWin(playerId) == MatchScore.win) {
            winCell = new ScoreCellView(BigDecimal.ONE);
        } else if (playoffLevelTable.isWin(playerId) == MatchScore.lose) {
            winCell = new ScoreCellView(BigDecimal.ZERO);
        } else {
            winCell = new NoScoreCellView();
        }
        winCell.setStyle("text-center fixed-square-score");
        cells.add(winCell);
        return cells;
    }

    private List<CellView> createEmptyPlayerCells(PlayoffLevelTable playoffLevelTable, Player player) {
        List<CellView> cells = new ArrayList<>();
        if (player != null) {
            String playerName = MessageFormat.format("{0} {1}",
                    player.getFirstName(), player.getLastName());
            CellView cell = new CellView(playerName);
            cell.setStyle("text-center fixed-square-player");
            cells.add(cell);
        } else {
            CellView cell = new CellView("");
            cell.setStyle("text-center fixed-square-player");
            cells.add(cell);
        }
        for (int i = 0; i < playoffLevelTable.getLevelSetup().getClassicRounds(); i++) {
            NoScoreCellView cell = new NoScoreCellView();
            cell.setStyle("text-center fixed-square-score");
            cells.add(cell);
        }
        for (int i = 0; i < playoffLevelTable.getLevelSetup().getRapidRounds(); i++) {
            NoScoreCellView cell = new NoScoreCellView();
            cell.setStyle("text-center fixed-square-score");
            cells.add(cell);
        }
        for (int i = 0; i < playoffLevelTable.getLevelSetup().getBlitzRounds(); i++) {
            NoScoreCellView cell = new NoScoreCellView();
            cell.setStyle("text-center fixed-square-score");
            cells.add(cell);
        }
        {
            NoScoreCellView cell = new NoScoreCellView();
            cell.setStyle("text-center fixed-square-score");
            cells.add(cell);
        }
        {
            NoScoreCellView cell = new NoScoreCellView();
            cell.setStyle("text-center fixed-square-score");
            cells.add(cell);
        }
        {
            NoScoreCellView cell = new NoScoreCellView();
            cell.setStyle("text-center fixed-square-score");
            cells.add(cell);
        }
        return cells;
    }
}

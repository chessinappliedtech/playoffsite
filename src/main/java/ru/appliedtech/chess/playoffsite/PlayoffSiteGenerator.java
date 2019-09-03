package ru.appliedtech.chess.playoffsite;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import ru.appliedtech.chess.*;
import ru.appliedtech.chess.playoff.PlayoffLevel;
import ru.appliedtech.chess.playoff.PlayoffLevelPlayers;
import ru.appliedtech.chess.playoff.PlayoffLevelTable;
import ru.appliedtech.chess.playoff.PlayoffSetup;
import ru.appliedtech.chess.playoff.io.PlayoffSetupObjectNodeReader;
import ru.appliedtech.chess.playoffsite.level_table.PlayoffLevelTableView;
import ru.appliedtech.chess.playoffsite.level_table.PlayoffLevelViewHtmlRenderingEngine;
import ru.appliedtech.chess.storage.GameReadOnlyStorage;
import ru.appliedtech.chess.storage.GameStorage;
import ru.appliedtech.chess.storage.PlayerReadOnlyStorage;
import ru.appliedtech.chess.storage.PlayerStorage;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;

import static java.util.Collections.emptyMap;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class PlayoffSiteGenerator {

    public static void main(String[] args) throws IOException, TemplateException {
        new PlayoffSiteGenerator().run(
                args[0],
                args[1],
                args[2],
                args[3],
                args[4],
                args[5]);
    }

    public void run(String localeDef,
                    String tournamentDescriptionFilePath,
                    String playersFilePath,
                    String gamesFilePath,
                    String levelPlayersFilePath,
                    String outputDir) throws IOException, TemplateException {
        Configuration configuration = createTemplatesConfiguration();
        TournamentDescription tournamentDescription = readTournamentDescription(tournamentDescriptionFilePath);
        PlayoffSetup playoffSetup = (PlayoffSetup) tournamentDescription.getTournamentSetup();
        PlayerStorage playerStorage = readPlayers(playersFilePath, tournamentDescription);
        GameStorage gameStorage = readGames(gamesFilePath, tournamentDescription);
        List<PlayoffLevelPlayers> playoffLevelPlayers = readPlayoffLevelPlayers(levelPlayersFilePath);

        //noinspection ResultOfMethodCallIgnored
        new File(outputDir).mkdirs();

        Locale locale = resolveLocale(localeDef);

        List<String> matches = new ArrayList<>();
        for (PlayoffLevelPlayers levelPlayers : playoffLevelPlayers) {
            PlayoffLevelTable playoffLevelTable = new PlayoffLevelTable(
                    levelPlayers.getLevel(), levelPlayers, playerStorage,
                    gameStorage,
                    playoffSetup, tournamentDescription);
            PlayoffLevelTableView view = new PlayoffLevelTableView(
                    locale, playoffLevelTable, tournamentDescription);
            PlayoffLevelViewHtmlRenderingEngine renderingEngine = new PlayoffLevelViewHtmlRenderingEngine(configuration);
            try (StringWriter writer = new StringWriter()) {
                renderingEngine.render(view, writer);
                matches.add(writer.toString());
            }
        }
        Map<String, Object> model = new HashMap<>();
        model.put("matches", matches);
        model.put("tournamentDescription", resolve(tournamentDescription, readKnownParticipants(playersFilePath)));
        try (Writer writer = new OutputStreamWriter(
                new FileOutputStream(new File(outputDir, "index.html")),
                StandardCharsets.UTF_8)) {
            Template template = configuration.getTemplate("index.ftl");
            template.process(model, writer);
        }
    }

    private List<Player> readKnownParticipants(String playersFilePath) throws IOException {
        ObjectMapper baseMapper = new ChessBaseObjectMapper(emptyMap());
        List<Player> players;
        try (FileInputStream fis = new FileInputStream(playersFilePath)) {
            players = baseMapper.readValue(fis, new TypeReference<ArrayList<Player>>() {});
        }
        return players;
    }

    private Map<String, String> resolve(TournamentDescription tournamentDescription, List<Player> registeredPlayers) {
        Map<String, String> result = new HashMap<>();
        result.put("tournamentTitle", tournamentDescription.getTournamentTitle());
        result.put("tournamentId", tournamentDescription.getTournamentId());
        result.put("arbiter", idToName(registeredPlayers).apply(tournamentDescription.getArbiter()));
        String deputyArbiters = tournamentDescription.getDeputyArbiters().stream()
                .map(idToName(registeredPlayers))
                .collect(joining(", "));
        result.put("deputyArbiters", deputyArbiters);
        String gameWriters = tournamentDescription.getGameWriters().stream()
                .map(idToName(registeredPlayers))
                .collect(joining(", "));
        result.put("gameWriters", gameWriters);
        result.put("regulations", tournamentDescription.getRegulations());
        if (tournamentDescription.getLinks() != null) {
            tournamentDescription.getLinks().stream()
                    .filter(l -> l.getPurpose().equals("roundrobin"))
                    .findFirst()
                    .ifPresent(l -> {
                        result.put("linkvalue", l.getValue());
                        result.put("linkname", l.getName());
                    });
        }
        return result;
    }

    private Function<String, String> idToName(List<Player> registeredPlayers) {
        return id -> registeredPlayers.stream().filter(p -> p.getId().equals(id)).map(p -> p.getFirstName() + " " + p.getLastName())
                .findFirst().orElse(id);
    }

    private List<PlayoffLevelPlayers> readPlayoffLevelPlayers(String levelPlayersFilePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        List<PlayoffLevelPlayersJsonRecord> records;
        try (FileInputStream fis = new FileInputStream(levelPlayersFilePath)) {
            records = mapper.readValue(fis, new TypeReference<ArrayList<PlayoffLevelPlayersJsonRecord>>() {});
        }
        return records.stream()
                .map(r -> new PlayoffLevelPlayers(r.getLevel(), r.getPlayer1(), r.getPlayer2()))
                .collect(toList());
    }

    public static final class PlayoffLevelPlayersJsonRecord {
        @JsonProperty("player1")
        private String player1;
        @JsonProperty("player2")
        private String player2;
        @JsonProperty("level")
        private PlayoffLevel level;

        public PlayoffLevel getLevel() {
            return level;
        }

        public String getPlayer1() {
            return player1;
        }

        public String getPlayer2() {
            return player2;
        }
    }

    private Locale resolveLocale(String localeDef) {
        String[] strings = localeDef.split("_");
        String language = strings.length > 0 ? strings[0] : "";
        String country = strings.length > 1 ? strings[1] : "";
        String variant = strings.length > 2 ? strings[2] : "";
        return language.isEmpty() ? Locale.US : new Locale(language, country, variant);
    }

    private GameStorage readGames(String gamesFilePath, TournamentDescription tournamentDescription) throws IOException {
        ObjectMapper gameObjectMapper = new GameObjectMapper(tournamentDescription.getTournamentSetup());
        List<Game> games;
        try (FileInputStream fis = new FileInputStream(gamesFilePath)) {
            games = gameObjectMapper.readValue(fis, new TypeReference<ArrayList<Game>>() {});
        }
        return new GameReadOnlyStorage(games);
    }

    private PlayerStorage readPlayers(String playersFilePath, TournamentDescription tournamentDescription) throws IOException {
        ObjectMapper baseMapper = new ChessBaseObjectMapper(emptyMap());
        List<Player> players;
        try (FileInputStream fis = new FileInputStream(playersFilePath)) {
            players = baseMapper.readValue(fis, new TypeReference<ArrayList<Player>>() {});
        }
        List<String> registeredPlayerIds = tournamentDescription.getPlayers();
        List<Player> registeredPlayers = players.stream()
                .filter(player -> registeredPlayerIds.contains(player.getId()))
                .sorted(comparingInt(p -> registeredPlayerIds.indexOf(p.getId())))
                .collect(toList());
        List<String> joinedPlayers = tournamentDescription.getJoinedPlayers();
        registeredPlayers.addAll(players.stream()
                .filter(player -> joinedPlayers.contains(player.getId()))
                .sorted(comparingInt(p -> joinedPlayers.indexOf(p.getId())))
                .collect(toList()));
        return new PlayerReadOnlyStorage(registeredPlayers);
    }

    private TournamentDescription readTournamentDescription(String tournamentDescriptionFilePath) throws IOException {
        Map<String, TournamentSetupObjectNodeReader> tournamentSetupReaders = new HashMap<>();
        tournamentSetupReaders.put("playoff", new PlayoffSetupObjectNodeReader());
        ObjectMapper baseMapper = new ChessBaseObjectMapper(tournamentSetupReaders);
        TournamentDescription tournamentDescription;
        try (FileInputStream fis = new FileInputStream(tournamentDescriptionFilePath)) {
            tournamentDescription = baseMapper.readValue(fis, TournamentDescription.class);
        }
        return tournamentDescription;
    }

    private Configuration createTemplatesConfiguration() {
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_28);
        configuration.setDefaultEncoding(StandardCharsets.UTF_8.displayName());
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        configuration.setLogTemplateExceptions(true);
        configuration.setWrapUncheckedExceptions(true);
        configuration.setClassForTemplateLoading(PlayoffSiteGenerator.class, "/");
        return configuration;
    }
}

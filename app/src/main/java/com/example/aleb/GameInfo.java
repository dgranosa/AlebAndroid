package com.example.aleb;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

enum GameState {
    TRUMP, DECLARE, PLAY, WAIT
}

enum Adut {
    NISTA(-1),
    SRCE(0),
    PIK(1),
    TREF(2),
    KARO(3);

    private int value;
    private static Map map = new HashMap<>();

    private Adut(int value) {
        this.value = value;
    }

    static {
        for (Adut pageType : Adut.values()) {
            map.put(pageType.value, pageType);
        }
    }

    public static Adut valueOf(int pageType) {
        return (Adut)map.get(pageType);
    }

    public int getValue() {
        return value;
    }
}

public class GameInfo {
    List<String> players = new ArrayList<>();
    int myPosition;
    int myTeam;

    GameState gameState;

    int startingPlayer, lastPlayer;
    int currentPlayer;
    int[] cards;
    int[] playgroundCards;

    ArrayList<String>[] lastRoundCards = new ArrayList[4];
    List<String> scoreHistory = new ArrayList<>();

    Adut adut;
    String adutSelectedByPlayer;

    GameInfoInterface gameInfoInterface;

    int zvanja_mi, zvanja_vi, igra_mi, igra_vi, ukupno_mi, ukupno_vi, partija_mi, partija_vi;
    int score_mi, score_vi, ukupno_score_mi, ukupno_score_vi, ukupno_zvanja_mi, ukupno_zvanja_vi, ukupno_rusili_mi, ukupno_rusili_vi;

    GameInfo(String players, String lobbyGameInfo, GameInfoInterface gameInfoInterface) {
        this(players.split(","), lobbyGameInfo, gameInfoInterface);
    }

    GameInfo(String[] players, String lobbyGameInfo, GameInfoInterface gameInfoInterface) {
        this.players.add(players[0]);
        this.players.add(players[2]);
        this.players.add(players[1]);
        this.players.add(players[3]);

        this.myPosition = this.players.indexOf(Constants.USERNAME);
        this.myTeam = this.myPosition % 2;

        for (int i = 0; i < this.myPosition; i++) {
            String s = this.players.get(0);
            this.players.remove(0);
            this.players.add(s);
        }

        currentPlayer = parseFromServerIndex(0);
        startingPlayer = currentPlayer;
        lastPlayer = (startingPlayer + 3) % 4;

        resetCards();
        lastRoundCards[0] = new ArrayList<>();
        lastRoundCards[1] = new ArrayList<>();
        lastRoundCards[2] = new ArrayList<>();
        lastRoundCards[3] = new ArrayList<>();

        if (lobbyGameInfo != null) {
            String[] sGameInfo = lobbyGameInfo.split(",");
            score_mi = Integer.parseInt(sGameInfo[0]);
            score_vi = Integer.parseInt(sGameInfo[1]);
            ukupno_score_mi = Integer.parseInt(sGameInfo[2]);
            ukupno_score_vi = Integer.parseInt(sGameInfo[3]);
            ukupno_zvanja_mi = Integer.parseInt(sGameInfo[4]);
            ukupno_zvanja_vi = Integer.parseInt(sGameInfo[5]);
            ukupno_rusili_mi = Integer.parseInt(sGameInfo[6]);
            ukupno_rusili_vi = Integer.parseInt(sGameInfo[7]);
        }

        this.gameInfoInterface = gameInfoInterface;
    }

    public void startRound(String cards) {
        adut = Adut.NISTA;
        gameInfoInterface.onTrumpChange();

        gameState = GameState.TRUMP;

        resetCards();
        updateCards(cards);

        zvanja_mi = zvanja_vi = igra_mi = igra_vi = ukupno_mi = ukupno_vi = 0;

        gameInfoInterface.onPlayerChange();
        gameInfoInterface.onScoreChange();
    }

    public void startPlaying() {
        gameState = GameState.PLAY;

        currentPlayer = startingPlayer;
        gameInfoInterface.onPlayerChange();
    }

    public void next() {
        currentPlayer++;
        currentPlayer %= 4;
        gameInfoInterface.onPlayerChange();
    }

    public void setStartingPlayer(int i) {
        currentPlayer = parseFromServerIndex(i);
        startingPlayer = currentPlayer;

        gameInfoInterface.onPlayerChange();
    }

    public void setStartingPlayer(int i, boolean newRound) {
        currentPlayer = parseFromServerIndex(i);
        startingPlayer = currentPlayer;

        if (newRound)
            lastPlayer = (startingPlayer + 3) % 4;

        gameInfoInterface.onPlayerChange();
    }

    public void updateScore(String zvanj, String bod) {
        String[] score_zvanj = zvanj.split(",");
        String[] score_bod = bod.split(",");

        if (myTeam == 0) {
            zvanja_mi = Integer.parseInt(score_zvanj[0]);
            zvanja_vi = Integer.parseInt(score_zvanj[1]);
            igra_mi = Integer.parseInt(score_bod[0]);
            igra_vi = Integer.parseInt(score_bod[1]);
        } else {
            zvanja_mi = Integer.parseInt(score_zvanj[1]);
            zvanja_vi = Integer.parseInt(score_zvanj[0]);
            igra_mi = Integer.parseInt(score_bod[1]);
            igra_vi = Integer.parseInt(score_bod[0]);
        }

        if (igra_mi < 0) igra_mi = 0;
        if (igra_vi < 0) igra_vi = 0;

        ukupno_mi = zvanja_mi + igra_mi;
        ukupno_vi = zvanja_vi + igra_vi;

        gameInfoInterface.onScoreChange();
    }

    public void chooseTrump(Adut trump) {
        adut = trump;
        adutSelectedByPlayer = players.get(currentPlayer);
        gameInfoInterface.onTrumpChange();

        gameState = GameState.DECLARE;

        currentPlayer = startingPlayer;
        gameInfoInterface.onPlayerChange();
    }

    public void cleanPlayground() {
        playgroundCards = new int[]{-1, -1, -1, -1};
        gameInfoInterface.onCardChange();
    }

    private void resetCards() {
        cards = new int[]{32, 32, 32, 32, 32, 32, 32, 32};
        playgroundCards = new int[]{-1, -1, -1, -1};
    }

    public void updateCards(String s) {
        String[] tmp = s.split("\\|");
        for (int i = 0; i < tmp.length; i++)
            cards[i] = Integer.parseInt(tmp[i]);
        gameInfoInterface.onCardChange();
    }

    public void updateLastRoundCards(int id, String cards, String talon) {
        id = parseFromServerIndex(id);
        String[] cardsP = cards.split("\\|"), talonP = talon.split("\\|");

        lastRoundCards[id].clear();
        for (String card : cardsP)
            lastRoundCards[id].add(card);
        lastRoundCards[id].add("32");
        for (String card : talonP)
            lastRoundCards[id].add(card);
    }

    public void addToScoreHistory(String score) {
        if (myTeam == 0) {
            scoreHistory.add(score);
            return;
        }

        String[] data = score.split(",");
        scoreHistory.add(Constants.stringJoin(",", new String[]{data[1], data[0], data[2], data[3]}));
    }

    public String getLobbyGameInfo() {
        return Constants.stringJoin(",", new String[]{String.valueOf(score_mi), String.valueOf(score_vi), String.valueOf(ukupno_score_mi), String.valueOf(ukupno_score_vi), String.valueOf(ukupno_zvanja_mi), String.valueOf(ukupno_zvanja_vi), String.valueOf(ukupno_rusili_mi), String.valueOf(ukupno_rusili_vi)});
    }

    int parseFromServerIndex(int i) {
        i -= myPosition;
        if (i < 0) i += 4;
        return i;
    }

    int parseToServerIndex(int i) {
        i += myPosition;
        i %= 4;
        return i;
    }
}

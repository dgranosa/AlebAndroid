package com.example.aleb;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.Objects;

public class Room {
    private String name;
    private String type;
    private int goal;
    private boolean showScore;
    private boolean hasPassword;
    private int numOfPlayers;
    private String[] players;

    Room(String name) {
        this.name = name;
    }

    Room(String name, String type, int goal, boolean showScore, boolean hasPassword, int numOfPlayers, String[] players) {
        this.name = name;
        this.type = type;
        this.goal = goal;
        this.showScore = showScore;
        this.hasPassword = hasPassword;
        this.numOfPlayers = numOfPlayers;
        this.players = players.clone();
    }

    public void update(Room r) {
        name = r.getName();
        type = r.getType();
        goal = r.getGoal();
        showScore = r.isShowScore();
        hasPassword = r.isHasPassword();
        numOfPlayers = r.getNumOfPlayers();
        players = r.getPlayers().clone();

    }

    public void update(String s) {
        Room r = Room.parse(s);
        update(r);
    }

    static Room parse(String s) {
        String[] tmp = s.split(",");

        return new Room(tmp[0], tmp[1], Integer.parseInt(tmp[2]), Boolean.parseBoolean(tmp[3]), Boolean.parseBoolean(tmp[4]), Integer.parseInt(tmp[5]), tmp[6].split("\\|"));
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public int getGoal() {
        return goal;
    }

    public boolean isShowScore() {
        return showScore;
    }

    public boolean isHasPassword() {
        return hasPassword;
    }

    public int getNumOfPlayers() {
        return numOfPlayers;
    }

    public String getNumOfPlayersS() {
        return numOfPlayers + "/4";
    }

    public String[] getPlayers() {
        return players;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Room room = (Room) o;
        return name.equals(room.name);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}

package com.example.aleb;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Lobby extends AppCompatActivity implements TCPListener {

    private TCPCommunicator tcpClient;
    private Handler UIHandler;

    private Room room;
    private List<String> players = new ArrayList<>();
    private Boolean[] playerStates = {false, false, false, false};
    private boolean status = false;
    private String selectedPlayer;
    private String lobbyGameInfo;

    private TextView nameView;
    private Button startView;

    private int playerNameIds[] = {R.id.lobby_l_player0, R.id.lobby_l_player1, R.id.lobby_l_player2, R.id.lobby_l_player3};
    private int playerStatusIds[] = {R.id.lobby_i_player0, R.id.lobby_i_player1, R.id.lobby_i_player2, R.id.lobby_i_player3};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        UIHandler = new Handler();
        tcpClient = TCPCommunicator.getInstance();
        tcpClient.addListener(this);

        nameView = findViewById(R.id.l_roomName);
        startView = findViewById(R.id.b_start);

        Bundle bundle = getIntent().getExtras();
        lobbyGameInfo = bundle.getString("gameInfo", null);
        fillLobbyInfo(bundle.getString("roomInfo"), bundle.getString("playerStates"), lobbyGameInfo);

        nameView.setText(room.getName());
        nameView.setSelected(true);
    }

    private void fillLobbyInfo(String roomData, String userStatesData, String gameInfo) {
        room = Room.parse(roomData);

        players.addAll(Arrays.asList(room.getPlayers()));

        String[] tmp = userStatesData.split("\\|");
        for (int i = 0; i < room.getNumOfPlayers(); i++)
            playerStates[i] = Boolean.parseBoolean(tmp[i]);

        if (gameInfo != null) {
            findViewById(R.id.lobby_l_score).setVisibility(View.VISIBLE);
            findViewById(R.id.lobby_separator).setVisibility(View.VISIBLE);
            findViewById(R.id.lobby_lay_info).setVisibility(View.VISIBLE);

            String[] data = gameInfo.split(",");

            ((TextView)findViewById(R.id.lobby_l_score)).setText(data[0] + " : " + data[1]);
            ((TextView)findViewById(R.id.lobby_l_ukupno_mi)).setText(data[2]);
            ((TextView)findViewById(R.id.lobby_l_ukupno_vi)).setText(data[3]);
            ((TextView)findViewById(R.id.lobby_l_zvanja_mi)).setText(data[4]);
            ((TextView)findViewById(R.id.lobby_l_zvanja_vi)).setText(data[5]);
            ((TextView)findViewById(R.id.lobby_l_rusili_mi)).setText(data[6]);
            ((TextView)findViewById(R.id.lobby_l_rusili_vi)).setText(data[7]);

        }

        refreshLobbyInfo();
    }

    private void refreshLobbyInfo() {
        UIHandler.post(new Runnable() {
            @Override
            public void run() {
                int numOfPlayerReady = 0;

                for (int i = 0; i < players.size(); i++) {
                    TextView nameView = findViewById(playerNameIds[i]);
                    ImageView statusView = findViewById(playerStatusIds[i]);

                    nameView.setText(players.get(i));

                    if (playerStates[i])
                        statusView.setImageResource(R.drawable.ic_check_on);
                    else
                        statusView.setImageResource(R.drawable.ic_check_off);

                    numOfPlayerReady += playerStates[i] ? 1 : 0;

                    nameView.setVisibility(View.VISIBLE);
                    statusView.setVisibility(View.VISIBLE);
                }

                for (int i = players.size(); i < 4; i++) {
                    findViewById(playerNameIds[i]).setVisibility(View.INVISIBLE);
                    findViewById(playerStatusIds[i]).setVisibility(View.INVISIBLE);
                }

                if (players.get(0).equals(Constants.USERNAME) && numOfPlayerReady == 4)
                    startView.setEnabled(true);
                else
                    startView.setEnabled(false);
            }
        });
    }

    public void toggleReady(View v) {
        status = !status;
        TCPCommunicator.sendMessage("SetReady;" + (status ? "True" : "False"), UIHandler, this);
    }

    public void startGame(View v) {
        if (players.size() != 4) {
            Toast.makeText(getApplicationContext(), "Not enough players", Toast.LENGTH_LONG).show();
            return;
        }

        if (players.indexOf(Constants.USERNAME) != 0) {
            Toast.makeText(getApplicationContext(), "You are not the host of the room", Toast.LENGTH_LONG).show();
            return;
        }

        if (!playerStates[0] || !playerStates[1] || !playerStates[2] || !playerStates[3]) {
            Toast.makeText(getApplicationContext(), "Not everyone is ready", Toast.LENGTH_LONG).show();
            return;
        }

        TCPCommunicator.sendMessage("StartGame", UIHandler, this);
    }

    public void selectPlayer(View v) {
        if (players.indexOf(Constants.USERNAME) != 0)
            return;

        if (v.getVisibility() != View.VISIBLE)
            return;

        String name = ((TextView)v).getText().toString();

        if (selectedPlayer == null || selectedPlayer.equals(name)) {
            selectedPlayer = name;
            return;
        }

        TCPCommunicator.sendMessage("SwitchUsers;" + name + ";" + selectedPlayer, UIHandler, this);
        selectedPlayer = null;
    }

    public void dismissSelectedPlayer(View v) {
        selectedPlayer = null;
    }

    @Override
    public void onTCPMessageReceived(String message) {
        String[] msg = message.split(";");

        switch (msg[0]) {
            case "GameStarted":
                Intent in = new Intent(Lobby.this, Game.class);
                in.putExtra("meta??", msg[1]);
                in.putExtra("startingCards", msg[2]);
                in.putExtra("playerInfo", Constants.stringJoin(",", players));
                in.putExtra("lobbyGameInfo", lobbyGameInfo);
                finish();
                startActivity(in);
                break;

            case "UserReady":
                playerStates[players.indexOf(msg[1])] = Boolean.parseBoolean(msg[2]);
                refreshLobbyInfo();
                break;
            case "UserJoined":
                players.add(msg[1]);
                refreshLobbyInfo();
                break;
            case "UserLeft":
                int position = players.indexOf(msg[1]);
                players.remove(position);
                for (int i = position; i < players.size(); i++)
                    playerStates[i] = playerStates[i+1];
                playerStates[players.size()] = false;
                refreshLobbyInfo();
                break;
            case "UsersSwitched":
                Collections.swap(players, players.indexOf(msg[1]), players.indexOf(msg[2]));
                playerStates[players.indexOf(msg[1])] = playerStates[players.indexOf(msg[2])] = false;
                refreshLobbyInfo();
                break;
        }
    }

    @Override
    public void onTCPConnectionStatusChanged(boolean isConnectedNow) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        tcpClient.addListener(this);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        TCPCommunicator.sendMessage("LeaveRoom", UIHandler, this);
    }
}
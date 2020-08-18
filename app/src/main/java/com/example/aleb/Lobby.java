package com.example.aleb;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class Lobby extends AppCompatActivity implements TCPListener {

    private TCPCommunicator tcpClient;
    private Handler UIHandler;

    private Room room;
    private List<String> players = new ArrayList<>();
    private Boolean[] playerStates = {false, false, false, false};
    private ArrayAdapter<String> playersAdapter;
    private boolean status = false;

    private TextView nameView;
    private ListView listView;
    private Button startView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        UIHandler = new Handler();
        tcpClient = TCPCommunicator.getInstance();
        tcpClient.addListener(this);

        nameView = findViewById(R.id.l_roomName);
        listView = findViewById(R.id.li_players);
        startView = findViewById(R.id.b_start);

        playersAdapter = new ArrayAdapter<String>(this, R.layout.player_item, players);
        listView.setAdapter(playersAdapter);

        Bundle bundle = getIntent().getExtras();
        fillLobbyInfo(bundle.getString("roomInfo"), bundle.getString("playerStates"));

        nameView.setText(room.getName());
    }

    private void fillLobbyInfo(String roomData, String userStatesData) {
        room = Room.parse(roomData);

        for (int i = 0; i < room.getNumOfPlayers(); i++)
            players.add(room.getPlayers()[i]);

        String[] tmp = userStatesData.split("\\|");
        for (int i = 0; i < room.getNumOfPlayers(); i++)
            playerStates[i] = Boolean.parseBoolean(tmp[i]);

        refreshLobbyInfo();
    }

    private void refreshLobbyInfo() {
        UIHandler.post(new Runnable() {
            @Override
            public void run() {
                playersAdapter.notifyDataSetChanged();

                int numOfPlayerReady = 0;

                for (int i = 0; i < players.size(); i++) {
                    if (playerStates[i])
                        getViewByPosition(i, listView).setBackgroundColor(getResources().getColor(R.color.Ready));
                    else
                        getViewByPosition(i, listView).setBackgroundColor(getResources().getColor(R.color.NotReady));

                    numOfPlayerReady += playerStates[i] ? 1 : 0;
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
        String s = status ? "True" : "False";

        TCPCommunicator.sendMessage("SetReady;" + s, UIHandler, this);
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onTCPMessageReceived(String message) {
        String[] msg = message.split(";");

        switch (msg[0]) {
            case "GameStarted":
                Intent in = new Intent(Lobby.this, Game.class);
                in.putExtra("meta??", msg[1]);
                in.putExtra("startingCards", msg[2]);
                in.putExtra("playerInfo", String.join(",", players));
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
        }
    }

    @Override
    public void onTCPConnectionStatusChanged(boolean isConnectedNow) {

    }

    private View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
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
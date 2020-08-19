package com.example.aleb;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Checkable;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;

public class Rooms extends AppCompatActivity implements TCPListener, RecyclerViewClickInterface {

    private TCPCommunicator tcpClient;
    private Handler UIHandler;

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private MyAdapter myAdapter;
    private List<Room> rooms = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rooms);

        setupRecyclerView();

        UIHandler = new Handler();
        tcpClient = TCPCommunicator.getInstance();
        tcpClient.addListener(this);

        refreshRooms();
    }

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.r_rooms);
        myAdapter = new MyAdapter(rooms, this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(myAdapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        swipeRefreshLayout = findViewById(R.id.r_ref);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshRooms();
            }
        });
    }

    public void refreshRooms() {
        TCPCommunicator.sendMessage("GetRoomList", UIHandler, this);
    }

    public void addRoom(View v) {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
        final View mView = getLayoutInflater().inflate(R.layout.dialog_new_room, null);

        ((EditText)mView.findViewById(R.id.dr_l_name)).requestFocus();
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

        ((EditText)mView.findViewById(R.id.dr_l_pass)).setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                mView.findViewById(R.id.dr_b_create).callOnClick();
                return true;
            }
        });

        mView.findViewById(R.id.dr_b_create).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = ((EditText)mView.findViewById(R.id.dr_l_name)).getText().toString();
                String type = ((RadioButton)mView.findViewById(((RadioGroup)mView.findViewById(R.id.dr_rg_type)).getCheckedRadioButtonId())).getText().toString();
                String goal = ((EditText)mView.findViewById(R.id.dr_l_goal)).getText().toString();
                Boolean score = ((Checkable)mView.findViewById(R.id.dr_c_score)).isChecked();
                String pass = ((EditText)mView.findViewById(R.id.dr_l_pass)).getText().toString();
                Boolean hasPass = pass.length() > 0;

                if (name.length() < 4) {
                    Toast.makeText(getApplicationContext(), "Name is too short", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (Integer.parseInt(goal) < 501 || Integer.parseInt(goal) > 10001) {
                    Toast.makeText(getApplicationContext(), "Game goal needs to be between 501 and 10001", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (hasPass && pass.length() < 6) {
                    Toast.makeText(getApplicationContext(), "Password is too short", Toast.LENGTH_SHORT).show();
                    return;
                }

                TCPCommunicator.sendMessage("CreateRoom;" + name + ";" + type + ";" + goal + ";" + score + ";" + pass, UIHandler, Rooms.this);
                imm.hideSoftInputFromWindow(((EditText)mView.findViewById(R.id.dr_l_pass)).getWindowToken(), 0);
            }
        });

        mBuilder.setView(mView);
        AlertDialog dialog = mBuilder.create();
        dialog.show();
    }

    private void fillRoomList(String s) {
        String[] tmp = s.split(";");
        rooms.clear();
        for (int i = 1; i < tmp.length; i++)
            rooms.add(Room.parse(tmp[i]));

        UIHandler.post(new Runnable() {
            @Override
            public void run() {
                myAdapter.notifyDataSetChanged();
            }
        });
        swipeRefreshLayout.setRefreshing(false);
    }

    public void addRoomList(String s) {
        String[] tmp = s.split(";");
        rooms.add(Room.parse(tmp[1]));
        UIHandler.post(new Runnable() {
            @Override
            public void run() {
                myAdapter.notifyDataSetChanged();
            }
        });
    }

    public void updateRoomList(String s) {
        String[] tmp = s.split(";");
        Room room = Room.parse(tmp[1]);
        final int position = rooms.indexOf(room);
        rooms.get(position).update(room);
        UIHandler.post(new Runnable() {
            @Override
            public void run() {
                myAdapter.notifyItemChanged(position);
            }
        });
    }

    public void removeRoomList(String s) {
        String[] tmp = s.split(";");
        rooms.remove(new Room(tmp[1]));
        UIHandler.post(new Runnable() {
            @Override
            public void run() {
                myAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onTCPMessageReceived(String message) {
        String[] msg = message.split(";");

        switch (msg[0]) {
            case "RoomCreated":
                Intent i = new Intent(Rooms.this, Lobby.class);
                i.putExtra("roomInfo", msg[1]);
                i.putExtra("playerStates", "False");
                startActivity(i);
                break;
            case "RoomFailed":
                UIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Room creation failed", Toast.LENGTH_LONG).show();
                    }
                });
                break;
            case "RoomJoined":
                Intent in = new Intent(Rooms.this, Lobby.class);
                in.putExtra("roomInfo", msg[1]);
                in.putExtra("playerStates", msg[2]);
                startActivity(in);
                break;
            case "RoomJoinFailed":
                UIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Incorrect password", Toast.LENGTH_LONG).show();
                    }
                });
                break;

            case "RoomList":
                fillRoomList(message);
                break;
            case "RoomAdded":
                addRoomList(message);
                break;
            case "RoomUpdated":
                updateRoomList(message);
                break;
            case "RoomDestroyed":
                removeRoomList(message);
                break;
        }
    }

    @Override
    public void onTCPConnectionStatusChanged(boolean isConnectedNow) {

    }

    @Override
    public void onItemClick(int position) {
        final Room room = rooms.get(position);

        if (room.getNumOfPlayers() == 4) {
            Toast.makeText(getApplicationContext(), "Room is full", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!room.isHasPassword()) {
            TCPCommunicator.sendMessage("JoinRoom;" + room.getName() + ";", UIHandler, this);
        } else {
            AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
            View mView = getLayoutInflater().inflate(R.layout.dialog_room_password, null);

            TextView nameView = mView.findViewById(R.id.dp_l_roomName);
            final EditText passView = mView.findViewById(R.id.dp_l_password);
            final Button joinBtn = mView.findViewById(R.id.dp_b_join);

            nameView.setText(room.getName());

            passView.requestFocus();
            final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

            mBuilder.setView(mView);
            final AlertDialog dialog = mBuilder.create();
            dialog.show();

            passView.setOnEditorActionListener(new EditText.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    joinBtn.callOnClick();
                    return true;
                }
            });

            joinBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String pass = passView.getText().toString();
                    if (pass.length() < 6) {
                        Toast.makeText(getApplicationContext(), "Password too short", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    TCPCommunicator.sendMessage("JoinRoom;" + room.getName() + ";" + pass, UIHandler, Rooms.this);
                    imm.hideSoftInputFromWindow(passView.getWindowToken(), 0);
                    dialog.hide();
                }
            });
        }

    }

    @Override
    public void onLongItemClick(int position) {
        Toast.makeText(getApplicationContext(), "Players: " + Constants.stringJoin(", ", rooms.get(position).getPlayers()), Toast.LENGTH_LONG).show();
    }


    @Override
    protected void onResume() {
        super.onResume();
        tcpClient.addListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
package com.example.aleb;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Login extends AppCompatActivity implements TCPListener {

    private TCPCommunicator tcpClient;

    private EditText l_username;
    private EditText l_password;

    private Handler UIHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        l_username = (EditText)findViewById(R.id.l_username);
        l_password = (EditText)findViewById(R.id.l_password);

        l_username.setText(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("username", null));
        l_password.setText(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("password", null));

        tcpClient = TCPCommunicator.getInstance();
        tcpClient.addListener(this);
        UIHandler = new Handler();

        l_password.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                login(v);
                return true;
            }
        });
    }

    public void login(View v) {
        String username = l_username.getText().toString();
        String password = l_password.getText().toString();

        if (username.length() < 4 || password.length() < 8) {
            Toast.makeText(getApplicationContext(), "Login information is incorrect", Toast.LENGTH_LONG).show();
            return;
        }

        TCPCommunicator.sendMessage("Login;" + username + ";" + password, new Handler(), this);
    }

    private void onLogin(String status) {
        UIHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "Login successful", Toast.LENGTH_SHORT).show();
            }
        });

        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("username", l_username.getText().toString()).apply();
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("password", l_password.getText().toString()).apply();

        Constants.USERNAME = l_username.getText().toString();

        Intent i = new Intent(Login.this, Rooms.class);
        startActivity(i);

        if (status.equals("InGame")) {
            Intent in = new Intent(Login.this, Game.class);
            in.putExtra("reconnect", true);
            startActivity(in);
        }

        finish();
    }

    @Override
    public void onTCPMessageReceived(String message) {
        String[] msg = message.split(";");

        switch (msg[0]) {
            case "LoginResult":
                if (!msg[1].equals("Null"))
                    onLogin(msg[1]);
                else
                    Toast.makeText(getApplicationContext(), "Login unsuccessful", Toast.LENGTH_LONG).show();
                break;
        }
    }

    @Override
    public void onTCPConnectionStatusChanged(boolean isConnectedNow) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
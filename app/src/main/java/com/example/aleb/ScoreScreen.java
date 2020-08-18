package com.example.aleb;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class ScoreScreen extends AppCompatActivity {

    TextView status;
    TextView score_mi, score_vi;
    TextView player0, player1, player2, player3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score_screen);

        status = findViewById(R.id.r_l_status);
        score_mi = findViewById(R.id.r_l_score_mi);
        score_vi = findViewById(R.id.r_l_score_vi);
        player0 = findViewById(R.id.r_l_player0);
        player1 = findViewById(R.id.r_l_player1);
        player2 = findViewById(R.id.r_l_player2);
        player3 = findViewById(R.id.r_l_player3);

        Bundle bundle = getIntent().getExtras();
        Room room = Room.parse(bundle.getString("roomInfo"));
        String[] score = bundle.getString("score").split("\\|");
        int team = bundle.getInt("team");

        if (team == 0) {
            int[] result = new int[]{Integer.parseInt(score[0]), Integer.parseInt(score[1])};

            if (result[0] > result[1])
                status.setText("Pobijedili ste");
            else
                status.setText("Izgubili ste");

            score_mi.setText(String.valueOf(result[0]));
            score_vi.setText(String.valueOf(result[1]));

            player0.setText(room.getPlayers()[0]);
            player1.setText(room.getPlayers()[1]);
            player2.setText(room.getPlayers()[2]);
            player3.setText(room.getPlayers()[3]);
        } else {
            int[] result = new int[]{Integer.parseInt(score[1]), Integer.parseInt(score[0])};

            if (result[0] > result[1])
                status.setText("Pobijedili ste");
            else
                status.setText("Izgubili ste");

            score_mi.setText(String.valueOf(result[0]));
            score_vi.setText(String.valueOf(result[1]));

            player0.setText(room.getPlayers()[2]);
            player1.setText(room.getPlayers()[3]);
            player2.setText(room.getPlayers()[0]);
            player3.setText(room.getPlayers()[1]);
        }

    }
}
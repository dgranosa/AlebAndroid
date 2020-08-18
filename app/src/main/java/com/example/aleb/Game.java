package com.example.aleb;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import java.util.Timer;
import java.util.TimerTask;

public class Game extends AppCompatActivity implements TCPListener, GameInfoInterface {

    private TCPCommunicator tcpClient;
    private Handler UIHandler;
    private GameInfo gameInfo;

    private int[] cards = new int[]{R.drawable.card0, R.drawable.card1, R.drawable.card2, R.drawable.card3, R.drawable.card4, R.drawable.card5, R.drawable.card6, R.drawable.card7, R.drawable.card8, R.drawable.card9, R.drawable.card10, R.drawable.card11, R.drawable.card12, R.drawable.card13, R.drawable.card14, R.drawable.card15, R.drawable.card16, R.drawable.card17, R.drawable.card18, R.drawable.card19, R.drawable.card20, R.drawable.card21, R.drawable.card22, R.drawable.card23, R.drawable.card24, R.drawable.card25, R.drawable.card26, R.drawable.card27, R.drawable.card28, R.drawable.card29, R.drawable.card30, R.drawable.card31, R.drawable.card32};
    private int[] cardId = new int[]{R.id.g_card0, R.id.g_card1, R.id.g_card2, R.id.g_card3, R.id.g_card4, R.id.g_card5, R.id.g_card6, R.id.g_card7};
    private int[] playgroundCardId = new int[]{R.id.g_pCard0, R.id.g_pCard1, R.id.g_pCard2, R.id.g_pCard3};
    private int[] statusId = new int[]{R.id.g_status_player0, R.id.g_status_player1, R.id.g_status_player2, R.id.g_status_player3, R.id.g_status};
    private int[] playerId = new int[]{R.id.g_name0, R.id.g_name1, R.id.g_name2, R.id.g_name3};

    private String[] adut = new String[]{"Hearts", "Leaves", "Acorns", "Bells"};
    private int[] adutId = new int[]{R.drawable.hearts, R.drawable.leaves, R.drawable.acorns, R.drawable.bells};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_game);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setTitle("Aleb");

        UIHandler = new Handler();
        tcpClient = TCPCommunicator.getInstance();
        tcpClient.addListener(this);

        startGame();
    }

    private void startGame() {
        Intent it = getIntent();
        Bundle bundle = it.getExtras();

        gameInfo = new GameInfo(bundle.getString("playerInfo"), this);
        gameInfo.startRound(bundle.getString("startingCards"));

        for (int i = 0; i < playerId.length; i++)
            ((TextView)findViewById(playerId[i])).setText(gameInfo.players.get(i));
    }

    private void showStatus(int id, String msg) {
        showStatus(id, msg, 0);
    }

    private void showStatus(final int id, final String msg, final int ms) {
        UIHandler.post(new Runnable() {
            @Override
            public void run() {
                final TextView status = findViewById(statusId[id]);

                status.setText(msg);
                status.setVisibility(View.VISIBLE);

                if (ms == 0)
                    return;

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        status.setVisibility(View.INVISIBLE);
                    }
                }, ms);
            }
        });
    }

    private void hideStatus() {
        UIHandler.post(new Runnable() {
            @Override
            public void run() {
                for (int id : statusId) {
                    findViewById(id).setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void highlightPlayer(int id) {
        for (int i : playerId)
            ((TextView)findViewById(i)).setTextColor(getResources().getColor(R.color.Idle));
        ((TextView)findViewById(playerId[id])).setTextColor(getResources().getColor(R.color.Ready));
    }

    private void showTrump() {
        if (gameInfo.currentPlayer == gameInfo.lastPlayer)
            findViewById(R.id.g_adut_dalje).setVisibility(View.GONE);
        else
            findViewById(R.id.g_adut_dalje).setVisibility(View.VISIBLE);

        ConstraintLayout trumpLayout = findViewById(R.id.g_lay_adut);
        trumpLayout.setVisibility(View.VISIBLE);
    }

    public void pickTrump(View v) {
        if (gameInfo.gameState != GameState.TRUMP_SELECTION) {
            Toast.makeText(getApplicationContext(), "Nije vrijeme odabira aduta", Toast.LENGTH_SHORT).show();
            return;
        }

        if (gameInfo.currentPlayer != 0) {
            Toast.makeText(getApplicationContext(), "Nisi na redu", Toast.LENGTH_SHORT).show();
            return;
        }

        switch (v.getId()) {
            case R.id.g_adut_srce:
                TCPCommunicator.sendMessage("Bid;Hearts", UIHandler, this);
                break;
            case R.id.g_adut_pik:
                TCPCommunicator.sendMessage("Bid;Leaves", UIHandler, this);
                break;
            case R.id.g_adut_tref:
                TCPCommunicator.sendMessage("Bid;Acorns", UIHandler, this);
                break;
            case R.id.g_adut_karo:
                TCPCommunicator.sendMessage("Bid;Bells", UIHandler, this);
                break;
            case R.id.g_adut_dalje:
                TCPCommunicator.sendMessage("Bid;", UIHandler, this);
                break;
        }

        ConstraintLayout trumpLayout = findViewById(R.id.g_lay_adut);
        trumpLayout.setVisibility(View.GONE);
    }

    public void selectCard(View v) {
        if (gameInfo.gameState == GameState.TRUMP_SELECTION)
            return;

        if (gameInfo.gameState == GameState.DECLARE_SELECTION) {
            ImageView img = (ImageView)v;
            if (img.getScaleType() == ImageView.ScaleType.FIT_START)
                img.setScaleType(ImageView.ScaleType.FIT_END);
            else
                img.setScaleType(ImageView.ScaleType.FIT_START);
        }

        if (gameInfo.gameState == GameState.PLAY_TIME) {
            if (gameInfo.currentPlayer != 0) {
                Toast.makeText(getApplicationContext(), "Niste na redu", Toast.LENGTH_SHORT).show();
                return;
            }

            TCPCommunicator.sendMessage("PlayCard;" + getIdPosition(cardId, v.getId()), UIHandler, this);
        }
    }

    private void baciKartu(int pos) {
        if (pos == -1) {
            UIHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "Neispravna karta", Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }

        for (int i = pos + 1; i < gameInfo.cards.length; i++)
            gameInfo.cards[i-1] = gameInfo.cards[i];
        gameInfo.cards[gameInfo.cards.length-1] = -1;
        onCardChange();
    }

    private void bacenaKarta(int karta, boolean bela) {
        gameInfo.playgroundCards[gameInfo.currentPlayer] = karta;
        if (bela) {
            showStatus(gameInfo.currentPlayer, "Bela");
            findViewById(R.id.g_lay_bela).setVisibility(View.GONE);
        }
        onCardChange();
    }

    public void zovi(View v) {
        if (gameInfo.gameState != GameState.DECLARE_SELECTION)
            return;

        if (v.getId() == R.id.g_zvanje_zovi) {
            StringBuilder callCards = new StringBuilder();
            for (int i = 0; i < cardId.length; i++)
                if (((ImageView) findViewById(cardId[i])).getScaleType() == ImageView.ScaleType.FIT_START) {
                    callCards.append(callCards.length() > 0 ? "|" + i : String.valueOf(i));
                }

            TCPCommunicator.sendMessage("Declare;" + callCards.toString(), UIHandler, this);
        } else {
            TCPCommunicator.sendMessage("Declare;null", UIHandler, this);
        }
    }

    public void bela(View v) {
        if (gameInfo.gameState != GameState.PLAY_TIME)
            return;

        TCPCommunicator.sendMessage("Bela;" + (v.getId() == R.id.g_bela_da ? "True" : "False"), UIHandler, this);
        findViewById(R.id.g_lay_bela).setVisibility(View.GONE);
    }

    private void lowerCards() {
        UIHandler.post(new Runnable() {
            @Override
            public void run() {
                for (int id : cardId)
                    ((ImageView)findViewById(id)).setScaleType(ImageView.ScaleType.FIT_END);
            }
        });
    }

    private void showZvanja(int id, int zvanje, String c1, String c2) {
        if (zvanje == 0) {
            showStatus(4, "Nema zvanja");
            return;
        }

        showStatus(4, String.valueOf(zvanje));

        if (id % 2 == gameInfo.myTeam)
            gameInfo.zvanja_mi += zvanje;
        else
            gameInfo.zvanja_vi += zvanje;
        onScoreChange();

        String[] cards1 = c1.split("\\|");
        String[] cards2 = c2.split("\\|");

        popuniZvanja(id, cards1);
        if (c2.length() > 0)
            popuniZvanja((id+2)%4, cards2);
    }

    private void popuniZvanja(final int id, final String[] c) {
        UIHandler.post(new Runnable() {
            @Override
            public void run() {
                ConstraintLayout lay = findViewById(getResources().getIdentifier("g_lay_zvanja_player"+id, "id", getPackageName()));
                for (int i = 0; i < c.length; i++) {
                    ConstraintSet set = new ConstraintSet();
                    ImageView img = new ImageView(Game.this);
                    img.setId(View.generateViewId());
                    img.setImageResource(cards[Integer.parseInt(c[i])]);
                    if (id % 2 == gameInfo.myTeam) {
                        img.setLayoutParams(new ConstraintLayout.LayoutParams((int) (60 * getResources().getDisplayMetrics().density), (int) (90 * getResources().getDisplayMetrics().density)));
                    } else {
                        img.setRotation(90);
                        img.setLayoutParams(new ConstraintLayout.LayoutParams((int) (120 * getResources().getDisplayMetrics().density), (int) (80 * getResources().getDisplayMetrics().density)));
                    }
                    lay.addView(img, 0);
                    set.clone(lay);
                    if (id % 2 == gameInfo.myTeam) {
                        set.connect(img.getId(), ConstraintSet.TOP, lay.getId(), ConstraintSet.TOP, 0);
                        set.connect(img.getId(), ConstraintSet.BOTTOM, lay.getId(), ConstraintSet.BOTTOM, 0);
                        set.connect(img.getId(), ConstraintSet.LEFT, lay.getId(), ConstraintSet.LEFT, i * (int) (30 * getResources().getDisplayMetrics().density));
                    } else {
                        set.connect(img.getId(), ConstraintSet.LEFT, lay.getId(), ConstraintSet.LEFT, 0);
                        set.connect(img.getId(), ConstraintSet.RIGHT, lay.getId(), ConstraintSet.RIGHT, 0);
                        set.connect(img.getId(), ConstraintSet.TOP, lay.getId(), ConstraintSet.TOP, i * (int)(30 * getResources().getDisplayMetrics().density));
                    }
                    set.applyTo(lay);
                    if (c[i].equals("32"))
                        img.setVisibility(View.INVISIBLE);
                }
                lay.setVisibility(View.VISIBLE);
            }
        });
    }

    private void hideZvanja() {
        UIHandler.post(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 4; i++) {
                    ConstraintLayout lay = findViewById(getResources().getIdentifier("g_lay_zvanja_player"+i, "id", getPackageName()));
                    lay.removeAllViews();
                    lay.setVisibility(View.GONE);
                }
            }
        });
    }

    public void showLastRoundCards(View v) {
        int id = getIdPosition(playerId, v.getId());
        if (gameInfo.lastRoundCards[id].size() == 0)
            return;

        popuniZvanja(id, gameInfo.lastRoundCards[id].toArray(new String[0]));
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                hideZvanja();
            }
        }, 2000);
    }

    @Override
    public void onTCPMessageReceived(String message) {
        String[] msg = message.split(";", -1);

        switch (msg[0]) {
            case "GameStarted":
                hideStatus();
                gameInfo.startRound(msg[2]);
                gameInfo.setStartingPlayer((Integer.parseInt(msg[1]) + 1) % 4);
                break;
            case "TrumpNext":
                showStatus(gameInfo.currentPlayer, "Dalje");
                gameInfo.next();
                break;
            case "TrumpChosen":
                hideStatus();
                gameInfo.chooseTrump(Adut.valueOf(getIdPosition(adut, msg[1])));
                break;
            case "FullCards":
                gameInfo.updateCards(msg[1]);
                break;
            case "PlayerDeclared":
                showStatus(gameInfo.currentPlayer, msg[1].equals("0") ? "Dalje" : msg[1]);
                gameInfo.next();
                break;
            case "YouDeclared":
                if (msg[1].equals("True")) {
                    UIHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            findViewById(R.id.g_lay_zvanja).setVisibility(View.GONE);
                        }
                    });
                    gameInfo.gameState = GameState.PLAY_TIME;
                } else {
                    UIHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Nevazece zvanje", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                break;
            case "WinningDeclaration":
                hideStatus();
                lowerCards();
                showZvanja(gameInfo.parseFromServerIndex(Integer.parseInt(msg[1])), Integer.parseInt(msg[2]), msg[3], msg[4]);
                break;
            case "StartPlayingCards":
                hideStatus();
                hideZvanja();
                gameInfo.startPlaying();
                break;
            case "YouPlayed":
                baciKartu(Integer.parseInt(msg[1]));
                break;
            case "CardPlayed":
                bacenaKarta(Integer.parseInt(msg[1]), Boolean.parseBoolean(msg[2]));
                gameInfo.next();
                break;
            case "AskBela":
                UIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.g_lay_bela).setVisibility(View.VISIBLE);
                    }
                });
                break;
            case "TableComplete":
                gameInfo.updateScore(msg[1], msg[2]);
                break;
            case "ContinuePlayingCards":
                hideStatus();
                gameInfo.cleanPlayground();
                gameInfo.setStartingPlayer(Integer.parseInt(msg[1]));
                break;
            case "FinalScores":
                String[] scoreData = msg[1].split(",");
                if (Boolean.parseBoolean(scoreData[3]))
                    showStatus(gameInfo.players.indexOf(gameInfo.adutSelectedByPlayer), "Pad");

                gameInfo.zvanja_mi = gameInfo.zvanja_vi = gameInfo.igra_mi = gameInfo.igra_vi = 0;
                if (gameInfo.myTeam == 0) {
                    gameInfo.ukupno_mi = Integer.parseInt(scoreData[0]);
                    gameInfo.ukupno_vi = Integer.parseInt(scoreData[1]);
                } else {
                    gameInfo.ukupno_mi = Integer.parseInt(scoreData[1]);
                    gameInfo.ukupno_vi = Integer.parseInt(scoreData[0]);
                }
                onScoreChange();
                break;
            case "TotalScore":
                String[] data = msg[1].split(",");
                String[] final_score = msg[2].split("\\|");

                gameInfo.cleanPlayground();

                if (gameInfo.myTeam == 0) {
                    gameInfo.ukupno_mi = Integer.parseInt(data[0]);
                    gameInfo.ukupno_vi = Integer.parseInt(data[1]);
                    gameInfo.partija_mi = Integer.parseInt(final_score[0]);
                    gameInfo.partija_vi = Integer.parseInt(final_score[1]);
                } else {
                    gameInfo.ukupno_mi = Integer.parseInt(data[1]);
                    gameInfo.ukupno_vi = Integer.parseInt(data[0]);
                    gameInfo.partija_mi = Integer.parseInt(final_score[1]);
                    gameInfo.partija_vi = Integer.parseInt(final_score[0]);
                }
                onScoreChange();
                break;
            case "GameFinished":
                Intent in = new Intent(Game.this, Lobby.class);
                in.putExtra("roomInfo", msg[2]);
                in.putExtra("playerStates", "False|False|False|False");
                startActivity(in);

                Intent i = new Intent(Game.this, ScoreScreen.class);
                i.putExtra("score", msg[1]);
                i.putExtra("roomInfo", msg[2]);
                i.putExtra("team", gameInfo.myTeam);
                finish();
                startActivity(i);
                break;
            case "FinalCards":
                gameInfo.updateLastRoundCards(Integer.parseInt(msg[1]), msg[2], msg[3]);
                break;
        }
    }

    @Override
    public void onTCPConnectionStatusChanged(boolean isConnectedNow) {

    }

    @Override
    public void onPlayerChange() {
        UIHandler.post(new Runnable() {
            @Override
            public void run() {
                highlightPlayer(gameInfo.currentPlayer);

                if (gameInfo.currentPlayer == 0) {
                    if (gameInfo.gameState == GameState.TRUMP_SELECTION)
                        showTrump();

                    if (gameInfo.gameState == GameState.DECLARE_SELECTION)
                        findViewById(R.id.g_lay_zvanja).setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void onTrumpChange() {
        UIHandler.post(new Runnable() {
            @Override
            public void run() {
                ImageView adut = findViewById(R.id.g_adut);
                TextView adutPlayer = findViewById(R.id.g_adut_player);

                if (gameInfo.adut.getValue() == -1) {
                    adut.setVisibility(View.INVISIBLE);
                    adutPlayer.setText("");
                } else {
                    adut.setVisibility(View.VISIBLE);
                    adut.setImageResource(adutId[gameInfo.adut.getValue()]);
                    adutPlayer.setText(gameInfo.adutSelectedByPlayer);
                }
            }
        });
    }

    @Override
    public void onCardChange() {
        UIHandler.post(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < gameInfo.cards.length; i++) {
                    ImageView card = findViewById(cardId[i]);

                    if (gameInfo.cards[i] == -1) {
                        card.setVisibility(View.GONE);
                        continue;
                    }

                    card.setVisibility(View.VISIBLE);
                    card.setImageResource(cards[gameInfo.cards[i]]);
                }

                for (int i = 0; i < gameInfo.playgroundCards.length; i++) {
                    ImageView card = findViewById(playgroundCardId[i]);

                    if (gameInfo.playgroundCards[i] == -1) {
                        card.setVisibility(View.INVISIBLE);
                        continue;
                    }

                    card.setVisibility(View.VISIBLE);
                    card.setImageResource(cards[gameInfo.playgroundCards[i]]);
                }
            }
        });
    }

    @Override
    public void onScoreChange() {
        UIHandler.post(new Runnable() {
            @Override
            public void run() {
                ((TextView)findViewById(R.id.g_zvanja_mi)).setText(String.valueOf(gameInfo.zvanja_mi));
                ((TextView)findViewById(R.id.g_zvanja_vi)).setText(String.valueOf(gameInfo.zvanja_vi));
                ((TextView)findViewById(R.id.g_igra_mi)).setText(String.valueOf(gameInfo.igra_mi));
                ((TextView)findViewById(R.id.g_igra_vi)).setText(String.valueOf(gameInfo.igra_vi));
                ((TextView)findViewById(R.id.g_ukupno_mi)).setText(String.valueOf(gameInfo.ukupno_mi));
                ((TextView)findViewById(R.id.g_ukupno_vi)).setText(String.valueOf(gameInfo.ukupno_vi));
                ((TextView)findViewById(R.id.g_partija_mi)).setText(String.valueOf(gameInfo.partija_mi));
                ((TextView)findViewById(R.id.g_partija_vi)).setText(String.valueOf(gameInfo.partija_vi));
            }
        });
    }

    private int getIdPosition(int[] array, int id) {
        for (int i = 0; i < array.length; i++)
            if (array[i] == id)
                return i;
        return -1;
    }

    private int getIdPosition(String[] array, String id) {
        for (int i = 0; i < array.length; i++)
            if (array[i].equals(id))
                return i;
        return -1;
    }
}
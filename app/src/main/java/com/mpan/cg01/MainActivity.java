package com.mpan.cg01;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

/** @noinspection ALL*/
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button[][] buttons = new Button[3][3];
    private boolean player1Turn = true;
    private int roundCount;
    private int player1Points;
    private int player2Points;
    private TextView textViewPlayer1;
    private TextView textViewPlayer2;
    private Dialog pauseDialog;
    private String player1Name;
    private String player2Name;
    private boolean isTwoPlayersMode;
    private boolean isPlayerWinsGameAnimationPlaying = false;
    private boolean isGameWinAnimationPlaying = false;
    private boolean isPlayer1Starting = true;
    private boolean isOriginalPlayer1Starting = true;
    private boolean isEasyMode = false;
    private boolean isHardDifficulty = false;

    private VideoView videoView;


    public void openWebsite(View view) {
        Uri uri = Uri.parse("https://armaan44.is-a.dev/");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    private void resetBoardWithDelay() {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                resetBoard();
            }
        }, 1000);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        videoView = findViewById(R.id.videoView);

        textViewPlayer1 = findViewById(R.id.text_view_p1);
        textViewPlayer2 = findViewById(R.id.text_view_p2);
        textViewPlayer1.setBackgroundColor(Color.parseColor("#FF0000"));
        textViewPlayer2.setBackgroundColor(Color.parseColor("#0000FF"));


        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                String buttonID = "button_" + i + j;
                int resID = getResources().getIdentifier(buttonID, "id", getPackageName());
                buttons[i][j] = findViewById(resID);
                buttons[i][j].setBackgroundColor(Color.parseColor("#FF8C00"));
                buttons[i][j].setOnClickListener(this);
            }
        }

        Intent intent = getIntent();
        player1Name = intent.getStringExtra("player1Name");
        player2Name = intent.getStringExtra("player2Name");
        isTwoPlayersMode = intent.getBooleanExtra("isTwoPlayersMode", true);

        textViewPlayer1.setText(player1Name + ": " + player1Points);
        textViewPlayer2.setText(player2Name + ": " + player2Points);

        Button buttonPause = findViewById(R.id.button_pause);
        buttonPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pauseDialog.show();
            }
        });


        pauseDialog = new Dialog(this);
        pauseDialog.setContentView(R.layout.pause_menu);
        pauseDialog.setCancelable(false);
        pauseDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);



        Button buttonResume = pauseDialog.findViewById(R.id.button_resume);
        Button buttonResetGame = pauseDialog.findViewById(R.id.button_reset);
        Button buttonCredits = pauseDialog.findViewById(R.id.button_credits);


        buttonResume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pauseDialog.dismiss();
            }
        });

        buttonResetGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetGame();
            }
        });

        Button buttonNewGame = pauseDialog.findViewById(R.id.button_new_game);
        buttonNewGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GameOptionsActivity.class);
                startActivity(intent);
                finish();
            }
        });

        buttonCredits.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCredits();
            }
        });
        player1Turn = isPlayer1Starting;


    }

    private void showCredits() {
        Dialog creditsDialog = new Dialog(this);
        creditsDialog.setContentView(R.layout.credits_dialog);
        creditsDialog.setCancelable(true);
        creditsDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        ImageButton logoButton = creditsDialog.findViewById(R.id.logoButton);
        logoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWebsite(v);
            }
        });

        creditsDialog.show();
    }


    @Override
    public void onClick(View v) {
        if (!((Button) v).getText().toString().equals("")) {
            return;
        }

        if (player1Turn) {
            ((Button) v).setText("X");
            ((Button) v).setBackgroundColor(Color.RED);
            textViewPlayer1.setBackgroundColor(Color.parseColor("#FF0000"));
            textViewPlayer2.setBackgroundColor(Color.parseColor("#0000FF"));
        } else if (isTwoPlayersMode) {
            ((Button) v).setText("O");
            ((Button) v).setBackgroundColor(Color.BLUE);
            textViewPlayer1.setBackgroundColor(Color.parseColor("#FF0000"));
            textViewPlayer2.setBackgroundColor(Color.parseColor("#0000FF"));
        } else {
            ((Button) v).setText("X");
            ((Button) v).setBackgroundColor(Color.RED);
            v.setEnabled(false);
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    // AI move
                    if (isHardDifficulty) {
                        computerMoveHard();
                    } else {
                        computerMoveEasy();
                    }
                    v.setEnabled(true);
                }
            }, 1000);
            textViewPlayer1.setBackgroundColor(Color.parseColor("#FF0000"));
            textViewPlayer2.setBackgroundColor(Color.parseColor("#0000FF"));
        }

        roundCount++;

        if (checkForWin()) {
            if (player1Turn) {
                player1Wins();
            } else {
                player2Wins();
            }
        } else if (roundCount == 9) {
            draw();
        } else {
            player1Turn = !player1Turn;

            if (!player1Turn && !isTwoPlayersMode) {
                if (isHardDifficulty) {
                    computerMoveHard();
                } else {
                    computerMoveEasy();
                }
            }
        }
    }

    private boolean checkForWin() {

        String[][] field = new String[3][3];

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                field[i][j] = buttons[i][j].getText().toString();
            }
        }

        for (int i = 0; i < 3; i++) {
            if (field[i][0].equals(field[i][1])
                    && field[i][0].equals(field[i][2])
                    && !field[i][0].equals("")) {
                return true;
            }
        }

        for (int i = 0; i < 3; i++) {
            if (field[0][i].equals(field[1][i])
                    && field[0][i].equals(field[2][i])
                    && !field[0][i].equals("")) {
                return true;
            }
        }

        if (field[0][0].equals(field[1][1])
                && field[0][0].equals(field[2][2])
                && !field[0][0].equals("")) {
            return true;
        }

        if (field[0][2].equals(field[1][1])
                && field[0][2].equals(field[2][0])
                && !field[0][2].equals("")) {
            return true;
        }

        return false;

    }

    private void player1Wins() {
        player1Points++;
        updatePointsText();
        resetBoardWithDelay();
        checkOverallWin();

        if (!isPlayerWinsGameAnimationPlaying && !isGameWinAnimationPlaying) {
            int videoResourceId;

            if (isTwoPlayersMode) {
                // Two-player mode
                videoResourceId = R.raw.roundwin2player1; // Replace with the actual video resource
                Toast.makeText(this, "Player 1 wins the round!", Toast.LENGTH_SHORT).show();
            } else {
                // One-player mode
                videoResourceId = R.raw.roundwin1playerhuman; // Replace with the actual video resource
                Toast.makeText(this, "You win the round!", Toast.LENGTH_SHORT).show();
            }

            playEventVideo(videoResourceId);

            if (player1Points == 4) {
                playerWinsGame(1);
            }
        }
    }


    private void player2Wins() {
        player2Points++;
        updatePointsText();
        resetBoardWithDelay();
        checkOverallWin();

        if (!isPlayerWinsGameAnimationPlaying && !isGameWinAnimationPlaying) {
            if (!isTwoPlayersMode) {
                playEventVideo(R.raw.roundwin1playerbot); // Replace R.raw.bot_wins with the actual video resource
                if (player2Points == 4) {
                    playerWinsGame(2);
                } else {
                    Toast.makeText(this, "Bot wins the round!", Toast.LENGTH_SHORT).show();
                }
            } else {
                playEventVideo(R.raw.roundwin2player2); // Replace R.raw.player2_wins with the actual video resource
                if (player2Points == 4) {
                    playerWinsGame(2);
                } else {
                    Toast.makeText(this, "Player 2 wins the round!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    private void draw() {
        Toast.makeText(this, "Draw!", Toast.LENGTH_SHORT).show();

        // Add video playback for draw
        playEventVideo(R.raw.draw);

        resetBoardWithDelay();
    }




    private void updatePointsText() {
        textViewPlayer1.setText("Player 1: " + player1Points);
        textViewPlayer2.setText("Player 2: " + player2Points);


        Intent intent = getIntent();
        player1Name = intent.getStringExtra("player1Name");
        player2Name = intent.getStringExtra("player2Name");
        boolean isTwoPlayersMode = intent.getBooleanExtra("isTwoPlayersMode", true);

        updatePlayerNamesAndPoints();
    }

    private void updatePlayerNamesAndPoints() {
        textViewPlayer1.setText(player1Name + ": " + player1Points);
        textViewPlayer2.setText(player2Name + ": " + player2Points);

        textViewPlayer1.setBackgroundColor(Color.parseColor("#FF0000"));
        textViewPlayer2.setBackgroundColor(Color.parseColor("#0000FF"));


    }

    private void resetBoard() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                buttons[i][j].setText("");
                buttons[i][j].setBackgroundColor(Color.parseColor("#FF8C00"));
            }
        }

        roundCount = 0;

        player1Turn = isPlayer1Starting;

        updatePlayerNamesAndPoints();
    }


    private void resetGame() {
        player1Points = 0;
        player2Points = 0;
        /*player1Turn=true;*/
        isPlayer1Starting = isOriginalPlayer1Starting;

        updatePointsText();
        resetBoard();
    }


    private void checkOverallWin() {
        if (player1Points >= 4) {
            playerWinsGame(1);
            isPlayerWinsGameAnimationPlaying = false;
        } else if (player2Points >= 4) {
            playerWinsGame(2);
            isPlayerWinsGameAnimationPlaying = false;
        }
    }

    private void playerWinsGame(int player) {
        String winnerMessage;
        int videoResourceId;

        if (!isTwoPlayersMode && player == 2) {
            winnerMessage = "Bot wins the game!";
            videoResourceId = R.raw.gamewin1playerbot; // Replace with the actual video resource
        } else {
            winnerMessage = "Player " + player + " wins the game!";

            if (isTwoPlayersMode) {
                // Two-player mode
                videoResourceId = (player == 1) ? R.raw.gamewin2player1 : R.raw.gamewin2player2;
            } else {
                // One-player mode
                videoResourceId = R.raw.gamewin1playerhuman;
            }
        }

        Toast.makeText(this, winnerMessage, Toast.LENGTH_SHORT).show();
        isPlayerWinsGameAnimationPlaying = true;
        isGameWinAnimationPlaying = true;

        player1Points = 0;
        player2Points = 0;

        updatePointsText();
        resetBoardWithDelay();

        // Play the corresponding event video
        if (videoResourceId != 0) {
            playEventVideo(videoResourceId);
        }
    }



    private void playEventVideo(int videoResId) {
        // Check if videoView is null
        if (videoView == null) {
            // Initialize videoView if null
            videoView = findViewById(R.id.videoView);
            if (videoView == null) {
                // Log an error or handle the situation where videoView cannot be found
                return;
            }
        }

        // Hide buttons
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                buttons[i][j].setVisibility(View.INVISIBLE);
            }
        }

        // Hide TextViews
        textViewPlayer1.setVisibility(View.INVISIBLE);
        textViewPlayer2.setVisibility(View.INVISIBLE);

        // Show VideoView
        videoView.setVisibility(View.VISIBLE);

        // Set video file
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + videoResId);
        videoView.setVideoURI(videoUri);

        // Set completion listener to handle events after video completion
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                // Hide VideoView
                videoView.setVisibility(View.GONE);

                // Show buttons
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        buttons[i][j].setVisibility(View.VISIBLE);
                    }
                }

                // Show TextViews
                textViewPlayer1.setVisibility(View.VISIBLE);
                textViewPlayer2.setVisibility(View.VISIBLE);
            }
        });

        // Start playing the video
        videoView.start();
    }



    @Override
        protected void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);

            outState.putInt("roundCount", roundCount);
            outState.putInt("player1Points", player1Points);
            outState.putInt("player2Points", player2Points);
            outState.putBoolean("player1Turn", player1Turn);
        super.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override

        protected void onRestoreInstanceState(Bundle savedInstanceState) {
            super.onRestoreInstanceState(savedInstanceState);

            roundCount = savedInstanceState.getInt("roundCount");
            player1Points = savedInstanceState.getInt("player1Points");
            player2Points = savedInstanceState.getInt("player2Points");
            player1Turn = savedInstanceState.getBoolean("player1Turn");
        }
    private void computerMoveEasy() {

        if (!player1Turn && !isTwoPlayersMode) {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (buttons[i][j].getText().toString().equals("")) {
                        buttons[i][j].setText("O");
                        if (checkForWin()) {
                            buttons[i][j].setText("");
                            buttons[i][j].setText("O");
                            buttons[i][j].setBackgroundColor(Color.BLUE);
                            roundCount++;
                            player2Wins();
                            return;
                        } else {
                            buttons[i][j].setText("");
                        }

                        buttons[i][j].setText("X");
                        if (checkForWin()) {
                            buttons[i][j].setText("");
                            buttons[i][j].setText("O");
                            buttons[i][j].setBackgroundColor(Color.BLUE);
                            roundCount++;
                            player1Turn = !player1Turn;
                            return;
                        } else {
                            buttons[i][j].setText("");
                        }
                    }
                }
            }

            Random random = new Random();
            int row, col;
            do {
                row = random.nextInt(3);
                col = random.nextInt(3);
            } while (!buttons[row][col].getText().toString().equals(""));

            buttons[row][col].setText("O");
            buttons[row][col].setBackgroundColor(Color.BLUE);

            roundCount++;

            if (checkForWin()) {
                player2Wins();
            } else if (roundCount == 9) {
                draw();
            } else {
                player1Turn = !player1Turn;
            }
        }
    }
    private void computerMoveHard() {
        if (!player1Turn && !isTwoPlayersMode) {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (buttons[i][j].getText().toString().equals("")) {
                        buttons[i][j].setText("O");
                        if (checkForWin()) {
                            buttons[i][j].setText("");
                            buttons[i][j].setText("O");
                            buttons[i][j].setBackgroundColor(Color.BLUE);
                            roundCount++;
                            player2Wins();
                            return;
                        } else {
                            buttons[i][j].setText("");
                        }
                    }
                }
            }

            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (buttons[i][j].getText().toString().equals("")) {
                        buttons[i][j].setText("X");
                        if (checkForWin()) {
                            buttons[i][j].setText("O");
                            buttons[i][j].setBackgroundColor(Color.BLUE);
                            roundCount++;
                            player1Turn = !player1Turn;
                            return;
                        } else {
                            buttons[i][j].setText("");
                        }
                    }
                }
            }

            Random random = new Random();
            int row, col;
            do {
                row = random.nextInt(3);
                col = random.nextInt(3);
            } while (!buttons[row][col].getText().toString().equals(""));

            buttons[row][col].setText("O");
            buttons[row][col].setBackgroundColor(Color.BLUE);

            roundCount++;

            if (checkForWin()) {
                player2Wins();
            } else if (roundCount == 9) {
                draw();
            } else {
                player1Turn = !player1Turn;
            }
        }
    }
}
package com.aa.flyingdonut;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private boolean gameOver = false;
    private RelativeLayout gameLayout;
    private ImageView donut;
    private ImageView one;
    private ImageView two;
    private ImageView three;
    private TextView scoreView;
    private TextView snackTarget;
    private TextView actualSnackTarget;

    private Timer timer;
    private Timer scoreTimer;
    private int score;

    private MediaPlayer mp;
    private MediaPlayer victory;
    private MediaPlayer gameOverSound;
    private MediaPlayer backgroundMusic;

    private int gameDonut;
    private int loserID;
    private int winnerID;

    private int donutPosition = 0;
    private int screenHeight;
    private int backgroundHeight;
    private int displayHeight;

    private int clickCount;

    public static final String PREFS_NAME = "highscoreFile";
    public static final String HIGHSCORE_KEY = "highscore";
    SharedPreferences sp;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.leaderboard:
                Intent intent = new Intent(this, leaderboardActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_main);


        //reference grabbers
        //views
        gameLayout = (RelativeLayout) findViewById(R.id.gameLayout);
        donut = (ImageView) findViewById(R.id.donut);
        one = (ImageView) findViewById(R.id.background1);
        two = (ImageView) findViewById(R.id.background2);
        three = (ImageView) findViewById(R.id.background3);
        scoreView = (TextView) findViewById(R.id.scoreView);
        snackTarget = (TextView) findViewById(R.id.snackTarget);
        actualSnackTarget = (TextView) findViewById(R.id.snackbar);

        //y references
        screenHeight = getResources().getDisplayMetrics().heightPixels;
        backgroundHeight = screenHeight * 3;
        displayHeight = screenHeight / 3;
        donutPosition = screenHeight * 2 / 3;

        //drawable IDS
        loserID = R.drawable.losingdonutremovebg;
        winnerID = R.drawable.finalwinner;
        gameDonut = R.drawable.donut_fitted_one;

        //music
        //clickSound
        mp = MediaPlayer.create(this, R.raw.clicksound);
        mp.setVolume(1.0f, 1.0f);
        //victorysounds
        victory = MediaPlayer.create(this, R.raw.victory);
        victory.setVolume(1.0f, 1.0f);
        //losingsound
        gameOverSound = MediaPlayer.create(this, R.raw.losesound);
        gameOverSound.setVolume(1.0f, 1.0f);
        //backgroundmusic
        backgroundMusic = MediaPlayer.create(this, R.raw.backmusic);
        backgroundMusic.setVolume(1.0f, 1.0f);
        backgroundMusic.setLooping(true);
        backgroundMusic.start();
        //Supperneccesary
        donut.setOnClickListener(this);


    }

    private void resetGame() {
        // Cancel any active timers
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (scoreTimer != null) {
            scoreTimer.cancel();
            scoreTimer = null;
        }

        // Reset score, gameover and click count
        score = 0;
        clickCount = 0;
        gameOver = false;

        // Reset donut position
        donutPosition = screenHeight * 2 / 3;
        donut.setY(donutPosition);
        donut.setClickable(true);

        // Reset background images
        one.setVisibility(View.VISIBLE);
        two.setVisibility(View.INVISIBLE);
        three.setVisibility(View.INVISIBLE);

        // Reset score view
        scoreView.setText("0");
        scoreView.setTextColor(Color.BLACK);
        //reset donut drawable
        setDonutImage(gameDonut);

    }


    @Override
    public void onClick(View view) {
        snackTarget.setBackgroundColor(Color.TRANSPARENT);
        if (snackTarget.getText().toString().length() > 0) {
            snackTarget.setText("");
        }
        backgroundMusic.stop();
        clickCount += 1;
        //START THE SCORE TIMER
        if (clickCount == 1) {
            scoreTimer = new Timer();
            scoreTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    score++;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            scoreView.setText("" + score);
                        }
                    });
                }
            }, 0, 1000);
        }

        //GAME LOGIC (IMPORTANT)
        if (view == donut) {
            mp.start();
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
            //Move the donut up
            donutPosition -= screenHeight / 40;
            donut.setY(donutPosition);
            //Transition to second screen
            if (donutPosition <= 0 && one.getVisibility() == View.VISIBLE) {
                donutPosition = backgroundHeight - displayHeight + 10;
                one.setVisibility(View.INVISIBLE);
                two.setVisibility(View.VISIBLE);
                donutPosition = screenHeight * 2 / 3;
            }
            //transition to third screen
            if (donutPosition <= 0 && two.getVisibility() == View.VISIBLE) {
                donutPosition = backgroundHeight - displayHeight + 10;
                two.setVisibility(View.INVISIBLE);
                three.setVisibility(View.VISIBLE);
                donutPosition = screenHeight * 2 / 3;
                scoreView.setTextColor(Color.WHITE);
            }
            //Win trigger
            if (donutPosition <= 0 && three.getVisibility() == View.VISIBLE) {
                victoryAction();
            }
            //GRAVITY LOGIC(IMPORTANT)
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    // Move the donut down
                    if (!gameOver) {
                        donutPosition += screenHeight / 220;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                donut.setY(donutPosition);
                            }
                        });
                    }

                    // Check if the donut has reached the bottom
                    if (donutPosition >= screenHeight * 2 / 3) {
                        // Trigger game over,  cancel the timer and reset the donut's position
                        if (one.getVisibility() == View.VISIBLE) {
                            gameOverTrigger();
                        }
                        //CANCEL TIMER
                        timer.cancel();
                        timer = null;
                        donutPosition = screenHeight * 2 / 3;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                donut.setY(donutPosition);
                            }
                        });
                    }
                }
            }, 80, 30);
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (scoreTimer != null) {
            scoreTimer.cancel();
            scoreTimer = null;
        }

    }

    //Game over logic
    public void gameOverTrigger() {
        if (backgroundMusic.isPlaying()) {
            backgroundMusic.stop();
        }
        String loseString = String.format("%s", "Game over!");
        Spannable spannable = new SpannableString(loseString);
        spannable.setSpan(new AbsoluteSizeSpan(30, true), 0, loseString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        Snackbar GameOver = Snackbar.make(actualSnackTarget, spannable, Snackbar.LENGTH_INDEFINITE);
        GameOver.setAction("Play Again", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetGame();
                GameOver.dismiss();
            }
        });
        GameOver.setAnchorView(actualSnackTarget);
        ViewGroup.LayoutParams params = actualSnackTarget.getLayoutParams();
        View snackView = GameOver.getView();
        //Attempt to center the snackbars content
        TextView tv = snackView.findViewById(com.google.android.material.R.id.snackbar_text);
        snackView.setLayoutParams(params);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        } else {
            tv.setGravity(Gravity.CENTER_HORIZONTAL);
        }

        GameOver.show();
        setDonutImage(loserID);
        gameOverSound.start();
        donut.setClickable(false);

        if (scoreTimer != null) {
            scoreTimer.cancel();
            scoreTimer = null;


        }
    }

    //victorylogic
    public void victoryAction() {
        victory.start();
        setDonutImage(winnerID);
        scoreTimer.cancel();
        donut.setClickable(false);
        gameOver = true;
        String winString = String.format("%s %d", "Victory! Score: ", score);

        //SET SNACKBAR STYLING
        Spannable spannable = new SpannableString(winString);
        spannable.setSpan(new UnderlineSpan(), 9, 19, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new StyleSpan(Typeface.BOLD), 0, 7, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), 17, 19, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new ForegroundColorSpan(Color.RED), 9, 19, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new AbsoluteSizeSpan(30, true), 0, winString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        Snackbar winMsg = Snackbar.make(actualSnackTarget, spannable, Snackbar.LENGTH_INDEFINITE);
        winMsg.setAction("Play Again", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetGame();
                winMsg.dismiss();
            }
        });
        winMsg.setAnchorView(actualSnackTarget);
        ViewGroup.LayoutParams params = actualSnackTarget.getLayoutParams();
        View snackLayout = winMsg.getView();
        snackLayout.setLayoutParams(params);
        winMsg.show();

    }

    //UI function to reset donut image
    private void setDonutImage(int drawableId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                donut.setImageResource(drawableId);
            }
        });
    }

    @Override
    protected void onPause() {
        if (backgroundMusic.isPlaying()) {
            backgroundMusic.pause();

        }
        super.onPause();
    }

    //continue playing background music
    @Override
    protected void onRestart() {
        super.onRestart();
        if (snackTarget.getText().toString().length() > 0) {
            if (!backgroundMusic.isPlaying()) {
                backgroundMusic.start();
            }
        }
    }

}






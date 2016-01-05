package com.example.marcus.simonsays;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.*;

import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.BaseGameActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatCallback;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.*;

public class MainActivity extends BaseGameActivity implements View.OnTouchListener, AppCompatCallback {

    private List<Integer> sequence;
    private int currentGuess = 0;
    private Handler sequenceHandler;
    private boolean showingFinished;
    private AlertDialog.Builder alertDialogBuilder;
    private AlertDialog alertDialog;
    private AppCompatDelegate delegate;

    private final int SPEED = 500;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Create AppCompat delegate so we can use a toolbar
        delegate = AppCompatDelegate.create(this, this);
        delegate.onCreate(savedInstanceState);
        delegate.setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        delegate.setSupportActionBar(toolbar);


        sequence = new ArrayList<>();
        sequenceHandler = new Handler();
        alertDialogBuilder = new AlertDialog.Builder(this);
        //alertDialogBuilder.setPositiveButton("Okay", DialogInterface.OnClickListener listener);
        alertDialog = alertDialogBuilder.create();

        Button red_button = (Button) findViewById(R.id.red_button);
        Button green_button = (Button) findViewById(R.id.green_button);
        Button blue_button = (Button) findViewById(R.id.blue_button);
        Button yellow_button = (Button) findViewById(R.id.yellow_button);

        red_button.setOnTouchListener(this);
        green_button.setOnTouchListener(this);
        blue_button.setOnTouchListener(this);
        yellow_button.setOnTouchListener(this);

        //Create a sequence
        resetSequence();

    }

    @Override
    public void onSignInFailed() {
        System.out.println("Sign-in Failed!");
    }

    @Override
    public void onSignInSucceeded() {
        System.out.println("Sign-in Success!");

    }

    @Override
    public void onStart() {
        super.onStart();


    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(showingFinished && currentGuess < sequence.size()) {
            //highlight Button on Click
            if (event.getAction() == MotionEvent.ACTION_DOWN) {

                highlightButton(v.getId());

            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                //unhighligt Button on release
                unhighlightButton(v.getId());

                //Check if guess is correct, increment if correct
                if (v.getId() == R.id.red_button) {
                    if(sequence.get(currentGuess) == 0) {
                        correctGuess();
                    }
                    else {
                        //gameover
                        gameOver(sequence.size() - 1);
                    }
                } else if (v.getId() == R.id.green_button) {
                    if(sequence.get(currentGuess) == 1) {
                        correctGuess();
                    }
                    else {
                        //gameover
                        gameOver(sequence.size() - 1);;
                    }

                } else if (v.getId() == R.id.blue_button) {

                    if(sequence.get(currentGuess) == 2) {
                        correctGuess();
                    }
                    else {
                        //gameover
                        gameOver(sequence.size() - 1);
                    }

                } else if (v.getId() == R.id.yellow_button) {

                    if(sequence.get(currentGuess) == 3) {
                        correctGuess();
                    }
                    else {
                        //gameover
                        gameOver(sequence.size() - 1);
                    }

                }

            }

        }


        return true;
    }

    private void correctGuess() {
        currentGuess++;
        if (currentGuess == sequence.size()) {
            //Reset guesses, add one to sequence and show it again
            currentGuess = 0;
            extendSequence(1);

            //Start new sequence after a delay
            sequenceHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    showSequence();
                }
            }, SPEED);


            //show sequence - CHEATING :)
            System.out.println(sequence.toString());
        }
    }

    private void gameOver(int score) {
        alertDialog.setMessage("Game Over.\nScore: " + score);
        alertDialog.show();
        currentGuess = 0;

        resetSequence();
        showingFinished = false;

        //Send Score to Scoreboards and do an achievement
        Games.Leaderboards.submitScore(getApiClient(), getString(R.string.leaderboard_longest_sequence), score);
        //Achievement
        if(score >= 10) {
            Games.Achievements.unlock(getApiClient(), getString(R.string.achievement_sequence_of_10));
        }
    }

    private void extendSequence(int incrementNum) {
        for(int i = 0; i < incrementNum; i++) {
            int next = (int) Math.floor(Math.random() * 4);
            sequence.add(next);
        }
    }

    private void resetSequence() {
        sequence.clear();
        extendSequence(1);
    }

    private void highlightButton(int buttonId) {
        //unhighlight all
        for(int i = 0; i < 4; i++) {
            unhighlightButton(convertToId(i));
        }

        View v = findViewById(buttonId);
        v.getBackground().setColorFilter(new LightingColorFilter(0xff444444, 0x000000));
        Button button = (Button)v;
        button.setText("");
    }

    private void unhighlightButton(int buttonId) {

        View v = findViewById(buttonId);
        v.getBackground().clearColorFilter();
        Button button = (Button)v;
        button.setText("");
    }

    private void showSequence() {
        showingFinished = false;
        showNextSequence(0);
    }

    private void showNextSequence(int iter) {
        if(iter < sequence.size()) {
            highlightButton(convertToId(sequence.get(iter)));
            iter++;
            final int newIter = iter;
            sequenceHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    unhighlightButton(convertToId(sequence.get(newIter - 1)));
                }
            }, SPEED/2);
            sequenceHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    showNextSequence(newIter);
                }
            }, SPEED);
        }
        else {
            showingFinished = true;
        }
    }

    private int convertToId(int num) {
        switch(num) {
            case 0:
                return R.id.red_button;
            case 1:
                return R.id.green_button;
            case 2:
                return R.id.blue_button;
            case 3:
                return R.id.yellow_button;
            default:
                return -1;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_layout, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_begin) {
            showSequence();
            return true;
        }
        else if(id == R.id.action_view_achievements) {
            startActivityForResult(Games.Achievements.getAchievementsIntent(getApiClient()), 1);
            return true;
        }
        else if (id == R.id.action_view_leaderboards) {
            startActivityForResult(Games.Leaderboards.getLeaderboardIntent(getApiClient(), getString(R.string.leaderboard_longest_sequence)), 1);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSupportActionModeStarted(ActionMode mode) {

    }

    @Override
    public void onSupportActionModeFinished(ActionMode mode) {

    }

    @Nullable
    @Override
    public ActionMode onWindowStartingSupportActionMode(ActionMode.Callback callback) {
        return null;
    }
}

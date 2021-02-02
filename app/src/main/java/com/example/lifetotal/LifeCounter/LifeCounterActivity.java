package com.example.lifetotal.LifeCounter;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.FragmentTransaction;

import com.example.lifetotal.Models.PlayerAction;
import com.example.lifetotal.Models.PlayerState;
import com.example.lifetotal.MyApplication;
import com.example.lifetotal.R;
import com.example.lifetotal.Utils.Game;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class LifeCounterActivity extends AppCompatActivity implements LifeCounterFragment.OnClickListener, Game.GameListener {

    private TreeMap<String, LifeCounterFragment> playerFragments = new TreeMap<>();
    private int numUsers = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.numUsers = savedInstanceState.getInt("numUsers");
            this.playerFragments = (TreeMap<String, LifeCounterFragment>)savedInstanceState.getSerializable("playerFragments");
        } else {
            MyApplication.game.setListener(this);

            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                MyApplication.game.setRoomID(bundle.getString("room").toUpperCase());
                MyApplication.game.setRoomPw(bundle.getString("password"));

                MyApplication.game.startGame();

            }
        }

        setContentView(R.layout.life_counter_main_layout);

        if (savedInstanceState != null) {
            setupLayout(null, "");
        }

        //hide top title bar
        if (this.getSupportActionBar() != null) {
            this.getSupportActionBar().hide();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        TextView roomCode = findViewById(R.id.room_id_textview);
        roomCode.setText(String.format(getString(R.string.room_id_text), MyApplication.game.getRoomID()));

        TextView pwCode = findViewById(R.id.room_pw_textview);
        pwCode.setText(String.format(getString(R.string.room_pw_text), MyApplication.game.getRoomPw()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (MyApplication.game.isInProgress()) {
            MyApplication.game.endGame();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("numUsers", this.numUsers);
        savedInstanceState.putSerializable("playerFragments", playerFragments);
    }

    @Override
    public void update(PlayerAction action) {
        Gson gson = new Gson();
        String jsonMessage = gson.toJson(action);

        MyApplication.game.sendMessage(jsonMessage);
    }

    private void createTestData(PlayerState playerState) {
        String[] players = { "test1", "test2", "test3", "test4", "test5", "test6", "test7" };
        for (int i = 0; i < 0; i++) {
            int currentTally = MyApplication.game.getCurrentUserTally();
            LifeCounterFragment playerFragment = new LifeCounterFragment();
            playerFragment.setName(players[i]);
            playerFragment.setIndex(currentTally);
            playerFragment.setPlayerState(playerState);
            setColors(playerFragment, currentTally);

            MyApplication.game.getPlayerTable().put(currentTally, players[i]);
            MyApplication.game.setCurrentUserTally(++currentTally);

            playerFragments.put(players[i], playerFragment);
            numUsers++;
        }
    }

    @Override
    public void onMessageReceived(String s) {

        final String str = s;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Gson gson = new Gson();
                Type objectListType = new TypeToken<Map<String, PlayerState>>(){}.getType();

                PlayerAction action = gson.fromJson(str, PlayerAction.class);
                Map<String, PlayerState> playerStates = gson.fromJson(action.getDetails(), objectListType);

                switch(action.getAction()) {
                    case JOINED:
                        for (String key : playerStates.keySet()) {
                            if (!playerFragments.containsKey(key)) {
                                int currentTally = MyApplication.game.getCurrentUserTally();
                                LifeCounterFragment playerFragment = new LifeCounterFragment();
                                playerFragment.setName(key);
                                playerFragment.setIndex(currentTally);
                                playerFragment.setPlayerState(playerStates.get(key));
                                setColors(playerFragment, currentTally);

                                MyApplication.game.getPlayerTable().put(currentTally, key);
                                MyApplication.game.setCurrentUserTally(++currentTally);

                                playerFragments.put(key, playerFragment);

                                createTestData(playerStates.get(key));

                                setupLayout(true, key);
                            } else {
                                if (!playerFragments.get(key).getVisible()) {
                                    playerFragments.get(key).setVisible(true);
                                    setupLayout(true, key);
                                }
                            }
                        }
                        break;
                    case UPDATE_LIFETOTAL:
                        LifeCounterFragment lcf = playerFragments.get(action.getUsername());

                        if (lcf != null) {
                            lcf.getPlayerState().setLifeTotal(playerStates.get(action.getUsername()).getLifeTotal());
                            lcf.refreshLife();
                        }

                        break;
                    case LEAVE:
                        playerFragments.get(action.getUsername()).setVisible(false);
                        setupLayout(false, action.getUsername());
                        break;
                }
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }


    @Override
    public void onGameStart() {
        Log.i("Websocket", "Opened");
        SharedPreferences sharedPreferences = getSharedPreferences("internalVals", MODE_PRIVATE);

        PlayerAction action = new PlayerAction();
        action.setAction(PlayerAction.PlayerUpdate.LOGIN);

        action.setUsername(sharedPreferences.getString("username", "player1"));
        action.setDetails(MyApplication.game.getRoomPw());

        Gson gson = new Gson();
        String jsonMessage = gson.toJson(action);

        MyApplication.game.sendMessage(jsonMessage);
    }

    public void setupLayout(Boolean joined, String user) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        if (joined != null) {
            if (joined) {
                numUsers++;
            } else {
                numUsers--;
                LifeCounterFragment lcf = playerFragments.get(user);
                if (lcf != null) {
                    ft.remove(lcf);
                    lcf.setVisible(false);
                }
            }
        }

        ArrayList<Integer> placeHolders = new ArrayList<>();
        placeHolders.add(R.id.table_placeholder_1a);
        placeHolders.add(R.id.table_placeholder_2a);
        placeHolders.add(R.id.table_placeholder_3a);
        placeHolders.add(R.id.table_placeholder_4a);
        placeHolders.add(R.id.table_placeholder_1b);
        placeHolders.add(R.id.table_placeholder_2b);
        placeHolders.add(R.id.table_placeholder_3b);
        placeHolders.add(R.id.table_placeholder_4b);

        HashMap<Integer, String> pt = MyApplication.game.getPlayerTable();
        float t1w, t2w, t3w, t4w;

        View t1 = findViewById(placeHolders.get(0));
        View t2 = findViewById(placeHolders.get(1));
        View t3 = findViewById(placeHolders.get(2));
        View t4 = findViewById(placeHolders.get(3));
        View t5 = findViewById(placeHolders.get(4));
        View t6 = findViewById(placeHolders.get(5));
        View t7 = findViewById(placeHolders.get(6));
        View t8 = findViewById(placeHolders.get(7));

        ConstraintLayout row1 = (ConstraintLayout)t1.getParent();
        ConstraintLayout row2 = (ConstraintLayout)t2.getParent();
        ConstraintLayout row3 = (ConstraintLayout)t3.getParent();
        ConstraintLayout row4 = (ConstraintLayout)t4.getParent();

        ConstraintSet set1 = new ConstraintSet();
        ConstraintSet set2 = new ConstraintSet();
        ConstraintSet set3 = new ConstraintSet();
        ConstraintSet set4 = new ConstraintSet();
        set1.clone(row1);
        set2.clone(row2);
        set3.clone(row3);
        set4.clone(row4);

        ft.replace(placeHolders.get(0), playerFragments.get(pt.get(0)));
        set1.constrainPercentWidth(placeHolders.get(0), 1.0f);
        set1.constrainPercentWidth(placeHolders.get(4), 0.0f);
        set2.constrainPercentWidth(placeHolders.get(1), 1.0f);
        set2.constrainPercentWidth(placeHolders.get(5), 0.0f);
        set3.constrainPercentWidth(placeHolders.get(2), 1.0f);
        set3.constrainPercentWidth(placeHolders.get(6), 0.0f);
        set4.constrainPercentWidth(placeHolders.get(3), 1.0f);
        set4.constrainPercentWidth(placeHolders.get(7), 0.0f);

        t1w = 0.5f;
        t2w = 0.5f;
        t3w = 0.0f;
        t4w = 0.0f;

        // set up 2, 4
        if (numUsers == 2) {
            ft.add(placeHolders.get(1), playerFragments.get(pt.get(1)));
        }
        else if (numUsers == 4) {
            ft.add(placeHolders.get(4), playerFragments.get(pt.get(1)));
            ft.add(placeHolders.get(1), playerFragments.get(pt.get(2)));
            ft.add(placeHolders.get(5), playerFragments.get(pt.get(3)));

            set1.constrainPercentWidth(placeHolders.get(0), 0.5f);
            set1.constrainPercentWidth(placeHolders.get(4), 0.5f);
            set2.constrainPercentWidth(placeHolders.get(1), 0.5f);
            set2.constrainPercentWidth(placeHolders.get(5), 0.5f);
        }
        // setup 7, 8
        else if (numUsers > 6) {
            t1w = 0.25f;
            t2w = 0.25f;
            t3w = 0.25f;
            t4w = 0.25f;

            ft.add(placeHolders.get(4), playerFragments.get(pt.get(1)));
            ft.add(placeHolders.get(1), playerFragments.get(pt.get(2)));
            ft.add(placeHolders.get(5), playerFragments.get(pt.get(3)));
            ft.add(placeHolders.get(2), playerFragments.get(pt.get(4)));
            ft.add(placeHolders.get(6), playerFragments.get(pt.get(5)));
            ft.add(placeHolders.get(3), playerFragments.get(pt.get(6)));

            set1.constrainPercentWidth(placeHolders.get(0), 0.5f);
            set1.constrainPercentWidth(placeHolders.get(4), 0.5f);
            set2.constrainPercentWidth(placeHolders.get(1), 0.5f);
            set2.constrainPercentWidth(placeHolders.get(5), 0.5f);
            set3.constrainPercentWidth(placeHolders.get(2), 0.5f);
            set3.constrainPercentWidth(placeHolders.get(6), 0.5f);
            set4.constrainPercentWidth(placeHolders.get(3), 1.0f);
            set4.constrainPercentWidth(placeHolders.get(7), 0.0f);

            if (numUsers == 8) {
                ft.add(placeHolders.get(7), playerFragments.get(pt.get(7)));
                set4.constrainPercentWidth(placeHolders.get(3), 0.5f);
                set4.constrainPercentWidth(placeHolders.get(7), 0.5f);
            }
        }
        else if (numUsers == 1) {
            // do nothing right now TODO: just make the damn switch statment
        }
        // setup 3, 5, 6
        else {
            t1w = 0.33f;
            t2w = 0.33f;
            t3w = 0.33f;
            t4w = 0.0f;

            if (numUsers == 3) {
                ft.add(placeHolders.get(1), playerFragments.get(pt.get(1)));
                ft.add(placeHolders.get(2), playerFragments.get(pt.get(2)));
            } else if (numUsers == 5) {
                set1.constrainPercentWidth(placeHolders.get(0), 0.5f);
                set1.constrainPercentWidth(placeHolders.get(4), 0.5f);
                set2.constrainPercentWidth(placeHolders.get(1), 0.5f);
                set2.constrainPercentWidth(placeHolders.get(5), 0.5f);

                ft.add(placeHolders.get(4), playerFragments.get(pt.get(1)));
                ft.add(placeHolders.get(1), playerFragments.get(pt.get(2)));
                ft.add(placeHolders.get(5), playerFragments.get(pt.get(3)));
                ft.add(placeHolders.get(2), playerFragments.get(pt.get(4)));
            } else {
                ft.add(placeHolders.get(4), playerFragments.get(pt.get(1)));
                ft.add(placeHolders.get(1), playerFragments.get(pt.get(2)));
                ft.add(placeHolders.get(5), playerFragments.get(pt.get(3)));
                ft.add(placeHolders.get(2), playerFragments.get(pt.get(4)));
                ft.add(placeHolders.get(6), playerFragments.get(pt.get(5)));

                set1.constrainPercentWidth(placeHolders.get(0), 0.5f);
                set1.constrainPercentWidth(placeHolders.get(4), 0.5f);
                set2.constrainPercentWidth(placeHolders.get(1), 0.5f);
                set2.constrainPercentWidth(placeHolders.get(5), 0.5f);
                set3.constrainPercentWidth(placeHolders.get(2), 0.5f);
                set3.constrainPercentWidth(placeHolders.get(6), 0.5f);
            }
        }

        row1.setConstraintSet(set1);
        row2.setConstraintSet(set2);
        row3.setConstraintSet(set3);
        row4.setConstraintSet(set4);

        TableRow.LayoutParams param1 = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 0, t1w);
        TableRow.LayoutParams param2 = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 0, t2w);
        TableRow.LayoutParams param3 = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 0, t3w);
        TableRow.LayoutParams param4 = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 0, t4w);

        row1.setLayoutParams(param1);
        row2.setLayoutParams(param2);
        row3.setLayoutParams(param3);
        row4.setLayoutParams(param4);

        ft.commit();
    }

    public void setColors(LifeCounterFragment lcf, int index) {

        if (lcf != null) {
            switch (index) {
                case 0:
                    lcf.setColors("#34376a", "#5B5D83");
                    break;
                case 1:
                    lcf.setColors("#07aee6", "#2fa5cc");
                    break;
                case 2:
                    lcf.setColors("#94006d", "#7b1961");
                    break;
                case 3:
                    lcf.setColors("#145714", "#317131");
                    break;
                case 4:
                    lcf.setColors("#cf5c0b", "#b6672e");
                    break;
                case 5:
                    lcf.setColors("#03ebe3", "#2cd2cc");
                    break;
                case 6:
                    lcf.setColors("#c20808", "#a92929");
                    break;
                case 7:
                    lcf.setColors("#eefa03", "#d7e02f");
                    break;
            }
        }
    }
}

package com.example.lifetotal.LifeCounter;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TableRow;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.lifetotal.Models.PlayerAction;
import com.example.lifetotal.Models.PlayerState;
import com.example.lifetotal.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class LifeCounterActivity extends AppCompatActivity implements LifeCounterFragment.OnClickListener {

    private final int MAX_USERS = 8;
    private int currentUserTally = 0;
    private WebSocketClient mWebSocketClient;

    private ArrayList<Integer> placeHolders = new ArrayList<>();
    private TreeMap<String, LifeCounterFragment> playerFragments = new TreeMap<>();
    private HashMap<Integer, String> playerTable = new HashMap<>();

    private int numUsers = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String room = bundle.getString("room");
            String password = bundle.getString("password");

            connectWebSocket(room, password);

            setContentView(R.layout.life_counter_main_layout);

            placeHolders.add(R.id.table_placeholder_1);
            placeHolders.add(R.id.table_placeholder_2);
            placeHolders.add(R.id.table_placeholder_3);
            placeHolders.add(R.id.table_placeholder_4);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWebSocketClient.close();
    }

    private void connectWebSocket(String room, final String password) {
        URI uri;
        try {
            uri = new URI("ws://192.168.1.199:8076/lifeTotalServer_war_exploded/room/" + room);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        mWebSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.i("Websocket", "Opened");
                SharedPreferences sharedPreferences = getSharedPreferences("internalVals", MODE_PRIVATE);

                PlayerAction action = new PlayerAction();
                action.setAction(PlayerAction.PlayerUpdate.LOGIN);

                action.setUsername(sharedPreferences.getString("username", "player1"));
                action.setDetails(password);

                Gson gson = new Gson();
                String jsonMessage = gson.toJson(action);

                mWebSocketClient.send(jsonMessage);
            }

            @Override
            public void onMessage(String s) {

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
                                        LifeCounterFragment playerFragment = new LifeCounterFragment(key);
                                        playerFragment.setIndex(currentUserTally);
                                        playerTable.put(currentUserTally, key);
                                        currentUserTally++;

                                        playerFragments.put(key, playerFragment);

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
                                    lcf.lifeTotal = playerStates.get(action.getUsername()).getLifeTotal();
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
            public void onClose(int i, String s, boolean b) {
                Log.i("Websocket", "Closed " + s);
            }

            @Override
            public void onError(Exception e) {
                Log.i("Websocket", "Error " + e.getMessage());
            }
        };
        mWebSocketClient.connect();
    }

    @Override
    public void update(PlayerAction action) {
        Gson gson = new Gson();
        String jsonMessage = gson.toJson(action);

        mWebSocketClient.send(jsonMessage);
    }

    public void setupLayout(boolean joined, String user) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        if (joined) {
            numUsers++;
        } else {
            numUsers--;
            ft.remove(playerFragments.get(user));
            playerFragments.get(user).setVisible(false);
        }

        float t1w , t2w, t3w, t4w;

        TableRow t1 = findViewById(placeHolders.get(0));
        TableRow t2 = findViewById(placeHolders.get(1));
        TableRow t3 = findViewById(placeHolders.get(2));
        TableRow t4 = findViewById(placeHolders.get(3));

        ft.replace(placeHolders.get(0), playerFragments.get(playerTable.get(0)));

        if (numUsers < 3 || numUsers == 4) {
            t1w = 0.5f;
            t2w = 0.5f;
            t3w = 0.0f;
            t4w = 0.0f;

            if (numUsers == 2) {
                ft.replace(placeHolders.get(1), playerFragments.get(playerTable.get(1)));
            } else if (numUsers == 4) {
                ft.replace(placeHolders.get(0), playerFragments.get(playerTable.get(1)));
                ft.replace(placeHolders.get(1), playerFragments.get(playerTable.get(2)));
                ft.replace(placeHolders.get(1), playerFragments.get(playerTable.get(4)));
            }
        }
        else if (numUsers > 6) {
            t1w = 0.25f;
            t2w = 0.25f;
            t3w = 0.25f;
            t4w = 0.25f;

            ft.add(placeHolders.get(0), playerFragments.get(playerTable.get(1)));
            ft.add(placeHolders.get(1), playerFragments.get(playerTable.get(2)));
            ft.add(placeHolders.get(1), playerFragments.get(playerTable.get(3)));
            ft.add(placeHolders.get(2), playerFragments.get(playerTable.get(4)));
            ft.add(placeHolders.get(2), playerFragments.get(playerTable.get(5)));
            ft.add(placeHolders.get(3), playerFragments.get(playerTable.get(6)));

            if (numUsers == 8) {
                ft.add(placeHolders.get(3), playerFragments.get(playerTable.get(7)));
            }
        }
        else {
            t1w = 0.33f;
            t2w = 0.33f;
            t3w = 0.33f;
            t4w = 0.0f;

            if (numUsers == 3) {
                ft.add(placeHolders.get(1), playerFragments.get(playerTable.get(1)));
                ft.add(placeHolders.get(2), playerFragments.get(playerTable.get(1)));
            } else if (numUsers == 5) {
                ft.add(placeHolders.get(0), playerFragments.get(playerTable.get(1)));
                ft.add(placeHolders.get(1), playerFragments.get(playerTable.get(2)));
                ft.add(placeHolders.get(1), playerFragments.get(playerTable.get(3)));
                ft.add(placeHolders.get(2), playerFragments.get(playerTable.get(4)));
            } else {
                ft.add(placeHolders.get(2), playerFragments.get(playerTable.get(5)));
            }
        }

        TableRow.LayoutParams param1 = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, 0, t1w);
        TableRow.LayoutParams param2 = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, 0, t2w);
        TableRow.LayoutParams param3 = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, 0, t3w);
        TableRow.LayoutParams param4 = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, 0, t4w);
        t1.setLayoutParams(param1);
        t2.setLayoutParams(param2);
        t3.setLayoutParams(param3);
        t4.setLayoutParams(param4);

        ft.commit();
    }
}

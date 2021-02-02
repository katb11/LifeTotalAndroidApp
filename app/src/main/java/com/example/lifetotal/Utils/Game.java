package com.example.lifetotal.Utils;

import android.util.Log;

import com.example.lifetotal.BuildConfig;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;


public class Game {

    private boolean inProgress;
    private WebSocketClient mWebSocketClient;
    private GameListener listener;

    private int currentUserTally = 0;

    private String roomID;
    private String roomPw;

    private HashMap<Integer, String> playerTable = new HashMap<>();

    public Game() { }

    public synchronized void startGame() {
        if (!inProgress) {
            inProgress = true;
            connectWebSocket(roomID, roomPw);
        }
    }

    public synchronized void endGame() {
        mWebSocketClient.close();
        inProgress = false;
    }

    public void setListener(GameListener listener) {
        this.listener = listener;
    }

    private void connectWebSocket(String room, final String password) {
        URI uri;
        try {
            uri = new URI(BuildConfig.API_ENDPOINT + "/room/" + room);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        mWebSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                listener.onGameStart();
            }

            @Override
            public void onMessage(String s) {
                listener.onMessageReceived(s);
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

    public void sendMessage(String s) {
        mWebSocketClient.send(s);
    }

    // getters and setters

    public boolean isInProgress() {
        return inProgress;
    }

    public void setInProgress(boolean inProgress) {
        this.inProgress = inProgress;
    }

    public String getRoomID() {
        return roomID;
    }

    public void setRoomID(String roomID) {
        this.roomID = roomID;
    }

    public String getRoomPw() {
        return roomPw;
    }

    public void setRoomPw(String roomPw) {
        this.roomPw = roomPw;
    }

    public int getCurrentUserTally() {
        return currentUserTally;
    }

    public void setCurrentUserTally(int currentUserTally) {
        this.currentUserTally = currentUserTally;
    }

    public HashMap<Integer, String> getPlayerTable() {
        return playerTable;
    }

    public void setPlayerTable(HashMap<Integer, String> playerTable) {
        this.playerTable = playerTable;
    }

    public interface GameListener {
        // These methods are the different events and
        // need to pass relevant arguments related to the event triggered
        public void onMessageReceived(String title);
        // or when data has been loaded
        public void onGameStart();
    }
}

package com.example.lifetotal.Models;

public class PlayerAction {

    public enum PlayerUpdate {
        LOGIN,
        JOINED,
        LEAVE,
        UPDATE_LIFETOTAL,
        UPDATE_POISON,
        UPDATE_COMMANDER_DMG
    }

    private String username;
    private PlayerUpdate action;
    private String details;

    public PlayerAction() {}

    public PlayerAction(String username, PlayerUpdate action, String details) {
        this.username = username;
        this.action = action;
        this.details = details;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public PlayerUpdate getAction() {
        return action;
    }

    public void setAction(PlayerUpdate action) {
        this.action = action;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}


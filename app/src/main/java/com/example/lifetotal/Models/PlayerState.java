package com.example.lifetotal.Models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;

public class PlayerState implements Parcelable {

    private Integer lifeTotal;
    private Integer poisonCounters;
    private HashMap<String, Integer> commanderDamage = new HashMap<>();

    public PlayerState() {
        this.lifeTotal = 20;
        this.poisonCounters = 0;
        this.commanderDamage = new HashMap<>();
    }

    protected PlayerState(Parcel in) {
        lifeTotal = in.readInt();
        poisonCounters = in.readInt();
        commanderDamage = (HashMap<String, Integer>)in.readSerializable();
    }

    public static final Creator<PlayerState> CREATOR = new Creator<PlayerState>() {
        @Override
        public PlayerState createFromParcel(Parcel in) {
            return new PlayerState(in);
        }

        @Override
        public PlayerState[] newArray(int size) {
            return new PlayerState[size];
        }
    };

    public Integer getLifeTotal() {
        return lifeTotal;
    }

    public void setLifeTotal(Integer lifeTotal) {
        this.lifeTotal = lifeTotal;
    }

    public int getPoisonCounters() {
        return poisonCounters;
    }

    public void setPoisonCounters(Integer poisonCounters) {
        this.poisonCounters = poisonCounters;
    }

    public HashMap<String, Integer> getCommanderDamage() {
        return commanderDamage;
    }

    public void setCommanderDamage(HashMap<String, Integer> commanderDamage) {
        this.commanderDamage = commanderDamage;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(lifeTotal);
        dest.writeInt(poisonCounters);
        dest.writeMap(commanderDamage);
    }
}


package com.example.lifetotal.Models;

import java.util.HashMap;

public class PlayerState {

    private Integer lifeTotal;
    private Integer poisonCounters;
    private HashMap<String, Integer> commanderDamage;

    public PlayerState() {
        this.lifeTotal = 20;
        this.poisonCounters = 0;
        this.commanderDamage = new HashMap<>();
    }

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
}


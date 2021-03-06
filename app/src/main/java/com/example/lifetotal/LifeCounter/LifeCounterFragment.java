package com.example.lifetotal.LifeCounter;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.lifetotal.Models.PlayerAction;
import com.example.lifetotal.Models.PlayerState;
import com.example.lifetotal.R;


public class LifeCounterFragment extends Fragment implements Comparable<LifeCounterFragment> {

    private TextView lifeTotalTextView;

    private int mainColor;
    private int buttonColor;
    private PlayerState playerState;
    private String name;
    private int index;
    private boolean visible;

    Button incrementButton, decrementButton;
    CardView mainView;

    private LifeCounterFragment.OnClickListener lifeTotalUpdate;

    private View.OnClickListener increment = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            update(name, "INC1");
        }
    };

    private View.OnClickListener decrement = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            update(name, "DEC1");
        }
    };

    void refreshLife() {
        lifeTotalTextView.setText(Integer.toString(playerState.getLifeTotal()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.life_counter_layout, container, false);

        if (savedInstanceState != null) {
            this.name = savedInstanceState.getString("name");
            this.index = savedInstanceState.getInt("index");
            this.visible = savedInstanceState.getBoolean("visible");
            this.playerState = savedInstanceState.getParcelable("playerState");
        }

        if (view != null) {
            visible = true;
            Button incrementButton = view.findViewById(R.id.increment);
            Button decrementButton = view.findViewById(R.id.decrement);
            lifeTotalTextView = view.findViewById(R.id.lifeTotal);
            TextView playerName = view.findViewById(R.id.playerName);

            playerName.setText(name);
            lifeTotalTextView.setText(Integer.toString(playerState.getLifeTotal()));

            incrementButton.setOnClickListener(increment);
            decrementButton.setOnClickListener(decrement);

            mainView = view.findViewById(R.id.main);

            mainView.setBackgroundColor(mainColor);
            incrementButton.setBackgroundColor(buttonColor);
            decrementButton.setBackgroundColor(buttonColor);
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("name", this.name);
        savedInstanceState.putInt("index", this.index);
        savedInstanceState.putBoolean("visible", this.visible);
        savedInstanceState.putParcelable("playerState", this.playerState);
    }

    @Override
    public void onInflate(Context context, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(context, attrs, savedInstanceState);
    }

    // Store the listener (activity) that will have events fired once the fragment is attached
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnClickListener) {
            lifeTotalUpdate = (OnClickListener)context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement LifeCounterFragment.OnClickListener");
        }
    }

    @Override
    public int compareTo(LifeCounterFragment other) {
        return this.getIndex() - other.getIndex();
    }


    // Define the events that the fragment will use to communicate
    public interface OnClickListener {
        // This can be any number of events to be sent to the activity
        void update(PlayerAction action);
    }

    private void update(String player, String type) {
        PlayerAction action = new PlayerAction(player, PlayerAction.PlayerUpdate.UPDATE_LIFETOTAL, type);
        lifeTotalUpdate.update(action);
    }

    public void setColors(String color1, String color2) {
        this.mainColor = Color.parseColor(color1);
        this.buttonColor = Color.parseColor(color2);
    }

    void setVisible(boolean visible) {
        this.visible = visible;
    }

    boolean getVisible() {
        return this.visible;
    }

    void setIndex(int index) {
        this.index = index;
    }

    private int getIndex() {
        return this.index;
    }

    void setPlayerState(PlayerState playerState) {
        this.playerState = playerState;
    }

    PlayerState getPlayerState() {
        return this.playerState;
    }

    String getName() {
        return this.name;
    }

    void setName(String name) {
        this.name = name;
    }
}
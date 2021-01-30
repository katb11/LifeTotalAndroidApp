package com.example.lifetotal.LifeCounter;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lifetotal.R;
import com.example.lifetotal.Utils.APIService;

import org.json.JSONException;
import org.json.JSONObject;


public class MainMenuActivity extends AppCompatActivity {

    public String roomID;
    public String password;

    protected Button hostButton;
    protected Button joinButton;

    private View.OnClickListener join = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            joinRoom();
        }
    };

    private View.OnClickListener host = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            createRoom();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_menu_layout);

        hostButton = findViewById(R.id.host_session);
        joinButton = findViewById(R.id.join_session);

        hostButton.setOnClickListener(host);
        joinButton.setOnClickListener(join);
    }

    private void createRoom() {

        SharedPreferences sharedPreferences = getSharedPreferences("internalVals", MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");

        JSONObject body = new JSONObject();

        try {
            body.put("token", token);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        APIService apiService = new APIService(new APIService.AsyncResponse() {

            @Override
            public void processFinish(String output) {
                if (output != null) {
                    try {
                        JSONObject resp = new JSONObject(output);
                        String roomID = resp.getString("room");
                        String password = resp.getString("password");

                        enterRoom(roomID, password);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        apiService.setMethod("POST");
        apiService.execute("http://192.168.1.199:8076/lifeTotalServer_war_exploded/requestRoom", body.toString());
    }

    private void enterRoom(String roomID, String password) {
        Bundle bundle = new Bundle();
        bundle.putString("room", roomID);
        bundle.putString("password", password);

        Intent i = new Intent(getBaseContext(), LifeCounterActivity.class);
        i.putExtras(bundle);
        startActivity(i);
    }

    private void joinRoom() {
        final Dialog commentDialog = new Dialog(this);
        commentDialog.setContentView(R.layout.room_dialog_layout);

        Button okButton = commentDialog.findViewById(R.id.ok);
        final EditText roomEdit = commentDialog.findViewById(R.id.roomEdit);
        final EditText passwordEdit = commentDialog.findViewById(R.id.passwordEdit);

        okButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //do anything you want here before close the dialog
                roomID = roomEdit.getText().toString();
                password = passwordEdit.getText().toString();
                commentDialog.dismiss();
                enterRoom(roomID, password);
            }
        });
        Button cancelBtn = commentDialog.findViewById(R.id.cancel);
        cancelBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                commentDialog.dismiss();
            }
        });

        commentDialog.show();
    }

}
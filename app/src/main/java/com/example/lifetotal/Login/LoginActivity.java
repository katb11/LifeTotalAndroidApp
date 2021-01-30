package com.example.lifetotal.Login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lifetotal.LifeCounter.MainMenuActivity;
import com.example.lifetotal.R;
import com.example.lifetotal.Utils.APIService;
import com.example.lifetotal.Utils.CryptoUtil;

import org.json.JSONException;
import org.json.JSONObject;


public class LoginActivity extends AppCompatActivity {

    TextView errorTextView;
    EditText usernameText, passwordText;

    String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupView();
    }


    private void setupView() {
        setContentView(R.layout.activity_login);

        usernameText = findViewById(R.id.username);
        passwordText = findViewById(R.id.password);
        errorTextView = findViewById(R.id.error_text_view);

        Button button = findViewById(R.id.login_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                authenticate();
            }
        });
    }


    private void authenticate() {
        boolean isValid = true;
        String pw = (passwordText != null) ? passwordText.getText().toString() : null;
        String user = (usernameText != null) ? usernameText.getText().toString() : null;
        this.email = user;

        if (pw == null || pw.length() < 1) {
            errorTextView.setText("Enter a valid password");
            isValid = false;
        }

        if (!checkUsername(user)) {
            errorTextView.setText("Enter a valid email");
            isValid = false;
        }

        if (isValid) {
            APIService apiService = new APIService(new APIService.AsyncResponse() {

                @Override
                public void processFinish(String output) {

                    switch (output) {
                            case "access denied":
                                errorTextView.setText("Incorrect Username or Password.");
                                break;
                            case "connection failed":
                                errorTextView.setText("Unable to connect to server. Check your internet connection.");
                                break;
                            case "unexpected server error":
                                errorTextView.setText("Server error.");
                                break;
                            default:
                                SharedPreferences sharedPreferences = getSharedPreferences("internalVals", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("token", output);
                                //editor.putLong("accountID", accountID);
                                editor.putString("username", email);
                                editor.commit();

                                Intent i = new Intent(getBaseContext(), MainMenuActivity.class);
                                startActivity(i);
                                break;
                    }

                }
            });
            apiService.setMethod("POST");
            apiService.execute("http://192.168.1.199:8091/AuthenticationServer_war_exploded/authenticate", setParameters(user, pw));
        }
    }

    private boolean checkUsername(String username) {
        return username != null && username.length() >= 1 && username.contains("@") && username.contains(".");
    }

    private String setParameters(String username, String password) {
        JSONObject body = new JSONObject();

        try {
            body.put("email", username);
            body.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return Base64.encodeToString(CryptoUtil.encrypt(body.toString().getBytes()), Base64.NO_WRAP);
    }
}

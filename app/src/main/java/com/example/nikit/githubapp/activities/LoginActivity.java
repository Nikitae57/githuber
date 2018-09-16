package com.example.nikit.githubapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.example.nikit.githubapp.R;
import com.example.nikit.githubapp.enums.REQUEST_METHOD;
import com.example.nikit.githubapp.networkUtil.NetworkUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {

    private EditText etLogin, etPassword;
    private View background, loginFormFog;
    private ProgressBar progressBar;
    private ColorDrawable backgroundColors[];
    private TransitionDrawable colorTransition;

    private String login, password;
    private boolean errorOccured = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etLogin = findViewById(R.id.et_login_or_email);
        etPassword = findViewById(R.id.et_password);
        progressBar = findViewById(R.id.login_progress_bar);
        loginFormFog = findViewById(R.id.login_auth_fog);

        background = findViewById(R.id.login_background);
        backgroundColors = new ColorDrawable[]{
                new ColorDrawable(Color.WHITE),
                new ColorDrawable(Color.parseColor("#ff5252"))
        };
        colorTransition = new TransitionDrawable(backgroundColors);
        background.setBackground(colorTransition);

    }

    public void login(View view) {

        String login = etLogin.getText().toString(),
               password = etPassword.getText().toString();

        if (login.equals("") || password.equals("")) {
            return;
        }

        this.login = login;
        this.password = password;

        URL url = NetworkUtil.makeUserUrl();
        new AuthTask().execute(url);
    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
        loginFormFog.setVisibility(View.VISIBLE);
        if (errorOccured) {
            colorTransition.reverseTransition(500);
        }
    }

    private void showError() {
        progressBar.setVisibility(View.INVISIBLE);
        loginFormFog.setVisibility(View.GONE);
        colorTransition.startTransition(500);
        errorOccured = true;
    }

    class AuthTask extends AsyncTask<URL, Void, String> {

        @Override
        protected void onPreExecute() {
            showProgressBar();
        }

        @Override
        protected String doInBackground(URL... urls) {

            int respondCode = 401;
            String respond = null;
            try {
                respondCode = NetworkUtil.makeAuthRespondCodeRequest(urls[0],
                        login, password, REQUEST_METHOD.GET);

                respond = NetworkUtil.makeAuthRequest(urls[0],
                        login, password, REQUEST_METHOD.GET);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (respondCode != 401) {
                return respond;
            }

            return null;
        }

        @Override
        protected void onPostExecute(String respond) {

            if (respond == null) {
                showError();
                return;
            }

            try {
                JSONObject respondJson = new JSONObject(respond);
                login = respondJson.getString("login");
            } catch (JSONException e) { e.printStackTrace(); }

            Intent intent = new Intent();
            intent.putExtra("login", login);
            intent.putExtra("password", password);
            intent.putExtra("json", respond);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    }
}

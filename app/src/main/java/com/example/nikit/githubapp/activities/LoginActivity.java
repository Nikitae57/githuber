package com.example.nikit.githubapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.nikit.githubapp.R;
import com.example.nikit.githubapp.enums.REQUEST_METHOD;
import com.example.nikit.githubapp.networkUtil.NetworkUtil;

import java.io.IOException;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {

    private EditText etLogin, etPassword;
    private TextView tvWrong;

    private String login, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etLogin = findViewById(R.id.et_login_or_email);
        etPassword = findViewById(R.id.et_password);
        tvWrong = findViewById(R.id.tv_wrong_login_or_password);
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

    class AuthTask extends AsyncTask<URL, Void, String> {

        @Override
        protected void onPreExecute() {
            tvWrong.setVisibility(View.INVISIBLE);
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
                tvWrong.setVisibility(View.VISIBLE);
                return;
            }

            Intent intent = new Intent();
            intent.putExtra("login", login);
            intent.putExtra("password", password);
            intent.putExtra("json", respond);
            setResult(Activity.RESULT_OK, intent);
            finish();

        }
    }
}

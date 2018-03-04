package com.example.nikit.githubapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.nikit.githubapp.networkUtil.NetworkUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class ReadmeActicity extends AppCompatActivity {

    TextView tvReadme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_readme);



        tvReadme = findViewById(R.id.tv_readme);

        URL readmeUrl = null;
        Intent receivedIntent = getIntent();

        if (receivedIntent.hasExtra(Intent.EXTRA_TEXT)) {
            try {
                readmeUrl = new URL(receivedIntent.getStringExtra(Intent.EXTRA_TEXT));
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            }
        }

        new QueryReadmeTask().execute(readmeUrl);
    }

    class QueryReadmeTask extends AsyncTask<URL, Void, String> {

        /*
        @Override
        protected void onPreExecute() {
            showProgressBar();
        }
        */

        @Override
        protected String doInBackground(URL... urls) {

            String readmeRawText = null;
            try {
                String apiRespond = NetworkUtil.makeHTTPRequest(urls[0]);
                JSONObject jsonRespond = new JSONObject(apiRespond);
                String readmeUrlStr = jsonRespond.getString("download_url");

                readmeRawText = NetworkUtil.makeHTTPRequest(new URL(readmeUrlStr));

            } catch (IOException | JSONException ex) {
                ex.printStackTrace();
            }

            return readmeRawText;
        }


        @Override
        protected void onPostExecute(String s) {

            /*
            if (s == null || s.equals("")) {
                showError();
                return;
            }

            showResult(s);
            */

            tvReadme.setText(s);
        }
    }
}

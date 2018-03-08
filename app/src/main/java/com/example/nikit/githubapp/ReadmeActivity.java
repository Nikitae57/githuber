package com.example.nikit.githubapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ShareActionProvider;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.nikit.githubapp.networkUtil.NetworkUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class ReadmeActivity extends AppCompatActivity {

    TextView tvError;
    ProgressBar progressBar;
    ScrollView scrollView;
    com.mukesh.MarkdownView mdView;

    private String repoUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_readme);

        progressBar = findViewById(R.id.pb_ReadmeProgressbar);
        mdView = findViewById(R.id.mdv_readme);
        tvError = findViewById(R.id.tv_ReadmeError);
        scrollView = findViewById(R.id.sv_readmeScrollView);

        URL readmeUrl = null;
        Intent receivedIntent = getIntent();

        try {
            Bundle extras = receivedIntent.getExtras();
            repoUrl = extras.getString("repoUrl");
            readmeUrl = new URL(extras.getString("readmeUrl"));
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }


        new QueryReadmeTask().execute(readmeUrl);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.readme_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.readme_menu_item_share) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, repoUrl);

            if (shareIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(shareIntent);
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class QueryReadmeTask extends AsyncTask<URL, Void, String> {

        @Override
        protected void onPreExecute() {
            showProgressBar();
        }

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

            if (s == null || s.equals("")) {
                showError();
                return;
            }

            showResult();
            mdView.setMarkDownText(s);
        }
    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);

        scrollView.setVisibility(View.INVISIBLE);
        tvError.setVisibility(View.INVISIBLE);
    }

    private void showError() {
        tvError.setVisibility(View.VISIBLE);

        scrollView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
    }

    private void showResult() {
        scrollView.setVisibility(View.VISIBLE);

        tvError.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
    }
}

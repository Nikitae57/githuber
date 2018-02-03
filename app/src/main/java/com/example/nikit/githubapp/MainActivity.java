package com.example.nikit.githubapp;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.nikit.githubapp.networkUtil.NetworkUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private int NUMBER_OF_ITEMS;

    RecyclerView recyclerView;
    EditText searchField;
    ProgressBar progressBar;
    TextView tvQueryUrl;
    TextView tvError;

    JSONArray itemsArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchField = findViewById(R.id.etQuery);
        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.rvListItems);
        tvQueryUrl = findViewById(R.id.tvQueryUrl);
        tvError = findViewById(R.id.tvError);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.btnSearch) {

            String repoToSearch = String.valueOf(searchField.getText());
            URL url = NetworkUtil.makeURL(repoToSearch);

            String displayURL = "URL: " + url.toString();
            tvQueryUrl.setText(displayURL);

            QueryTask queryTask = new QueryTask();
            queryTask.execute(url);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void makeUpData(String s) {
        try {
            JSONObject jsonRespond = new JSONObject(s);
            int numberOfItems = jsonRespond.getJSONArray("items").length();

            if (numberOfItems > 100) {
                NUMBER_OF_ITEMS = 100;
            } else {
                NUMBER_OF_ITEMS = numberOfItems;
            }

            itemsArray = jsonRespond.getJSONArray("items");
            recyclerView.setAdapter(new MyAdapter(NUMBER_OF_ITEMS, itemsArray));
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    class QueryTask extends AsyncTask<URL, Void, String> {

        private void showProgressBar() {
            progressBar.setVisibility(View.VISIBLE);

            recyclerView.setVisibility(View.INVISIBLE);
            tvError.setVisibility(View.INVISIBLE);
        }

        private void showResult() {
            recyclerView.setVisibility(View.VISIBLE);

            progressBar.setVisibility(View.INVISIBLE);
            tvError.setVisibility(View.INVISIBLE);
        }

        private void showError() {
            tvError.setVisibility(View.VISIBLE);

            recyclerView.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
        }

        @Override
        protected void onPreExecute() {
            showProgressBar();
        }

        @Override
        protected String doInBackground(URL... urls) {

            String result = null;
            try {
                result = NetworkUtil.makeHTTPRequest(urls[0]);
            } catch (IOException ioex) {
                ioex.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            if (s != null && !s.equals("")) {
                makeUpData(s);
                showResult();
            } else {
                showError();
            }
        }
    }
}

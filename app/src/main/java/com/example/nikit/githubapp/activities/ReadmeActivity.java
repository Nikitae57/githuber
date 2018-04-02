package com.example.nikit.githubapp.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nikit.githubapp.R;
import com.example.nikit.githubapp.activities.MainActivity;
import com.example.nikit.githubapp.enums.REQUEST_METHOD;
import com.example.nikit.githubapp.networkUtil.NetworkUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class ReadmeActivity extends AppCompatActivity {

    private TextView tvError;
    private ProgressBar progressBar;
    private ScrollView scrollView;
    private com.mukesh.MarkdownView mdView;
    private Menu menu;
    private DrawerLayout drawer;

    private int idShare, idOpenRepo, idStarRepo;
    private Context context;

    private String repoUrl;
    private String repoFullName;
    private URL starRepoUrl;
    private boolean repoIsChecked;
    private int respondCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_readme);

        context = this;

        Toolbar toolbar = findViewById(R.id.toolbar_readme);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);

        drawer = findViewById(R.id.readme_drawer);
        progressBar = findViewById(R.id.pb_ReadmeProgressbar);
        mdView = findViewById(R.id.mdv_readme);
        tvError = findViewById(R.id.tv_ReadmeError);
        scrollView = findViewById(R.id.sv_readmeScrollView);

        repoIsChecked = false;
        URL readmeUrl = null;
        Intent receivedIntent = getIntent();

        try {
            Bundle extras = receivedIntent.getExtras();
            repoUrl = extras.getString("repoUrl");
            readmeUrl = new URL(extras.getString("readmeUrl"));
            repoFullName = extras.getString("repoFullName");
            starRepoUrl = NetworkUtil.makeStarRepoURL(repoFullName);
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }

        new CheckStarRepoTask().execute(starRepoUrl);

        idShare = R.id.readme_menu_item_share;
        idOpenRepo = R.id.readme_menu_item_open_repo;
        idStarRepo = R.id.readme_menu_item_star_repo;

        new QueryReadmeTask().execute(readmeUrl);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.readme_menu, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int idClicked = item.getItemId();

        if (idClicked == idShare) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, repoUrl);

            if (shareIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(shareIntent);
            }
            return true;

        } else if (idClicked == idOpenRepo) {

            Intent openInBrowserIntent = new Intent(Intent.ACTION_VIEW);
            openInBrowserIntent.setData(Uri.parse(repoUrl));

            if (openInBrowserIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(openInBrowserIntent);
            }
            return true;

        } else if (idClicked == idStarRepo) {
            if (!repoIsChecked) {
                new StarRepoTask().execute(starRepoUrl);
            } else {
                new UnstarRepoTask().execute(starRepoUrl);
            }
            return true;

        } else if (idClicked == android.R.id.home) {
            drawer.openDrawer(GravityCompat.START);
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

    class CheckStarRepoTask extends AsyncTask<URL, Void, Boolean> {

        @Override
        protected Boolean doInBackground(URL... urls) {

            try {
                respondCode = NetworkUtil.makeAuthRespondCodeRequest(urls[0],
                        MainActivity.login, MainActivity.password, REQUEST_METHOD.GET);

                if (respondCode == 204) { return true; }

            } catch (IOException ex) {
                ex.printStackTrace();
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean bool) {
            if (bool) {
                menu.getItem(0).setIcon(ContextCompat.
                        getDrawable(context, R.drawable.ic_star_white));
                repoIsChecked = true;
            }
        }
    }

    class UnstarRepoTask extends AsyncTask<URL, Void, Boolean> {

        @Override
        protected Boolean doInBackground(URL... urls) {

            try {
                NetworkUtil.makeAuthRequest(urls[0], MainActivity.login,
                        MainActivity.password, REQUEST_METHOD.DELETE);

                respondCode = NetworkUtil.makeAuthRespondCodeRequest(urls[0],
                        MainActivity.login, MainActivity.password, REQUEST_METHOD.GET);

                if (respondCode == 204) { return true; }

            } catch (IOException ex) {
                ex.printStackTrace();
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean bool) {
            if (!bool) {
                menu.getItem(0).setIcon(ContextCompat.
                        getDrawable(context, R.drawable.ic_unstar));
                repoIsChecked = false;

            } else {

                Toast toast = new Toast(context);
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.setText("Respond code: " + respondCode);
                toast.show();
            }
        }
    }

    class StarRepoTask extends AsyncTask<URL, Void, Boolean> {

        @Override
        protected Boolean doInBackground(URL... urls) {

            try {
                NetworkUtil.makeAuthRequest(urls[0], MainActivity.login,
                        MainActivity.password, REQUEST_METHOD.PUT);

                respondCode = NetworkUtil.makeAuthRespondCodeRequest(urls[0],
                        MainActivity.login, MainActivity.password, REQUEST_METHOD.GET);

                if (respondCode == 204) { return true; }

            } catch (IOException ex) {
                ex.printStackTrace();
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean bool) {
            if (bool) {
                menu.getItem(0).setIcon(ContextCompat.
                        getDrawable(context, R.drawable.ic_star_white));

                repoIsChecked = true;

            } else {
                Toast toast = new Toast(context);
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.setText("Respond code: " + respondCode);
                toast.show();
            }
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

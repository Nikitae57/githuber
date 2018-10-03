package com.example.nikit.githubapp.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nikit.githubapp.R;
import com.example.nikit.githubapp.activities.layout.RepoFilesAdapter;
import com.example.nikit.githubapp.enums.REQUEST_METHOD;
import com.example.nikit.githubapp.networkUtil.NetworkUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class ReadmeActivity extends AppCompatActivity {

    private TextView tvError, tvRepoStars,
            tvRepoViews, tvRepoForks, tvRepoName;
    private ProgressBar progressBar;
    private ScrollView scrollView;
    private com.mukesh.MarkdownView mdView;
    private Menu menu;
    private DrawerLayout drawer;
    private NavigationView navView;
    private RecyclerView rvFiles;
    private LinearLayout llHomeBack;

    private int idShare, idOpenRepo, idStarRepo;
    private Context context;

    private String repoUrl,
            repoFullName,
            repoJsonStr;
    private JSONObject repoJSON;
    private URL starRepoUrl, readmeUrl;
    private boolean repoIsChecked;
    private int respondCode;
    private boolean filesDownloaded;
    private RepoFilesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_readme);

        findViews();
        setDefaultValues();
        setToolbarAndActionBar();
        setListeners();

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        rvFiles.setLayoutManager(layoutManager);

        new QueryReadmeTask().execute(readmeUrl);
    }

    private void setToolbarAndActionBar() {

        Toolbar toolbar = findViewById(R.id.toolbar_readme);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }
    }

    private void findViews() {

        drawer = findViewById(R.id.readme_drawer);
        progressBar = findViewById(R.id.pb_ReadmeProgressbar);
        mdView = findViewById(R.id.mdv_readme);
        tvError = findViewById(R.id.tv_ReadmeError);
        scrollView = findViewById(R.id.sv_readmeScrollView);
        rvFiles = findViewById(R.id.rv_readme);
        llHomeBack = findViewById(R.id.readme_ll_home_back);

        navView = findViewById(R.id.nv_readme_repo_content);
        View headerView = navView.getHeaderView(0);
        tvRepoForks = headerView.findViewById(R.id.tv_ReadmeRepoForks);
        tvRepoName = headerView.findViewById(R.id.tv_ReadmeRepoName);
        tvRepoStars = headerView.findViewById(R.id.tv_ReadmeRepoStars);
        tvRepoViews = headerView.findViewById(R.id.tv_ReadmeRepoViews);

        navView.setCheckedItem(R.id.action_show_repo_readme);
    }

    private void setDefaultValues() {

        context = this;

        repoIsChecked = false;
        filesDownloaded = false;
        readmeUrl = null;
        Intent receivedIntent = getIntent();

        try {
            Bundle extras = receivedIntent.getExtras();
            if (extras == null) { throw new NullPointerException(); }

            repoUrl = extras.getString("repoUrl");
            readmeUrl = new URL(extras.getString("readmeUrl"));
            repoFullName = extras.getString("repoFullName");
            repoJsonStr = extras.getString("repoJSON");
            starRepoUrl = NetworkUtil.makeStarRepoURL(repoFullName);

        } catch (MalformedURLException ex) {
            ex.printStackTrace();

        } catch (NullPointerException npe) {
            npe.printStackTrace();
            finish();
        }

        try {
            repoJSON = new JSONObject(repoJsonStr);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (MainActivity.userIsLoggedIn) {
            new CheckStarRepoTask().execute(starRepoUrl);
        }

        tvRepoName.setText(repoFullName);

        idShare = R.id.readme_menu_item_share;
        idOpenRepo = R.id.readme_menu_item_open_repo;
        idStarRepo = R.id.readme_menu_item_star_repo;
    }

    private void setListeners() {
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                drawer.closeDrawers();

                if (item.isChecked()) {
                    return true;
                }
                item.setCheckable(true);

                switch (item.getItemId()) {
                    case R.id.action_show_repo_readme:
                        showReadme();
                    break;

                    case R.id.action_show_repo_file:
                        if (filesDownloaded) {
                            showFiles();
                        } else {
                            URL masterBranchUrl = NetworkUtil.makeMasterBranchURL(repoFullName);
                            new QueryMasterTreeShaTask().execute(masterBranchUrl);
                            showFiles();
                        }
                    break;
                }

                return true;
            }
        });

        llHomeBack.findViewById(R.id.readme_fl_home).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.homePressed();
                adapter.notifyDataSetChanged();
                llHomeBack.setVisibility(View.GONE);
            }
        });

        llHomeBack.findViewById(R.id.readme_tv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.backPressed();
                adapter.notifyDataSetChanged();
                if (RepoFilesAdapter.browsingRootDir) {
                    llHomeBack.setVisibility(View.GONE);
                }
            }
        });
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

            if (!MainActivity.userIsLoggedIn) {
                Toast toast = Toast.makeText(context,
                        "Необходима авторизация", Toast.LENGTH_SHORT);
                toast.show();
                return true;
            }

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

                URL repoApiUrl = NetworkUtil.makeApiRepoUrl(repoFullName);

                repoJsonStr = NetworkUtil.makeHTTPRequest(repoApiUrl);
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

            showReadme();
            mdView.setMarkDownText(s);

            try {

                repoJSON = new JSONObject(repoJsonStr);

                String nStars = repoJSON.getString("stargazers_count");
                String nForks = repoJSON.getString("forks_count");
                String nViews = repoJSON.getString("subscribers_count");

                tvRepoStars.setText(nStars);
                tvRepoForks.setText(nForks);
                tvRepoViews.setText(nViews);

            } catch (JSONException e) {
                Log.d("TAG", "NO SUBSCR");
                e.printStackTrace();
            }
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

    class QueryFileListTask extends AsyncTask<URL, Void, String> {

        @Override
        protected String doInBackground(URL... urls) {

            String treeStr = null;
            try {
                treeStr = NetworkUtil.makeAuthRequest(urls[0],
                        MainActivity.login, MainActivity.password, REQUEST_METHOD.GET);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Log.d("TREE", treeStr);
            return treeStr;
        }

        @Override
        protected void onPostExecute(String s) {

            JSONArray filesArray = null;
            try {

                JSONObject treeJSON = new JSONObject(s);
                filesArray = treeJSON.getJSONArray("tree");

            } catch (JSONException e) {
                e.printStackTrace();
            }

            JSONArray repoFilesArray = filesArray;
            adapter = new RepoFilesAdapter(repoFilesArray);
            adapter.setFilesClickedListener(new RepoFilesAdapter.FileClickedListener() {
                @Override
                public void fileClicked() {
                    adapter.notifyDataSetChanged();
                    llHomeBack.setVisibility(View.VISIBLE);
                }
            });

            rvFiles.setAdapter(adapter);
            showFiles();
        }
    }

    class QueryMasterTreeShaTask extends AsyncTask<URL, Void, String> {

        @Override
        protected void onPreExecute() {
            showProgressBar();
        }

        @Override
        protected String doInBackground(URL... urls) {

            String respond = null;
            try {
                respond = NetworkUtil.makeAuthRequest(urls[0],
                        MainActivity.login, MainActivity.password, REQUEST_METHOD.GET);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return respond;
        }

        @Override
        protected void onPostExecute(String s) {

            Log.d("SHA", s);
            String treeSha = null;
            try {
                JSONObject masterBranchJSON = new JSONObject(s);

                treeSha = masterBranchJSON.
                        getJSONObject("commit").getJSONObject("commit").
                        getJSONObject("tree").getString("sha");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (treeSha == null || treeSha.equals("null")) { return; }

            URL masterTreeUrl = NetworkUtil.makeMasterTreeUrl(repoFullName, treeSha);
            new QueryFileListTask().execute(masterTreeUrl);
        }
    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);

        scrollView.setVisibility(View.INVISIBLE);
        tvError.setVisibility(View.INVISIBLE);
        rvFiles.setVisibility(View.INVISIBLE);
    }

    private void showError() {
        tvError.setVisibility(View.VISIBLE);

        scrollView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
        rvFiles.setVisibility(View.INVISIBLE);
    }

    private void showReadme() {
        scrollView.setVisibility(View.VISIBLE);

        tvError.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
        rvFiles.setVisibility(View.INVISIBLE);
    }

    private void showFiles() {
        rvFiles.setVisibility(View.VISIBLE);

        scrollView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
        tvError.setVisibility(View.INVISIBLE);
    }
}

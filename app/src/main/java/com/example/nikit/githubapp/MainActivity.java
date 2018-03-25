package com.example.nikit.githubapp;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.nikit.githubapp.networkUtil.NetworkUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private int NUMBER_OF_ITEMS;

    public static Context context;

    private RecyclerView recyclerView;
    private EditText searchField, etSortBylanguage;
    private ProgressBar progressBar;
    private TextView tvError;
    private DrawerLayout drawer;
    private NavigationView navView;
    private View headerView;

    private NetworkUtil.SORT_BY sortBy;
    private String languageSort;

    JSONArray itemsArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();

        sortBy = NetworkUtil.SORT_BY.BEST_MATCH;

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        searchField = findViewById(R.id.etQuery);
        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.rvListItems);
        tvError = findViewById(R.id.tvError);

        drawer = findViewById(R.id.drawer_layout);
        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                if (slideOffset != 0) {
                    hideKeyboard();
                }
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        navView = findViewById(R.id.nv_main);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.isChecked()) {
                    return true;
                }

                item.setCheckable(true);
                drawer.closeDrawers();

                switch (item.getItemId()) {
                    case R.id.action_sort_by_best_match:
                        sortBy = NetworkUtil.SORT_BY.BEST_MATCH;
                    break;

                    case R.id.action_sort_by_stars:
                        sortBy = NetworkUtil.SORT_BY.MOST_STARS;
                    break;

                    case R.id.action_sort_by_forks:
                        sortBy = NetworkUtil.SORT_BY.MOST_FORKS;
                    break;

                    case R.id.action_sort_by_updates:
                        sortBy = NetworkUtil.SORT_BY.RECENTLY_UPDATED;
                    break;
                }

                makeSearchQuery(new View(context));

                return true;
            }
        });

        headerView = navView.getHeaderView(0);
        etSortBylanguage = headerView.findViewById(R.id.et_sort_by_language);

        etSortBylanguage.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                    drawer.closeDrawers();
                    makeSearchQuery(new View(context));
                    return true;
                }
                return false;
            }
        });

        searchField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    makeSearchQuery(new View(context));
                    return true;
                }
                return false;
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_open_sort_drawer:
                drawer.openDrawer(GravityCompat.END);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void makeUpData(String s) {
        try {
            JSONObject jsonRespond = new JSONObject(s);
            int numberOfItems = jsonRespond.getJSONArray("items").length();

            NUMBER_OF_ITEMS = numberOfItems > 100 ? 100 : numberOfItems;

            itemsArray = jsonRespond.getJSONArray("items");
            recyclerView.setAdapter(new MyAdapter(NUMBER_OF_ITEMS, itemsArray, this));
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void makeSearchQuery(View view) {

        String repoToSearch = String.valueOf(searchField.getText());
        if (repoToSearch == null || repoToSearch.equals("")) {
            return;
        }


        URL url = null;
        String languageSort = etSortBylanguage.getText().toString();
        if (languageSort.equals("") || languageSort == null) {
            url = NetworkUtil.makeURL(repoToSearch, sortBy);
            System.out.println(url.toString());
        } else {
            url = NetworkUtil.makeURL(repoToSearch, sortBy, languageSort);
            System.out.println(url.toString());
        }

        QueryTask queryTask = new QueryTask();
        queryTask.execute(url);

        hideKeyboard();
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
                NetworkUtil.makeAuthRequest(new URL("https://api.github.com/user"));
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

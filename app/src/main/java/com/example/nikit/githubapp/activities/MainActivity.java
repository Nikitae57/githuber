package com.example.nikit.githubapp.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.nikit.githubapp.activities.layout.MyAdapter;
import com.example.nikit.githubapp.R;
import com.example.nikit.githubapp.networkUtil.NetworkUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private int NUMBER_OF_ITEMS;

    public static Context context;
    public static String login, password;
    public static JSONObject jsonUser;
    private String jsonStr;

    public static boolean userIsLoggedIn;

    private static final String PREFS_NAME = "preferences";
    private static final String PREF_UNAME = "Username";
    private static final String PREF_PASSWORD = "Password";
    private static final String PREF_USER_JSON = "UserJSON";

    private RecyclerView recyclerView;
    private EditText searchField, etSortBylanguage;
    private ProgressBar progressBar;
    private TextView tvError, tvUserLogin, tvUserMail;
    private DrawerLayout drawer;
    private NavigationView navView, loginNavView;
    private View headerView, loginHeaderView;

    private NetworkUtil.SORT_BY sortBy;

    JSONArray itemsArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();

        sortBy = NetworkUtil.SORT_BY.BEST_MATCH;
        userIsLoggedIn = false;

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);

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

        loginNavView = findViewById(R.id.nv_main_login);
        loginNavView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                drawer.closeDrawers();

                switch (item.getItemId()) {
                    case R.id.action_log_in:
                        Intent loginIntent = new Intent(context, LoginActivity.class);
                        startActivityForResult(loginIntent, 1);
                    break;

                    case R.id.action_log_out:

                        login = null;
                        password = null;
                        userIsLoggedIn = false;
                        jsonUser = null;

                        tvUserLogin.setText(R.string.github_login);
                        tvUserMail.setText(R.string.email);

                        loginNavView.inflateMenu(R.menu.main_activity_logged_out);
                        Menu menu = loginNavView.getMenu();
                        menu.setGroupVisible(R.id.group_log_out, false);
                        menu.setGroupVisible(R.id.group_view_user_favorites, false);

                        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.remove("NOT_LOGIN_DATA");
                        editor.commit();
                }

                return true;
            }
        });

        loginHeaderView = loginNavView.getHeaderView(0);
        tvUserLogin = loginHeaderView.findViewById(R.id.tv_user_login);
        tvUserMail = loginHeaderView.findViewById(R.id.tv_user_mail);

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
    public void onPause() {
        super.onPause();
        savePreferences();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPreferences();
    }

    @Override
    protected void onStop() {
        super.onStop();
        savePreferences();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        savePreferences();
    }

    private void savePreferences() {

        if (!userIsLoggedIn) return;

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        // Save value
        editor.putString(PREF_UNAME, login);
        editor.putString(PREF_PASSWORD, password);
        editor.putString(PREF_USER_JSON, jsonStr);
        editor.commit();
    }

    private void loadPreferences() {

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Get value
        login = settings.getString(PREF_UNAME, null);
        password = settings.getString(PREF_PASSWORD, null);
        jsonStr = settings.getString(PREF_USER_JSON, null);

        if (login != null && password != null && jsonStr != null) {
            userIsLoggedIn = true;
            inflateLoggedInMenu();
        }
    }

    private void inflateLoggedInMenu() {
        loginNavView.getMenu().findItem(R.id.action_log_in).setVisible(false);
        loginNavView.inflateMenu(R.menu.main_activity_logged_in);
        tvUserLogin.setText(login);

        String userMail = null;
        try {
            jsonUser = new JSONObject(jsonStr);
            userMail = jsonUser.getString("email");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (userMail != null && !userMail.equals("null")) {
            tvUserMail.setText(userMail);
        } else {
            tvUserMail.setText("");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {

            userIsLoggedIn = true;

            login = data.getStringExtra("login");
            password = data.getStringExtra("password");
            jsonStr = data.getStringExtra("json");
            savePreferences();

            inflateLoggedInMenu();
        }
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

            case android.R.id.home :
                drawer.openDrawer(GravityCompat.START);
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
            url = NetworkUtil.makeSearchURL(repoToSearch, sortBy);
        } else {
            url = NetworkUtil.makeSearchURL(repoToSearch, sortBy, languageSort);
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

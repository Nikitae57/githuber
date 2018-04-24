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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.nikit.githubapp.activities.layout.MyAdapter;
import com.example.nikit.githubapp.R;
import com.example.nikit.githubapp.enums.REQUEST_METHOD;
import com.example.nikit.githubapp.networkUtil.NetworkUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private int NUMBER_OF_ITEMS;
    private Set<String> languageSet;
    private int checkLanguageSelection = 0;
    private boolean reposAreSorted = false;

    public static Context context;
    public static String login, password;
    public static JSONObject jsonUser;
    private String jsonStr;
    private JSONArray reposJsonArray;

    public static boolean userIsLoggedIn;

    private static final String PREFS_NAME = "preferences";
    private static final String PREF_UNAME = "Username";
    private static final String PREF_PASSWORD = "Password";
    private static final String PREF_USER_JSON = "UserJSON";

    private RecyclerView recyclerView;
    private EditText searchField;
    private Spinner spinnerSortByLang;
    private ProgressBar progressBar;
    private TextView tvError, tvUserLogin,
            tvUserMail, tvSortFoundLangs;
    private DrawerLayout drawer;
    private NavigationView navView, loginNavView;
    private View headerView, loginHeaderView;
    private TextView tvSelectedLang;
    private LinearLayout llSelectedLang;

    private NetworkUtil.SORT_BY sortBy;

    JSONArray itemsArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initStartValues();
        setToolbarAndActionBar();
        findViews();
        setListeners();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
    }

    private void setToolbarAndActionBar() {

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
    }

    private void findViews() {

        searchField = findViewById(R.id.etQuery);
        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.rvListItems);
        tvError = findViewById(R.id.tvError);

        drawer = findViewById(R.id.drawer_layout);
        loginNavView = findViewById(R.id.nv_main_login);
        loginHeaderView = loginNavView.getHeaderView(0);
        tvUserLogin = loginHeaderView.findViewById(R.id.tv_user_login);
        tvUserMail = loginHeaderView.findViewById(R.id.tv_user_mail);

        navView = findViewById(R.id.nv_main);
        headerView = navView.getHeaderView(0);
        tvSelectedLang = headerView.findViewById(R.id.tv_selected_sort_lang);
        llSelectedLang = headerView.findViewById(R.id.llSelectedLang);
        tvSortFoundLangs = headerView.findViewById(R.id.tvSortFoundLangs);
        llSelectedLang.setVisibility(View.GONE);
        spinnerSortByLang = headerView.findViewById(R.id.sp_sort_by_language);
    }

    private void setListeners() {

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
                        break;

                    case R.id.action_view_starred:

                        URL starredUrl = NetworkUtil.makeUserStarredUrl(login);
                        new QueryAuthTask().execute(starredUrl);

                        break;

                    case R.id.action_view_user_repos:

                        URL userReposUrl = NetworkUtil.makeUserReposUrl(login);
                        new QueryAuthTask().execute(userReposUrl);

                        break;
                }
                return true;
            }
        });
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
        spinnerSortByLang.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (checkLanguageSelection++ == 0) return;

                JSONArray sortedJsonArray = new JSONArray();
                try {
                    String selectedLanguage = (String) spinnerSortByLang.getSelectedItem();
                    if (selectedLanguage.equals("") || selectedLanguage == null) { return; }

                    if (spinnerSortByLang.getSelectedItemPosition() == 0) {
                        reposAreSorted = false;
                        makeUpData(reposJsonArray);
                    }

                    for (int i = 0; i < NUMBER_OF_ITEMS; i++) {
                        if (reposJsonArray.getJSONObject(i).
                                getString("language").equals(selectedLanguage)) {

                            sortedJsonArray.put(reposJsonArray.getJSONObject(i));
                        }
                    }

                    reposAreSorted = true;
                    sortRepos(sortedJsonArray, selectedLanguage);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

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

    }

    private void initStartValues() {
        context = getApplicationContext();

        sortBy = NetworkUtil.SORT_BY.BEST_MATCH;
        userIsLoggedIn = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        savePreferences();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!userIsLoggedIn) {
            loadPreferences();
        }
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
            inflateLoggedInMenu();
            userIsLoggedIn = true;
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

        hideKeyboard();

        if (resultCode == RESULT_OK) {

            login = data.getStringExtra("login");
            password = data.getStringExtra("password");
            jsonStr = data.getStringExtra("json");

            savePreferences();
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

    private void makeUpData(JSONArray array) {

        NUMBER_OF_ITEMS = array.length();
        recyclerView.setAdapter(new MyAdapter(NUMBER_OF_ITEMS, array, this));
        tvSortFoundLangs.setVisibility(View.VISIBLE);

        try {
            languageSet = new HashSet<>();
            JSONObject jsonObject;
            for (int i = 0; i < NUMBER_OF_ITEMS; i++) {
                jsonObject = array.getJSONObject(i);
                languageSet.add(jsonObject.getString("language"));
            }

            if (languageSet.contains("null")) {
                languageSet.remove("null");
            }

            String languages[] = new String[languageSet.size() + 1];
            languages[0] = "Все";
            int i = 1;
            for (String str : languageSet) {
                languages[i++] = str;
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, languages);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerSortByLang.setAdapter(adapter);

            spinnerSortByLang.setVisibility(View.VISIBLE);
            checkLanguageSelection = 0;

        } catch (JSONException ex) {
            ex.printStackTrace();
        }

    }

    private void makeUpData(String s) {

        tvSortFoundLangs.setVisibility(View.VISIBLE);
        
        try {

            JSONObject jsonRespond = new JSONObject(s);
            int numberOfItems = jsonRespond.getJSONArray("items").length();
            NUMBER_OF_ITEMS = numberOfItems > 100 ? 100 : numberOfItems;

            itemsArray = jsonRespond.getJSONArray("items");

            if (!reposAreSorted) {
                reposJsonArray = itemsArray;
            }

            recyclerView.setAdapter(new MyAdapter(NUMBER_OF_ITEMS, itemsArray, this));

            try {
                languageSet = new HashSet<>();
                JSONObject jsonObject;
                for (int i = 0; i < NUMBER_OF_ITEMS; i++) {
                    jsonObject = itemsArray.getJSONObject(i);
                    languageSet.add(jsonObject.getString("language"));
                }

                if (languageSet.contains("null")) {
                    languageSet.remove("null");
                }

                String languages[] = new String[languageSet.size() + 1];
                languages[0] = "Все";
                int i = 1;
                for (String str : languageSet) {
                    languages[i++] = str;
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, languages);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerSortByLang.setAdapter(adapter);
                spinnerSortByLang.setVisibility(View.VISIBLE);
                checkLanguageSelection = 0;

            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    private void sortRepos(JSONArray array, String selectedLanguage) {

        spinnerSortByLang.setVisibility(View.GONE);
        tvSelectedLang.setText(selectedLanguage);
        llSelectedLang.setVisibility(View.VISIBLE);

        NUMBER_OF_ITEMS = array.length();
        recyclerView.setAdapter(new MyAdapter(NUMBER_OF_ITEMS, array, this));
    }

    public void UnsortRepos(View view) {

        spinnerSortByLang.setVisibility(View.VISIBLE);
        llSelectedLang.setVisibility(View.GONE);
        makeUpData(reposJsonArray);
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

        URL url = NetworkUtil.makeSearchURL(repoToSearch, sortBy);
        if (userIsLoggedIn) {
            new QueryAuthTask().execute(url);
        } else {
            new QueryTask().execute(url);
        }

        hideKeyboard();
    }

    class QueryAuthTask extends QueryTask {
        @Override
        protected String doInBackground(URL... urls) {
            String result = null;
            try {
                result = NetworkUtil.makeAuthRequest(urls[0],
                        login, password, REQUEST_METHOD.GET);

            } catch (IOException ioex) {
                ioex.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            if (s != null && !s.equals("")) {
                try {

                    JSONArray jsonArray = new JSONArray(s);
                    makeUpData(jsonArray);

                } catch (JSONException e) {
                    makeUpData(s);
                }
                showResult();
            } else {
                showError();
            }
        }
    }

    class QueryTask extends AsyncTask<URL, Void, String> {

        protected void showProgressBar() {
            progressBar.setVisibility(View.VISIBLE);

            recyclerView.setVisibility(View.INVISIBLE);
            tvError.setVisibility(View.INVISIBLE);
        }

        protected void showResult() {
            recyclerView.setVisibility(View.VISIBLE);

            progressBar.setVisibility(View.INVISIBLE);
            tvError.setVisibility(View.INVISIBLE);
        }

        protected void showError() {
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

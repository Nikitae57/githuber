package com.example.nikit.githubapp.networkUtil;

import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import com.example.nikit.githubapp.enums.REQUEST_METHOD;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Arrays;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

public class NetworkUtil {

    final static String GITHUB_API_BASE_URL = "https://api.github.com";
    final static String GITHUB_SEARCH_URL = "https://api.github.com/search/repositories";
    final static String PARAM_QUERY = "q";
    final static String PARAM_SORT = "sort";
    final static String PARAM_LANG = "language";
    private static String sortBy;

    final static String README_BASE_URL = "https://api.github.com/repos";

    public static URL makeUserUrl() {

        URL url = null;
        try {
            url =  new URL(GITHUB_API_BASE_URL + "/user");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    public static URL makeSearchURL(String query, SORT_BY sort_by) {

        Uri uri;
        if (sort_by == SORT_BY.BEST_MATCH) {
            uri = Uri.parse(GITHUB_SEARCH_URL).buildUpon().
                    appendQueryParameter(PARAM_QUERY, query).build();
        } else {
            switch (sort_by) {
                case MOST_STARS: sortBy = "stars";
                break;

                case MOST_FORKS: sortBy = "forks";
                break;

                case RECENTLY_UPDATED: sortBy = "updated";
                break;
            }

            uri = Uri.parse(GITHUB_SEARCH_URL).buildUpon().
                    appendQueryParameter(PARAM_QUERY, query).
                    appendQueryParameter(PARAM_SORT, sortBy).build();
        }

        URL url = null;
        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }

        return url;
    }

    public static URL makeSearchURL(String query, SORT_BY sort_by, String language) {
        Uri uri;
        if (sort_by == SORT_BY.BEST_MATCH) {
            uri = Uri.parse(GITHUB_SEARCH_URL).buildUpon().
                    appendQueryParameter(PARAM_QUERY, query +
                    "+" + PARAM_LANG + ":" + language).build();
        } else {
            switch (sort_by) {
                case MOST_STARS: sortBy = "stars";
                    break;

                case MOST_FORKS: sortBy = "forks";
                    break;

                case RECENTLY_UPDATED: sortBy = "updated";
                    break;
            }

            uri = Uri.parse(GITHUB_SEARCH_URL).buildUpon().
                    appendQueryParameter(PARAM_QUERY, query +
                    "+" + PARAM_LANG + ":" + language).
                    appendQueryParameter(PARAM_SORT, sortBy).
                    appendQueryParameter(PARAM_LANG, language).build();
        }

        URL url = null;
        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }

        return url;
    }

    public static URL makeStarRepoURL(String repoFullName) {

        URL url = null;
        try {
            url = new URL(GITHUB_API_BASE_URL + "/user/starred/" + repoFullName);
            String u = url.toString();
            Log.d("url", u);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    public static String makeAuthRequest(URL url, String login,
            String password, REQUEST_METHOD request_method) throws IOException {

        String ur = url.toString();
        Log.d("URL", ur);

        String respond = null;
        HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
        try {
            httpsURLConnection.setRequestProperty("X-Requested-With", "Curl");

            switch (request_method) {
                case GET:
                    httpsURLConnection.setRequestMethod("GET");
                break;

                case POST:
                    httpsURLConnection.setRequestMethod("POST");
                break;

                case PATCH:
                    httpsURLConnection.setRequestMethod("PATCH");
                break;

                case PUT:
                    httpsURLConnection.setRequestMethod("PUT");
                    break;

                case DELETE:
                    httpsURLConnection.setRequestMethod("DELETE");
                    break;

                default: break;
            }

            String userpass = login + ":" + password;
            String basicAuth = "Basic " + Base64.encodeToString(userpass.getBytes(), Base64.NO_WRAP);
            httpsURLConnection.setRequestProperty("Authorization", basicAuth);

            int respondCode = httpsURLConnection.getResponseCode();
            Log.d("CODE", String.valueOf(respondCode));

            InputStreamReader inputStreamReader = new InputStreamReader(httpsURLConnection.getInputStream());
            Scanner sc = new Scanner(inputStreamReader);
            sc.useDelimiter("\\A");

            if (sc.hasNext()) {
                respond = sc.next();
            }

        } finally {
            if (httpsURLConnection != null) {
                httpsURLConnection.disconnect();
            }
        }

        return respond;
    }

    public static int makeAuthRespondCodeRequest(URL url, String login,
            String password, REQUEST_METHOD request_method) throws IOException {

        int respondCode = 404;
        HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
        try {
            httpsURLConnection.setRequestProperty("X-Requested-With", "Curl");

            switch (request_method) {
                case GET:
                    httpsURLConnection.setRequestMethod("GET");
                    break;

                case POST:
                    httpsURLConnection.setRequestMethod("POST");
                    break;

                case PATCH:
                    httpsURLConnection.setRequestMethod("PATCH");
                    break;

                case PUT:
                    httpsURLConnection.setRequestMethod("PUT");
                    break;

                case DELETE:
                    httpsURLConnection.setRequestMethod("DELETE");
                    break;

                default: break;
            }

            String userpass = login + ":" + password;
            String basicAuth = "Basic " + Base64.encodeToString(userpass.getBytes(), Base64.NO_WRAP);
            httpsURLConnection.setRequestProperty("Authorization", basicAuth);

            respondCode = httpsURLConnection.getResponseCode();

        } catch (IOException ex) {
            Log.d("STACK", ex.toString());
        } finally {
            if (httpsURLConnection != null) {
                httpsURLConnection.disconnect();
            }
        }

        return respondCode;
    }

    public static String makeHTTPRequest(URL url) throws IOException {
        HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
        httpConnection.setConnectTimeout(5000);

        String result = null;
        try {
            InputStream is = httpConnection.getInputStream();
            Scanner sc = new Scanner(is);
            sc.useDelimiter("\\A");

            if (sc.hasNext()) {
                result =  sc.next();
            }
        } catch (SocketTimeoutException timeEx) {
            return null;
        } finally {
            httpConnection.disconnect();
        }

        return result;
    }

    public static URL makeReadmeUrl(String fullName) {

        if (fullName == null || fullName.equals("")) {
            throw new NullPointerException();
        }

        URL url = null;
        try {
            StringBuilder urlStr = new StringBuilder(README_BASE_URL);
            urlStr.append("/").append(fullName).append("/readme");
            url = new URL(urlStr.toString());

        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }

        return url;
    }

    public enum SORT_BY {
        BEST_MATCH, MOST_STARS, MOST_FORKS, RECENTLY_UPDATED
    }
}

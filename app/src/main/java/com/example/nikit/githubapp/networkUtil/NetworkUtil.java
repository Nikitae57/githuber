package com.example.nikit.githubapp.networkUtil;

import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Scanner;

public class NetworkUtil {

    final static String GITHUB_BASE_URL = "https://api.github.com/search/repositories";
    final static String PARAM_QUERY = "q";
    final static String PARAM_SORT = "sort";
    final static String PARAM_LANG = "language";
    private static String sortBy;

    final static String README_BASE_URL = "https://api.github.com/repos";

    public static URL makeURL(String query, SORT_BY sort_by) {

        Uri uri;
        if (sort_by == SORT_BY.BEST_MATCH) {
            uri = Uri.parse(GITHUB_BASE_URL).buildUpon().
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

            uri = Uri.parse(GITHUB_BASE_URL).buildUpon().
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

    public static URL makeURL(String query, SORT_BY sort_by, String language) {
        Uri uri;
        if (sort_by == SORT_BY.BEST_MATCH) {
            uri = Uri.parse(GITHUB_BASE_URL).buildUpon().
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

            uri = Uri.parse(GITHUB_BASE_URL).buildUpon().
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

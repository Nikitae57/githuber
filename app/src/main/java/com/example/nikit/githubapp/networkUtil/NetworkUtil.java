package com.example.nikit.githubapp.networkUtil;

import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by nikit on 01.02.2018.
 */

public class NetworkUtil {

    final static String GITHUB_BASE_URL = "https://api.github.com/search/repositories";
    final static String PARAM_QUERY = "q";
    final static String PARAM_SORT = "sort";
    private static String sortBy;

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

    public enum SORT_BY {
        BEST_MATCH, MOST_STARS, MOST_FORKS, RECENTLY_UPDATED
    }
}

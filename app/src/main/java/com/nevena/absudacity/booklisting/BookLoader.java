package com.nevena.absudacity.booklisting;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;


public class BookLoader extends AsyncTaskLoader<List<BookItem>> {

    public static final int NO_ERROR = 0;
    private static final String QUERY_RETURN_FIELDS = "&fields=items(volumeInfo(title,authors))";
    private static final String QUERY_MAX_RESULTS = "&maxResults=20";
    private static final String QUERY_SUFIX = QUERY_MAX_RESULTS + QUERY_RETURN_FIELDS;
    private static final String QUERY_PREFIX = "https://www.googleapis.com/books/v1/volumes?q=";

    private int errorMessageResourceId; //0=OK, otherwise error message resource Id
    private ConnectivityManager connectivityManager;
    private String searchStr;

    public BookLoader(Context context) {
        super(context);
        // Get a reference to the ConnectivityManager to check state of network connectivity in isOnline method
        connectivityManager = (ConnectivityManager) ((BookActivity) context).getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public int getErrorMessageResourceId() {
        return errorMessageResourceId;
    }

    public void doSearch(String s) {
        searchStr = s;
        forceLoad(); // force execute search now (why?)
    }

    @Override
    public List<BookItem> loadInBackground() {
        errorMessageResourceId = NO_ERROR; // clear previous error, assume that is all good

        List<BookItem> results = new ArrayList<>();

        if (!isOnline()) {
            // return empty list and provide error message resource id
            errorMessageResourceId = R.string.no_internet_connection;
            return results;
        }

        try { // try/catch block for I/O errors

            URL url = new URL(QUERY_PREFIX + URLEncoder.encode(searchStr, "UTF-8") + QUERY_SUFIX); // encode query string
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            try { // try-finally: ensure disconnect

                httpURLConnection.setReadTimeout(10000 /* milliseconds */);
                httpURLConnection.setConnectTimeout(15000 /* milliseconds */);
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.connect();
                StringBuilder stringBuilder = new StringBuilder();

                // reader will allow reading lines of text instead of bytes from stream
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                try { // try-finally: ensure reader close
                    String line;
                    while ((line = bufferedReader.readLine()) != null) { // read complete stream
                        stringBuilder.append(line); // ignore line breaks
                    }
                } finally {
                    bufferedReader.close();
                }

                try { // try-catch: JSON parser errors

                    JSONObject jsonResults = new JSONObject(stringBuilder.toString());
                    if (!jsonResults.has("items")) { // items may not exist, return empty list (not an error)
                        return results;
                    }

                    JSONArray items = jsonResults.getJSONArray("items");
                    for (int i = 0; i < items.length(); i++) {

                        JSONObject volumeInfo = items.getJSONObject(i).getJSONObject("volumeInfo"); // title and authors are contained within volumeInfo structure, volumeInfo was always there

                        String title = null; // will allow title not to exist
                        if (volumeInfo.has("title")) title = volumeInfo.getString("title");

                        // extract authors from query result into ArrayList
                        List<String> authors = new ArrayList<>();
                        if (volumeInfo.has("authors")) {
                            JSONArray jsonAuthors = volumeInfo.getJSONArray("authors");
                            for (int j = 0; j < jsonAuthors.length(); j++)
                                authors.add(jsonAuthors.getString(j));
                        }

                        results.add(new BookItem(title, authors)); // create new book item and add it to results
                    }

                } catch (JSONException jsone) {
                    // error in JSON parsing
                    errorMessageResourceId = R.string.parse_error;
                    return results;
                }
            } finally {
                httpURLConnection.disconnect();
            }
        } catch (IOException ioe) {
            // Error in networking
            errorMessageResourceId = R.string.io_error;
            return results;
        }
        return results;
    }

    public boolean isOnline() {

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        // If there is a network connection, fetch data
        return networkInfo != null && networkInfo.isConnected();

    }

}

package com.example.android.projectseven_booklistingapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String GOOGLE_BOOKS_REQUEST_URL = "https://www.googleapis.com/books/v1/volumes";
    private static final String BOOKS_LIST_STATE = "booksList";
    private static final int NUMBER_OF_RESULTS = 10;

    private NetworkInfo activeNetwork;
    private ConnectivityManager connectManager;
    private ArrayList<Books> mBooksList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        SearchView searchWindow = (SearchView) findViewById(R.id.search);
        searchWindow.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                activeNetwork = connectManager.getActiveNetworkInfo();

                if ((activeNetwork != null) && activeNetwork.isConnectedOrConnecting()) {
                    BookAsyncTask task = new BookAsyncTask(query);
                    task.execute();
                    return true;
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(), R.string.no_internet, Toast.LENGTH_LONG);
                    toast.show();
                    return false;
                }
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        if (savedInstanceState != null) {
            mBooksList = savedInstanceState.getParcelableArrayList(BOOKS_LIST_STATE);
            getUpdatedUI();
        } else {
            ListView listView = (ListView) findViewById(R.id.list_view);
            listView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle wrongParameter) {
        wrongParameter.putParcelableArrayList(BOOKS_LIST_STATE, mBooksList);

        super.onSaveInstanceState(wrongParameter);
    }

    private void getUpdatedUI() {
        BookAdapter newAdapter = new BookAdapter(this, mBooksList);

        TextView firtsText = (TextView) findViewById(R.id.text);
        firtsText.setVisibility(View.GONE);

        ListView listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(newAdapter);
        listView.setVisibility(View.VISIBLE);
    }

    private class BookAsyncTask extends AsyncTask<URL, Void, ArrayList<Books>> {
        private final String mSearchSQL;
        private final int mNumbers;

        public BookAsyncTask(String searchQuery) {
            mSearchSQL = searchQuery.replaceAll(" ", "%20");
            mNumbers = MainActivity.NUMBER_OF_RESULTS;
        }

        @Override
        protected ArrayList<Books> doInBackground(URL... params) {
            URL url = createUrl(mSearchSQL, mNumbers);

            String jsonResponse = "";
            try {
                jsonResponse = makeHttpRequest(url);
            } catch (IOException e) {
                Log.e(LOG_TAG, "doInBackground error: ", e);
            }

            return extractItemsFromJson(jsonResponse);
        }

        @Override
        protected void onPostExecute(ArrayList<Books> books) {
            if (books == null) {
                return;
            }
            mBooksList = books;
            getUpdatedUI();
        }

        private URL createUrl(String searchQuery, int count) {
            URL url = null;

            try {
                url = new URL(MainActivity.GOOGLE_BOOKS_REQUEST_URL + "?q=" + searchQuery + "&maxResults=" + count);
            } catch (MalformedURLException exception) {
                Log.e(LOG_TAG, "Error creating URL: ", exception);
            }

            return url;
        }

        private String makeHttpRequest(URL url) throws IOException {
            String jsonResponse = "";
            HttpURLConnection urlConnection;
            InputStream inputStream;

            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(1000);
                urlConnection.setConnectTimeout(2000);
                urlConnection.connect();

                if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    inputStream = urlConnection.getInputStream();
                    jsonResponse = readFromStream(inputStream);
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error with httpRequest: ", e);
            }
            return jsonResponse;
        }

        private String readFromStream(InputStream inputStream) throws IOException {
            StringBuilder output = new StringBuilder();
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = reader.readLine();
                while (line != null) {
                    output.append(line);
                    line = reader.readLine();
                }
            }
            return output.toString();
        }

        private ArrayList<Books> extractItemsFromJson(String booksJson) {

            try {
                JSONObject baseJsonResponse = new JSONObject(booksJson);
                JSONArray itemsArray = baseJsonResponse.getJSONArray("items");

                if (itemsArray.length() > 0) {
                    return Books.fromJson(itemsArray);
                }

            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error with parsing books JSON results: ", e);
            }
            return null;
        }
    }
}
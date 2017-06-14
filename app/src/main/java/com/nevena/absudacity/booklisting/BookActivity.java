package com.nevena.absudacity.booklisting;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;


public class BookActivity extends AppCompatActivity implements LoaderCallbacks<List<BookItem>> {

    // Save for reuse
    private BookLoader bookLoader;
    private TextView notificationPanel;
    private ListView listView;
    private View loadingIndicator;
    private BookAdapter bookAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book_activity);

        // Save UI element references for later
        notificationPanel = (TextView) findViewById(R.id.empty_view);
        loadingIndicator = findViewById(R.id.loading_indicator);
        listView = (ListView) findViewById(R.id.list);

        // Initialize book adapter (we'll use only one instance)
        bookAdapter = new BookAdapter(this);
        listView.setAdapter(bookAdapter); // bookAdapter provides data for listView

        // Initialize book loader (we'll use only one instance)
        bookLoader = new BookLoader(this);

        // Initialize loader manager
        getLoaderManager().initLoader(1, null, this); // LoaderManager notifies BookActivity about loader events via LoaderCallbacks interface

        // Hide loading indicator
        loadingIndicator.setVisibility(View.GONE);

        if (!bookLoader.isOnline()) { // check if not online
            notificationPanel.setText(R.string.no_internet_connection);
        }
    }

    public void onSearchClick(View view) {
        String searchString = ((EditText) findViewById(R.id.searchText)).getText().toString().trim(); // extract and trim entered search string
        if (searchString.equals("")) {
            notificationPanel.setText(R.string.empty_srch_string);
            return; // will not perform search if search string is not provided
        }

        bookAdapter.clear(); // clear previous search results, if any
        notificationPanel.setText(""); // clear notification, if any

        // Show loading indicator because we will wait for data to be loaded
        loadingIndicator.setVisibility(View.VISIBLE);

        // TODO: 14.6.2017. hide keyboard

        // perform search (LoaderManager will notify BookActivity about loader events via LoaderCallbacks interface)
        bookLoader.doSearch(searchString);
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        // since we have only one loader, id is not important
        return bookLoader;
    }

    @Override
    public void onLoadFinished(Loader<List<BookItem>> loader, List<BookItem> data) {
        // This gets called when data has been fetched

        loadingIndicator.setVisibility(View.GONE); // hide loading indicator

        if (data.isEmpty()) { // data is empty if there are no search results or if error occured
            //check for errors
            if (bookLoader.getErrorMessageResourceId() != BookLoader.NO_ERROR) {
                notificationPanel.setText(bookLoader.getErrorMessageResourceId()); // display error message
            } else { // no errors
                notificationPanel.setText(R.string.no_data); // display "no results found" message
            }
        } else { // we've got some search results
            bookAdapter.addAll(data); // update adapter/view
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {
        bookAdapter.clear(); //why is this never called?
    }

}

package com.nevena.absudacity.booklisting;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class BookAdapter extends ArrayAdapter<BookItem> {

    public BookAdapter(Context context) {
        // pass on empty list to ArrayAdapter, this list will be updated by BookActivity.onLoadFinished (LoaderCallbacks interface implementation)
        super(context, 0, new ArrayList<BookItem>());
    }

    // Returns a list item view that displays information about the book
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Check if there is an existing list item view (called convertView) that we can reuse,
        // otherwise, if convertView is null, then inflate a new list item layout.
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.book_list_item, parent, false);
        }
        BookItem bookItem = getItem(position); // get element at given position (from list created in constructor)

        TextView textViewTitle = (TextView) listItemView.findViewById(R.id.title);
        if (bookItem.getTitle() != null) {
            textViewTitle.setText(bookItem.getTitle());
        } else {
            textViewTitle.setText(R.string.no_title);
        }

        // concut authors into one string (separated by comma), authors may be empty
        StringBuilder allAuthors = new StringBuilder();
        List<String> authors = bookItem.getAuthors();
        for (int i = 0; i < authors.size(); i++) {
            String author = authors.get(i);
            allAuthors.append(author);
            if (i != authors.size() - 1) { // do not add comma after last author
                allAuthors.append(", ");
            }
        }
        TextView textViewAuthors = (TextView) listItemView.findViewById(R.id.author);
        textViewAuthors.setText(allAuthors.toString());

        return listItemView;
    }
}

package com.example.android.projectseven_booklistingapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by StanleyPC on 17. 6. 2017.
 */

public class BookAdapter extends ArrayAdapter<Books> {
    private ArrayList<Books> mBooksList;

      public BookAdapter(Context context, ArrayList<Books> booksList){
           super(context, 0, booksList);
       }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent){
        Books book = getItem(position);
        if (convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
        }
        TextView bookTitle = (TextView) convertView.findViewById(R.id.title);
        assert book != null;
        bookTitle.setText(book.getBookTitle());
        TextView bookAuthors = (TextView) convertView.findViewById(R.id.authors);
        bookAuthors.setText(book.getBookAuthors());
        return convertView;
    }
}
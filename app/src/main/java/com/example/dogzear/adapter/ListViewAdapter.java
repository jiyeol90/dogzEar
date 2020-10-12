package com.example.dogzear.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.dogzear.dto.BookItem;
import com.example.dogzear.R;

import java.util.ArrayList;

public class ListViewAdapter extends BaseAdapter {

    private ArrayList<BookItem> listViewItemList;

    public ListViewAdapter(ArrayList<BookItem> list) {
        listViewItemList = list;
    }

    @Override
    public int getCount() {
        return listViewItemList.size();
    }

    @Override
    public BookItem getItem(int position) {
        return listViewItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();

        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listview_item, parent, false);
        }

        ImageView bookImageView = (ImageView) convertView.findViewById(R.id.book_image);
        TextView titleTextView = (TextView) convertView.findViewById(R.id.book_title);
        TextView authorTextView = (TextView) convertView.findViewById(R.id.book_author);
        TextView dateTextView = (TextView) convertView.findViewById(R.id.book_date);

        BookItem listViewItem = listViewItemList.get(position);

        bookImageView.setImageBitmap(listViewItem.getImage());
        titleTextView.setText(listViewItem.getTitle());
        authorTextView.setText(listViewItem.getAuthor());
        dateTextView.setText(listViewItem.getStartDate());

        return convertView;
    }

    public void addItem(Bitmap icon, String title, String author, String date) {
        BookItem item = new BookItem();

        item.setImage(icon);
        item.setTitle(title);
        item.setAuthor(author);
        item.setStartDate(date);

        listViewItemList.add(item);
    }

    public void removeItem(int position) {
        listViewItemList.remove(position);
    }
}

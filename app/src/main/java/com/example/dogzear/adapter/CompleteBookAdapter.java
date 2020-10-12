package com.example.dogzear.adapter;

import android.content.Context;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dogzear.R;
import com.example.dogzear.dto.BookItem;

import java.util.ArrayList;

public class CompleteBookAdapter extends RecyclerView.Adapter<CompleteBookAdapter.CompleteViewHolder>{

    private ArrayList<BookItem> completeList;
    private Context context;



    public class CompleteViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener{
        protected ImageView bookCover;
        protected TextView bookTitle;
        protected RatingBar bookRating;

        public CompleteViewHolder(View view) {
            super(view);
            this.bookCover = (ImageView) view.findViewById(R.id.book_cover);
            this.bookTitle = (TextView) view.findViewById(R.id.book_title);
            this.bookRating = (RatingBar) view.findViewById(R.id.book_rating);

            view.setOnCreateContextMenuListener(this);
        }


        private final MenuItem.OnMenuItemClickListener onEditMenu = new MenuItem.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case 1001:
                        completeList.remove(getAdapterPosition());
                        notifyItemRemoved(getAdapterPosition());
                        notifyItemRangeChanged(getAdapterPosition(), completeList.size());
                }
                return false;
            }
        };

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            MenuItem Delete = menu.add(Menu.NONE, 1001, 1, "삭제");
            Delete.setOnMenuItemClickListener(onEditMenu);
        }
    }

    public CompleteBookAdapter(ArrayList<BookItem> completeList) {
        this.completeList = completeList;
    }



    @NonNull
    @Override
    public CompleteBookAdapter.CompleteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.complete_book_item, parent, false);

        CompleteViewHolder viewHolder = new CompleteViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CompleteBookAdapter.CompleteViewHolder viewHolder, int position) {

        viewHolder.bookTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);

        viewHolder.bookCover.setImageBitmap(completeList.get(position).getImage());
        viewHolder.bookTitle.setText(completeList.get(position).getTitle());
        viewHolder.bookRating.setRating(Float.parseFloat(completeList.get(position).getRating()));
    }

    @Override
    public int getItemCount() {
        return (null != completeList ? completeList.size() : 0);
    }
}

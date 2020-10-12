package com.example.dogzear.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.dogzear.dto.BookItem;
import com.example.dogzear.R;

import java.util.ArrayList;

public class BookAdapter extends BaseAdapter implements Filterable{

    private ArrayList<BookItem> listViewItemList;
    //필터링된 결과 데이터를 저장하기 위한 ArrayList. 최초에는 전체 리스트 보유
    //원본데이터 listViewItemList와 검색결과만 필터링 한 filteredItemList 모두를 관리해 주어야 한다.
    private ArrayList<BookItem> filteredItemList;
    Filter listFilter;


    public BookAdapter(ArrayList<BookItem> list) {

        listViewItemList = list;
        //최초에는 filteredItemList가 전체 리스트 보유
        filteredItemList = listViewItemList;
    }


    //임의로 만들어준다.
    public ArrayList<BookItem> getListViewItemList() {
        return listViewItemList;
    }
    //임의로 만들어준다.
    public void setListViewItemList(ArrayList<BookItem> listViewItemList) {
        this.listViewItemList = listViewItemList;
    }

    //필수 구현 : Adapter에 사용되는 데이터의 개수를 리턴.
    @Override
    public int getCount() {
        return filteredItemList.size();
    }

    //필수 구현 : 지정한 위치(position)에 있는 데이터를 리턴.
    @Override
    public BookItem getItem(int position) {
        return filteredItemList.get(position);
    }

    //기존의 리스트에있는 데이터를 리턴한다.
    public BookItem getOriginItem(int position) { return listViewItemList.get(position); }

    //필수 구현 : 지정한 위치(position)에 있는 데이터와 관계된 아이템의 ID를 리턴.
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

        BookItem listViewItem = filteredItemList.get(position);

        bookImageView.setImageBitmap(listViewItem.getImage());
        titleTextView.setText(listViewItem.getTitle());
        authorTextView.setText(listViewItem.getAuthor());
        dateTextView.setText(listViewItem.getStartDate());

        return convertView;
    }

    public void addItem(Bitmap icon, String title, String author, String date, String page) {
        BookItem item = new BookItem();

        item.setImage(icon);
        item.setTitle(title);
        item.setAuthor(author);
        item.setStartDate(date);
        item.setTotalPage(page);
        listViewItemList.add(item);
    }

    public void removeItem(int position) {

        listViewItemList.remove(position);
        //원본 데이터를 지운후 filteredItemList에 있는 데이터도 지워줘야 한다. -> 직접찾아서 지우지않고 초기화 시킨다.
        filteredItemList = listViewItemList;
    }

    //실질적인 필터링 기능은 Filter클래스에 구현해야 한다.
    @Override
    public Filter getFilter() {
        if(listFilter == null) {
            listFilter = new ListFilter();
        }
        return listFilter;
    }

    private class ListFilter extends Filter {

        //필터링을 수행하는 함수-> 필터링을 수행하는 루프를 이 함수에 구현한 다음, 필터링된 결과 리스트를 FilterResults에 담아서 리턴한다.
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults() ;

            if (constraint == null || constraint.length() == 0) {
                results.values = listViewItemList ;
                results.count = listViewItemList.size() ;
            } else {
                ArrayList<BookItem> itemList = new ArrayList<BookItem>() ;

                for (BookItem item : listViewItemList) {
                    //책 제목과 작가이름을 검색한다.
                    if (item.getTitle().contains(constraint.toString()) ||
                            item.getAuthor().contains(constraint.toString()))
                    {
                        itemList.add(item) ;
                    }
                }
                //필터링된 리스트를 담는다.
                results.values = itemList ;
                results.count = itemList.size() ;
            }
            return results;
        }

        //performFiltering()함수에서 필터링된 결과를 UI에 갱신시키는 역할을 수행한다.
        //커스텀 Adapter를 통한 ListView 갱신 작업을 구현한다.
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            // update listview by filtered data list.
            filteredItemList = (ArrayList<BookItem>) results.values ;

            // notify
            if (results.count > 0) {
                notifyDataSetChanged() ;
            } else {
                notifyDataSetInvalidated() ;
            }
        }
    }

}






















package com.example.dogzear.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dogzear.dto.BookMemo;
import com.example.dogzear.R;

import java.util.ArrayList;

public class BookMemoAdapter extends RecyclerView.Adapter<BookMemoAdapter.MemoViewHolder> {

    private ArrayList<BookMemo> memoList;
    private Context context;

    //임의로 만들어준다.
    public ArrayList<BookMemo> getMemoItemList() {
        return memoList;
    }
    //임의로 만들어준다.
    public void setMemoItemList(ArrayList<BookMemo> memoList) {
        this.memoList = memoList;
    }

    // 1. 컨텍스트 메뉴를 사용하라면 RecyclerView.ViewHolder를 상속받은 클래스에서
    // OnCreateContextMenuListener 리스너를 구현해야 한다.
    public class MemoViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener{
        protected TextView page;
        protected TextView chapter;
        protected TextView date;

        public MemoViewHolder(View view) {
            super(view);
            this.page = (TextView) view.findViewById(R.id.page_listitem);
            this.chapter = (TextView) view.findViewById(R.id.chapter_listitem);
            this.date = (TextView) view.findViewById(R.id.date_listitem);

            view.setOnCreateContextMenuListener(this);
        }



        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            //ID 1001, 1002로 어떤 메뉴를 택했는지 리스너에게 구분하게 된다.
            MenuItem Edit = menu.add(Menu.NONE, 1001, 1, "편집");
            MenuItem Delete = menu.add(Menu.NONE, 1002, 2, "삭제");
            Edit.setOnMenuItemClickListener(onEditMenu);
            Delete.setOnMenuItemClickListener(onEditMenu);

        }

        //컨텍스트 메뉴에서 항목 클릭시 동작을 설정한다.
        private final MenuItem.OnMenuItemClickListener onEditMenu = new MenuItem.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()) {
                    case 1001 :
                        //"편집" 선택시 다이얼로그 창을 띄어준다.
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);

                        View view = LayoutInflater.from(context).inflate(R.layout.book_memo_box, null, false);
                        builder.setView(view);

                        final Button submitBtn = (Button) view.findViewById(R.id.button_ok);
                        final EditText editTextPage = (EditText) view.findViewById(R.id.et_page);
                        final EditText editTextChapter = (EditText) view.findViewById(R.id.et_chapter);
                        final EditText editTextDate = (EditText) view.findViewById(R.id.et_date);

                        editTextPage.setText(memoList.get(getAdapterPosition()).getPage());
                        editTextChapter.setText(memoList.get(getAdapterPosition()).getChapter());
                        editTextDate.setText(memoList.get(getAdapterPosition()).getDate());

                        final AlertDialog dialog = builder.create();
                        submitBtn.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                String page = editTextPage.getText().toString();
                                String chapter = editTextChapter.getText().toString();
                                String date = editTextDate.getText().toString();

                                BookMemo memo = new BookMemo(page, chapter, date);

                                memoList.set(getAdapterPosition(), memo);

                                notifyItemChanged(getAdapterPosition());

                                dialog.dismiss();
                            }
                        });
                        dialog.show();

                        break;
                    case 1002 :

                        memoList.remove(getAdapterPosition());
                        notifyItemRemoved(getAdapterPosition());
                        notifyItemRangeChanged(getAdapterPosition(), memoList.size());
                        break;
                }
                return true;
            }
        };

    }

    public BookMemoAdapter(Context context, ArrayList<BookMemo> list) {
        this.memoList = list;
        this.context = context;
    }

    @NonNull
    @Override
    public MemoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.book_memo_item, parent, false);
        MemoViewHolder viewHolder = new MemoViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MemoViewHolder viewholder, int position) {
        //Layout에서 TextSize를 조정해도 변화가 없다. 무조건 뷰홀더 에서 조절해줘야한다.
        viewholder.page.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        viewholder.chapter.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        viewholder.date.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);

        viewholder.page.setText(memoList.get(position).getPage());
        viewholder.chapter.setText(memoList.get(position).getChapter());
        viewholder.date.setText(memoList.get(position).getDate());
    }

    @Override
    public int getItemCount() {
        return (null != memoList ? memoList.size() : 0);
    }


}

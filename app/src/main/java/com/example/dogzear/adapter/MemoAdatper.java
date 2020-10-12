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

import com.example.dogzear.dto.MemoItem;
import com.example.dogzear.R;

import java.util.ArrayList;

public class MemoAdatper extends RecyclerView.Adapter<MemoAdatper.CustomViewHolder> {

    //리사이클러뷰에 담을 아이템의 리스트
    private ArrayList<MemoItem> recyclerViewItemList;
    private Context context;
    //리스트뷰에서의 아이템 리스트와 변수명 통일
    //private ArrayList<ListViewItem> listViewItemList = new ArrayList<ListViewItem>();

    //컨텍스트 메뉴(길게 누를때 활성화 되는 메뉴)를 사용하기 위해서는 OnCreateContextMenuListener를 구현해야 한다.
    public class CustomViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener{
        //findViewById 대상이 된느 뷰들을 묶어서 관리한다.
        protected TextView title;
        protected TextView content;
        protected TextView page;

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);
            this.title = (TextView) itemView.findViewById(R.id.book_title);
            this.content = (TextView) itemView.findViewById(R.id.memo_content);
            this.page = (TextView) itemView.findViewById(R.id.page_number);

            itemView.setOnCreateContextMenuListener(this);
        }

        //컨텍스트 메뉴 생성하고 메뉴 항목 선택시 호출되는 리스너를 등록
        // ID 1001, 1002로 어떤 메뉴를 선택했는지 리스너에서 구분한다.
        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {

            MenuItem Edit = contextMenu.add(Menu.NONE, 1001, 1, "편집");
            MenuItem Delete = contextMenu.add(Menu.NONE, 1002, 2, "삭제");
            Edit.setOnMenuItemClickListener(onEditMenu);
            Delete.setOnMenuItemClickListener(onEditMenu);
        }

        //컨텍스트 메뉴에서 항목 클릭시 동작을 설정
        private final MenuItem.OnMenuItemClickListener onEditMenu = new MenuItem.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                switch(menuItem.getItemId()) {
                    //편집항목 선택
                    case 1001:
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        View view = LayoutInflater.from(context).inflate(R.layout.edit_box, null, false);
                        builder.setView(view);

                        final Button submitBtn = (Button) view.findViewById(R.id.submit_btn);
                        final EditText et_title = (EditText) view.findViewById(R.id.et_title);
                        final EditText et_content = (EditText) view.findViewById(R.id.et_content);
                        final EditText et_page = (EditText) view.findViewById(R.id.et_page);

                        //해당 줄에 입력되어 있던 데이터를 불러와 다이얼로그에 출력한다.
                        et_title.setText(recyclerViewItemList.get(getAdapterPosition()).getTitle());
                        et_content.setText(recyclerViewItemList.get(getAdapterPosition()).getContent());
                        et_page.setText(recyclerViewItemList.get(getAdapterPosition()).getPage());

                        final AlertDialog dialog = builder.create();
                        submitBtn.setOnClickListener(new View.OnClickListener(){

                            //수정 버튼을 클릭하면 현재 UI에 입력되어 있는 내용으로
                            @Override
                            public void onClick(View view) {
                                String title = et_title.getText().toString();
                                String content = et_content.getText().toString();
                                String page = et_page.getText().toString();

                                MemoItem item = new MemoItem(title, content, page);

                                //ListArray에 있는 데이터를 변경 -> getAdapterPosition()으로 position값을 얻어온다.
                                recyclerViewItemList.set(getAdapterPosition(), item);
                                //변경 후 반영한다.
                                notifyItemChanged(getAdapterPosition());

                                dialog.dismiss();
                            }
                        });
                        dialog.show();
                        break;
                    //삭제항목 선택 -> getAdapterPosition()로 position만 얻어오고 리스트에서 해당 값을 remove한다
                   case 1002:
                        recyclerViewItemList.remove(getAdapterPosition());
                        notifyItemRemoved(getAdapterPosition());
                        notifyItemRangeChanged(getAdapterPosition(), recyclerViewItemList.size());
                        break;
                }
                return true;
            }
        };


    }

    public MemoAdatper(Context context, ArrayList<MemoItem> recyclerViewItemList) {
        this.recyclerViewItemList = recyclerViewItemList;
        this.context = context;
    }

    @NonNull
    @Override
    public MemoAdatper.CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.memo_list, parent, false);

        CustomViewHolder viewHolder = new CustomViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MemoAdatper.CustomViewHolder viewHolder, int position) {

        //뷰홀더의 뷰에 설정을 해준다. (xml에서 android:textSize="15sp") 와 같음.
        viewHolder.title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        viewHolder.content.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        viewHolder.page.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);

        viewHolder.title.setText(recyclerViewItemList.get(position).getTitle());
        viewHolder.content.setText(recyclerViewItemList.get(position).getContent());
        viewHolder.page.setText(recyclerViewItemList.get(position).getPage());

    }

    @Override
    public int getItemCount() {
        return (null != recyclerViewItemList ? recyclerViewItemList.size() : 0);
    }


    /*
    * public void addItem(Bitmap icon, String title, String author, String date) {
        ListViewItem item = new ListViewItem();

        item.setImage(icon);
        item.setTitle(title);
        item.setAuthor(author);
        item.setDate(date);

        listViewItemList.add(item);
    }
    * */
    /*샘플 아이템을 넣어주기 위한 메소드*/
    public void addItem(String title, String content, String page) {
        MemoItem item = new MemoItem(title, content, page);

//        item.setTitle(title);
//        item.setContent(content);
//        item.setPage(page);

        recyclerViewItemList.add(item);
    }
}

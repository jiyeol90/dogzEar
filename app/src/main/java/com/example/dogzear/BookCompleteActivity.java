package com.example.dogzear;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dogzear.adapter.CompleteBookAdapter;
import com.example.dogzear.customchart.DayAxisValueFormatter;
import com.example.dogzear.dto.BookItem;
import com.example.dogzear.lock.BaseActivity;
import com.example.dogzear.saveAndLoad.SaveAndLoad;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.MPPointF;

import java.util.ArrayList;

public class BookCompleteActivity extends BaseActivity implements SeekBar.OnSeekBarChangeListener,
        OnChartValueSelectedListener {

    private static final String SETTING_COMPLETE_JSON = "setting_complete_json";
    private ArrayList<BookItem> completeList;
    private CompleteBookAdapter completeAdapter;
    private static RecyclerView completeRecyclerView;
    private BookItem completeBook;
    private HorizontalBarChart chart;
    private SeekBar seekBarX, seekBarY;
    private TextView tvX, tvY;
    private int count = -1;
    protected Typeface tfLight;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_chart);

        //완독한 책을 보여줄 리사이클러뷰
        completeRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_complete_list);
        //가로로 스크롤 하면서 보여주기 위해 LinearLayoutManager.HORIZONTAL 을 사용한다.
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false );
        completeRecyclerView.setLayoutManager(linearLayoutManager);
        //완독한 책 목록을 불러온다.
        completeList =  SaveAndLoad.getStringArrayPref(this, SETTING_COMPLETE_JSON);

        completeAdapter = new CompleteBookAdapter(completeList);
        completeRecyclerView.setAdapter(completeAdapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(completeRecyclerView.getContext(),
                linearLayoutManager.getOrientation());
        completeRecyclerView.addItemDecoration(dividerItemDecoration);

        //차트를 가져온다.////////////////////////////////////////////////////////////////////////////////////////////////////////////
        tfLight = Typeface.createFromAsset(getAssets(), "OpenSans-Light.ttf");

//        tvX = findViewById(R.id.tvXMax);
//        tvY = findViewById(R.id.tvYMax);
//
//        seekBarX = findViewById(R.id.seekBar1);
//        seekBarY = findViewById(R.id.seekBar2);
//
//        seekBarY.setOnSeekBarChangeListener(this);
//        seekBarX.setOnSeekBarChangeListener(this);

        chart = findViewById(R.id.chart);

        setData(12, 5);

        chart.setOnChartValueSelectedListener(this);
        // chart.setHighlightEnabled(false);

        chart.setDrawBarShadow(false);

        chart.setDrawValueAboveBar(true);

        chart.getDescription().setEnabled(false);

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        chart.setMaxVisibleValueCount(60);

        // scaling can now only be done on x- and y-axis separately
        chart.setPinchZoom(false);

        // draw shadows for each bar that show the maximum value
        // chart.setDrawBarShadow(true);

        chart.setDrawGridBackground(false);


        IAxisValueFormatter xAxisFormatter = new DayAxisValueFormatter(chart);
        XAxis xl = chart.getXAxis();
        xl.setPosition(XAxis.XAxisPosition.BOTTOM);
        xl.setTypeface(tfLight);
        xl.setDrawAxisLine(true);
        xl.setDrawGridLines(false);
        xl.setGranularity(1f);
        xl.setValueFormatter(xAxisFormatter);

        YAxis yl = chart.getAxisLeft();
        yl.setTypeface(tfLight);
        yl.setDrawAxisLine(true);
        yl.setDrawGridLines(true);
//        yl.setAxisMinimum(0f);
        // this replaces setStartAtZero(true)
//        yl.setInverted(true);

        YAxis yr = chart.getAxisRight();
        yr.setTypeface(tfLight);
        yr.setDrawAxisLine(true);
        yr.setDrawGridLines(false);
  //      yr.setAxisMinimum(0f); // this replaces setStartAtZero(true)
//        yr.setInverted(true);

        //chart.setFitBars(true);
        chart.animateY(2500);
//
//        // setting data
//        seekBarY.setProgress(9);
//        seekBarX.setProgress(12);
//
//        Legend l = chart.getLegend();
//        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
//        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
//        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
//        l.setDrawInside(false);
//        l.setFormSize(8f);
//        l.setXEntrySpace(4f);

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //리사이클러뷰에 리스너를 등록한다.
        ItemClickSupport.addTo(completeRecyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener(){

            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                //클릭한 책을 담는다.
                completeBook = completeList.get(position);
                String duration = completeBook.getStartDate() + " ~ " + completeBook.getFinishDate();
                String rating = completeBook.getRating();
                String review = completeBook.getComment();
                //아이템을 클릭할시 요약 정보와 내용들을 dialog로 보여준다.
                AlertDialog.Builder builder = new AlertDialog.Builder(BookCompleteActivity.this);
                View view = LayoutInflater.from(BookCompleteActivity.this)
                        .inflate(R.layout.book_review_item, null, false);
                builder.setView(view);
                final Button ButtonOk = (Button) view.findViewById(R.id.button_ok);
                final TextView readingDuration = (TextView) view.findViewById(R.id.readingDuration);
                final RatingBar ratingBar = (RatingBar) view.findViewById(R.id.book_rating_bar);
                final TextView bookReview = (TextView) view.findViewById(R.id.review_content);

                //다이얼로그에 표시해줄 내용들 : 읽은 기간, 별점, 리뷰내용
                readingDuration.setText(duration);
                ratingBar.setRating(Float.parseFloat(rating));
                bookReview.setText(review);

                final AlertDialog dialog = builder.create();
                //확인버튼을 누르면 다이얼로그 창을 닫는다. -> 삭제기능은 있지만 수정기능은 없다.
                ButtonOk.setOnClickListener(new View.OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
                Toast.makeText(BookCompleteActivity.this, "클릭한 아이템의 이름은 " + completeList.get(position).getTitle(), Toast.LENGTH_SHORT).show();
            }
        });

        //리사이클러뷰의 아이템을 길게 눌렀을때 Alert창을 띄어주고 삭제한다.
        ItemClickSupport.addTo(completeRecyclerView).setOnItemLongClickListener(new ItemClickSupport.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClicked(RecyclerView recyclerView, final int position, View v) {
                Toast.makeText(BookCompleteActivity.this, "길게 눌렀구나 " + completeList.get(position).getTitle(), Toast.LENGTH_SHORT).show();

                AlertDialog.Builder builder = new AlertDialog.Builder(BookCompleteActivity.this);
                builder.setIcon(R.drawable.book);
                builder.setTitle("알림");
                builder.setMessage("정말 삭제 하시겠습니까?");
                builder.setNegativeButton("아니오", null);
                builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        completeList.remove(position);
                        completeAdapter.notifyItemRemoved(position);

                        SaveAndLoad.setStringArrayPref(getApplicationContext(),SETTING_COMPLETE_JSON, completeList);
                        completeAdapter.notifyItemRangeChanged(position, completeList.size());
                    }
                });
                builder.create().show();
                return true;
            }
        });
    }

  //ItemClickSupport class를 이용해보자. 참고 사이트 (https://guides.codepath.com/android/using-the-recyclerview#attaching-click-handlers-to-items)
  public static class ItemClickSupport {

      private OnItemClickListener mOnItemClickListener;
      private OnItemLongClickListener mOnItemLongClickListener;
      private View.OnClickListener mOnClickListener = new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              if (mOnItemClickListener != null) {
                  RecyclerView.ViewHolder holder = completeRecyclerView.getChildViewHolder(v);
                  mOnItemClickListener.onItemClicked(completeRecyclerView, holder.getAdapterPosition(), v);
              }
          }
      };
      private View.OnLongClickListener mOnLongClickListener = new View.OnLongClickListener() {
          @Override
          public boolean onLongClick(View v) {
              if (mOnItemLongClickListener != null) {
                  RecyclerView.ViewHolder holder = completeRecyclerView.getChildViewHolder(v);
                  return mOnItemLongClickListener.onItemLongClicked(completeRecyclerView, holder.getAdapterPosition(), v);
              }
              return false;
          }
      };
      private RecyclerView.OnChildAttachStateChangeListener mAttachListener
              = new RecyclerView.OnChildAttachStateChangeListener() {
          @Override
          public void onChildViewAttachedToWindow(View view) {
              if (mOnItemClickListener != null) {
                  view.setOnClickListener(mOnClickListener);
              }
              if (mOnItemLongClickListener != null) {
                  view.setOnLongClickListener(mOnLongClickListener);
              }
          }

          @Override
          public void onChildViewDetachedFromWindow(View view) {

          }
      };

      private ItemClickSupport(RecyclerView recyclerView) {
          completeRecyclerView = recyclerView;
          completeRecyclerView.setTag(R.id.complete_list_item, this);
          completeRecyclerView.addOnChildAttachStateChangeListener(mAttachListener);
      }

      public static ItemClickSupport addTo(RecyclerView view) {
          ItemClickSupport support = (ItemClickSupport) view.getTag(R.id.complete_list_item);
          if (support == null) {
              support = new ItemClickSupport(view);
          }
          return support;
      }

      public static ItemClickSupport removeFrom(RecyclerView view) {
          ItemClickSupport support = (ItemClickSupport) view.getTag(R.id.complete_list_item);
          if (support != null) {
              support.detach(view);
          }
          return support;
      }

      public ItemClickSupport setOnItemClickListener(OnItemClickListener listener) {
          mOnItemClickListener = listener;
          return this;
      }

      public ItemClickSupport setOnItemLongClickListener(OnItemLongClickListener listener) {
          mOnItemLongClickListener = listener;
          return this;
      }

      private void detach(RecyclerView view) {
          view.removeOnChildAttachStateChangeListener(mAttachListener);
          view.setTag(R.id.complete_list_item, null);
      }

      public interface OnItemClickListener {

          void onItemClicked(RecyclerView recyclerView, int position, View v);
      }

      public interface OnItemLongClickListener {

          boolean onItemLongClicked(RecyclerView recyclerView, int position, View v);
      }
  }
  ////////////////////////////////////////////////////////////////////////////////////////////
  private void setData(int count, float range) {

      float barWidth = 5f;
      float spaceForBar = 8f;
      ArrayList<BarEntry> values = new ArrayList<>();

      for (int i = 0; i < count; i++) {
          float val = (float) (Math.random() * range);
          values.add(new BarEntry(i * spaceForBar, val,
                  getResources().getDrawable(R.drawable.star)));
      }

      BarDataSet set1;

      if (chart.getData() != null &&
              chart.getData().getDataSetCount() > 0) {
          set1 = (BarDataSet) chart.getData().getDataSetByIndex(0);
          set1.setValues(values);
          chart.getData().notifyDataChanged();
          chart.notifyDataSetChanged();
      } else {
          set1 = new BarDataSet(values, "월별 독서량");

         // set1.setDrawIcons(false);

          ArrayList<IBarDataSet> dataSets = new ArrayList<>();
          dataSets.add(set1);

          BarData data = new BarData(dataSets);
          data.setValueTextSize(10f);
          data.setValueTypeface(tfLight);
          data.setBarWidth(barWidth);
          chart.setData(data);
      }
  }
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        tvX.setText(String.valueOf(seekBarX.getProgress()));
        tvY.setText(String.valueOf(seekBarY.getProgress()));

        setData(seekBarX.getProgress(), seekBarY.getProgress());
        chart.setFitBars(true);
        chart.invalidate();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {}

    private final RectF mOnValueSelectedRectF = new RectF();

    @Override
    public void onValueSelected(Entry e, Highlight h) {

        if (e == null)
            return;

        RectF bounds = mOnValueSelectedRectF;
        chart.getBarBounds((BarEntry) e, bounds);

        MPPointF position = chart.getPosition(e, chart.getData().getDataSetByIndex(h.getDataSetIndex())
                .getAxisDependency());

        Log.i("bounds", bounds.toString());
        Log.i("position", position.toString());

        MPPointF.recycleInstance(position);
    }

    @Override
    public void onNothingSelected() {}
}




















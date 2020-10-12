package com.example.dogzear;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.dogzear.decorators.EventDecorator;
import com.example.dogzear.decorators.OneDayDecorator;
import com.example.dogzear.decorators.SaturdayDecorator;
import com.example.dogzear.decorators.SundayDecorator;
import com.example.dogzear.dto.BookItem;
import com.example.dogzear.saveAndLoad.SaveAndLoad;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executors;

public class BookCalendarActivity extends AppCompatActivity {

    private static final String TAG = "BookCalendarActivity";
    private static final String SETTING_COMPLETE_JSON = "setting_complete_json";
    private ArrayList<BookItem> completeList;
    private BookItem completeBook;
    //완독한 책 날짜들을 담을 배열 -> 해당 날짜에 점을 찍어준다.
    private String[] completeBookDate;

    private final OneDayDecorator oneDayDecorator = new OneDayDecorator();
    Cursor cursor;
    MaterialCalendarView materialCalendarView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        completeList =  SaveAndLoad.getStringArrayPref(this, SETTING_COMPLETE_JSON);

        materialCalendarView = (MaterialCalendarView)findViewById(R.id.calendarView);

        materialCalendarView.state().edit()
                .setFirstDayOfWeek(Calendar.SUNDAY)
                .setMinimumDate(CalendarDay.from(2020, 0, 1)) // 달력의 시작
                .setMaximumDate(CalendarDay.from(2030, 11, 31)) // 달력의 끝
                .setCalendarDisplayMode(CalendarMode.MONTHS)
                .commit();

        materialCalendarView.addDecorators(
                new SundayDecorator(),
                new SaturdayDecorator(),
                oneDayDecorator);

        Log.d(TAG, "completeList의 사이즈 : " + completeList.size());
        completeBookDate = new String[completeList.size()];
        for(int i = 0; i < completeList.size(); i++) {
            completeBookDate[i] = completeList.get(i).getFinishDate();
        }


        new ApiSimulator(completeBookDate).executeOnExecutor(Executors.newSingleThreadExecutor());

        materialCalendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                int Year = date.getYear();
                int Month = date.getMonth() + 1;
                int Day = date.getDay();

                Log.i("Year test", Year + "");
                Log.i("Month test", Month + "");
                Log.i("Day test", Day + "");

                String bookDay = Year + "/" + Month + "/" + Day;

                Log.i("shot_Day test", bookDay + "");
                materialCalendarView.clearSelection();

                for(BookItem item : completeList) {
                    if(item.getFinishDate().equals(bookDay)) {
                        Toast.makeText(getApplicationContext(), item.getTitle() , Toast.LENGTH_SHORT).show();
                    }
                }

                Toast.makeText(getApplicationContext(), bookDay , Toast.LENGTH_SHORT).show();

            }
        });
    }

    private class ApiSimulator extends AsyncTask<Void, Void, List<CalendarDay>> {

        String[] Time_Result;

        ApiSimulator(String[] Time_Result){
            this.Time_Result = Time_Result;
        }

        @Override
        protected List<CalendarDay> doInBackground(@NonNull Void... voids) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Calendar calendar = Calendar.getInstance();
            ArrayList<CalendarDay> dates = new ArrayList<>();

            /*특정날짜 달력에 점표시해주는곳*/
            /*월은 0이 1월 년,일은 그대로*/
            //string 문자열인 Time_Result 을 받아와서 ,를 기준으로짜르고 string을 int 로 변환
            for(int i = 0 ; i < Time_Result.length ; i ++){

                String[] time = Time_Result[i].split("/");
                int year = Integer.parseInt(time[0]);
                int month = Integer.parseInt(time[1]);
                int day = Integer.parseInt(time[2]);

                calendar.set(year, month - 1, day);
                CalendarDay completeDay = CalendarDay.from(calendar);
                dates.add(completeDay);
            }



            return dates;
        }

        @Override
        protected void onPostExecute(@NonNull List<CalendarDay> calendarDays) {
            super.onPostExecute(calendarDays);

            if (isFinishing()) {
                return;
            }

            materialCalendarView.addDecorator(new EventDecorator(Color.RED, calendarDays,BookCalendarActivity.this));
        }
    }
}
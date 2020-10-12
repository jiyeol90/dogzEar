package com.example.dogzear.openapi;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dogzear.R;
import com.example.dogzear.openapi.GetFindDustThread;
import com.example.dogzear.openapi.GetStationListThread;
import com.example.dogzear.openapi.GetTransCoord2Thread;

import java.util.ArrayList;
import java.util.List;

public class DustOpenAPIActivity extends Activity implements View.OnClickListener, AdapterView.OnItemSelectedListener {


    Location mLastLocation;
    Button getBtn, getNearStation;
    static EditText where;
    String from = "WGS84";
    String to = "TM";
    static Spinner sido, station;    //스피너
    static String sidolist[] = {"서울", "부산", "대전", "대구", "광주", "울산", "경기", "강원", "충북", "충남", "경북", "경남", "전북", "전남", "제주"};
    static String stationlist[];    //측정소목록(이건 api로 가져올꺼라 몇개인지 모른다)
    static ArrayAdapter<String> spinnerSido, spinnerStation;    //spinner에 붙일 array adapter
    static TextView totalcnt, date, so2value, covalue, o3value, no2value, pm10value, khaivalue, so2grade, cograde, o3grade, no2grade, pm10grade, khaigrade;
    static int stationCnt = 0;
    static Context mContext;    //static에서 context를 쓰기위해
    private String[] permissions = {android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    private static final int MULTIPLE_PERMISSIONS = 101;
    private static final int REQUEST_CODE_LOCATION = 2;
    protected LocationManager locationManager;
    Location location;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_dust_open_api);
        init();
    }

    public void init() {


        mContext = getApplicationContext();    //static에서 context를 쓰기위해~
        totalcnt = findViewById(R.id.totalcnt);
        date = findViewById(R.id.date);
        so2value = findViewById(R.id.so2value);
        covalue = findViewById(R.id.covalue);
        o3value = findViewById(R.id.o3value);
        no2value = findViewById(R.id.no2value);
        pm10value = findViewById(R.id.pm10value);
        khaivalue = findViewById(R.id.khaivalue);
        so2grade = findViewById(R.id.so2grade);
        cograde = findViewById(R.id.cograde);
        o3grade = findViewById(R.id.o3grade);
        no2grade = findViewById(R.id.no2grade);
        pm10grade = findViewById(R.id.pm10grade);
        khaigrade = findViewById(R.id.khaigrade);

        where = findViewById(R.id.where);
        getNearStation = findViewById(R.id.getNearStation);        //측정소버튼 객체생성
        getBtn = findViewById(R.id.getBtn);        //대기정보버튼 객체생성

        sido = findViewById(R.id.sido);    //시도 스피너
        station = findViewById(R.id.station);    //측정소 스피너
        sido.setOnItemSelectedListener(this);    //스피너 선택할때 작동시킬 리스너등록
        station.setOnItemSelectedListener(this);

        getNearStation.setOnClickListener(this); //gps로 가까운 측정소 가져오는 버튼 리스너
        getBtn.setOnClickListener(this);    //대기정보 가져오는 버튼 리스너
        spinnerSido = new ArrayAdapter<>(getApplication(), R.layout.spinner_location, sidolist);    //array adapter에 시도 리스트를 넣어줌
        sido.setAdapter(spinnerSido);    //스피너에 adapter를 연결

        checkPermissions();

    }

    private boolean checkPermissions() {
        int result;
        List<String> permissionList = new ArrayList<>();
        for (String pm : permissions) {
            result = ContextCompat.checkSelfPermission(this, pm);
            if (result != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(pm);
            }
        }
        if (!permissionList.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionList.toArray(new String[permissionList.size()]), MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }


    public static void getFindDust(String name) {    //대기정보를 가져오는 스레드

        GetFindDustThread.active = true;
        GetFindDustThread getweatherthread = new GetFindDustThread(false, name);        //스레드생성(UI 스레드사용시 system 뻗는다)
        getweatherthread.start();    //스레드 시작

    }

    public static void FindDustThreadResponse(String getCnt, String[] sDate, String[] sSo2Value, String[] sCoValue, String[] sO3Value, String[] sNo2Value, String[] sPm10Value, String[] sKhaiValue, String[] sKhaiGrade, String[] sSo2Grade, String[] sNo2Grade, String[] sCoGrade, String[] sO3Grade, String[] sPm10Grade) {    //대기정보 가져온 결과값
        stationCnt = 0;    //측정개수정보(여기선 1개만 가져온다
        stationCnt = Integer.parseInt(getCnt);

        Log.w("stationcnt", String.valueOf(stationCnt));

        if (stationCnt == 0) {    //만약 측정정보가 없다면
            totalcnt.setText("측정소 정보가 없거나 측정정보가 없습니다.");
            date.setText("");    //
            so2value.setText("");
            covalue.setText("");
            no2value.setText("");
            o3value.setText("");
            pm10value.setText("");
            khaivalue.setText("");
            khaigrade.setText("");
            so2grade.setText("");
            no2grade.setText("");
            cograde.setText("");
            o3grade.setText("");
            no2grade.setText("");
            pm10grade.setText("");
        } else {    //측정정보있으면
            totalcnt.setText(sDate[0] + "에 대기정보가 업데이트 되었습니다.");

            date.setText(sDate[0]);    //
            so2value.setText(sSo2Value[0] + "ppm");
            covalue.setText(sCoValue[0] + "ppm");
            no2value.setText(sNo2Value[0] + "ppm");
            o3value.setText(sO3Value[0] + "ppm");
            pm10value.setText(sPm10Value[0] + "μg/m³");
            khaivalue.setText(sKhaiValue[0]);
            khaigrade.setText(transGrade(sKhaiGrade[0]));
            so2grade.setText(transGrade(sSo2Grade[0]));
            no2grade.setText(transGrade(sNo2Grade[0]));
            cograde.setText(transGrade(sCoGrade[0]));
            o3grade.setText(transGrade(sO3Grade[0]));
            no2grade.setText(transGrade(sNo2Grade[0]));
            pm10grade.setText(transGrade(sPm10Grade[0]));

        }

        GetFindDustThread.active = false;
        GetFindDustThread.interrupted();
    }

    public static void getStationList(String name) {    //이건 측정소 정보가져올 스레드

        GetStationListThread.active = true;
        GetStationListThread getstationthread = new GetStationListThread(false, name);        //스레드생성(UI 스레드사용시 system 뻗는다)
        getstationthread.start();    //스레드 시작

    }

    public static void StationListThreadResponse(String cnt, String[] sStation) {    //측정소 정보를 가져온 결과
        stationCnt = 0;
        stationCnt = Integer.parseInt(cnt);
        stationlist = new String[stationCnt];
        for (int i = 0; i < stationCnt; i++) {
            stationlist[i] = sStation[i];
            Log.e("station", stationlist[i]);
        }
        //stationlist=sStation;
        //if(stationCnt!=0){
        spinnerStation = new ArrayAdapter<>(mContext, R.layout.spinner_location, stationlist);
        station.setAdapter(spinnerStation);
        //}


        GetFindDustThread.active = false;
        GetFindDustThread.interrupted();


    }

    public static void getNearStation(String yGrid, String xGrid) {    //이건 측정소 정보가져올 스레드

        GetStationListThread.active = true;
        GetStationListThread getstationthread = new GetStationListThread(false, yGrid, xGrid);        //스레드생성(UI 스레드사용시 system 뻗는다)
        getstationthread.start();    //스레드 시작

    }

    public static void NearStationThreadResponse(String[] sStation, String[] sAddr, String[] sTm) {    //측정소 정보를 가져온 결과
        where.setText(sStation[0]);
        totalcnt.setText("가까운 측정소 :" + sStation[0] + "\r\n측정소 주소 :" + sAddr[0] + "\r\n측정소까지 거리 :" + sTm[0] + "km");
        GetFindDustThread.active = false;
        GetFindDustThread.interrupted();
    }

    void getStation(String yGrid, String xGrid) {

        if (xGrid != null && yGrid != null) {
            GetTransCoord2Thread.active = true;
            GetTransCoord2Thread getCoordthread = new GetTransCoord2Thread(false, xGrid, yGrid, from, to);        //스레드생성(UI 스레드사용시 system 뻗는다)
            getCoordthread.start();    //스레드 시작
        } else {
            Toast.makeText(getApplication(), "좌표값 잘못 되었습니다.", Toast.LENGTH_SHORT).show();
        }

    }

    public static void TransCoordThreadResponse(String x, String y) {    //대기정보 가져온 결과값
        if (x.equals("NaN") || y.equals("NaN")) {
            totalcnt.setText("좌표값이 잘못 입력되었거나 해당값이 없습니다.");
        } else {
            //totalcnt.append("\r\n변환된 좌표값은 " + x + "," + y + "입니다.");
            getNearStation(y, x);
        }
        GetTransCoord2Thread.active = false;
    }

    static public String transGrade(String intGrade) {
        String trans = null;
        switch (intGrade) {
            case "1":
                trans = "좋음";
                break;
            case "2":
                trans = "보통";
                break;
            case "3":
                trans = "나쁨";
                break;
            case "4":
                trans = "매우나쁨";
                break;
            default:
                break;

        }
        return trans;
    }

    /**
     * 버튼에 대한 처리
     */

    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.getBtn:    //대기정보 가져오는 버튼
                String stationName;
                stationName = where.getText().toString();
                getFindDust(stationName);

                break;
            case R.id.getNearStation:
                try {
                    locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

                    double latitude;
                    double longitude;
                    if (locationManager != null) {

                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null)
                        {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            getStation(String.valueOf(latitude),String.valueOf(longitude));
                        }
                    }else{

                    }
                }catch(Exception e) {
                    Log.e("getNearStation",e.toString());

                }

                break;
            default:
                break;
        }
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {

        switch(parent.getId()){

            case R.id.sido:		//시도 변경 스피너
                getStationList(sidolist[position]);


                break;
            case R.id.station:	//측정소 변경 스피너
                try{
                    Log.e("station name", stationlist[position]);
                }catch (Exception e){
                    Log.e("exception",""+e);
                }

                where.setText(stationlist[position]);	//측정소이름을 바로 입력해 준다.

                break;


            default:
                break;

        }


    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

}




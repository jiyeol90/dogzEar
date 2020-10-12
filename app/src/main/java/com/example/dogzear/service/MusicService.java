package com.example.dogzear.service;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.dogzear.MainActivity;
import com.example.dogzear.R;


public class MusicService extends Service{
    //재생하기위한 객체
    MediaPlayer mediaPlayer;
    //현재 곡이 재생되어 있는지 상태값을 저장한다. -> 설정화면에서 띄어주어야할 버튼을 조절하기 위해
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    boolean isPlaying;

    public static final String NOTIFICATION_CHANNEL_ID = "101";


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();

        sharedPreferences = getSharedPreferences("setting", MODE_PRIVATE);
        isPlaying = sharedPreferences.getBoolean("isPlaying",false);
        editor = sharedPreferences.edit();
        //포그라운드로 재생하기 위해서는 알림을 반드시 띄어주어야 한다.
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        //노티피케이션을 누르면 이동할 액티비티 설정
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_LAUNCHER)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //

        //사용자가 자체적으로 호출할 수 없고 시스템에 맡긴다.
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,  PendingIntent.FLAG_UPDATE_CURRENT);

        //노티피케이션에 띄어줄 세부 정보사항
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.book)) //BitMap 이미지 요구
                .setContentTitle("집중모드 음악재생")
                .setContentText("Justice der")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent) // 사용자가 노티피케이션을 탭시 ResultActivity로 이동하도록 설정
                .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            builder.setSmallIcon(R.drawable.book); //mipmap 사용시 Oreo 이상에서 시스템 UI 에러남
            CharSequence channelName = "집중모드 음악";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, importance);


            // 노티피케이션 채널을 시스템에 등록
            assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);
            startForeground(101, builder.build());

            mediaPlayer = MediaPlayer.create(this, R.raw.justice_der);
            mediaPlayer.setLooping(false);
            mediaPlayer.start();
            editor.putBoolean("isPlaying", true);
            editor.commit();
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        showToast("앱이 종료 되었다." + rootIntent);
        stopSelf();
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sharedPreferences = getSharedPreferences("setting", MODE_PRIVATE);
        isPlaying = sharedPreferences.getBoolean("isPlaying",false);
        editor = sharedPreferences.edit();

        String state = intent.getStringExtra("state");
        if(state.equals("pause")) {
            mediaPlayer.pause();
        }else {
            mediaPlayer.start();
            editor.putBoolean("isPlaying", true);
        }
        editor.commit();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        sharedPreferences = getSharedPreferences("setting", MODE_PRIVATE);
        isPlaying = sharedPreferences.getBoolean("isPlaying",false);
        editor = sharedPreferences.edit();
        super.onDestroy();
        editor.putBoolean("isPlaying", false);
        editor.commit();
        mediaPlayer.stop();

    }

    private void showToast(String message){
        Toast toast=Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.show();
    }
}

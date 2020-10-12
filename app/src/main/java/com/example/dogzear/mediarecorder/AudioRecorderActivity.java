package com.example.dogzear.mediarecorder;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dogzear.R;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class AudioRecorderActivity extends AppCompatActivity {

    private static String TAG = "AudioRecorderActivity";
    TextView textView;
    ImageView start, stop, recordings, logo;
    EditText editTitle;
    Button submitBtn;
    MediaRecorder mediaRecorder;
    CountDownTimer countDownTimer;
    int second = -1, minute, hour;
    String filePath;
    String audioFile;
    ContentValues values;
    Animation rotateAnimation;
    public static final int PERMISSION_ALL = 0;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_recorder);

        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                Toast.makeText(AudioRecorderActivity.this, "권한 허가", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                Toast.makeText(AudioRecorderActivity.this, "권한 거부\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
            }

        };
        TedPermission.with(this)
                .setPermissionListener(permissionlistener)
                .setRationaleMessage("접근 권한이 필요해요")
                .setDeniedMessage("왜 거부하셨어요...\n하지만 [설정] > [권한] 에서 권한을 허용할 수 있어요.")
                .setPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO})
                .check();


        setAudioRecorder();
//        if (checkPermission()) {

//        }
    }

    public void setAudioRecorder() {
        logo = (ImageView) findViewById(R.id.record_logo);
        textView = (TextView) findViewById(R.id.text);
        start = (ImageView) findViewById(R.id.start);
        stop = (ImageView) findViewById(R.id.stop);
        recordings = (ImageView) findViewById(R.id.recordings);

        rotateAnimation = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.flow);
        logo.startAnimation(rotateAnimation);

        stop.setEnabled(false);
        stop.setBackgroundResource(R.drawable.normal_background);
        stop.setImageResource(R.drawable.noraml_stop);
        Recording();
        stopRecording();
        getRecordings();
    }

    public void Recording() {
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start.setEnabled(false);
                recordings.setEnabled(false);
                stop.setEnabled(true);
                stop.setBackgroundResource(R.drawable.round_shape);
                stop.setImageResource(R.drawable.ic_stop_black_35dp);
                recordings.setBackgroundResource(R.drawable.normal_background);
                recordings.setImageResource(R.drawable.normal_menu);

                try {
                    // Create folder to store recordingss
                    File myDirectory = new File(Environment.getExternalStorageDirectory(), "dogzEar");
                    if (!myDirectory.exists()) {
                        myDirectory.mkdirs();
                    }
                    SimpleDateFormat dateFormat = new SimpleDateFormat("mmddyyyyhhmmss");
                    String date = dateFormat.format(new Date());
                    audioFile = "REC" + date;
                    filePath = myDirectory.getAbsolutePath() + File.separator + audioFile;
                    startAudioRecorder();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                showTimer();
            }
        });
    }

    //start audio recorder
    public void startAudioRecorder() {
        try {
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setOutputFile(filePath);
            mediaRecorder.prepare();
            mediaRecorder.start();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    //stop audio recorder
    public void stopRecording() {
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //cancel count down timer
                countDownTimer.cancel();
                start.setEnabled(true);
                recordings.setEnabled(true);
                stop.setEnabled(false);
                stop.setBackgroundResource(R.drawable.normal_background);
                stop.setImageResource(R.drawable.noraml_stop);
                recordings.setBackgroundResource(R.drawable.round_shape);
                recordings.setImageResource(R.drawable.ic_menu_black_35dp);
                second = -1;
                minute = 0;
                hour = 0;
                textView.setText("00:00:00");

                if (mediaRecorder != null) {
                    try {
                        //stop mediaRecorder
                        mediaRecorder.stop();
                        mediaRecorder.reset();
                    }catch (IllegalStateException e) {
                        e.printStackTrace();
                    }
                }

                //creating content resolver and put the values
                 values = new ContentValues();
                values.put(MediaStore.Audio.Media.DATA, filePath);
                values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/3gpp");
                values.put(MediaStore.Audio.Media.TITLE, audioFile);
                //showFileName();
                Log.d(TAG,"values의 값 : "  + values.get(MediaStore.Audio.Media.TITLE).toString());
                String title = values.get(MediaStore.Audio.Media.TITLE).toString();

                AlertDialog.Builder builder = new AlertDialog.Builder(AudioRecorderActivity.this);
                View view = LayoutInflater.from(AudioRecorderActivity.this).inflate(R.layout.record_title_box, null, false);
                builder.setView(view);

                //Toast.makeText(getApplicationContext(),edittext.getText().toString() ,Toast.LENGTH_LONG).show();
                // values.put(MediaStore.Audio.Media.TITLE, edittext.getText().toString());
                final AlertDialog dialog = builder.create();
                editTitle = (EditText) view.findViewById(R.id.et_title);
                submitBtn = (Button) view.findViewById(R.id.submit_btn);
                editTitle.setText(title);
                submitBtn.setOnClickListener(new View.OnClickListener(){

                    @Override
                    public void onClick(View view) {
                        Toast.makeText(getApplicationContext(),editTitle.getText().toString() , Toast.LENGTH_LONG).show();
                        values.put(MediaStore.Audio.Media.TITLE, editTitle.getText().toString());
                        getContentResolver().insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);
                        dialog.dismiss();
                    }
                });
                dialog.show();

                //store audio recorder file in the external content uri

            }
        });
    }

    void showFileName() {
        final EditText editTitle = (EditText)findViewById(R.id.et_title);
        final Button submitBtn = (Button)findViewById(R.id.submit_btn);
        editTitle.setText(values.get(MediaStore.Audio.Media.TITLE).toString());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(AudioRecorderActivity.this).inflate(R.layout.edit_box, null, false);
        builder.setView(view);
        //Toast.makeText(getApplicationContext(),edittext.getText().toString() ,Toast.LENGTH_LONG).show();
        // values.put(MediaStore.Audio.Media.TITLE, edittext.getText().toString());
        final AlertDialog dialog = builder.create();
        submitBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(),editTitle.getText().toString() , Toast.LENGTH_LONG).show();
                values.put(MediaStore.Audio.Media.TITLE, editTitle.getText().toString());
                dialog.dismiss();
            }
        });

        dialog.show();

    }

    //lanuch RecordingsActivity
    public void getRecordings() {
        recordings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start.setEnabled(true);
                recordings.setEnabled(true);
                stop.setEnabled(false);
                stop.setBackgroundResource(R.drawable.normal_background);
                stop.setImageResource(R.drawable.noraml_stop);
                recordings.setBackgroundResource(R.drawable.round_shape);
                recordings.setImageResource(R.drawable.ic_menu_black_35dp);
                startActivity(new Intent(getApplicationContext(), RecordingsActivity.class));
            }
        });
    }

    //display recording time
    public void showTimer() {
        countDownTimer = new CountDownTimer(Long.MAX_VALUE, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                second++;
                textView.setText(recorderTime());
            }
            public void onFinish() {
            }
        };
        countDownTimer.start();
    }

    //recorder time
    public String recorderTime() {
        if (second == 60) {
            minute++;
            second = 0;
        }
        if (minute == 60) {
            hour++;
            minute = 0;
        }
        return String.format("%02d:%02d:%02d", hour, minute, second);
    }

    //runtime permission
//    public boolean checkPermission() {
//        int RECORD_AUDIO_PERMISSION = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
//        int WRITE_EXTERNAL_PERMISSION = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
//        ArrayList<String> PERMISSION_LIST =new ArrayList<>();
//        if((RECORD_AUDIO_PERMISSION != PackageManager.PERMISSION_GRANTED)) {
//            PERMISSION_LIST.add(Manifest.permission.RECORD_AUDIO);
//        }
//        if((WRITE_EXTERNAL_PERMISSION != PackageManager.PERMISSION_GRANTED)) {
//            PERMISSION_LIST.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
//        }
//        if(!PERMISSION_LIST.isEmpty()) {
//            ActivityCompat.requestPermissions(this, PERMISSION_LIST.toArray(new String[PERMISSION_LIST.size()]), PERMISSION_ALL);
//            return false;
//        }
//        return true;
//    }

//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        boolean record = false,storage =  false;
//        switch (requestCode) {
//            case  PERMISSION_ALL: {
//                if (grantResults.length > 0) {
//                    for (int i = 0; i < permissions.length; i++) {
//                        if (permissions[i].equals(Manifest.permission.RECORD_AUDIO)) {
//                            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
//                                record = true;
//                            } else {
//                                Toast.makeText(getApplicationContext(), "Please allow Microphone permission", Toast.LENGTH_LONG).show();
//                            }
//                        } else if (permissions[i].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//                            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
//                                storage = true;
//                            } else {
//                                Toast.makeText(getApplicationContext(), "Please allow Storage permission", Toast.LENGTH_LONG).show();
//                            }
//                        }
//                    }
//                }
//                if (record && storage) {
//                    setAudioRecorder();
//                }
//            }
//        }
//    }

    //release mediarecorder
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaRecorder != null) {
            mediaRecorder.release();
        }
    }
}
package com.example.dogzear.mediarecorder;

import android.content.ContentResolver;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dogzear.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class RecordingsActivity extends AppCompatActivity {

    ArrayList<ModelRecordings> audioArrayList;
    RecyclerView recyclerView;
    MediaPlayer mediaPlayer;
    double current_pos, total_duration;
    TextView current, total;
    ImageView prev, next, pause;
    SeekBar seekBar;
    int audio_index = 0;
    public static final int PERMISSION_READ = 0;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recordings);
      // if (checkPermission()) {
            getRecordings();
       // }
    }

    public void getRecordings() {
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        current = (TextView) findViewById(R.id.current);
        total = (TextView) findViewById(R.id.total);
        prev = (ImageView) findViewById(R.id.prev);
        next = (ImageView) findViewById(R.id.next);
        pause = (ImageView) findViewById(R.id.pause);
        seekBar = (SeekBar) findViewById(R.id.seekbar);


        audioArrayList = new ArrayList<>();
        mediaPlayer = new MediaPlayer();
        getAudioRecordings();
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                current_pos = seekBar.getProgress();
                mediaPlayer.seekTo((int) current_pos);
            }
        });

//        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//            @Override
//            public void onCompletion(MediaPlayer mp) {
//                audio_index++;
//                if (audio_index < (audioArrayList.size())) {
//                    playRecording(audio_index);
//                } else {
//                    audio_index = 0;
//                    playRecording(audio_index);
//                }
//
//            }
//        });

        if (!audioArrayList.isEmpty()) {
            //액티비티가 전환되자마자 바로 실행되는 것을 방지
            //playRecording(audio_index);
            //이전 트랙을 재생 (첫번째트랙이면 마지막 트랙을 재생)
            prevRecording();
            //다음 트랙을 재생 (마지막 트랙이면 첫번째 트랙을 재생)
            nextRecording();
            //재생 정지
            setPause();
        }
    }

    public void playRecording(int pos) {
        try  {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(this, audioArrayList.get(pos).getUri());
            mediaPlayer.prepare();
            mediaPlayer.start();
            pause.setImageResource(R.drawable.ic_pause_circle_filled_black_24dp);
            audio_index=pos;
        } catch (Exception e) {
            e.printStackTrace();
        }
        setAudioProgress();
    }

    public void setAudioProgress() {
        current_pos = mediaPlayer.getCurrentPosition();
        total_duration = mediaPlayer.getDuration();

        total.setText(timeConversion((long) total_duration));
        current.setText(timeConversion((long) current_pos));
        seekBar.setMax((int) total_duration);

        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    current_pos = mediaPlayer.getCurrentPosition();
                    current.setText(timeConversion((long) current_pos));
                    seekBar.setProgress((int) current_pos);
                    handler.postDelayed(this, 1000);
                } catch (IllegalStateException ed){
                    ed.printStackTrace();
                }
            }
        };
        handler.postDelayed(runnable, 1000);
    }

    public void prevRecording() {
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (audio_index > 0) {
                    audio_index--;
                    playRecording(audio_index);
                } else {
                    audio_index = audioArrayList.size() - 1;
                    playRecording(audio_index);
                }
            }
        });
    }

    public void nextRecording() {
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (audio_index < (audioArrayList.size()-1)) {
                    audio_index++;
                    playRecording(audio_index);
                } else {
                    audio_index = 0;
                    playRecording(audio_index);
                }
            }
        });
    }

    public void setPause() {
        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    pause.setImageResource(R.drawable.ic_play_circle_filled_black_24dp);
                } else {
                    mediaPlayer.start();
                    pause.setImageResource(R.drawable.ic_pause_circle_filled_black_24dp);
                }
            }
        });
    }

    //time conversion
    public String timeConversion(long value) {
        String audioTime;
        int dur = (int) value;
        int hrs = (dur / 3600000);
        int mns = (dur / 60000) % 60000;
        int scs = dur % 60000 / 1000;

        if (hrs > 0) {
            audioTime = String.format("%02d:%02d:%02d", hrs, mns, scs);
        } else {
            audioTime = String.format("%02d:%02d", mns, scs);
        }
        return audioTime;
    }

    public void getAudioRecordings() {
        //ContentResolver를 이용해 ContentProvider에 접근한 후 녹음파일에 접근하여 정보를 가져온다.
        ContentResolver contentResolver = getContentResolver();
        //creating content resolver and fetch audio files
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        //ContentResolver의 query() 함수를 이용해서 데이터를 조회한다.
        //1번째 인자 : uri -> content://scheme 방식의 원하는 데이터를 가져오기 위한 정해진 주소
        //2번째 인자 : projection -> 가져올 컬럼 이름 목록, null이면 모든 컬럼
        //3번째 인자 : selection -> where 절에 해당하는 내용
        //4번째 인자 : selectionArgs -> selection에서 ?로 표시한 곳에 들어갈 데이터
        //5번째 인자 : sortOrder -> 정렬을 위한 order by 구문
        Cursor cursor = contentResolver.query(uri, null, MediaStore.Audio.Media.DATA + " like ?", new String[]{"%dogzEar%"}, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {

                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                ModelRecordings modelRecordings  = new ModelRecordings();
                modelRecordings.setTitle(title);
                File file = new File(data);
                Date date = new Date(file.lastModified());
                SimpleDateFormat format = new SimpleDateFormat("MMMM dd, yyyy");

                modelRecordings.setDate(format.format(date));
                modelRecordings.setUri(Uri.parse(data));

                //fetch the audio duration using MediaMetadataRetriever class
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(data);

                modelRecordings.setDuration(timeConversion(Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION))));
                audioArrayList.add(modelRecordings);

            } while (cursor.moveToNext());
        }

        RecordingsAdapter adapter = new RecordingsAdapter(this, audioArrayList);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new RecordingsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int pos, View v) {
                playRecording(pos);
            }
        });

    }

//    public boolean checkPermission() {
//        int READ_EXTERNAL_PERMISSION = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
//        if ((READ_EXTERNAL_PERMISSION != PackageManager.PERMISSION_GRANTED)) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_READ);
//            return false;
//        }
//        return true;
//    }

//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        switch (requestCode) {
//            case  PERMISSION_READ: {
//                if (grantResults.length > 0 && permissions[0].equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
//                    if (grantResults.length > 0 && permissions[0].equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
//                        Toast.makeText(getApplicationContext(), "Please allow storage permission", Toast.LENGTH_LONG).show();
//                    } else {
//                        getAudioRecordings();
//                    }
//                }
//            }
//        }
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }
}
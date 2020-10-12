package com.example.dogzear;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.dogzear.lock.BaseActivity;
import com.example.dogzear.saveAndLoad.SaveAndLoad;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileModifyActivity extends BaseActivity implements View.OnClickListener{

    private final String TAG = getClass().getSimpleName();
    private CircleImageView profile;
    private EditText userId;
    private EditText userPw;
    private EditText userText;
    private Button cameraBtn;
    private Button modifyBtn;

    //카메라, 앨범 등의 기능을 사용하기 위해 상수들을 지정한다.
    private static final int REQUESET_TAKE_PHOTO = 2000;
    private static final int REQUEST_TAKE_ALBUM = 3000;

    private File tempFile;
    String currentPhotoPath;
    Uri imageURI;
    //photoURI는 사진을 찍고 저장한 파일을 크랍한 경우 사용
    //albumURI는 사진을 찍지 않고 앨범에서 가져온 파일을 크랍한 뒤 저장할 때 사용.
    Uri photoURI, albumURI;

    SharedPreferences userInfoDetail;
    SharedPreferences userInfo;

    String user_id = "";
    String user_pwd = "";
    String user_text = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_modify);

        //profile = (ImageView)findViewById(R.id.profile);
        profile = (CircleImageView)findViewById(R.id.profile);
        userId = (EditText)findViewById(R.id.et_user_id);
        userPw = (EditText)findViewById(R.id.et_user_pwd);
        userText = (EditText)findViewById(R.id.et_user_Text);
        modifyBtn = (Button)findViewById(R.id.profile_modify_btn);
        cameraBtn = (Button)findViewById(R.id.camera_btn);

        //cameraBtn.setOnClickListener(this);
        profile.setOnClickListener(this);
        modifyBtn.setOnClickListener(this);

        userInfoDetail = getSharedPreferences("user_info_detail",MODE_PRIVATE);
        userInfo = getSharedPreferences("user_info",MODE_PRIVATE);

        String profileImageStr = userInfo.getString("user_profile_img", "");
        if(!profileImageStr.equals("")) {
            Bitmap bitmap = SaveAndLoad.StringToBitMap(profileImageStr);
            profile.setImageBitmap(bitmap);
        }

        user_id = userInfo.getString("userId", "");
        user_pwd = userInfo.getString("userPwd", "");
        user_text = userInfo.getString("userText", "");

        userId.setText(user_id);
        userPw.setText(user_pwd);
        userText.setText(user_text);

        //user_info_detail 값 세팅 확인
//        Set<String> infos = userInfoDetail.getStringSet("infos", null);
//        Log.d(TAG, "값은 총 " + infos.size() + " 개 들어있다. ");
//        HashMap<String,String> convertInfos = new HashMap<>();
//
//
//        Log.d(TAG, "Environment.getExternalStorageDirectory(): " + Environment.getExternalStorageDirectory().toString());
//
//        int i = 0;
//        Iterator<String> iterator = infos.iterator();
//        while(iterator.hasNext()) {
//            String element = iterator.next();
//            String[] splitElement = element.split(":");
//            convertInfos.put(splitElement[0],splitElement[1]);
//            i++;
//            Log.d(TAG, i + " 번재 값 : " + splitElement[0] + " " + splitElement[1]);
//        }
//
//        Log.d(TAG, "hashmap에서 가져온 값   :    " + convertInfos.get("genre"));

    }

    @Override
    public void onClick(View view) {
        //프로필을 누르면 popupmenu가 나온다.
        if(view == profile) {
            PopupMenu pop = new PopupMenu(getApplicationContext(), view);
            getMenuInflater().inflate(R.menu.popup_menu, pop.getMenu());

            pop.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.camera://카메라 앱을 이용하기
                            takePhoto();
                            break;
                        case R.id.gallery://갤러리에서 가져오기
                            getAlbum();
                            break;
                    }
                    return true;
                }
            });
            pop.show();
        }else if(view == modifyBtn) {
            user_id = userId.getText().toString();
            user_pwd = userPw.getText().toString();
            user_text = userText.getText().toString();

            if(user_id.length() != 0 && user_pwd.length() != 0) {
                SharedPreferences.Editor editor = userInfo.edit();
                editor.putString("userId", user_id);
                editor.putString("userPwd", user_pwd);
                editor.putString("userText", user_text);
                editor.commit();

                finish();
            } else {
                Toast.makeText(this, "아이디와 비밀번호를 다시 입력하세요", Toast.LENGTH_LONG).show();
            }
//            String user_id = "";
//            String user_pwd = "";
//            String user_text = "";
//            userId = (EditText)findViewById(R.id.et_user_id);
//            userPw = (EditText)findViewById(R.id.et_user_pwd);
//            userText = (EditText)findViewById(R.id.et_user_Text);

        }
    }

    //popup되어 나타나는 메뉴를 만들어 준다.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.popup_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == 1) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /*
        앨범화면으로 이동 했지만 선택을 하지 않고 뒤로 간 경우 또는 카메라로 촬영한 후 저장하지 않고 뒤로 가기를 간 경우입니다.
        이는 resultCode 값을 통해 확인할 수 있습니다.이 경우 "취소 되었습니다.' 토스트를 보여줍니다.
        createImageFile() 을 통해 tempFile 을 생성했었는데 만약 사진 촬영 중 취소를 하게 되면 temFile 이 빈썸네일로 디바이스에 저장됩니다.
        따라서 예외 사항에서 tempFIle 이 존재하면 이를 삭제해 주는 작업이 필요합니다.
         */
        if (resultCode != Activity.RESULT_OK) {

            Toast.makeText(this, "취소 되었습니다.", Toast.LENGTH_SHORT).show();

            if(tempFile != null) {
                if (tempFile.exists()) {
                    if (tempFile.delete()) {
                        Log.e(TAG, tempFile.getAbsolutePath() + " 삭제 성공");
                        tempFile = null;
                    }
                }
            }
            return;
        }
        if(requestCode == REQUEST_TAKE_ALBUM) {
           sendPicture(data.getData());

        } else if(requestCode == REQUESET_TAKE_PHOTO) {
            setImage();
        }
    }

    private void sendPicture(Uri imgUri) {
        //리사이징을 하기위한 옵션
        BitmapFactory.Options imgOptions = new BitmapFactory.Options();
        imgOptions.inSampleSize = 10;
        String imagePath = getRealPathFromURI(imgUri);
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(imagePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        int exifDegree = exifOrientationToDegrees(exifOrientation);

        Bitmap bitmap = BitmapFactory.decodeFile(imagePath,imgOptions);//경로를 통해 비트맵으로 전환
        bitmap = rotate(bitmap, exifDegree);

        String bitmapString = SaveAndLoad.BitMapToString(bitmap);
        Log.d(TAG, "비트맵을 String 으로 바꿨을때 :       " + bitmapString);
        SharedPreferences.Editor editor = userInfo.edit();
        editor.putString("user_profile_img", bitmapString);
        editor.commit();
        profile.setImageBitmap(bitmap);//이미지 뷰에 비트맵 넣기
    }

    private Bitmap rotate(Bitmap src, float degree) {

        // Matrix 객체 생성
        Matrix matrix = new Matrix();
        // 회전 각도 셋팅
        matrix.postRotate(degree);
        // 이미지와 Matrix 를 셋팅해서 Bitmap 객체 생성
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
    }

    private int exifOrientationToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

    //사진의 절대경로를 구한다.
    private String getRealPathFromURI(Uri contentUri) {
        int column_index = 0;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if(cursor.moveToFirst()){
            column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        }
        return cursor.getString(column_index);
    }

    private void setImage() {
        BitmapFactory.Options imgOptions = new BitmapFactory.Options();
        imgOptions.inSampleSize = 10;
        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, imgOptions);
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(currentPhotoPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int exifOrientation;
        int exifDegree;

        if (exif != null) {
            exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            exifDegree = exifOrientationToDegrees(exifOrientation);
        } else {
            exifDegree = 0;
        }
        //제대로 회전시킨 이미지를 만든다.
        bitmap = rotate(bitmap, exifDegree);
        String bitmapString = SaveAndLoad.BitMapToString(bitmap);

        SharedPreferences.Editor editor = userInfo.edit();
        editor.putString("user_profile_img", bitmapString);
        editor.commit();
        // 이미지 뷰에 비트맵을 set하여 이미지 표현
        profile.setImageBitmap(bitmap);

    }

    private void takePhoto() {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            }catch (IOException e) {
                Toast.makeText(getApplicationContext(), "createImageFile Failed", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            if(photoFile != null) {
                photoURI = FileProvider.getUriForFile(getApplicationContext(), getPackageName(), photoFile);// Uri.fromFile(photoFile);//Todo 차이점 정리할것
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUESET_TAKE_PHOTO);
            }
        }

    }


    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "TEST_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    //앨범 에서 이미지를 가져오는 함수
    private void getAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, REQUEST_TAKE_ALBUM);
    }

}




















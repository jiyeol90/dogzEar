package com.example.dogzear;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.airbnb.lottie.LottieAnimationView;
import com.example.dogzear.lock.BaseActivity;
import com.example.dogzear.lottie.LottieAddBookDialogFragment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AddBookActivity extends BaseActivity implements View.OnClickListener, DatePickerDialog.OnDateSetListener {

    private static final String TAG = "AddBookActivity";
    private final int GET_GALLERY_IMAGE = 200;
    private final int REQUEST_IMAGE_CAPTURE = 300;

    private String imageFilePath;
    private Uri photoUri;
    private final String BOOK_TITLE = "BOOK_TITLE";
    /*카메라 앱으로 사진을 찍었을 경우 true */
    private boolean isCaptured = false;
    /*갤러리에서 가져와 나타낼 이미지의 크기*/
    int reqHeight;
    int reqWidth;

    private Handler mhandler;

    ImageView bookImage;
    Button galleryImageBtn;
    Button cameraBtn;
    Button cancelBtn;
    Button bookUpdateBtn;


    EditText bookTitle;
    EditText bookAuthor;
    EditText bookDate;
    EditText totalPage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);

        //권한요청 시점을 첫 시작페이지로 바꿔준다.-> 실제 앱을 생각해서 적용
//        TedPermission.with(getApplicationContext())
//                .setPermissionListener(permissionListener)
//                .setRationaleMessage("카메라 권한이 필요합니다.")
//                .setDeniedMessage("거부하셨습니다.")
//                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
//                .check();

        bookImage = (ImageView) findViewById(R.id.search_bar);
        cameraBtn = (Button) findViewById(R.id.camera_btn);
        cancelBtn = (Button) findViewById(R.id.cancel_btn);
        bookUpdateBtn = (Button) findViewById(R.id.book_update_btn);
        galleryImageBtn = (Button) findViewById(R.id.complete_btn);
        bookTitle = (EditText) findViewById(R.id.et_title);
        bookAuthor = (EditText) findViewById(R.id.et_author);
        bookDate = (EditText) findViewById(R.id.et_date);
        totalPage = (EditText) findViewById(R.id.et_total_page);

        bookImage.setOnClickListener(this);
        cameraBtn.setOnClickListener(this);
        galleryImageBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);
        bookUpdateBtn.setOnClickListener(this);
        bookDate.setOnClickListener(this);


        /*임시저장 값들(제목, 작가, 날짜)을 뿌려준다*/
        Intent intent = getIntent();
        String getTitle = intent.getStringExtra("bookTitle");
        Log.d(TAG, "bookTitle : " + getTitle);
        bookTitle.setText(getTitle);
        String getAuthor = intent.getStringExtra("bookAuthor");
        bookAuthor.setText(getAuthor);
        String getBookDate = intent.getStringExtra("bookDate");
        bookDate.setText(getBookDate);
        String getTotalPage = intent.getStringExtra("bookPage");
        totalPage.setText(getTotalPage);

    }

    @Override
    public void onClick(View view) {
        /*카메라 앱의 액티비티 호출*/
        if (view == cameraBtn) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(getPackageManager()) != null) {
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "createImageFile Failed", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                if (photoFile != null) {
                    //authorities에는 프로젝트의 패키지명을 작성함으로써 상단 'FileProvider.getUriForFile()'가 정상작동합니다.
                    photoUri = FileProvider.getUriForFile(getApplicationContext(), getPackageName(), photoFile);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                }
            }
            /*갤러리에서 이미지 가져오기*/
        } else if (view == galleryImageBtn) {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
            startActivityForResult(intent, GET_GALLERY_IMAGE);

        } else if (view == cancelBtn) {
            Log.d(TAG, "취소버튼을 누르면 바로 취소");
            Intent intent = new Intent();
            intent.putExtra("RESULT", "CANCEL");
            setResult(RESULT_OK, intent);
            finish();
        } else if (view == bookUpdateBtn) {
            Log.d(TAG, "등록 버튼을 눌렀다. ==================================bookUpdateBtn");
            System.out.println("등록 버튼을 눌렀다. ==================================bookUpdateBtn");
            Intent intent = new Intent();
            intent.putExtra("RESULT", "ADD");
            /*저장할 데이터를 담는다.*/
            //bookImage = (ImageView) findViewById(R.id.book_image);
            //bookTitle = (EditText) findViewById(R.id.et_title);
            //bookAuthor = (EditText) findViewById(R.id.et_author);
            //bookDate = (EditText) findViewById(R.id.et_date);
            String title = bookTitle.getText().toString();
            String author = bookAuthor.getText().toString();
            String startDate = bookDate.getText().toString();
            String page = totalPage.getText().toString();
            //todo 페이지값을 입력하지 않을경우 100으로 값을 넣어준다.-> 추후에 값을 입력하지 않을시 입력을 강제하도록 고칠것
            if(page.equals("")) {
                page = "100";
            }

            //BitmapDrawable drawable = (BitmapDrawable)bookImage.getDrawable();
            //Bitmap bitmap = drawable.getBitmap();
            //Drawable bookCover = bookImage.getDrawable();

                if(isCaptured) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    Bitmap bitmap = ((BitmapDrawable) bookImage.getDrawable()).getBitmap();
//                BitmapDrawable drawable = (BitmapDrawable)bookImage.getDrawable();
//                Bitmap bitmap = drawable.getBitmap();
                    System.out.println("bitmap.getByteCount() : " + bitmap.getByteCount() + " 을 저장합니다");
                    Log.d(TAG, "bitmap.getByteCount() : " + bitmap.getByteCount() + " 을 저장합니다");
//            float scale = (float) (1024/(float)bitmap.getWidth());
//            int image_w = (int) (bitmap.getWidth() * scale);
//            int image_h = (int) (bitmap.getHeight() * scale);
//            Bitmap resize = Bitmap.createScaledBitmap(bitmap, image_w, image_h, true);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    byte[] byteArray = stream.toByteArray();
                    intent.putExtra("image", byteArray);
                }


            intent.putExtra("title", title);
            intent.putExtra("author", author);
            intent.putExtra("startDate", startDate);
            intent.putExtra("page", page);

            //intent.putExtra("image", bitmap);
            setResult(RESULT_OK, intent);
            //책 등록을 버튼을 누르면 로티 로딩화면을 띄어준다.
            showProgressDialog();
            mhandler = new Handler(Looper.getMainLooper());
            new AddBookLoadingThread().start();
            //finish();
        } else if (view == bookDate) {
            DatePickerDialog datePicker = new DatePickerDialog(this, this, 2020, 9, 1);
            datePicker.show();
        }

    }

    //로딩 애니메이션 화면을 보여주기위해 finish()의 호출을 Thread.sleep()의 시간만큼 지연시킨다.
    class AddBookLoadingThread extends Thread {
        Handler handler = mhandler ;

        @Override
        public void run() {
                // create Runnable instance.
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                };
            try {
                Thread.sleep(2000);
                // send runnable object.
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            handler.post(runnable) ;
        }

    }
//    private boolean hasImage(@NonNull ImageView view) {
//        Drawable drawable = view.getDrawable();
//        boolean hasImage = (drawable != null);
//
//        if (hasImage && (drawable instanceof BitmapDrawable)) {
//            hasImage = ((BitmapDrawable)drawable).getBitmap() != null;
//        }
//
//        return hasImage;
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /*카메라 앱의 결과*/
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            isCaptured = true;
            BitmapFactory.Options imgOptions = new BitmapFactory.Options();
            /*options.inJustDecodeBounds를 true로 설정하면 이미지를 decodeing할때 이미지의 크기만을 먼저 불러와
             *OutofMemory Exception을 일으킬만한 큰 이미지를 불러오더라도 선처리를 가능하도록 해준다.
             */
            //imgOptions.inJustDecodeBounds = true;
            imgOptions.inSampleSize = 10;
            Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath, imgOptions);
            ExifInterface exif = null;

            try {
                exif = new ExifInterface(imageFilePath);
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

            // 이미지 뷰에 비트맵을 set하여 이미지 표현
            bookImage.setImageBitmap(rotate(bitmap, exifDegree));
            /*갤러리 앱의 결과*/
        } else if (requestCode == GET_GALLERY_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            isCaptured = true;
            //사진의 절대경로를 구한다. -> 제대로 회전을 시킨다. -> 용량을 줄인다.
            BitmapFactory.Options imgOptions = new BitmapFactory.Options();
            //Todo 이미지 크기에 따라 동적으로 변경할것.
            imgOptions.inSampleSize = 10;
            String imagePath = getRealPathFromURI(data.getData());
            ExifInterface exif = null;
            try {
                exif = new ExifInterface(imagePath);
                int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                int exifDegree = exifOrientationToDegrees(exifOrientation);

                Bitmap img = BitmapFactory.decodeFile(imagePath,imgOptions);//경로를 통해 비트맵으로 전환

                //InputStream in = getContentResolver().openInputStream(data.getData());
                //Bitmap img = BitmapFactory.decodeStream(in);
                //Bitmap resized = Bitmap.createScaledBitmap(img, 350, 250, true);

                //in.close();
                bookImage.setImageBitmap(rotate(img, exifDegree));

            }catch (Exception e) {
                e.printStackTrace();

//                BitmapFactory.Options imgOptions = new BitmapFactory.Options();
//                /*options.inJustDecodeBounds를 true로 설정하면 이미지를 decodeing할때 이미지의 크기만을 먼저 불러와
//                 *OutofMemory Exception을 일으킬만한 큰 이미지를 불러오더라도 선처리를 가능하도록 해준다.
//                 */
//                //imgOptions.inJustDecodeBounds = true;
//                imgOptions.inSampleSize = 10;
//                Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath, imgOptions);
            }
//            final int height = imgOptions.outHeight;
//            final int width = imgOptions.outWidth;
//            reqHeight = 150;
//            reqWidth = 150;
//            int inSampleSize = 1;
//            if(height < reqHeight || width > reqWidth) {
//                final int heightRatio = Math.round((float) height / (float) reqHeight);
//                final int widthRatio = Math.round((float) height / (float) reqWidth);
//                inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
//            }


        }
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

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
            EditText bookTitle = findViewById(R.id.et_title);
            String title = bookTitle.getText().toString();
            outState.putCharSequence(BOOK_TITLE, title);
            Log.d(TAG, "종료전에 제목: " + title +" 을 저장합니다");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "AddBook액티비티가 일시정지됩니다. onPause()");
        /*back버튼을 눌렀는지 확인*/
//        if(isClicked) {
//            EditText bookTitle = findViewById(R.id.et_title);
//            String title = bookTitle.getText().toString();
//
//            Intent intent = new Intent();
//            intent.putExtra("back_button", "백버튼 눌렀다.");
//            if (title.length() != 0) {
//                intent.putExtra("title", title);
//                Log.d(TAG, "AddBook액티비티가 BACKBUTTON을 눌러일지정지되었고  BOOK_TITLE 은 " + title + " 입니다.");
//            } else {
//                Log.d(TAG, "AddBook액티비티가 BACKBUTTON을 눌러일지정지되었고 제목은 없다. onPause()");
//            }
//            setResult(RESULT_OK, intent);
//            finish();
//        } else {
//            Log.d(TAG, "AddBook액티비티가 일지정지됩니다. onPause()");
//        }
//////////////////////////////////////////////////////////////////////////

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "AddBook액티비티가 정지됩니다. onStop()");
    }

    /*BackButton을 누를시*/
    @Override
    public void onBackPressed() {
        //ToDo 이곳의 기능을 가능할 수 있게 하기.
        //부모클래스에서 백버튼 처리를 하기 전에 처리해줄 것들
        //isClicked = true;
//        Log.d(TAG, "백버튼을 눌렀다. onBackPressed() , isClicked : ");
//            EditText bookTitle = findViewById(R.id.et_title);
//            String title = bookTitle.getText().toString();
//            String author = bookAuthor.getText().toString();
//            String date = bookDate.getText().toString();
//
//            if(title.length() == 0) title = "";
//            if(author.length() == 0) author = "";
//            if(date.length() == 0) date = "";
//
//            Intent intent = new Intent();
//            intent.putExtra("back_button", true);
//
//            intent.putExtra("title", title);
//            intent.putExtra("author", author);
//            intent.putExtra("date", date);
//            Log.d(TAG, "AddBook액티비티가 BACKBUTTON을 눌러일지정지되었고  BOOK_TITLE 은 " + title + " 입니다.");
//
//            setResult(RESULT_OK, intent);
//            finish();
//            super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "AddBook액티비티가 소멸됩니다. onDestroy()");
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        EditText bookTitle = findViewById(R.id.et_title);
        CharSequence title = savedInstanceState.getCharSequence(BOOK_TITLE);
        bookTitle.setText(title);
        Log.d(TAG, "다시시작할때 제목: " + title + "을 저장합니다");
    }

//    PermissionListener permissionListener = new PermissionListener() {
//        @Override
//        public void onPermissionGranted() {
//            Toast.makeText(getApplicationContext(), "권한이 허용됨",Toast.LENGTH_SHORT).show();
//        }
//
//        @Override
//        public void onPermissionDenied(ArrayList<String> deniedPermissions) {
//            Toast.makeText(getApplicationContext(), "권한이 거부됨",Toast.LENGTH_SHORT).show();
//        }
//    };

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "TEST_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        imageFilePath = image.getAbsolutePath();
        return image;
    }

    /*사진의 각도를 계산하여 바르게 맞춰준다.*/
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

    private Bitmap rotate(Bitmap bitmap, float degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        bookDate.setText(year+"/"+(month+1)+"/"+dayOfMonth);
    }

    //책 추가 Lottie애니메이션을 호출한다.
    private void showProgressDialog(){
        new LottieAddBookDialogFragment().newInstance().
                show(getSupportFragmentManager(),"");
    }
}

package com.example.dogzear;

import android.animation.Animator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;

public class SplashActivity extends AppCompatActivity {
    ImageView splashLogo;
    TextView splashBrand;
    Animation rotateAnimation;
    Animation scaleAnimation;

    /*
     *  이 액티비티는 앱을 처음 실행할때 보여주는 splash화면이다. 기존에 사용한 안드로이드에서 제공하는 애니메이션 에서
     *  Lottie 애니메이션을 적용하였다.
     *
     * 1. 화면이 시작되면 애니메이션을 실행한다. -> xml 파일에서 autoPlay 속성을 true로 설정
     * 2. 애니메이션이 종료되고 로그인 화면으로 넘어간다.
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.splash);
//
//        rotateAnimation = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate);
//        scaleAnimation = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.scale);
//
//        splashLogo = (ImageView)findViewById(R.id.splash_logo);
//        splashBrand = (TextView)findViewById(R.id.splash_brand);
//
//        splashLogo.startAnimation(rotateAnimation);
//        splashBrand.startAnimation(scaleAnimation);

        setContentView(R.layout.start_loading);
        final LottieAnimationView animationView = (LottieAnimationView)findViewById(R.id.start_loading);
        animationView.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                //애니메이션이 종료되고 로그인 화면으로 넘어간다.
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        //시간간격(개발자가 설정 가능)을 두고 실행시키고 싶을때, 딜레이를 주고 싶을때 Handler의 postDelayed를 사용하여 아주 쉽게 적용 시킬 수 있다
        //startLoading();
    }

//    private void startLoading() {
//        Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//
//            @Override
//            public void run() {
//
//                Intent intent = new Intent(getBaseContext(), LoginActivity.class);
//                startActivity(intent);
//                finish();
//            }
//        }, 3000);
//    }
}

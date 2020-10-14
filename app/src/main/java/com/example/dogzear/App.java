package com.example.dogzear;

import android.app.Application;
import android.content.Context;

import com.example.dogzear.lock.LockManager;
import com.kakao.auth.ApprovalType;
import com.kakao.auth.AuthType;
import com.kakao.auth.IApplicationConfig;
import com.kakao.auth.ISessionConfig;
import com.kakao.auth.KakaoAdapter;
import com.kakao.auth.KakaoSDK;

/*
 * 카카오 로그인 API를 정상적으로 사용하려면 Application을 상속받은 class를 하나 만들어야 합니다.
 * 현재 기존의 잠금화면설정을 위해 이미 상속을 받은 클래스 이므로 기존의 코드와 합니다.
 * 대부분의 코드는 카카오 로그인과 관련있는 함수이다.
 * 1. 카카오 로그인 관련 기능
 * 2. 잠금화면 기능
 */
public class App extends Application {

    private static App instance;

    public static App getGlobalApplicationContext() {
        if (instance == null) {
            throw new IllegalStateException("This Application does not inherit com.kakao.GlobalApplication");
        }

        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        //Manifest.xml에서 설정해준다. 그래야 enableAppLock을 통해 사용 가능하다.
    /*
     <application
        android:name=".App"...
     */
        //2. 잠금화면 기능
        LockManager.getInstance().enableAppLock(this); //
        KakaoSDK.init(new KakaoSDKAdapter());
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        instance = null;
    }

    public class KakaoSDKAdapter extends KakaoAdapter {

        @Override
        public ISessionConfig getSessionConfig() {
            return new ISessionConfig() {
                @Override
                public AuthType[] getAuthTypes() {
                    return new AuthType[] {AuthType.KAKAO_LOGIN_ALL};
                }

                @Override
                public boolean isUsingWebviewTimer() {
                    return false;
                }

                @Override
                public boolean isSecureMode() {
                    return false;
                }

                @Override
                public ApprovalType getApprovalType() {
                    return ApprovalType.INDIVIDUAL;
                }

                @Override
                public boolean isSaveFormData() {
                    return true;
                }
            };
        }

        // Application이 가지고 있는 정보를 얻기 위한 인터페이스
        @Override
        public IApplicationConfig getApplicationConfig() {
            return new IApplicationConfig() {
                @Override
                public Context getApplicationContext() {
                    return App.getGlobalApplicationContext();
                }
            };
        }
    }
}
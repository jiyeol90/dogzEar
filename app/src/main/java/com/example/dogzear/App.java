package com.example.dogzear;

import com.example.dogzear.lock.LockManager;
import android.app.Application;

public class App extends Application {

    //Manifest.xml에서 설정해준다. 그래야 enableAppLock을 통해 사용 가능하다.
    /*
     <application
        android:name=".App"...
     */
    @Override
    public void onCreate() {
        super.onCreate();
        LockManager.getInstance().enableAppLock(this);
    }

}

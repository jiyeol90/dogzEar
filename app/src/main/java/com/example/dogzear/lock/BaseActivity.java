package com.example.dogzear.lock;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {
	//기존의 코드는 Activity를 상속받지만 MainActivity에서 toolbar를 사용할때
    //setSupportActionBar(toolbar);는 AppCompatActivity에 속한 메서드이기때문에 수정해 준다.
	//내가 잠금 설정을 해주어야 할 모든 액티비티는 BaseActivity 를 상속받도록 수정해준다.
	private static PageListener pageListener;

	public static void setListener(PageListener listener) {
		pageListener = listener;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (pageListener != null) {
			pageListener.onActivityCreated(this);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();

		if (pageListener != null) {
			pageListener.onActivityStarted(this);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (pageListener != null) {
			pageListener.onActivityResumed(this);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (pageListener != null) {
			pageListener.onActivityPaused(this);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();

		if (pageListener != null) {
			pageListener.onActivityStopped(this);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (pageListener != null) {
			pageListener.onActivityDestroyed(this);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (pageListener != null) {
			pageListener.onActivitySaveInstanceState(this);
		}
	}

}

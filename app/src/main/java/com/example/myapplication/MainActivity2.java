package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity2 extends AppCompatActivity {

    private static final String TAG = "MainActivity2LifeCycle";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_2);
        Log.d(TAG, "on Create rồi nèeeeeee");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "on Start rồi nèeeeeee");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "on Resume rồi nèeeeeee");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "on Pause rồi nèeeeeee");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "on Stop rồi nèeeeeee");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "on Destroy rồi nèeeeeee");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "on Restart rồi nèeeeeee");
    }
}
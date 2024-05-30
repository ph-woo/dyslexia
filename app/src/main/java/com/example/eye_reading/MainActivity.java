package com.example.eye_reading;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SplashScreen.installSplashScreen(this);




        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        boolean isRegistered = sharedPreferences.getBoolean("isRegistered", false);

        if (isRegistered) {
            // HomeActivity로 이동
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(intent);
        } else {
            // IdcreateActivity로 이동
            Intent intent = new Intent(MainActivity.this, IdcreateActivity.class);
            startActivity(intent);
       }
        finish();
    }
}

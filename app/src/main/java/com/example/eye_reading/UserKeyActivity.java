package com.example.eye_reading;

import android.content.Intent;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;


public class UserKeyActivity extends AppCompatActivity {
    protected String getUserId() {
        SharedPreferences sharedPreferences = getSharedPreferences("login_state", MODE_PRIVATE);
        return sharedPreferences.getString("USERKEY", null);
    }

    protected String getUserNickname() {
        SharedPreferences sharedPreferences = getSharedPreferences("login_state", MODE_PRIVATE);
        return sharedPreferences.getString("USERNAME", null);
    }
}

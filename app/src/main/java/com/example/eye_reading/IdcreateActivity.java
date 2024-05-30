package com.example.eye_reading;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.splashscreen.SplashScreen;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class IdcreateActivity extends AppCompatActivity {

    String userKey;
    private EditText etUsername, etUserId;
    private Button btnRegister;
    private DatabaseReference databaseUsers;

    String username;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SplashScreen.installSplashScreen(this);
        setContentView(R.layout.activity_idcreate);

        etUsername = findViewById(R.id.etUsername);
        etUserId = findViewById(R.id.etUserId);
        btnRegister = findViewById(R.id.btnRegister);

        databaseUsers = FirebaseDatabase.getInstance("https://song-62299-default-rtdb.firebaseio.com/").getReference("Users");

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    private void registerUser() {
        username = etUsername.getText().toString().trim();
        userId = etUserId.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(userId)) {
            Toast.makeText(this, "Please enter a user ID", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int userCount = (int) dataSnapshot.getChildrenCount();
                userKey = "user" + (userCount + 1);

                databaseUsers.child(userKey).child("username").setValue(username);
                databaseUsers.child(userKey).child("userid").setValue(userId);

                // Create an array with 5 zeros
                List<Integer> zeros = new ArrayList<>();
                for (int i = 0; i < 5; i++) {
                    zeros.add(0);
                }

                List<Integer> anotherzeros = new ArrayList<>();
                anotherzeros.add(1);
                    anotherzeros.add(0);


                databaseUsers.child(userKey).child("gameprocessivity").child("game1").setValue(zeros);
                databaseUsers.child(userKey).child("gameprocessivity").child("game2").setValue(zeros);
                databaseUsers.child(userKey).child("gameprocessivity").child("game3").setValue(anotherzeros);
                databaseUsers.child(userKey).child("bookmarkcount").setValue(0);

                // SharedPreferences에 회원가입 정보 저장
                SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("isRegistered", true);
                editor.putString("username", username);
                editor.putString("userkey", userKey);
                editor.apply();

                Intent gameIntent = new Intent(IdcreateActivity.this, HomeActivity.class);
                gameIntent.putExtra("USERNAME", username);
                gameIntent.putExtra("USERKEY", userKey);
                startActivity(gameIntent);
                finish();

                Toast.makeText(IdcreateActivity.this, "User registered successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(IdcreateActivity.this, "Failed to register user", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

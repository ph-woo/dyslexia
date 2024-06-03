package com.example.eye_reading;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserActivity extends AppCompatActivity {
    private DatabaseReference databaseReference;
    String nickname = "";
    String userkey = "";
    private TextView bookmarkCountText;
    private TextView nicknameText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SplashScreen.installSplashScreen(this);
        setContentView(R.layout.activity_user);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }

        ImageView navHome = findViewById(R.id.nav_home);
        ImageView navGame = findViewById(R.id.nav_game);
        ImageView navUser = findViewById(R.id.nav_user);

        databaseReference = FirebaseDatabase.getInstance("https://song-62299-default-rtdb.firebaseio.com/").getReference();

        navGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gameIntent = new Intent(UserActivity.this, GameActivity.class);
                gameIntent.putExtra("USERNAME", nickname);
                gameIntent.putExtra("USERKEY", userkey);
                startActivity(gameIntent);
            }
        });

        navHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent homeIntent = new Intent(UserActivity.this, HomeActivity.class);
                startActivity(homeIntent);
            }
        });
    }
}
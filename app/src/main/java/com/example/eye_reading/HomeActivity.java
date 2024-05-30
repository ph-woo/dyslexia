package com.example.eye_reading;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.splashscreen.SplashScreen;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SplashScreen.installSplashScreen(this);
        setContentView(R.layout.activity_home);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }

        String nickname = "김아이";
        int bookmarkCount = 23;

        Button BtnShop = findViewById(R.id.btn_shop);
        TextView nicknameText = findViewById(R.id.nickname);
        TextView bookmarkCountText = findViewById(R.id.bookmark_count);

        nicknameText.setText(nickname);
        bookmarkCountText.setText(String.valueOf(bookmarkCount));

        ImageView navHome = findViewById(R.id.nav_home);
        ImageView navGame = findViewById(R.id.nav_game);
        ImageView navUser = findViewById(R.id.nav_user);

        BtnShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shopIntent = new Intent(HomeActivity.this, ShopActivity.class);
                startActivity(shopIntent);
            }
        });

        navGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gameIntent = new Intent(HomeActivity.this, GameActivity.class);
                startActivity(gameIntent);
            }
        });

        navUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(HomeActivity.this, "계정 클릭됨", Toast.LENGTH_SHORT).show();
                // 계정 화면으로 이동
            }
        });
    }
}

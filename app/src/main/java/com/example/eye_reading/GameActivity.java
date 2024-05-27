package com.example.eye_reading;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class GameActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }

        Button btnBubbleGame = findViewById(R.id.btn_bubble_game);
        Button btnDeliveryGame = findViewById(R.id.btn_delivery_game);
        Button btnSongGame = findViewById(R.id.btn_song_game);

        ImageView navHome = findViewById(R.id.nav_home);
        ImageView navGame = findViewById(R.id.nav_game);
        ImageView navUser = findViewById(R.id.nav_user);

        btnBubbleGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent bubbleIntent = new Intent(GameActivity.this, BubbleActivity.class);
                startActivity(bubbleIntent);
            }
        });

        btnDeliveryGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent deliveryIntent = new Intent(GameActivity.this, DeliveryActivity.class);
                startActivity(deliveryIntent);
            }
        });

        btnSongGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent songIntent = new Intent(GameActivity.this, SongActivity.class);
                startActivity(songIntent);
            }
        });

        navHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent homeIntent = new Intent(GameActivity.this, HomeActivity.class);
                startActivity(homeIntent);
            }
        });
    }
}

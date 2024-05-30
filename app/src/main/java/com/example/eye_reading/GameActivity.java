package com.example.eye_reading;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class GameActivity extends AppCompatActivity {

    String nickname="";
    String userkey="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);


        Intent gameIntent = getIntent();
        if (gameIntent != null && gameIntent.hasExtra("USERNAME")) {
            nickname= gameIntent.getStringExtra("USERNAME");

            Log.d("HomeAct", "Received nickname: " + nickname);
        } else {
            Log.e("HomeAct", "No nickname provided");
        }

        if (gameIntent != null && gameIntent.hasExtra("USERKEY")) {
           userkey= gameIntent.getStringExtra("USERKEY");

            Log.d("HomeAct", "Received userkey: " + userkey);
        } else {
            Log.e("HomeAct", "No userkey provided");
        }



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
                bubbleIntent.putExtra("USERNAME", nickname);
                bubbleIntent.putExtra("USERKEY", userkey);
                startActivity(bubbleIntent);
            }
        });

        btnDeliveryGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent deliveryIntent = new Intent(GameActivity.this, DeliveryActivity.class);
                deliveryIntent.putExtra("USERNAME", nickname);
                deliveryIntent.putExtra("USERKEY", userkey);
                startActivity(deliveryIntent);
            }
        });

        btnSongGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent songIntent = new Intent(GameActivity.this, SongActivity.class);
                songIntent.putExtra("USERNAME", nickname);
                songIntent.putExtra("USERKEY", userkey);
                startActivity(songIntent);
            }
        });

        navHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent homeIntent = new Intent(GameActivity.this, HomeActivity.class);
                homeIntent.putExtra("USERNAME", nickname);
               homeIntent.putExtra("USERKEY", userkey);
                startActivity(homeIntent);
            }
        });
    }
}

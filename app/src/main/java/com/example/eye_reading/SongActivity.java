package com.example.eye_reading;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class SongActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }

        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> onBackPressed());

        ImageView navHome = findViewById(R.id.nav_home);
        ImageView navGame = findViewById(R.id.nav_game);
        ImageView navUser = findViewById(R.id.nav_user);

        List<Song> songList = new ArrayList<>();
        songList.add(new Song("학교종이 땡땡땡", 1));
        songList.add(new Song("곰 세 마리", 1));
        songList.add(new Song("아기 상어", 0));
        songList.add(new Song("떴다떴다 비행기", -1));
        songList.add(new Song("다음 노래", -1));
        songList.add(new Song("다다음 노래", -1));
        songList.add(new Song("다다다음 노래", -1));

        LinearLayout songListContainer = findViewById(R.id.song_list);

        // 노래 목록 기반으로 버튼 생성
        for (int i = 0; i < songList.size(); i++) {
            Song song = songList.get(i);
            Button songButton = new Button(this);
            songButton.setText(song.getTitle());
            songButton.setTextSize(40);
            songButton.setPadding(10, 10, 10, 10);
            songButton.setTextColor(getResources().getColor(R.color.brown));

            // 클리어 여부에 따라 버튼 배경색 설정
            if (song.isCleared() == 1) {
                songButton.setBackgroundResource(R.drawable.btn_song_cleared);
            } else if (song.isCleared() == 0) {
                songButton.setBackgroundResource(R.drawable.btn_song);
            } else {
                songButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.lock_resize, 0, 0, 0);
                songButton.setCompoundDrawablePadding(20);
                songButton.setBackgroundResource(R.drawable.btn_song_locked);
            }

            // 버튼 ID 설정
            int buttonId = getResources().getIdentifier("btn_song" + (i + 1), "id", getPackageName());
            songButton.setId(buttonId);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 200
            );

            if (i != 0) {
                params.setMargins(0, 100, 0, 0);
            }

            songButton.setLayoutParams(params);

            // 버튼 클릭 이벤트 설정
            songButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(SongActivity.this, EyeTracking.class);
                    startActivity(intent);
                }
            });

            songListContainer.addView(songButton);
        }

        navHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent homeIntent = new Intent(SongActivity.this, HomeActivity.class);
                startActivity(homeIntent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}

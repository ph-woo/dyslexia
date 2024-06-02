package com.example.eye_reading;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SongActivity extends AppCompatActivity {

    String nickname = "";
    String userkey = "";
    DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);

        Intent gameIntent = getIntent();
        if (gameIntent != null && gameIntent.hasExtra("USERNAME")) {
            nickname = gameIntent.getStringExtra("USERNAME");
            Log.d("HomeAct", "Received nickname: " + nickname);
        } else {
            Log.e("HomeAct", "No nickname provided");
        }

        if (gameIntent != null && gameIntent.hasExtra("USERKEY")) {
            userkey = gameIntent.getStringExtra("USERKEY");
            Log.d("HomeAct", "Received userkey: " + userkey);
        } else {
            Log.e("HomeAct", "No userkey provided");
        }

//        Intent intent = getIntent();
//        if (intent != null && intent.hasExtra("USERNAME")) {
//            nickname = intent.getStringExtra("USERNAME");
//            Log.d("HomeAct", "Received nickname: " + nickname);
//        } else {
//            Log.e("HomeAct", "No nickname provided");
//        }
//
//        if (intent != null && intent.hasExtra("USERKEY")) {
//            userkey = gameIntent.getStringExtra("USERKEY");
//            Log.d("HomeAct", "Received userkey: " + userkey);
//        } else {
//            Log.e("HomeAct", "No userkey provided");
//        }


        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }

        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> onBackPressed());

        ImageView navHome = findViewById(R.id.nav_home);
        ImageView navGame = findViewById(R.id.nav_game);
        ImageView navUser = findViewById(R.id.nav_user);

        database = FirebaseDatabase.getInstance("https://song-62299-default-rtdb.firebaseio.com/").getReference();

        loadSongsAndStatus();

        navHome.setOnClickListener(v -> {
            Intent homeIntent = new Intent(SongActivity.this, HomeActivity.class);
            homeIntent.putExtra("USERNAME", nickname);
            homeIntent.putExtra("USERKEY", userkey);
            startActivity(homeIntent);
        });
    }

    private void loadSongsAndStatus() {
        List<Song> songList = new ArrayList<>();
        LinearLayout songListContainer = findViewById(R.id.song_list);

        // 노래 목록 가져오기
        database.child("songs").child("songs").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot songSnapshot : dataSnapshot.getChildren()) {
                    String title = songSnapshot.getKey(); // 노래 제목은 키에서 가져옵니다.
                    if (title != null) {
                        songList.add(new Song(title, -1));
                    }
                }

                // 클리어 여부 가져오기
                database.child("Users").child(userkey).child("game3").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()) {
                            // dataSnapshot이 존재할 때 해당 값을 가져옴
                            // game3 하위에 저장된 값은 dataSnapshot.getValue()를 통해 가져올 수 있음
                            Object game3Value = dataSnapshot.getValue();
                            System.out.println("game3 값: " + game3Value);
                        } else {
                            // dataSnapshot이 존재하지 않을 때
                            System.out.println("game3 값이 존재하지 않습니다.");
                        }
                        List<Integer> game3Status = new ArrayList<>();
                        for (DataSnapshot statusSnapshot : dataSnapshot.getChildren()) {
                            Integer status = statusSnapshot.getValue(Integer.class);
                            if (status != null) {
                                game3Status.add(status);
                            }
                        }
                        System.out.println(game3Status);

                        // songList와 game3Status 크기 비교
                        int minSize = Math.min(songList.size(), game3Status.size());

                        // songList와 game3Status의 크기를 초과하지 않도록 설정
                        for (int i = 0; i < minSize; i++) {
                            songList.get(i).setCleared(game3Status.get(i));
                        }
                        System.out.println(songList);

                        // 노래 목록 기반으로 버튼 생성
                        createSongButtons(songListContainer, songList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("SongActivity", "Error fetching game status", databaseError.toException());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("SongActivity", "Error fetching songs", databaseError.toException());
            }
        });
    }


    private void createSongButtons(LinearLayout songListContainer, List<Song> songList) {
        for (int i = 0; i < songList.size(); i++) {
            Song song = songList.get(i);
            Button songButton = new Button(this);
            songButton.setText(song.getTitle());
            songButton.setTextSize(40);
            songButton.setPadding(10, 10, 10, 10);
            songButton.setTextColor(getResources().getColor(R.color.brown));

            // 클리어 여부에 따라 버튼 배경색 설정
            if (song.getCleared() == 0) {
                songButton.setBackgroundResource(R.drawable.btn_song);
            } else if (song.getCleared() == 1) {
                songButton.setBackgroundResource(R.drawable.btn_song_cleared);
                songButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.lock_resize, 0, 0, 0);
                songButton.setCompoundDrawablePadding(20);
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
            songButton.setOnClickListener(v -> {
                String songTitle = ((Button) v).getText().toString();
                Intent intent = new Intent(SongActivity.this, EyeTracking.class);
                intent.putExtra("SONG_TITLE", songTitle);
                intent.putExtra("USERNAME", nickname);
                intent.putExtra("USERKEY", userkey);
                startActivity(intent);
            });

            songListContainer.addView(songButton);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}

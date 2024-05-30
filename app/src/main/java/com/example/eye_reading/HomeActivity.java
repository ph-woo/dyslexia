package com.example.eye_reading;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeActivity extends AppCompatActivity {

    String nickname = "";
    String userkey = "";
    private DatabaseReference databaseReference;
    private TextView bookmarkCountText;
    private TextView nicknameText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        databaseReference = FirebaseDatabase.getInstance("https://song-62299-default-rtdb.firebaseio.com/").getReference();

//
//         SharedPreferences에서 회원가입 정보를 불러옴
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        nickname = sharedPreferences.getString("username", null);
        userkey = sharedPreferences.getString("userkey", null);

        if (nickname == null || nickname.isEmpty()) {
            Log.e("HomeAct", "No nickname found in SharedPreferences");
            // 사용자에게 알림 또는 다시 로그인 화면으로 이동
            Toast.makeText(this, "No nickname found, please log in again.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(HomeActivity.this, IdcreateActivity.class);
            startActivity(intent);
            finish();
            return;
        } else {
            Log.d("HomeAct", "Loaded nickname from SharedPreferences: " + nickname);
        }

        if (userkey == null || userkey.isEmpty()) {
            Log.e("HomeAct", "No userkey found in SharedPreferences");
            // 사용자에게 알림 또는 다시 로그인 화면으로 이동
            Toast.makeText(this, "No user key found, please log in again.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(HomeActivity.this, IdcreateActivity.class);
            startActivity(intent);
            finish();
            return;
        } else {
            Log.d("HomeAct", "Loaded userkey from SharedPreferences: " + userkey);
        }



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

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }

        Button BtnShop = findViewById(R.id.btn_shop);
        nicknameText = findViewById(R.id.nickname);
        bookmarkCountText = findViewById(R.id.bookmark_count);

        nicknameText.setText(nickname);
        loadBookmarkCount();

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
                System.out.println(nickname);
                gameIntent.putExtra("USERNAME", nickname);
                gameIntent.putExtra("USERKEY", userkey);
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

    private void loadBookmarkCount() {
        if (userkey.isEmpty()) {
            Log.e("HomeAct", "User key is empty, cannot load bookmark count");
            return;
        }

        DatabaseReference bookmarkCountRef = databaseReference.child("Users").child(userkey).child("bookmarkcount");
        bookmarkCountRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Long bookmarkCount = dataSnapshot.getValue(Long.class);
                    if (bookmarkCount != null) {
                        bookmarkCountText.setText(String.valueOf(bookmarkCount));
                    } else {
                        Log.e("HomeAct", "Bookmark count is null");
                        bookmarkCountText.setText("0");
                    }
                } else {
                    Log.e("HomeAct", "Bookmark count does not exist");
                    bookmarkCountText.setText("0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("HomeAct", "Database error: " + databaseError.getMessage());
                bookmarkCountText.setText("0");
            }
        });
    }
}

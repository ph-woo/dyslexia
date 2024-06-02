package com.example.eye_reading;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeActivity extends AppCompatActivity {
    private static final String[] PERMISSIONS = new String[]{
            Manifest.permission.CAMERA
    };
    private static final int REQ_PERMISSION = 1000;

    String nickname = "";
    String userkey = "";
    private DatabaseReference databaseReference;
    private TextView bookmarkCountText;
    private TextView nicknameText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SplashScreen.installSplashScreen(this);
        setContentView(R.layout.activity_home);

        databaseReference = FirebaseDatabase.getInstance("https://song-62299-default-rtdb.firebaseio.com/").getReference();

//
//         SharedPreferences에서 회원가입 정보를 불러옴
//        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
//        nickname = sharedPreferences.getString("username", null);
//        userkey = sharedPreferences.getString("userkey", null);
//
//        if (nickname == null || nickname.isEmpty()) {
//            Log.e("HomeAct", "No nickname found in SharedPreferences");
//            // 사용자에게 알림 또는 다시 로그인 화면으로 이동
//            Toast.makeText(this, "No nickname found, please log in again.", Toast.LENGTH_SHORT).show();
//            Intent intent = new Intent(HomeActivity.this, IdcreateActivity.class);
//            startActivity(intent);
//            finish();
//            return;
//        } else {
//            Log.d("HomeAct", "Loaded nickname from SharedPreferences: " + nickname);
//        }
//
//        if (userkey == null || userkey.isEmpty()) {
//            Log.e("HomeAct", "No userkey found in SharedPreferences");
//            // 사용자에게 알림 또는 다시 로그인 화면으로 이동
//            Toast.makeText(this, "No user key found, please log in again.", Toast.LENGTH_SHORT).show();
//            Intent intent = new Intent(HomeActivity.this, IdcreateActivity.class);
//            startActivity(intent);
//            finish();
//            return;
//        } else {
//            Log.d("HomeAct", "Loaded userkey from SharedPreferences: " + userkey);
//        }



        Intent gameIntent = getIntent();
//        if (gameIntent != null && gameIntent.hasExtra("USERNAME")) {
//            nickname = gameIntent.getStringExtra("USERNAME");
//            Log.d("HomeAct", "Received nickname: " + nickname);
//        } else {
//            Log.e("HomeAct", "No nickname provided");
//        }

        if (gameIntent != null && gameIntent.hasExtra("USERKEY")) {
            userkey = gameIntent.getStringExtra("USERKEY");
            Log.d("HomeAct", "Received userkey: " + userkey);
        } else {
            Log.e("HomeAct", "No userkey provided");
        }

        fetchUsername(userkey);







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
        checkPermission();
    }

    // 권한 확인 및 요청
    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 권한 상태 확인
            if (!hasPermissions(PERMISSIONS)) {
                requestPermissions(PERMISSIONS, REQ_PERMISSION);
            } else {
                checkPermission(true);
            }
        } else {
            checkPermission(true);
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private boolean hasPermissions(String[] permissions) {
        int result;
        // 권한 상태 확인
        for (String perms : permissions) {
            if (perms.equals(Manifest.permission.SYSTEM_ALERT_WINDOW)) {
                if (!Settings.canDrawOverlays(this)) {
                    return false;
                }
            }
            result = ContextCompat.checkSelfPermission(this, perms);
            if (result == PackageManager.PERMISSION_DENIED) {
                // 권한이 허용되지 않았을 때
                return false;
            }
        }
        // 모든 권한이 허용되었을 때
        return true;
    }

    private void checkPermission(boolean isGranted) {
        if (isGranted) {
            permissionGranted();
        } else {
            showToast("카메라 권한이 없습니다.", true);
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQ_PERMISSION:
                if (grantResults.length > 0) {
                    boolean cameraPermissionAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (cameraPermissionAccepted) {
                        checkPermission(true);
                    } else {
                        checkPermission(false);
                    }
                }
                break;
        }
    }

    private void permissionGranted() {
        // 권한이 부여된 후 실행할 작업
        //showToast("Camera permission granted", true);
    }

    private void showToast(final String msg, final boolean isShort) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(HomeActivity.this, msg, isShort ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG).show();
            }
        });
    }



    private void fetchUsername(String userkey) {
        databaseReference.child("Users").child(userkey).child("username").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    nickname = snapshot.getValue().toString();
                    Log.d("HomeAct", "Received username from database: " + nickname);
                    nicknameText.setText(nickname);
                } else {
                    Toast.makeText(HomeActivity.this, "username not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("HomeAct", "Database error: " + error.getMessage());
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

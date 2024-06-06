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

public class HomeActivity extends UserKeyActivity {
    private static final String[] PERMISSIONS = new String[]{
            Manifest.permission.CAMERA
    };
    private static final int REQ_PERMISSION = 1000;

    String nickname = "";
    String userkey = "";
    private DatabaseReference databaseReference;
    private TextView bookmarkCountText;
    private TextView nicknameText;

    private ImageView currentCharacterImage;

    String currentCharacter = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }

        databaseReference = FirebaseDatabase.getInstance("https://song-62299-default-rtdb.firebaseio.com/").getReference();




//        Intent homeIntent = getIntent();
//
//        if (homeIntent != null && homeIntent.hasExtra("USERKEY")) {
//            userkey = homeIntent.getStringExtra("USERKEY");
//            Log.d("HomeAct", "Received userkey: " + userkey);
//        } else {
//            Log.e("HomeAct", "No userkey provided");
//        }


        userkey = getUserId();
        if (userkey != null) {
            // userId를 사용하여 필요한 작업 수행
            Log.d("MainActivity", "User ID: " + userkey);
        } else {
            // userId가 null이면 로그인 화면으로 이동
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
        fetchUsername(userkey);
      
        currentCharacterImage = findViewById(R.id.character);

        fetchingCurrentCharacterData();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }

        Button BtnShop = findViewById(R.id.btn_shop);
        nicknameText = findViewById(R.id.nickname);
        bookmarkCountText = findViewById(R.id.bookmark_count);

        nicknameText.setText(nickname);
//        System.out.println("nikckkk"+nickname);
//        SharedPreferences sharedPreferences = getSharedPreferences("login_state", MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putString("USERNAME", nickname);
//        editor.apply();

        loadBookmarkCount();

        ImageView navHome = findViewById(R.id.nav_home);
        ImageView navGame = findViewById(R.id.nav_game);
        ImageView navUser = findViewById(R.id.nav_user);

        BtnShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shopIntent = new Intent(HomeActivity.this, ShopActivity.class);
//                shopIntent.putExtra("USERNAME", nickname);
//                shopIntent.putExtra("USERKEY", userkey);
                startActivity(shopIntent);
            }
        });

        navGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gameIntent = new Intent(HomeActivity.this, GameActivity.class);
                System.out.println(nickname);
//                gameIntent.putExtra("USERNAME", nickname);
//                gameIntent.putExtra("USERKEY", userkey);
                startActivity(gameIntent);
            }
        });

        navUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent userIntent = new Intent(HomeActivity.this, UserActivity.class);
//                userIntent.putExtra("USERNAME", nickname);
//                userIntent.putExtra("USERKEY", userkey);
                startActivity(userIntent);
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
            showToast("카메라 권한이 없습니다", true);
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
                putNickname(nickname);
            }


            private  void  putNickname(String nickname){
                System.out.println("nikckkk"+nickname);
                SharedPreferences sharedPreferences = getSharedPreferences("login_state", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("USERNAME", nickname);
                editor.apply();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("HomeAct", "Database error: " + error.getMessage());
            }
        });
    }

    public void fetchingCurrentCharacterData() {
        // 데이터베이스에서 currentCharacter와 ownedCharacters를 가져오기
        databaseReference.child("Users").child(userkey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // currentCharacter 값을 가져와 설정
                if (dataSnapshot.child("currentCharacter").exists()) {
                    currentCharacter = dataSnapshot.child("currentCharacter").getValue(String.class);
                } else {
                    currentCharacter = "기본 캐릭터"; // 기본 값 설정
                }
                System.out.println("현재 캐릭터: " + currentCharacter);
                updateCurrentCharacterInfo();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // 데이터베이스 접근 중 오류 발생 시 처리
                System.err.println("데이터 읽기 실패: " + databaseError.getMessage());
            }
        });
    }

    private void updateCurrentCharacterInfo() {
        if (currentCharacter != null) {
            switch (currentCharacter) {
                case "강아지":
                    currentCharacterImage.setImageResource(R.drawable.character_dog);
                    break;
                case "돼지":
                    currentCharacterImage.setImageResource(R.drawable.character_pig);
                    break;
                case "판다":
                    currentCharacterImage.setImageResource(R.drawable.character_panda);
                    break;
                case "원숭이":
                    currentCharacterImage.setImageResource(R.drawable.character_monkey);
                    break;
                case "기린":
                    currentCharacterImage.setImageResource(R.drawable.character_giraffe);
                    break;
                case "젖소":
                    currentCharacterImage.setImageResource(R.drawable.character_milkcow);
                    break;
                case "펭귄":
                    currentCharacterImage.setImageResource(R.drawable.character_penguin);
                    break;
                case "호랑이":
                    currentCharacterImage.setImageResource(R.drawable.character_tiger);
                    break;
                case "얼룩말":
                    currentCharacterImage.setImageResource(R.drawable.character_zebra);
                    break;
                case "사자":
                    currentCharacterImage.setImageResource(R.drawable.character_lion);
                    break;
                default:
                    currentCharacterImage.setImageResource(R.drawable.character_dog);
                    break;
            }
        }
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

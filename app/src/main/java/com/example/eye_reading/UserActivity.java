package com.example.eye_reading;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
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
    private EditText nicknameEdit;
    private Button nicknameEditButton;
    private Button nicknameConfirmButton;
    private Button nicknameCancelButton;
    private ImageView guideIcon;
    private ImageView guideArrow;
    private ScrollView guideContent;
    private boolean isGuideExpanded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SplashScreen.installSplashScreen(this);
        setContentView(R.layout.activity_user);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }

        databaseReference = FirebaseDatabase.getInstance("https://song-62299-default-rtdb.firebaseio.com/").getReference();

        bookmarkCountText = findViewById(R.id.bookmark_count);
        nicknameText = findViewById(R.id.nickname);
        nicknameEdit = findViewById(R.id.nickname_edit);
        nicknameEditButton = findViewById(R.id.nickname_edit_button);
        nicknameConfirmButton = findViewById(R.id.nickname_confirm_button);
        nicknameCancelButton = findViewById(R.id.nickname_cancel_button);
        guideIcon = findViewById(R.id.guide_icon);
        guideArrow = findViewById(R.id.guide_arrow);
        guideContent = findViewById(R.id.guide_content);

        nicknameEditButton.setOnClickListener(v -> {
            nicknameText.setVisibility(View.GONE);
            nicknameEdit.setVisibility(View.VISIBLE);
            nicknameEdit.setText(nicknameText.getText());
            nicknameEditButton.setVisibility(View.GONE);
            nicknameConfirmButton.setVisibility(View.VISIBLE);
            nicknameConfirmButton.setEnabled(false);
            nicknameConfirmButton.setAlpha(0.5f);
            nicknameCancelButton.setVisibility(View.VISIBLE);
        });

        nicknameCancelButton.setOnClickListener(v -> {
            nicknameText.setVisibility(View.VISIBLE);
            nicknameEdit.setVisibility(View.GONE);
            nicknameEditButton.setVisibility(View.VISIBLE);
            nicknameConfirmButton.setVisibility(View.GONE);
            nicknameCancelButton.setVisibility(View.GONE);
        });

        nicknameConfirmButton.setOnClickListener(v -> {
            String newNickname = nicknameEdit.getText().toString().trim();
            if (!newNickname.isEmpty() && !newNickname.equals(nickname)) {
                databaseReference.child("Users").child(userkey).child("username").setValue(newNickname);
                nickname = newNickname;
                nicknameText.setText(newNickname);
                nicknameCancelButton.callOnClick();
            }
        });

        nicknameEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String newNickname = nicknameEdit.getText().toString().trim();
                if (!newNickname.equals(nicknameText.getText().toString().trim()) && !newNickname.isEmpty()) {
                    nicknameConfirmButton.setEnabled(true);
                    nicknameConfirmButton.setAlpha(1.0f);
                } else {
                    nicknameConfirmButton.setEnabled(false);
                    nicknameConfirmButton.setAlpha(0.5f);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        RelativeLayout parentGuide = findViewById(R.id.parent_guide);
        parentGuide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isGuideExpanded) {
                    guideContent.setVisibility(View.GONE);
                    guideIcon.setImageResource(R.drawable.book);
                    guideArrow.setImageResource(R.drawable.down);
                    isGuideExpanded = false;
                } else {
                    guideContent.setVisibility(View.VISIBLE);
                    guideIcon.setImageResource(R.drawable.book_open);
                    guideArrow.setImageResource(R.drawable.up);
                    isGuideExpanded = true;
                }
            }
        });

        Button logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> logoutUser());

        ImageView navHome = findViewById(R.id.nav_home);
        ImageView navGame = findViewById(R.id.nav_game);
        ImageView navUser = findViewById(R.id.nav_user);

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

    private void logoutUser() {
        saveLoginState(false);

        Intent loginIntent = new Intent(this, LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }

    private void saveLoginState(boolean isLoggedIn) {
        SharedPreferences sharedPreferences = getSharedPreferences("login_state", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", isLoggedIn);
        editor.apply();
    }
}

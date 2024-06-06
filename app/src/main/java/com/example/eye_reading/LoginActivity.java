package com.example.eye_reading;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView registerText;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SplashScreen.installSplashScreen(this);
        setContentView(R.layout.activity_login);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerText = findViewById(R.id.registerText);

        mAuth = FirebaseAuth.getInstance();

        // 앱 시작 시 로그인 상태 확인
        checkLoginState();

        loginButton.setOnClickListener(v -> loginUser());

        registerText.setOnClickListener(v -> {
            Intent gameIntent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(gameIntent);
            finish();
        });
    }

    private void loginUser() {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        TextView errorTextView = findViewById(R.id.errorText);

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            String errorMessage = "";
            if (TextUtils.isEmpty(email)) {
                errorMessage = "이메일을 입력해주세요.";
            }
            else if (TextUtils.isEmpty(password)) {
                errorMessage = "비밀번호를 입력해주세요.";
            }

            errorTextView.setText(errorMessage);
            errorTextView.setVisibility(View.VISIBLE);
            return;
        } else {
            errorTextView.setVisibility(View.GONE);
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // 로그인 상태를 저장
                            saveLoginState(true);
                            // 홈 화면으로 이동
                            Intent gameIntent = new Intent(LoginActivity.this, HomeActivity.class);
                            gameIntent.putExtra("USERKEY", user.getUid());
                            startActivity(gameIntent);
                            finish();
                        }
                    } else {
                        errorTextView.setText("이메일 또는 비밀번호를 확인해주세요.");
                        errorTextView.setVisibility(View.VISIBLE);
                    }
                });
    }

    // SharedPreferences에 로그인 상태 저장
    private void saveLoginState(boolean isLoggedIn) {
        FirebaseUser user = mAuth.getCurrentUser();
        SharedPreferences sharedPreferences = getSharedPreferences("login_state", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", isLoggedIn);
        editor.putString("USERKEY", user.getUid());
        editor.apply();
    }

    // 앱 시작 시 로그인 상태 확인하여 홈 화면으로 이동
    private void checkLoginState() {
        FirebaseUser user = mAuth.getCurrentUser();
        SharedPreferences sharedPreferences = getSharedPreferences("login_state", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        if (isLoggedIn) {
            // 로그인 상태가 저장되어 있다면 홈 화면으로 이동
            Intent homeIntent = new Intent(this, HomeActivity.class);
//            homeIntent.putExtra("USERKEY", user.getUid());;
            startActivity(homeIntent);
            finish(); // 현재 액티비티 종료
        }
    }
}

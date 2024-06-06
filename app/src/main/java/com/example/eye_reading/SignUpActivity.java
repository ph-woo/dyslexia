package com.example.eye_reading;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

import java.util.ArrayList;
import java.util.List;

public class SignUpActivity extends AppCompatActivity {
    private EditText emailEditText, passwordEditText,usernameEditText;
    private Button loginButton, registerButton;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }

        usernameEditText = findViewById(R.id.usernameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);

        registerButton = findViewById(R.id.registerButton);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance("https://song-62299-default-rtdb.firebaseio.com/").getReference();

        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> {
            Intent LoginIntent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(LoginIntent);
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    private void registerUser() {
        String username = usernameEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        TextView errorTextView = findViewById(R.id.errorText);

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(username)) {
            String errorMessage = "";
            if (TextUtils.isEmpty(email)) {
                errorMessage = "이메일을 입력해주세요.";
            }
            else if (TextUtils.isEmpty(password)) {
                errorMessage = "비밀번호를 입력해주세요.";
            }
            else if (TextUtils.isEmpty(username)) {
                errorMessage = "닉네임을 입력해주세요.";
            }

            errorTextView.setText(errorMessage);
            errorTextView.setVisibility(View.VISIBLE);
            return;
        } else {
            errorTextView.setVisibility(View.GONE);
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        System.out.println(username);
                        saveUserToDatabase(user, username);
                        Toast.makeText(this, "회원가입 완료. 로그인해주세요.", Toast.LENGTH_SHORT).show();
                    } else {
                        Exception exception = task.getException();
                        if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                            errorTextView.setText("잘못된 형식입니다.");
                        } else if (exception instanceof FirebaseAuthUserCollisionException) {
                            errorTextView.setText("중복된 이메일입니다.");
                        } else {
                            errorTextView.setText("회원가입에 실패했습니다.");
                        }
                        errorTextView.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void saveUserToDatabase(FirebaseUser user, String username) {
        if (user != null) {
            String userId = user.getUid();
            User newUser = new User(user.getEmail());
            System.out.println(userId);
            System.out.println(newUser);
            System.out.println(username);
            mDatabase.child("Users").child(userId).setValue(newUser);
            mDatabase.child("Users").child(userId).child("username").setValue(username);
            List<Integer> zeros = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                zeros.add(0);
            }
            List<String> characters = new ArrayList<>();
            characters.add("강아지");
            mDatabase.child("Users").child(userId).child("game3").setValue(zeros);
            mDatabase.child("Users").child(userId).child("bookmarkcount").setValue(0);
            mDatabase.child("Users").child(userId).child("currentCharacter").setValue("강아지");
            mDatabase.child("Users").child(userId).child("characters").setValue(characters);
            SharedPreferences sharedPreferences = getSharedPreferences("login_state", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("CURRENTCHARACTER", "강아지");
            editor.apply();

            Intent gameIntent = new Intent(SignUpActivity.this, LoginActivity.class);

            gameIntent.putExtra("USERKEY", userId);
            startActivity(gameIntent);
            finish();
        }
    }

    public static class User {
        public String email;

        public User() {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }

        public User(String email) {
            this.email = email;
        }
    }
}
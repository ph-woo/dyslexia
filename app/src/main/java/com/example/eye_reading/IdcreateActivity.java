package com.example.eye_reading;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class IdcreateActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText,usernameEditText;
    private Button loginButton, registerButton;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_idcreate);

        usernameEditText = findViewById(R.id.usernameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);


        registerButton = findViewById(R.id.registerButton);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance("https://song-62299-default-rtdb.firebaseio.com/").getReference();



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

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)||TextUtils.isEmpty(username)) {
            Toast.makeText(this, "칸을 채워주세요", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        System.out.println(username);
                        saveUserToDatabase(user,username);
                        updateUI(user);
                    } else {
                        Toast.makeText(IdcreateActivity.this, "회원가입 실패!", Toast.LENGTH_SHORT).show();
                        updateUI(null);
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

            Intent gameIntent = new Intent(IdcreateActivity.this, LoginActivity.class);

            gameIntent.putExtra("USERKEY", userId);
            startActivity(gameIntent);
            finish();
        }
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Toast.makeText(this, "회원가입 성공!", Toast.LENGTH_SHORT).show();
            // Navigate to the main activity
        } else {
            Toast.makeText(this, "Please sign in.", Toast.LENGTH_SHORT).show();
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
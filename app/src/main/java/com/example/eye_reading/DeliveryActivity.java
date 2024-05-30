package com.example.eye_reading;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class DeliveryActivity extends AppCompatActivity {


    private static final String TAG = DeliveryActivity.class.getSimpleName();

//    private String targetWord = "사과";
    private String targetWord;
//    private String[] candidateWords = {"자과", "차과"};
    private String[] candidateWords;

    private DatabaseReference databaseReference;
    private TextToSpeech tts;
    private TextView house1Text, house2Text, house3Text;
    private ImageView truck;
    private RelativeLayout container;
    private TextView timerText;
    private CountDownTimer countDownTimer;
    private boolean truckMoving = false;
    private float initialTruckX, initialTruckY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery);


        databaseReference = FirebaseDatabase.getInstance("https://song-62299-default-rtdb.firebaseio.com/").getReference();


        try {
            fetchData();
        } catch (Exception e) {
            Log.e(TAG, "Error in fetchSongData: ", e);
        }

    }


    private void initializeGame() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }

        tts = new TextToSpeech(this, status -> {
            if (status != TextToSpeech.ERROR) {
                tts.setLanguage(Locale.KOREAN);
                speakOut(targetWord);
            }
        });

        truck = findViewById(R.id.truck);
        container = findViewById(R.id.container);
        timerText = findViewById(R.id.timer_text);

        house1Text = findViewById(R.id.house1_word);
        house2Text = findViewById(R.id.house2_word);
        house3Text = findViewById(R.id.house3_word);

        List<String> words = new ArrayList<>(Arrays.asList(candidateWords));
        words.add(targetWord);

        Collections.shuffle(words);

        house1Text.setText(words.get(0));
        house2Text.setText(words.get(1));
        house3Text.setText(words.get(2));

        // Save initial position of the truck
        truck.post(() -> {
            initialTruckX = truck.getX();
            initialTruckY = truck.getY();
        });

        // Start countdown timer
        startTimer();

        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> onBackPressed());

        ImageView soundButton = findViewById(R.id.sound);
        soundButton.setOnClickListener(v -> speakOut(targetWord));

        // Set onTouchListener for the truck image
        truck.setOnTouchListener(new View.OnTouchListener() {
            float dX, dY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dX = v.getX() - event.getRawX();
                        dY = v.getY() - event.getRawY();
                        truckMoving = true;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        if (truckMoving) {
                            v.animate()
                                    .x(event.getRawX() + dX)
                                    .y(event.getRawY() + dY)
                                    .setDuration(0)
                                    .start();
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                        truckMoving = false;
                        // Check if truck is over any house
                        checkHouse(v);
                        // Return truck to start position
                        v.animate()
                                .x(initialTruckX)
                                .y(initialTruckY)
                                .setDuration(200)
                                .start();
                        return true;
                }
                return false;
            }
        });

    }



    private void fetchData() {


        databaseReference.child("twowords").child("twoword1").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    List<String> candidateWordsList = (List<String>) dataSnapshot.child("candidateWords").getValue();

                        // targetWord를 리스트 형식으로 가져옵니다.
                        List<String> targetWordList = (List<String>) dataSnapshot.child("targetWord").getValue();

                        // 리스트에서 첫 번째 요소를 가져와서 문자열로 변환합니다.
                        if (targetWordList != null && !targetWordList.isEmpty()) {
                            String targetWordValue = targetWordList.get(0);

                            if (candidateWordsList != null && targetWordValue != null) {

                                candidateWords = candidateWordsList.toArray(new String[0]);
                                // targetWord를 설정한다
                                targetWord = targetWordValue;
                                initializeGame();

                            }
                        } else {
                            Log.e(TAG, "targetWord list is null or empty");
                        }
                    } else {
                        Log.e(TAG, "targetChars is not a List");
                    }
                }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(DeliveryActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "DatabaseError: ", databaseError.toException());
            }
        });
    }

    private void checkHouse(View truck) {
        ImageView house1 = findViewById(R.id.house1);
        ImageView house2 = findViewById(R.id.house2);
        ImageView house3 = findViewById(R.id.house3);

        if (isViewOverlapping(truck, house1)) {
            if (house1Text.getText().toString().equals(targetWord)) {
                showToast("배달 완료!");
            } else {
                showToast("배달 실패");
            }
            finish();
        } else if (isViewOverlapping(truck, house2)) {
            if (house2Text.getText().toString().equals(targetWord)) {
                showToast("배달 완료!");
            } else {
                showToast("배달 실패");
            }
            finish();
        } else if (isViewOverlapping(truck, house3)) {
            if (house3Text.getText().toString().equals(targetWord)) {
                showToast("배달 완료!");
            } else {
                showToast("배달 실패");
            }
            finish();
        }
    }

    private boolean isViewOverlapping(View view1, View view2) {
        int[] loc1 = new int[2];
        int[] loc2 = new int[2];
        view1.getLocationOnScreen(loc1);
        view2.getLocationOnScreen(loc2);

        return loc1[0] < loc2[0] + view2.getWidth() &&
                loc1[0] + view1.getWidth() > loc2[0] &&
                loc1[1] < loc2[1] + view2.getHeight() &&
                loc1[1] + view1.getHeight() > loc2[1];
    }

    private void speakOut(String text) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = millisUntilFinished / 60000;
                long seconds = (millisUntilFinished % 60000) / 1000;
                timerText.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                timerText.setText("00:00");
                showToast("시간 초과로 배달 실패");
                // Game over logic here
                finish();
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        super.onDestroy();
    }
}

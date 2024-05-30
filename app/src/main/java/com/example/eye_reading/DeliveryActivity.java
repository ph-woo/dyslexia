package com.example.eye_reading;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.tts.TextToSpeech;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class DeliveryActivity extends AppCompatActivity {
    private List<WordPair> wordList;
    private String targetWord;
    private String[] candidateWords;
    private int bookmarks = 0;
    private TextToSpeech tts;
    private TextView house1Text, house2Text, house3Text;
    private ImageView truck;
    private RelativeLayout container;
    private TextView timerText;
    private CountDownTimer countDownTimer;
    private boolean truckMoving = false;
    private float initialTruckX, initialTruckY;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery);
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

        // Save initial position of the truck
        truck.post(() -> {
            initialTruckX = truck.getX();
            initialTruckY = truck.getY();
        });

        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> onBackPressed());

        ImageView soundButton = findViewById(R.id.sound);
        soundButton.setOnClickListener(v -> speakOut(targetWord));

        initWordList();
        startTimer();
        startNewGame();

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

    private void initWordList() {
        wordList = new ArrayList<>();
        wordList.add(new DeliveryActivity.WordPair("사과", new String[]{"자과", "차과"}));
        wordList.add(new DeliveryActivity.WordPair("과자", new String[]{"과사", "과차"}));
        wordList.add(new DeliveryActivity.WordPair("의자", new String[]{"의사", "의차"}));
        wordList.add(new DeliveryActivity.WordPair("우유", new String[]{"유유", "으유"}));
        wordList.add(new DeliveryActivity.WordPair("하늘", new String[]{"하들", "하를"}));
        wordList.add(new DeliveryActivity.WordPair("토끼", new String[]{"토기", "토키"}));
        wordList.add(new DeliveryActivity.WordPair("포도", new String[]{"포노", "포토"}));
        wordList.add(new DeliveryActivity.WordPair("비누", new String[]{"비두", "비투"}));
        wordList.add(new DeliveryActivity.WordPair("그네", new String[]{"그내", "그니"}));
        wordList.add(new DeliveryActivity.WordPair("학교", new String[]{"학고", "학그"}));
        // 단어 추가
    }

    private void checkHouse(View truck) {
        ImageView house1 = findViewById(R.id.house1);
        ImageView house2 = findViewById(R.id.house2);
        ImageView house3 = findViewById(R.id.house3);

        if (isViewOverlapping(truck, house1)) {
            handleHouseDelivery(house1Text.getText().toString());
        } else if (isViewOverlapping(truck, house2)) {
            handleHouseDelivery(house2Text.getText().toString());
        } else if (isViewOverlapping(truck, house3)) {
            handleHouseDelivery(house3Text.getText().toString());
        }
    }

    private void handleHouseDelivery(String chosenWord) {
        if (chosenWord.equals(targetWord)) {
            bookmarks++;
            showToast("배달 완료! 책갈피 획득: " + bookmarks);
            startNewGame();
        } else {
            showToast("다시 배달해주세요.");
            truck.setX(initialTruckX);
            truck.setY(initialTruckY);
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
                showGameOverDialog();
                // Game over logic here
            }
        }.start();
    }

    private void startNewGame() {
        Random random = new Random();
        DeliveryActivity.WordPair selectedPair = wordList.get(random.nextInt(wordList.size()));
        targetWord = selectedPair.targetWord;
        candidateWords = selectedPair.candidateWords;

        List<String> words = new ArrayList<>(Arrays.asList(candidateWords));
        words.add(targetWord);

        Collections.shuffle(words);

        house1Text.setText(words.get(0));
        house2Text.setText(words.get(1));
        house3Text.setText(words.get(2));

        speakOut(targetWord);

        truck.setX(initialTruckX);
        truck.setY(initialTruckY);
    }

    private void showGameOverDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("게임 종료");
        builder.setMessage("획득한 책갈피 수: " + bookmarks);
        builder.setPositiveButton("다시 플레이하기", (dialog, which) -> {
            bookmarks = 0;
            startTimer();
            startNewGame();
        });
        builder.setNegativeButton("나가기", (dialog, which) -> finish());
        builder.setCancelable(false);
        builder.show();
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

    private static class WordPair {
        String targetWord;
        String[] candidateWords;

        WordPair(String targetWord, String[] candidateWords) {
            this.targetWord = targetWord;
            this.candidateWords = candidateWords;
        }
    }
}

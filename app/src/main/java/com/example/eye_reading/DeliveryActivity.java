package com.example.eye_reading;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.tts.TextToSpeech;
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
import android.os.Handler;
import android.os.Looper;

import camp.visual.gazetracker.GazeTracker;
import camp.visual.gazetracker.callback.GazeCallback;
import camp.visual.gazetracker.callback.InitializationCallback;
import camp.visual.gazetracker.constant.InitializationErrorType;
import camp.visual.gazetracker.gaze.GazeInfo;

public class DeliveryActivity extends AppCompatActivity {
    private String targetWord = "사과";
    private String[] candidateWords = {"자과", "차과"};
    private TextToSpeech tts;
    private TextView house1Text, house2Text, house3Text;
    private ImageView truck;
    private RelativeLayout container;
    private TextView timerText;
    private CountDownTimer countDownTimer;
    private float initialTruckX, initialTruckY;

    private GazeTrackerManager gazeTrackerManager;
    private Handler handler;
    private static final long GAZE_UPDATE_INTERVAL = 135; // 0.135초
    private float gazeX, gazeY;
    private long lastTime = 0;

    private boolean result_flag = false;

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

        gazeTrackerManager = GazeTrackerManager.getInstance();
        gazeTrackerManager.initGazeTracker(initializationCallback, null);

        handler = new Handler(Looper.getMainLooper());
        handler.post(gazeRunnable);
    }

    private void checkHouse(View truck) {
        ImageView house1 = findViewById(R.id.house1);
        ImageView house2 = findViewById(R.id.house2);
        ImageView house3 = findViewById(R.id.house3);

        if (isViewOverlapping(truck, house1)) {
            if (house1Text.getText().toString().equals(targetWord)) {
                if (!result_flag) {
                    showToast("배달 완료!");
                    result_flag = true;
                }
            } else {
                if (!result_flag) {
                    showToast("배달 실패");
                    result_flag = true;
                }
            }
            finish();
        } else if (isViewOverlapping(truck, house2)) {
            if (house2Text.getText().toString().equals(targetWord)) {
                if (!result_flag) {
                    showToast("배달 완료!");
                    result_flag = true;
                }
            } else {
                if (!result_flag) {
                    showToast("배달 실패");
                    result_flag = true;
                }
            }
            finish();
        } else if (isViewOverlapping(truck, house3)) {
            if (house3Text.getText().toString().equals(targetWord)) {
                if (!result_flag) {
                    showToast("배달 완료!");
                    result_flag = true;
                }
            } else {
                if (!result_flag) {
                    showToast("배달 실패");
                    result_flag = true;
                }
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

        if (gazeTrackerManager != null) {
            gazeTrackerManager.deinitGazeTracker();
        }

        super.onDestroy();
    }

    private final GazeCallback gazeCallback = new GazeCallback() {
        @Override
        public void onGaze(GazeInfo gazeInfo) {
            gazeX = gazeInfo.x;
            gazeY = gazeInfo.y;
        }
    };

    private final Runnable gazeRunnable = new Runnable() {
        @Override
        public void run() {
            moveTruckTowardsGaze();
            handler.post(this);
        }
    };

    private void moveTruckTowardsGaze() {
        if (lastTime == 0 || System.currentTimeMillis() - lastTime > GAZE_UPDATE_INTERVAL) {
            lastTime = System.currentTimeMillis();
            runOnUiThread(() -> {
                float truckX = truck.getX();
                float truckY = truck.getY();

                float dx = gazeX - (truckX + truck.getWidth() / 2);
                float dy = gazeY - (truckY + truck.getHeight() / 2);

                if (Math.abs(dx) > 30 || Math.abs(dy) > 30) {
                    float newTruckX = truckX + (dx > 0 ? 30 : -30);
                    float newTruckY = truckY + (dy > 0 ? 30 : -30);

                    ObjectAnimator animatorX = ObjectAnimator.ofFloat(truck, "x", truckX, newTruckX);
                    ObjectAnimator animatorY = ObjectAnimator.ofFloat(truck, "y", truckY, newTruckY);

                    animatorX.setDuration(GAZE_UPDATE_INTERVAL);
                    animatorY.setDuration(GAZE_UPDATE_INTERVAL);

                    animatorX.start();
                    animatorY.start();
                }

                checkHouse(truck);
            });
        }
    }

    private final InitializationCallback initializationCallback = new InitializationCallback() {
        @Override
        public void onInitialized(GazeTracker gazeTracker, InitializationErrorType error) {
            if (gazeTracker != null) {
                gazeTrackerManager.setGazeTrackerCallbacks(gazeCallback);
                gazeTrackerManager.startGazeTracking();
            } else {
                showToast("GazeTracker 초기화 실패: " + error.name());
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        gazeTrackerManager.setGazeTrackerCallbacks(gazeCallback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        gazeTrackerManager.startGazeTracking();
    }

    @Override
    protected void onPause() {
        super.onPause();
        gazeTrackerManager.stopGazeTracking();
    }

    @Override
    protected void onStop() {
        super.onStop();
        gazeTrackerManager.removeCallbacks(gazeCallback);
    }
}

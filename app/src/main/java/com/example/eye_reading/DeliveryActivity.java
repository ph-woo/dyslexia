package com.example.eye_reading;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import java.util.Random;

import camp.visual.gazetracker.GazeTracker;
import camp.visual.gazetracker.callback.GazeCallback;
import camp.visual.gazetracker.callback.InitializationCallback;
import camp.visual.gazetracker.constant.InitializationErrorType;
import camp.visual.gazetracker.filter.OneEuroFilterManager;
import camp.visual.gazetracker.gaze.GazeInfo;

public class DeliveryActivity extends UserKeyActivity {
    private DatabaseReference databaseReference;
    String userKey;
    private List<WordPair> wordList;
    private String targetWord;
    private List<String> candidateWords;
    private String currentCharacter = getUserCurrentCharacter();
    private int bookmarks = 0;
    private TextToSpeech tts;
    private MediaPlayer deliverySuccess, deliveryFailure;
    private TextView house1Text, house2Text, house3Text;
    private RelativeLayout container, truck;
    private ImageView bike, characterFace;
    private TextView timerText;
    private CountDownTimer countDownTimer;
    private boolean truckMoving = false;
    private float initialTruckX, initialTruckY;

    private GazeTrackerManager gazeTrackerManager;
    private Handler handler;
    private static final long GAZE_UPDATE_INTERVAL = 20;
    private float gazeX, gazeY;
    private long lastTime = 0;
    private boolean isGameOver = false;
    private OneEuroFilterManager oneEuroFilterManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }

//        Intent deliveryIntent = getIntent();
//
//        if (deliveryIntent != null && deliveryIntent.hasExtra("USERKEY")) {
//            userKey = deliveryIntent.getStringExtra("USERKEY");
//
//            Log.d("HomeAct", "Received userkey: " + userKey);
//        } else {
//            Log.e("HomeAct", "No userkey provided");
//        }

        userKey=getUserId();

        databaseReference = FirebaseDatabase.getInstance("https://song-62299-default-rtdb.firebaseio.com/").getReference();

        tts = new TextToSpeech(this, status -> {
            if (status != TextToSpeech.ERROR) {
                tts.setLanguage(Locale.KOREAN);
                speakOut(targetWord);
            }
        });

        deliverySuccess = MediaPlayer.create(this, R.raw.doorbell);
        deliveryFailure = MediaPlayer.create(this, R.raw.error);

        truck = findViewById(R.id.truck);
        bike = findViewById(R.id.bike);
        characterFace = findViewById(R.id.character_face);
        container = findViewById(R.id.container);
        timerText = findViewById(R.id.timer_text);

        setCharacterFace();

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

        gazeTrackerManager = GazeTrackerManager.getInstance();
        gazeTrackerManager.initGazeTracker(initializationCallback, null);

        handler = new Handler(Looper.getMainLooper());
        handler.post(gazeRunnable);

        fetchData();
        startTimer();

        // Set onTouchListener for the truck image
        truck.setOnTouchListener(new View.OnTouchListener() {
            float dX, dY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isGameOver) return false;
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
        wordList = new ArrayList<>();
        databaseReference.child("deliveryWords").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    WordPair pair = dataSnapshot.getValue(WordPair.class);
                    if (pair != null) {
                        wordList.add(pair);
                    }
                }
                if (!wordList.isEmpty()) {
                    startNewGame();
                } else {
                    Toast.makeText(DeliveryActivity.this, "No data available", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DeliveryActivity.this, "Failed to load data from Firebase", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setCharacterFace() {
        if (currentCharacter != null) {
            switch (currentCharacter) {
                case "강아지":
                    characterFace.setImageResource(R.drawable.face_dog);
                    break;
                case "돼지":
                    characterFace.setImageResource(R.drawable.face_pig);
                    break;
                case "판다":
                    characterFace.setImageResource(R.drawable.face_panda);
                    break;
                case "원숭이":
                    characterFace.setImageResource(R.drawable.face_monkey);
                    break;
                case "기린":
                    characterFace.setImageResource(R.drawable.face_giraffe);
                    break;
                case "젖소":
                    characterFace.setImageResource(R.drawable.face_milkcow);
                    break;
                case "펭귄":
                    characterFace.setImageResource(R.drawable.face_penguin);
                    break;
                case "호랑이":
                    characterFace.setImageResource(R.drawable.face_tiger);
                    break;
                case "얼룩말":
                    characterFace.setImageResource(R.drawable.face_zebra);
                    break;
                case "사자":
                    characterFace.setImageResource(R.drawable.face_lion);
                    break;
                default:
                    characterFace.setImageResource(R.drawable.face_dog);
                    break;
            }
        }
    }

    private boolean checkHouse(View truck) {
        ImageView house1 = findViewById(R.id.house1);
        ImageView house2 = findViewById(R.id.house2);
        ImageView house3 = findViewById(R.id.house3);

        if (isViewOverlapping(truck, house1)) {
            handleHouseDelivery(house1Text.getText().toString());
            return true;
        } else if (isViewOverlapping(truck, house2)) {
            handleHouseDelivery(house2Text.getText().toString());
            return true;
        } else if (isViewOverlapping(truck, house3)) {
            handleHouseDelivery(house3Text.getText().toString());
            return true;
        }
        return false;
    }

    private void handleHouseDelivery(String chosenWord) {
        if (chosenWord.equals(targetWord)) {
            bookmarks++;
            updateBookmarkCount();
            if (deliverySuccess != null) {
                deliverySuccess.start();
            }
            truck.setX(initialTruckX);
            truck.setY(initialTruckY);
            startNewGame();
        } else {
            if (deliveryFailure != null) {
                deliveryFailure.start();
            }
            resetTruckPosition();
            truckMoving = false;
        }
    }

    private void resetTruckPosition() {
        truck.animate()
                .x(initialTruckX)
                .y(initialTruckY)
                .setDuration(400)
                .start();
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
                isGameOver = true;
                showGameOverDialog();
            }
        }.start();
    }

    private void startNewGame() {
        Random random = new Random();
        WordPair selectedPair = wordList.get(random.nextInt(wordList.size()));
        targetWord = selectedPair.targetWord;
        candidateWords = selectedPair.candidateWords;

        List<String> words = new ArrayList<>(candidateWords);
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
        View dialogView = LayoutInflater.from(this).inflate(R.layout.gameover_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setCancelable(false);
        AlertDialog alertDialog = builder.create();

        TextView bookmarkCountTextView = dialogView.findViewById(R.id.bookmark_count);
        bookmarkCountTextView.setText(bookmarks + "개 획득");

        Button playAgainButton = dialogView.findViewById(R.id.play_again_btn);
        playAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isGameOver = false;
                bookmarks = 0;
                startTimer();
                startNewGame();
                handler.post(gazeRunnable);
                alertDialog.dismiss();
            }
        });

        Button exitButton = dialogView.findViewById(R.id.exit_btn);
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        alertDialog.show();
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
        if (deliverySuccess != null) {
            deliverySuccess.release();
            deliverySuccess = null;
        }
        if (deliveryFailure != null) {
            deliveryFailure.release();
            deliveryFailure = null;
        }
        super.onDestroy();
    }

    public static class WordPair {
        private String targetWord;
        private List<String> candidateWords;

        public WordPair() {
        }

        public String getTargetWord() {
            return targetWord;
        }

        public void setTargetWord(String targetWord) {
            this.targetWord = targetWord;
        }

        public List<String> getCandidateWords() {
            return candidateWords;
        }

        public void setCandidateWords(List<String> candidateWords) {
            this.candidateWords = candidateWords;
        }
    }

    private void updateBookmarkCount() {
        databaseReference.child("Users").child(userKey).child("bookmarkcount").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    long currentBookmarkCount = (long) dataSnapshot.getValue();

                    long updatedBookmarkCount = currentBookmarkCount + 1;

                    databaseReference.child("Users").child(userKey).child("bookmarkcount").setValue(updatedBookmarkCount)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d("TAG", "Bookmark count updated successfully.");
                                    } else {
                                        Log.e("TAG", "Failed to update bookmark count.");
                                    }
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("TAG", "Failed to read bookmark count value.", databaseError.toException());
            }
        });
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
            if (!isGameOver) {
                moveTruckTowardsGaze();
                handler.post(this);
            }
        }
    };

    private void moveTruckTowardsGaze() {
        if (isGameOver) return;
        if (lastTime == 0 || System.currentTimeMillis() - lastTime > GAZE_UPDATE_INTERVAL) {
            lastTime = System.currentTimeMillis();
            runOnUiThread(() -> {
                float truckX = truck.getX();
                float truckY = truck.getY();

                float dx = gazeX - (truckX + truck.getWidth() / 2);
                float dy = gazeY - (truckY + truck.getHeight() / 2);

                if (Math.abs(dx) > 8 || Math.abs(dy) > 8) {
                    float newTruckX = truckX + (dx > 0 ? 8 : -8);
                    float newTruckY = truckY + (dy > 0 ? 8 : -8);

                    truck.setX(newTruckX);
                    truck.setY(newTruckY);
                }

                if (checkHouse(truck)) {
                    truck.setX(initialTruckX);
                    truck.setY(initialTruckY);
                }
            });
        }
    }

    private final InitializationCallback initializationCallback = new InitializationCallback() {
        @Override
        public void onInitialized(GazeTracker gazeTracker, InitializationErrorType error) {
            if (gazeTracker != null) {
                oneEuroFilterManager = new OneEuroFilterManager(2);
                gazeTrackerManager.setGazeTrackerCallbacks(gazeCallback);
                gazeTrackerManager.startGazeTracking();
            } else {
                showToast("GazeTracker 초기화 실패: " + error.name());
            }
        }
    };

    private void stopGazeTrackingTemporarily(long duration) {
        try {
            if (gazeTrackerManager.isTracking()) {
                gazeTrackerManager.stopGazeTracking();
            }

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                try {
                    if (!gazeTrackerManager.isTracking()) {
                        gazeTrackerManager.startGazeTracking();
                    }
                } catch (IllegalStateException e) {
                    Log.e("GazeTracker", "에러 트래킹 재시작: 카메라 이미 닫힘.", e);
                }
            }, duration);
        } catch (IllegalStateException e) {
            Log.e("GazeTracker", "트래킹 멈춤: 카메라 이미 닫힘.", e);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        gazeTrackerManager.setGazeTrackerCallbacks(gazeCallback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isGameOver) {
            gazeTrackerManager.startGazeTracking();
            handler.post(gazeRunnable);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        gazeTrackerManager.stopGazeTracking();
        handler.removeCallbacks(gazeRunnable);
    }

    @Override
    protected void onStop() {
        super.onStop();
        gazeTrackerManager.removeCallbacks(gazeCallback);
    }
}

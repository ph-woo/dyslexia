package com.example.eye_reading;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import camp.visual.gazetracker.callback.GazeCallback;
import camp.visual.gazetracker.gaze.GazeInfo;
import camp.visual.gazetracker.state.EyeMovementState;
import camp.visual.gazetracker.util.ViewLayoutChecker;
import camp.visual.gazetracker.filter.OneEuroFilterManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;


import camp.visual.gazetracker.util.ViewLayoutChecker;
import visual.camp.sample.view.GazePathView;

public class BubbleActivity extends AppCompatActivity {

    private static final String TAG = BubbleActivity.class.getSimpleName();
    private String targetWord = "자동차";
    private char[] targetChars = {'ㅈ', 'ㅏ', 'ㄷ', 'ㅗ', 'ㅇ', 'ㅊ', 'ㅏ'};
    private int currentIndex = 0;
    private int lives = 3;
    private TextToSpeech tts;
    private List<ImageView> heartImages;

    private GazePathView gazePathView;
    private GazeTrackerManager gazeTrackerManager;
    private final ViewLayoutChecker viewLayoutChecker = new ViewLayoutChecker();

    private final OneEuroFilterManager oneEuroFilterManager = new OneEuroFilterManager(
            2, 30, 0.5F, 0.001F, 1.0F);

    private Map<ImageView, Long> gazeStartTimeMap = new HashMap<>();
    private static final long GAZE_HOLD_DURATION = 700; // 0.7초

    private ImageView[] images;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bubble);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }

        gazeTrackerManager = GazeTrackerManager.getInstance();



        tts = new TextToSpeech(this, status -> {
            if (status != TextToSpeech.ERROR) {
                tts.setLanguage(Locale.KOREAN);
                speakOut(targetWord);
            }
        });

        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> onBackPressed());

        RelativeLayout container = findViewById(R.id.container);
        int numBubbles = 15;
        int bubbleSize = 80; // in dp
        int minDistance = 100; // in dp, minimum distance between bubbles

        // Convert dp to pixels
        final float scale = getResources().getDisplayMetrics().density;
        int bubbleSizeInPx = (int) (bubbleSize * scale + 0.5f);
        int minDistanceInPx = (int) (minDistance * scale + 0.5f);

        // List of characters to include in bubbles
        char[] characters = {'ㄱ', 'ㄴ', 'ㄷ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅅ', 'ㅇ', 'ㅈ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ',
                'ㅏ', 'ㅑ', 'ㅓ', 'ㅕ', 'ㅗ', 'ㅛ', 'ㅜ', 'ㅠ', 'ㅡ', 'ㅣ'};
        Random random = new Random();

        heartImages = new ArrayList<>();
        heartImages.add(findViewById(R.id.heart1));
        heartImages.add(findViewById(R.id.heart2));
        heartImages.add(findViewById(R.id.heart3));
        gazePathView = findViewById(R.id.gazePathView);

        ImageView soundButton = findViewById(R.id.sound);
        soundButton.setOnClickListener(v -> speakOut(targetWord));

        images = new ImageView[numBubbles];

        // Wait until layout is drawn to get the correct width and height

        container.post(() -> {
            int layoutWidth = container.getWidth();
            int layoutHeight = container.getHeight();
            Log.d("WIDGET_POS",String.valueOf(layoutHeight) + "," + String.valueOf(layoutWidth));
            List<int[]> positions = new ArrayList<>();
            List<Character> bubbleCharacters = new ArrayList<>();

            // Ensure targetChars are included
            for (char targetChar : targetChars) {
                bubbleCharacters.add(targetChar);
            }
            // Fill remaining bubbles with random characters
            while (bubbleCharacters.size() < numBubbles) {
                bubbleCharacters.add(characters[random.nextInt(characters.length)]);
            }

            // Shuffle the characters
            java.util.Collections.shuffle(bubbleCharacters);

            for (int i = 0; i < numBubbles; i++) {
                int leftMargin, topMargin;
                boolean overlaps;

                // Find a non-overlapping position with minimum distance
                do {
                    overlaps = false;
                    leftMargin = random.nextInt(layoutWidth - bubbleSizeInPx);
                    topMargin = random.nextInt(layoutHeight - bubbleSizeInPx);

                    for (int[] pos : positions) {
                        int otherLeft = pos[0];
                        int otherTop = pos[1];
                        double distance = Math.sqrt(Math.pow(leftMargin - otherLeft, 2) + Math.pow(topMargin - otherTop, 2));
                        if (distance < minDistanceInPx) {
                            overlaps = true;
                            break;
                        }
                    }
                } while (overlaps);

                positions.add(new int[]{leftMargin, topMargin});

                // Create ImageView for the bubble
                ImageView bubble = new ImageView(BubbleActivity.this);
                images[i] = bubble;
                Drawable bubbleDrawable = ContextCompat.getDrawable(BubbleActivity.this, R.drawable.bubble);
                bubble.setImageDrawable(bubbleDrawable);

                RelativeLayout.LayoutParams bubbleParams = new RelativeLayout.LayoutParams(bubbleSizeInPx, bubbleSizeInPx);
                bubbleParams.leftMargin = leftMargin;
                bubbleParams.topMargin = topMargin;

                container.addView(bubble, bubbleParams);

                // Create TextView for the character
                TextView textView = new TextView(BubbleActivity.this);
                textView.setTextSize(54);
                textView.setTextColor(Color.BLACK);
                textView.setText(String.valueOf(bubbleCharacters.get(i)));
                textView.setGravity(android.view.Gravity.CENTER);

                RelativeLayout.LayoutParams textParams = new RelativeLayout.LayoutParams(bubbleSizeInPx, bubbleSizeInPx);
                textParams.leftMargin = leftMargin;
                textParams.topMargin = topMargin;

                container.addView(textView, textParams);

                // Set click listener for the bubble
                final int index = i;
                bubble.setOnClickListener(v -> handleBubbleClick(textView, bubbleCharacters.get(index)));
            }
        });
    }

    private void handleBubbleClick(TextView textView, char character) {
        if (textView.getCurrentTextColor() == Color.GREEN) {
            return;
        }

        if (character == targetChars[currentIndex]) {
            textView.setTextColor(Color.GREEN);
            currentIndex++;
            if (currentIndex == targetChars.length) {
                // Game success logic here
                showToast("게임 성공!");
            }
        } else {
            textView.setTextColor(Color.RED);
            loseLife();
        }
    }

    private void loseLife() {
        if (lives > 0) {
            lives--;
            updateHearts();
            if (lives == 0) {
                // Game over logic here
                showToast("게임 실패");
            }
        }
    }

    private void updateHearts() {
        for (int i = 0; i < heartImages.size(); i++) {
            if (i < lives) {
                heartImages.get(i).setImageResource(R.drawable.heart_full);
            } else {
                heartImages.get(i).setImageResource(R.drawable.heart_empty);
            }
        }
    }

    private void speakOut(String text) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");
        gazeTrackerManager.setGazeTrackerCallbacks(gazeCallback);
        
    }

    @Override
    protected void onResume() {
        super.onResume();
        gazeTrackerManager.startGazeTracking();
        setOffsetOfView();
        Log.i(TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        gazeTrackerManager.stopGazeTracking();
        Log.i(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        gazeTrackerManager.removeCallbacks(gazeCallback);
        Log.i(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

        super.onDestroy();
    }

    private void setOffsetOfView() {
        viewLayoutChecker.setOverlayView(gazePathView, new ViewLayoutChecker.ViewLayoutListener() {
            @Override
            public void getOffset(int x, int y) {
                gazePathView.setOffset(x, y);
            }
        });
    }


    private final GazeCallback gazeCallback = new GazeCallback() {
        @Override
        public void onGaze(GazeInfo gazeInfo) {
            if (oneEuroFilterManager.filterValues(gazeInfo.timestamp, gazeInfo.x, gazeInfo.y)) {
                float[] filtered = oneEuroFilterManager.getFilteredValues();
                gazePathView.onGaze(filtered[0], filtered[1], gazeInfo.eyeMovementState == EyeMovementState.FIXATION);
                handleGazeEvent(filtered[0], filtered[1]);
            }
        }
    };
    private void handleGazeEvent(float gazeX, float gazeY) {
        long currentTime = System.currentTimeMillis();
        for (ImageView imageView : images) {
            int[] location = new int[2];
            imageView.getLocationOnScreen(location);
            float x = location[0];
            float y = location[1];
            float width = imageView.getWidth();
            float height = imageView.getHeight();

            // 시선이 특정 imageView 위에 있는지 확인
            if (gazeX >= x && gazeX <= x + width && gazeY >= y && gazeY <= y + height) {
                if (!gazeStartTimeMap.containsKey(imageView)) {
                    gazeStartTimeMap.put(imageView, currentTime);
                } else {
                    long gazeDuration = currentTime - gazeStartTimeMap.get(imageView);
                    if (gazeDuration >= GAZE_HOLD_DURATION) {
                        // 메인 스레드에서 UI 업데이트 수행
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                imageView.performClick();
                            }
                        });
                        gazeStartTimeMap.remove(imageView); // 시선이 유지된 후 맵에서 제거
                    }
                }
            } else {
                gazeStartTimeMap.remove(imageView); // 시선이 벗어나면 맵에서 제거
            }
        }
    }

}

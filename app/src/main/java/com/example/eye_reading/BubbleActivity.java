package com.example.eye_reading;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class BubbleActivity extends AppCompatActivity {
    private String targetWord = "자동차";
    private char[] targetChars = {'ㅈ', 'ㅏ', 'ㄷ', 'ㅗ', 'ㅇ', 'ㅊ', 'ㅏ'};
    private int currentIndex = 0;
    private int lives = 3;
    private TextToSpeech tts;
    private List<ImageView> heartImages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bubble);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }

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

        ImageView soundButton = findViewById(R.id.sound);
        soundButton.setOnClickListener(v -> speakOut(targetWord));

        // Wait until layout is drawn to get the correct width and height
        container.post(() -> {
            int layoutWidth = container.getWidth();
            int layoutHeight = container.getHeight();
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
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

        super.onDestroy();
    }
}

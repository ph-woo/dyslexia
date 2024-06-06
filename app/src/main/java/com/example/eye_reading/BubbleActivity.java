package com.example.eye_reading;

import android.content.Intent;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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


import visual.camp.sample.view.GazePathView;

public class BubbleActivity extends UserKeyActivity {
    private DatabaseReference databaseReference;
    String userKey;
    private List<WordCharacterPair> wordList;
    private String targetWord;
    private char[] targetChars;
    private int currentIndex;
    private int lives = 3;
    private int bookmarks = 0;
    private TextToSpeech tts;
    private MediaPlayer bubblePop, bubbleError;
    private List<View> bubbleViews;
    private List<ImageView> heartImages;
    private Animation shootAnimation;
    private RelativeLayout characterLayout;
    private String currentCharacter = "강아지";
    private ImageView characterImage;
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

//        Intent bubbleIntent = getIntent();
//
//        if (bubbleIntent != null && bubbleIntent.hasExtra("USERKEY")) {
//            userKey = bubbleIntent.getStringExtra("USERKEY");
//
//            Log.d("HomeAct", "Received userkey: " + userKey);
//        } else {
//            Log.e("HomeAct", "No userkey provided");
//        }

        userKey = getUserId();

        tts = new TextToSpeech(this, status -> {
            if (status != TextToSpeech.ERROR) {
                tts.setLanguage(Locale.KOREAN);
                speakOut(targetWord);
            }
        });

        bubblePop = MediaPlayer.create(this, R.raw.bubble_pop);
        bubbleError = MediaPlayer.create(this, R.raw.error);

        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> onBackPressed());

        heartImages = new ArrayList<>();
        heartImages.add(findViewById(R.id.heart1));
        heartImages.add(findViewById(R.id.heart2));
        heartImages.add(findViewById(R.id.heart3));
        bubbleViews = new ArrayList<>();
        gazePathView = findViewById(R.id.gazePathView);

        ImageView soundButton = findViewById(R.id.sound);
        soundButton.setOnClickListener(v -> speakOut(targetWord));

        setCharacterImage();

        shootAnimation = new RotateAnimation(
                0, -10, // fromDegrees, toDegrees
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 1.0f);
        shootAnimation.setDuration(300);
        shootAnimation.setRepeatCount(1);
        shootAnimation.setRepeatMode(Animation.REVERSE);
        characterLayout = findViewById(R.id.character);
        characterImage = findViewById(R.id.character_image);

        gazeTrackerManager = GazeTrackerManager.getInstance();

        databaseReference = FirebaseDatabase.getInstance("https://song-62299-default-rtdb.firebaseio.com/").getReference();
        fetchData();
    }

    private void fetchData() {
        wordList = new ArrayList<>();
        databaseReference.child("bubbleWords").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    WordCharacterPair pair = dataSnapshot.getValue(WordCharacterPair.class);
                    if (pair != null) {
                        wordList.add(pair);
                    }
                }
                startNewGame();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(BubbleActivity.this, "Failed to load data from Firebase", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setCharacterImage() {
        if (currentCharacter != null) {
            switch (currentCharacter) {
                case "강아지":
                    characterImage.setImageResource(R.drawable.character_dog);
                    break;
                case "돼지":
                    characterImage.setImageResource(R.drawable.character_pig);
                    break;
                case "판다":
                    characterImage.setImageResource(R.drawable.character_panda);
                    break;
                case "원숭이":
                    characterImage.setImageResource(R.drawable.character_monkey);
                    break;
                case "기린":
                    characterImage.setImageResource(R.drawable.character_giraffe);
                    break;
                case "젖소":
                    characterImage.setImageResource(R.drawable.character_milkcow);
                    break;
                case "펭귄":
                    characterImage.setImageResource(R.drawable.character_penguin);
                    break;
                case "호랑이":
                    characterImage.setImageResource(R.drawable.character_tiger);
                    break;
                case "얼룩말":
                    characterImage.setImageResource(R.drawable.character_zebra);
                    break;
                case "사자":
                    characterImage.setImageResource(R.drawable.character_lion);
                    break;
                default:
                    characterImage.setImageResource(R.drawable.character_dog);
                    break;
            }
        }
    }

    private void startNewGame() {
        Random random = new Random();
        WordCharacterPair selectedPair = wordList.get(random.nextInt(wordList.size()));
        targetWord = selectedPair.getWord();
        List<String> characterList = selectedPair.getCharacters();
        targetChars = new char[characterList.size()];
        for (int i = 0; i < characterList.size(); i++) {
            targetChars[i] = characterList.get(i).charAt(0);
        }
        currentIndex = 0;

        speakOut(targetWord);

        RelativeLayout container = findViewById(R.id.container);
        setupBubbles(container);
    }

    private void setupBubbles(RelativeLayout container) {
        int numBubbles = 12;
        int bubbleSize = 80; // in dp
        int minDistance = 110; // in dp, minimum distance between bubbles

        // Convert dp to pixels
        final float scale = getResources().getDisplayMetrics().density;
        int bubbleSizeInPx = (int) (bubbleSize * scale + 0.5f);
        int minDistanceInPx = (int) (minDistance * scale + 0.5f);

        char[] characters = {'ㄱ', 'ㄴ', 'ㄷ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅅ', 'ㅇ', 'ㅈ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ',
                'ㅏ', 'ㅑ', 'ㅓ', 'ㅕ', 'ㅗ', 'ㅛ', 'ㅜ', 'ㅠ', 'ㅡ', 'ㅣ'};
        Random random = new Random();

        for (View view : bubbleViews) {
            container.removeView(view);
        }
        bubbleViews.clear();

        images = new ImageView[numBubbles];

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

        container.post(() -> {
            int layoutWidth = container.getWidth();
            int layoutHeight = container.getHeight();

            int characterLayoutWidth = characterLayout.getWidth();
            int characterLayoutHeight = characterLayout.getHeight();

            for (int i = 0; i < numBubbles; i++) {
                int leftMargin, topMargin;
                boolean overlaps;

                // Find a non-overlapping position with minimum distance
                do {
                    overlaps = false;
                    leftMargin = random.nextInt(layoutWidth - bubbleSizeInPx);
                    topMargin = random.nextInt(layoutHeight - bubbleSizeInPx);

                    if (leftMargin >= characterLayout.getLeft() - bubbleSizeInPx &&
                            leftMargin <= characterLayout.getLeft() + characterLayoutWidth &&
                            topMargin >= characterLayout.getTop() - bubbleSizeInPx &&
                            topMargin <= characterLayout.getTop() + characterLayoutHeight) {
                        overlaps = true;
                    }

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
                bubble.setBackgroundResource(R.drawable.ripple_effect);

                RelativeLayout.LayoutParams bubbleParams = new RelativeLayout.LayoutParams(bubbleSizeInPx, bubbleSizeInPx);
                bubbleParams.leftMargin = leftMargin;
                bubbleParams.topMargin = topMargin;

                container.addView(bubble, bubbleParams);

                // Create TextView for the character
                TextView textView = new TextView(BubbleActivity.this);
                textView.setTextSize(60);
                textView.setTextColor(Color.BLACK);
                textView.setText(String.valueOf(bubbleCharacters.get(i)));
                textView.setGravity(android.view.Gravity.CENTER);

                RelativeLayout.LayoutParams textParams = new RelativeLayout.LayoutParams(bubbleSizeInPx, bubbleSizeInPx);
                textParams.leftMargin = leftMargin;
                textParams.topMargin = topMargin;

                container.addView(textView, textParams);
                bubbleViews.add(bubble);
                bubbleViews.add(textView);

                // Set click listener for the bubble
                final int index = i;
                bubble.setOnClickListener(v -> handleBubbleClick(bubble, textView, bubbleCharacters.get(index)));
            }
        });
    }

    private void handleBubbleClick(ImageView bubble, TextView textView, char character) {
        if (textView.getCurrentTextColor() == Color.GREEN) {
            return;
        }

        if (character == targetChars[currentIndex]) {
            textView.setTextColor(Color.GREEN);
            currentIndex++;
            if (currentIndex == targetChars.length) {
                bookmarks++;
                updateBookmarkCount();
                startNewGame();
            }
            if (bubblePop != null) {
                bubblePop.start();
            }
            bubble.animate()
                    .scaleX(1.2f)
                    .scaleY(1.2f)
                    .alpha(0.5f)
                    .setDuration(150)
                    .withEndAction(() -> {
                        bubble.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .alpha(0.5f)
                                .setDuration(150)
                                .start();
                    })
                    .start();
            characterLayout.startAnimation(shootAnimation);
        } else {
            textView.setTextColor(Color.RED);
            loseLife();
            if (bubbleError != null) {
                bubbleError.start();
            }
            bubble.animate()
                    .scaleX(1.1f)
                    .scaleY(1.1f)
                    .setDuration(150)
                    .withEndAction(() -> {
                        bubble.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(150)
                                .start();
                    })
                    .start();
            characterLayout.startAnimation(shootAnimation);
        }
    }

    private void loseLife() {
        if (lives > 0) {
            lives--;
            updateHearts();
            if (lives == 0) {
                gazeTrackerManager.stopGazeTracking();
                characterLayout.clearAnimation();
                if (tts != null) {
                    tts.stop();
                }
                if (bubblePop != null) {
                    bubblePop.stop();
                }
                if (bubbleError != null) {
                    bubbleError.stop();
                }
                showGameOverDialog();
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
                lives = 3;
                bookmarks = 0;
                updateHearts();
                startNewGame();
                alertDialog.dismiss();
                gazeTrackerManager.startGazeTracking();
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
    protected void onStart() {
        super.onStart();
        gazeTrackerManager.setGazeTrackerCallbacks(gazeCallback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        gazeTrackerManager.startGazeTracking();
        setOffsetOfView();
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

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        if (bubblePop != null) {
            bubblePop.release();
            bubblePop = null;
        }
        if (bubbleError != null) {
            bubbleError.release();
            bubbleError = null;
        }
        super.onDestroy();
    }

    public static class WordCharacterPair {
        private String word;
        private List<String> characters;

        public WordCharacterPair() {
        }

        public WordCharacterPair(String word, char[] characters) {
            this.word = word;
            this.characters = new ArrayList<>();
            for (char c : characters) {
                this.characters.add(String.valueOf(c));
            }
        }

        public String getWord() {
            return word;
        }

        public void setWord(String word) {
            this.word = word;
        }

        public List<String> getCharacters() {
            return characters;
        }

        public void setCharacters(List<String> characters) {
            this.characters = characters;
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
        if (images == null) {
            Log.e("TAG", "Images array is null");
            return;
        }

        long currentTime = System.currentTimeMillis();
        for (ImageView imageView : images) {
            if (imageView == null) continue;
            int[] location = new int[2];
            imageView.getLocationOnScreen(location);
            float x = location[0] ; //POINT_RADIUS
            float y = location[1] ;
            float width = imageView.getWidth() + 40;
            float height = imageView.getHeight() + 40;

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

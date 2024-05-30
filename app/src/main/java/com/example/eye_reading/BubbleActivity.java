package com.example.eye_reading;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class BubbleActivity extends AppCompatActivity {

    private int gameIndex = -1;

    private static final String TAG = BubbleActivity.class.getSimpleName();

    private DatabaseReference databaseReference;
//    private String targetWord = "자동차";
    private String targetWord;
//    private char[] targetChars = {'ㅈ', 'ㅏ', 'ㄷ', 'ㅗ', 'ㅇ', 'ㅊ', 'ㅏ'};
    private char[] targetChars;
    private int currentIndex = 0;
    private int lives = 3;
    private TextToSpeech tts;
    private List<ImageView> heartImages;

    String nickname="";

    String userkey="";

    int index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bubble);

        databaseReference = FirebaseDatabase.getInstance("https://song-62299-default-rtdb.firebaseio.com/").getReference();

        Intent bubbleIntent = getIntent();
        if (bubbleIntent != null && bubbleIntent.hasExtra("USERNAME")) {
            nickname= bubbleIntent.getStringExtra("USERNAME");

            Log.d("HomeAct", "Received nickname: " + nickname);
        } else {
            Log.e("HomeAct", "No nickname provided");
        }

        if (bubbleIntent != null && bubbleIntent.hasExtra("USERKEY")) {
            userkey= bubbleIntent.getStringExtra("USERKEY");

            Log.d("HomeAct", "Received userkey: " + userkey);
        } else {
            Log.e("HomeAct", "No userkey provided");
        }

        orderingData(userkey);



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

    // orderingData() 메소드 구현
    public void orderingData(String userKey) {
        databaseReference.child("Users").child(userKey).child("gameprocessivity").child("game1")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // game1 배열 데이터 가져오기
                            List<Long> game1Array = (List<Long>) dataSnapshot.getValue();
                            if (game1Array != null && gameIndex < game1Array.size()) {
                                for (int index = 0; index < game1Array.size(); index++) {
                                    long value = game1Array.get(index);
                                    // 값이 0이면 해당 인덱스를 출력하고 함수 종료
                                    if (value == 0) {
                                        System.out.println("처음으로 0이 발견된 인덱스: " + index);
                                        gameIndex = index;
                                        try {
                                            fetchData();
                                        } catch (Exception e) {
                                            Log.e(TAG, "Error in fetchSongData: ", e);
                                        }
                                        return; // 함수 종료
                                    }
                                }
                                // 0이 발견되지 않았을 경우 메시지 출력
                                System.out.println("배열에서 0이 발견되지 않았습니다.");
                            } else {
                                System.out.println("game1 배열이 비어있습니다.");
                            }
                        } else {
                            System.out.println("game1 배열이 존재하지 않습니다.");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // 에러 처리
                        System.out.println("데이터베이스에서 데이터를 가져오는 중 오류 발생: " + databaseError.getMessage());
                        try {
                            fetchData();
                        } catch (Exception e) {
                            Log.e(TAG, "Error in fetchSongData: ", e);
                        }
                    }
                });
    }

    private void fetchData() {

        Log.d("HomeAct", "fetchData called with index: " + gameIndex);

//        String finalgameIndex = Integer.toString(gameIndex);
//        Log.d("HomeAct", "finalgameindex: " + finalgameIndex);



        databaseReference.child("tripwords").child("tripword"+gameIndex).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Object targetCharsObject = dataSnapshot.child("targetChars").getValue();
                    if (targetCharsObject instanceof List) {
                        List<String> targetCharsList = (List<String>) targetCharsObject;

                        // targetWord를 리스트 형식으로 가져옵니다.
                        List<String> targetWordList = (List<String>) dataSnapshot.child("targetWord").getValue();

                        // 리스트에서 첫 번째 요소를 가져와서 문자열로 변환합니다.
                        if (targetWordList != null && !targetWordList.isEmpty()) {
                            String targetWordValue = targetWordList.get(0);

                            if (targetCharsList != null && targetWordValue != null) {
                                // targetChars를 문자 배열로 초기화합니다.
                                targetChars = new char[targetCharsList.size()];
                                for (int i = 0; i < targetCharsList.size(); i++) {
                                    String str = targetCharsList.get(i);

                                    if (str != null && str.length() == 1) {  // 문자열이 정확히 한 글자인지 확인
                                        targetChars[i] = str.charAt(0);
                                    } else {
                                        throw new IllegalArgumentException("인덱스 " + i + "의 요소가 한 글자가 아닙니다.");
                                    }
                                }

                                // targetWord를 설정한다
                                targetWord = targetWordValue;
                                initializeGame();

                            } else {
                                Log.e(TAG, "targetWord is null");
                            }
                        } else {
                            Log.e(TAG, "targetWord list is null or empty");
                        }
                    } else {
                        Log.e(TAG, "targetChars is not a List");
                    }
                } else {
                    Log.e(TAG, "DataSnapshot does not exist");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(BubbleActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "DatabaseError: ", databaseError.toException());
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
                // Update gameIndex in the database
                updateGameClear(userkey, gameIndex);

                Intent homeIntent = new Intent(BubbleActivity.this, GameActivity.class);

                homeIntent.putExtra("USERNAME", nickname);
                homeIntent.putExtra("USERKEY", userkey);
                startActivity(homeIntent);
            }
        } else {
            textView.setTextColor(Color.RED);
            loseLife();
        }
    }

    private void updateGameClear(String userKey, int gameIndex) {
        // game1의 특정 인덱스를 1로 설정
        databaseReference.child("Users").child(userKey).child("gameprocessivity").child("game1")
                .child(String.valueOf(gameIndex))
                .setValue(1)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("HomeAct", "Game index updated successfully.");

                            // bookmarkcount 값을 가져와서 +10을 더한 후 업데이트
                            DatabaseReference bookmarkCountRef = databaseReference.child("Users").child(userKey).child("bookmarkcount");
                            bookmarkCountRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        Long currentCount = dataSnapshot.getValue(Long.class);
                                        if (currentCount != null) {
                                            bookmarkCountRef.setValue(currentCount + 10)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                Log.d("HomeAct", "Bookmark count updated successfully.");
                                                            } else {
                                                                Log.e("HomeAct", "Failed to update bookmark count.", task.getException());
                                                            }
                                                        }
                                                    });
                                        } else {
                                            Log.e("HomeAct", "Current bookmark count is null.");
                                        }
                                    } else {
                                        Log.e("HomeAct", "Bookmark count does not exist.");
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.e("HomeAct", "Database error: " + databaseError.getMessage());
                                }
                            });
                        } else {
                            Log.e("HomeAct", "Failed to update game index.", task.getException());
                        }
                    }
                });
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

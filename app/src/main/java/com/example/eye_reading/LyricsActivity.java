 package com.example.eye_reading;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import camp.visual.gazetracker.GazeTracker;
import camp.visual.gazetracker.callback.GazeCallback;
import camp.visual.gazetracker.filter.OneEuroFilterManager;
import camp.visual.gazetracker.gaze.GazeInfo;
import camp.visual.gazetracker.state.EyeMovementState;
import camp.visual.gazetracker.util.ViewLayoutChecker;
import visual.camp.sample.view.GazePathView;

public class LyricsActivity extends AppCompatActivity {

    String songTitle = null; // 변수를 먼저 선언하고 초기화
    String nickname = null; // 변수를 먼저 선언하고 초기화
    String userkey = null; // 변수를 먼저 선언하고 초기화

    private static final String TAG = LyricsActivity.class.getSimpleName();
    private DatabaseReference databaseReference;
    private final ViewLayoutChecker viewLayoutChecker = new ViewLayoutChecker();
    private GazePathView gazePathView;
    private GazeTrackerManager gazeTrackerManager;
    private final OneEuroFilterManager oneEuroFilterManager = new OneEuroFilterManager(
            2, 30, 0.5F, 0.001F, 1.0F);
    private TextView livesTextView; // 목숨을 표시하는 TextView
    private TextView timerText;
    private CountDownTimer countDownTimer;
    private TextView[] textViews;
    private int currentIndex = 0; // 현재 표시 중인 가사의 인덱스
    private int correctIndex = 0; // 올바른 순서에서 현재 눌러야 하는 인덱스

    private int lives = 3; // 목숨의 개수
    private List<ImageView> heartImages;

    private static final long GAZE_HOLD_DURATION = 850; // 0.85초로 수정했습니다.

    /////// 각 버튼에 대한 시선 시작 시간을 저장하는 맵을 생성합니다.
    private Map<TextView, Long> gazeStartTimeMap = new HashMap<>();
/////////////////////////////

    private String[] lyrics;
    private String[] correctSequence;
    private String[] soundSequence;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lyrics);




        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("SONG_TITLE")) {
            songTitle = intent.getStringExtra("SONG_TITLE");
            // songTitle을 사용하여 작업 수행
            Log.d("EyeTracking", "Received song title: " + songTitle);
        } else {
            Log.e("EyeTracking", "No song title provided");
        }
        if (intent != null && intent.hasExtra("USERKEY")) {
           userkey = intent.getStringExtra("USERKEY");
            // songTitle을 사용하여 작업 수행
            Log.d("EyeTracking", "Received USERKEY: " + userkey);
        } else {
            Log.e("EyeTracking", "No USERKEY provided");
        }



        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }
        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> onBackPressed());

        gazeTrackerManager = GazeTrackerManager.getInstance();
        Log.i(TAG, "gazeTracker version: " + GazeTracker.getVersionName());
      
        databaseReference = FirebaseDatabase.getInstance("https://song-62299-default-rtdb.firebaseio.com/").getReference();
      
        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> onBackPressed());

        timerText = findViewById(R.id.timer_text);

        heartImages = new ArrayList<>();
        heartImages.add(findViewById(R.id.heart1));
        heartImages.add(findViewById(R.id.heart2));
        heartImages.add(findViewById(R.id.heart3));

        // 2분(120초) 타이머 생성
        startTimer();

        try {
            fetchSongData();
        } catch (Exception e) {
            Log.e(TAG, "Error in fetchSongData: ", e);
        }

    }




    private void fetchSongData() {

System.out.println(songTitle);
        databaseReference.child("songs").child("songs").child(songTitle).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    List<String> lyricsList = (List<String>) dataSnapshot.child("lyrics").getValue();
                    List<String> correctSequenceList = (List<String>) dataSnapshot.child("correctSequence").getValue();
                    List<String> soundSequenceList = (List<String>) dataSnapshot.child("soundSequence").getValue();

                    System.out.println(lyricsList);

                    if (lyricsList != null && correctSequenceList != null && soundSequenceList != null) {
                        lyrics = lyricsList.toArray(new String[0]);
                        correctSequence = correctSequenceList.toArray(new String[0]);
                        soundSequence = soundSequenceList.toArray(new String[0]);
                        setupGame();
                    } else {
                        Log.e(TAG, "One or more data lists are null");
                    }
                } else {
                    Log.e(TAG, "DataSnapshot does not exist");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(LyricsActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "DatabaseError: ", databaseError.toException());
            }
        });
    }

    private void setupGame() {

        LinearLayout[] buttonsLayouts = new LinearLayout[lyrics.length];

        // 각 레이아웃을 동적으로 찾기 위해 for 루프를 사용합니다.
        for (int i = 0; i < lyrics.length; i++) {
            String layoutID = "buttonsLayout" + (i + 1);  // i가 0일 때 "buttonsLayout1"부터 시작
            int resID = getResources().getIdentifier(layoutID, "id", getPackageName());
            buttonsLayouts[i] = findViewById(resID);
        }


//        buttonsLayouts[0] = findViewById(R.id.buttonsLayout1);
//        buttonsLayouts[1] = findViewById(R.id.buttonsLayout2);
//        buttonsLayouts[2] = findViewById(R.id.buttonsLayout3);
//        buttonsLayouts[3] = findViewById(R.id.buttonsLayout4);
//        buttonsLayouts[4] = findViewById(R.id.buttonsLayout5);
//        buttonsLayouts[5] = findViewById(R.id.buttonsLayout6);

        textViews = new TextView[correctSequence.length];
        for (int i = 0; i < correctSequence.length; i++) {
            final int index = i;
            textViews[i] = new TextView(this);
            textViews[i].setText(correctSequence[i]);
            textViews[i].setTextSize(30);
            textViews[i].setPadding(40, 16, 40, 16);
            textViews[i].setClickable(true);
            textViews[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleTextViewClick(index);
                }
            });



            if (i < lyrics[0].length()) {
                buttonsLayouts[0].addView(textViews[i]);
            } else if (i < lyrics[0].length()+lyrics[1].length()) {
                buttonsLayouts[1].addView(textViews[i]);
            } else if (i < lyrics[0].length()+lyrics[1].length()+lyrics[2].length()) {
                buttonsLayouts[2].addView(textViews[i]);
            } else if (i < lyrics[0].length()+lyrics[1].length()+lyrics[2].length()+lyrics[3].length()) {
                buttonsLayouts[3].addView(textViews[i]);
            } else if (i < lyrics[0].length()+lyrics[1].length()+lyrics[2].length()+lyrics[3].length()+lyrics[4].length()) {
                buttonsLayouts[4].addView(textViews[i]);
            } else if (i < lyrics[0].length()+lyrics[1].length()+lyrics[2].length()+lyrics[3].length()+lyrics[4].length()+lyrics[5].length()) {
                buttonsLayouts[5].addView(textViews[i]);
            }  else if (i < lyrics[0].length()+lyrics[1].length()+lyrics[2].length()+lyrics[3].length()+lyrics[4].length()+lyrics[5].length()+lyrics[6].length()) {
                buttonsLayouts[6].addView(textViews[i]);
            } else {
                buttonsLayouts[7].addView(textViews[i]);
            }
        }

        textViews[correctIndex].setTextColor(getResources().getColor(android.R.color.holo_purple));
    }

    private void handleTextViewClick(int index) {

        Log.d(TAG, "Current Index: " + currentIndex + ", Correct Index: " + correctIndex + ",cs.l"+correctSequence.length);

        if (index == correctIndex) {
            if (correctIndex < textViews.length) {
                textViews[index].setTextColor(getResources().getColor(android.R.color.holo_green_light));
                textViews[index].setClickable(false);
            }
//            playSound(soundSequence[correctIndex]);

            if (correctIndex == correctSequence.length-1) {
                Log.d("Game", "Success condition met, showing success dialog.");
                showGameSuccessDialog(songTitle);
                return;
//                if (currentIndex < lyrics.length) {
//                    resetTextViewColors();
//                    correctIndex = 0;
//                    if (correctIndex < textViews.length) {
//                        textViews[correctIndex].setTextColor(getResources().getColor(android.R.color.holo_purple));
//                    }
//                } else {
//                    showGameOverDialog();
//                    return;
//                }
            } else {
                correctIndex++;
                if (correctIndex < correctSequence.length && correctIndex < textViews.length) {
                    textViews[correctIndex].setTextColor(getResources().getColor(android.R.color.holo_purple));
                }
            }


        } else {
            if (index < textViews.length) {
                textViews[index].setTextColor(getResources().getColor(android.R.color.holo_red_light));
            }
            loseLife();
//            playSound("error");
        }
    }

    private void resetTextViewColors() {
        for (TextView textView : textViews) {
            textView.setTextColor(getResources().getColor(android.R.color.black));
        }
    }

    // 목숨 감소 메서드
    private void loseLife() {
        if (lives > 0) {
            lives--;
            updateHearts();
            if (lives == 0) {
                showGameOverDialog();
                disableAllTextViews();
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

    // 게임을 다시 시작하는 메서드
    private void restartGame() {
        // 현재 액티비티를 다시 시작하여 게임을 초기화
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    // 메인 메뉴로 이동하는 메서드
//    private void goToMainMenu() {
//        // MainActivity 대신 실제 메인 메뉴 액티비티로 이동하도록 수정해야 함
//        Intent intent = new Intent(this, MainMenuActivity.class);
//        startActivity(intent);
//        finish();
//    }





    // 모든 TextView 클릭 불가능하게 설정하는 메서드
    private void disableAllTextViews() {
        for (TextView textView : textViews) {
            textView.setClickable(false); // 클릭 불가능하게 설정
        }
    }

    // 음원 재생 메서드
    private void playSound(String note) {
        int soundResId = 0;

        // note에 따라 음원 파일의 리소스 ID 설정
        switch (note) {
            case "솔":
                soundResId = R.raw.piano08;
                break;
            case "라":
                soundResId = R.raw.piano10;
                break;
            case "미":
                soundResId = R.raw.piano05;
                break;
            case "레":
                soundResId = R.raw.piano03;
                break;
            case "error":
                soundResId = R.raw.error;
                break;
        }


        // MediaPlayer를 초기화하고 음원 재생
        MediaPlayer mediaPlayer = MediaPlayer.create(this, soundResId);
        mediaPlayer.start();
    }





    // 게임 성공 다이얼로그를 표시하는 메서드
    private void showGameSuccessDialog(String songTitle) {
        Toast.makeText(this, "게임 성공!", Toast.LENGTH_SHORT).show();


        int a = -1;
        if (songTitle.equals("곰세마리")) {
            a = 0;
        } else if (songTitle.equals("나비야")) {
            a = 1;
        }else if (songTitle.equals("나처럼 해봐요")) {
            a = 2;
        }else if (songTitle.equals("달 달 무슨달")) {
            a = 3;
        }else if (songTitle.equals("릿자로 끝나는 말은")) {
            a = 4;
        }else if (songTitle.equals("비행기")) {
            a = 5;
        }else if (songTitle.equals("악어떼")) {
            a = 6;
        }else if (songTitle.equals("얼룩송아지")) {
            a = 7;
        }else if (songTitle.equals("작은 별")) {
            a = 8;
        }else if (songTitle.equals("학교종이 땡땡땡")) {
            a = 9;
        }
        Log.d("LyricsActivity", "Mapped songTitle to index: " + a);

        // bookmarkcount 값을 가져와서 +10을 더한 후 업데이트
        DatabaseReference bookmarkCountRef = databaseReference.child("Users").child(userkey).child("bookmarkcount");
        DatabaseReference game3Ref = databaseReference.child("Users").child(userkey).child("game3").child(String.valueOf(a));

        game3Ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Integer game3Value = dataSnapshot.getValue(Integer.class);
                if (game3Value != null && game3Value.equals(0)) {
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
                                                        Log.d("LyricsActivity", "Bookmark count updated successfully.");

                                                        // 값을 1로 변경합니다.
                                                        game3Ref.setValue(1)
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()) {
                                                                            Log.d("Firebase", "Element updated successfully.");
                                                                        } else {
                                                                            Log.e("Firebase", "Failed to update element.", task.getException());
                                                                        }
                                                                    }
                                                                });
                                                    } else {
                                                        Log.e("LyricsActivity", "Failed to update bookmark count.", task.getException());
                                                    }
                                                }
                                            });
                                } else {
                                    Log.e("LyricsActivity", "Current bookmark count is null.");
                                }
                            } else {
                                Log.e("LyricsActivity", "Bookmark count does not exist.");
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.e("LyricsActivity", "Database error: " + databaseError.getMessage());
                        }
                    });
                } else {
                    Log.e("LyricsActivity", "game3 value is not 0 or is null.");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("LyricsActivity", "Database error: " + databaseError.getMessage());
            }
        });

        // Activity 전환
        Intent intent = new Intent(LyricsActivity.this, GameActivity.class);
        intent.putExtra("USERNAME", nickname);
        intent.putExtra("USERKEY", userkey);
        startActivity(intent);
        finish(); // Activity 종료
    }







//    // 게임 오버 다이얼로그를 표시하는 메서드를 확인
//    private void showGameOverDialog() {
//        new AlertDialog.Builder(this)
//                .setTitle("Game Over")
//                .setMessage("You have completed the game.")
//                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        // 다이얼로그가 닫힐 때 Activity를 종료하거나 다른 동작을 수행
//                        finish(); // Activity 종료
//                    }
//                })
//                .show();
//    }

    private void showGameOverDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Game Over!");
        builder.setMessage("게임 오버!").setCancelable(false);

        builder.setPositiveButton("다시하기", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                restartGame();
            }
        });

        builder.setNegativeButton("게임창으로 가기", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                // Activity 전환
                Intent intent = new Intent(LyricsActivity.this, GameActivity.class);
                intent.putExtra("USERNAME", nickname);
                intent.putExtra("USERKEY", userkey);
                startActivity(intent);
                finish(); // Activity 종료
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(120000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = millisUntilFinished / 60000;
                long seconds = (millisUntilFinished % 60000) / 1000;
                timerText.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                showGameOverDialog();
                // Game over logic here
            }
        }.start();
    }



    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");
        gazeTrackerManager.setGazeTrackerCallbacks(gazeCallback);
        initView();
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
        super.onDestroy();
    }

    private void initView() {
        gazePathView = findViewById(R.id.gazePathView);

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
        for (TextView textView : textViews) {
            int[] location = new int[2];
            textView.getLocationOnScreen(location);
            float x = location[0];
            float y = location[1];
            float width = textView.getWidth();
            float height = textView.getHeight();

            // 시선이 특정 TextView 위에 있는지 확인
            if (gazeX >= x && gazeX <= x + width && gazeY >= y && gazeY <= y + height) {
                if (!gazeStartTimeMap.containsKey(textView)) {
                    gazeStartTimeMap.put(textView, currentTime);
                } else {
                    long gazeDuration = currentTime - gazeStartTimeMap.get(textView);
                    if (gazeDuration >= GAZE_HOLD_DURATION) {
                        // 메인 스레드에서 UI 업데이트 수행
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                textView.performClick();
                            }
                        });
                        gazeStartTimeMap.remove(textView); // 시선이 유지된 후 맵에서 제거
                    }
                }
            } else {
                gazeStartTimeMap.remove(textView); // 시선이 벗어나면 맵에서 제거
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}

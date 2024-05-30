package com.example.eye_reading;


import android.app.AlertDialog;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import camp.visual.gazetracker.GazeTracker;
import camp.visual.gazetracker.callback.GazeCallback;
import camp.visual.gazetracker.filter.OneEuroFilterManager;
import camp.visual.gazetracker.gaze.GazeInfo;
import camp.visual.gazetracker.state.EyeMovementState;
import camp.visual.gazetracker.util.ViewLayoutChecker;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.example.eye_reading.GazeTrackerManager;
import com.example.eye_reading.R;
import visual.camp.sample.view.GazePathView;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.Toast;

public class LyricsActivity extends AppCompatActivity {
    private static final String TAG = LyricsActivity.class.getSimpleName();
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

    private static final long GAZE_HOLD_DURATION = 650; // 0.65초

    /////// 각 버튼에 대한 시선 시작 시간을 저장하는 맵을 생성합니다.
    private Map<TextView, Long> gazeStartTimeMap = new HashMap<>();
/////////////////////////////

    // 가사 및 올바른 순서
    private String[] lyrics = {
            "학교종이",
            "땡땡땡",
            "어서모이자",
            "선생님이",
            "우리를",
            "기다리신다"
    };
    private String[] correctSequence = {"학", "교", "종", "이", "땡", "땡", "땡", "어", "서", "모", "이", "자", "선", "생", "님", "이", "우", "리", "를", "기", "다", "리", "신", "다"}; // 올바른 순서


    // 음원 파일 재생 순서
    private String[] soundSequence = {"솔", "솔", "라", "라", "솔", "솔", "미", "솔", "솔", "미", "미", "레", "솔", "솔", "라", "라", "솔", "솔", "미", "솔", "미", "레", "미"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lyrics);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }
        gazeTrackerManager = GazeTrackerManager.getInstance();
        Log.i(TAG, "gazeTracker version: " + GazeTracker.getVersionName());

        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> onBackPressed());

        timerText = findViewById(R.id.timer_text);

        heartImages = new ArrayList<>();
        heartImages.add(findViewById(R.id.heart1));
        heartImages.add(findViewById(R.id.heart2));
        heartImages.add(findViewById(R.id.heart3));

        // 2분(120초) 타이머 생성
        startTimer();

        // LinearLayout 초기화
        LinearLayout buttonsLayout1 = findViewById(R.id.buttonsLayout1); // 첫 번째 줄 LinearLayout 찾기
        LinearLayout buttonsLayout2 = findViewById(R.id.buttonsLayout2); // 두 번째 줄 LinearLayout 찾기
        LinearLayout buttonsLayout3 = findViewById(R.id.buttonsLayout3);
        LinearLayout buttonsLayout4 = findViewById(R.id.buttonsLayout4);
        LinearLayout buttonsLayout5 = findViewById(R.id.buttonsLayout5);
        LinearLayout buttonsLayout6 = findViewById(R.id.buttonsLayout6);
        // 버튼 초기화

        textViews = new TextView[correctSequence.length]; // TextView 배열 초기화
        for (int i = 0; i < correctSequence.length; i++) { // 각 TextView 생성 반복
            final int index = i;

            // TextView 생성 및 속성 설정
            textViews[i] = new TextView(this); // TextView 생성
            textViews[i].setText(correctSequence[i]); // TextView 텍스트 설정
            textViews[i].setTextSize(36); // 텍스트 크기 설정
            textViews[i].setPadding(50, 16, 50, 16); // 패딩 설정
            textViews[i].setClickable(true); // 클릭 가능 설정
            textViews[i].setOnClickListener(new View.OnClickListener() { // TextView 클릭 리스너 설정

                @Override
                public void onClick(View v) {
                    Log.d("Click", "" + "BUtton");
                    if (index == correctIndex) { // 올바른 순서인지 확인
                        textViews[index].setTextColor(getResources().getColor(android.R.color.holo_green_light)); // 올바른 순서일 경우 텍스트 색상 변경
                        textViews[index].setClickable(false); // 클릭 불가능하게 설정
                        playSound(soundSequence[correctIndex]); // 올바른 순서의 음원 재생
                        if (correctIndex == correctSequence.length - 1) { // 마지막 버튼을 클릭했는지 확인
                            currentIndex++; // 다음 가사로 이동
                            if (currentIndex < lyrics.length) { // 모든 가사를 완료했는지 확인
                                resetTextViewColors(); // TextView 색상 초기화
                                correctIndex = 0; // 올바른 인덱스 초기화
                                textViews[correctIndex].setTextColor(getResources().getColor(android.R.color.holo_purple)); // 첫 번째 눌러야 하는 버튼의 색상을 보라색으로 설정
                            } else {
                                showGameOverDialog(); // 게임이 완료되었을 때 처리
                            }
                        } else {
                            correctIndex++; // 다음 올바른 인덱스로 이동
                            if (correctIndex < correctSequence.length) {
                                textViews[correctIndex].setTextColor(getResources().getColor(android.R.color.holo_purple)); // 다음 눌러야 하는 버튼의 색상을 보라색으로 설정
                            }
                        }
                    } else {
                        textViews[index].setTextColor(getResources().getColor(android.R.color.holo_red_light)); // 잘못된 순서일 경우 텍스트 색상 변경
                        loseLife(); // 목숨 감소 메서드 호출
                        playSound("error"); // 잘못된 순서일 때 'error.mp3' 재생
                        // 오답 시 처리
                        // 예: 다시 시도하도록 유도하는 메시지 출력
                    }
                }

            });




            // LinearLayout에 버튼 추가
            if (i < 4) { // 첫 번째 줄에 4개의 버튼 추가
                buttonsLayout1.addView(textViews[i]);
            } else if(i>=4&&i<7){ // 두 번째 줄에 3개의 버튼 추가
                buttonsLayout2.addView(textViews[i]);
            } else if (i>=7&&i<12) {
                buttonsLayout3.addView(textViews[i]);
            } else if (i>=12&&i<16) {
                buttonsLayout4.addView(textViews[i]);
            } else if (i>=16&&i<19){
                buttonsLayout5.addView(textViews[i]);
            } else if (i>=19&&i<24) {
                buttonsLayout6.addView(textViews[i]);
            }
        }


        // 첫 번째 눌러야 하는 버튼의 색상을 보라색으로 설정
        textViews[correctIndex].setTextColor(getResources().getColor(android.R.color.holo_purple));
    }

    private void resetTextViewColors() {
        for (int i = 0; i < textViews.length; i++) {
            textViews[i].setTextColor(getResources().getColor(android.R.color.black)); // TextView 텍스트 색상을 검정색으로 설정하여 초기화
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
                soundResId = R.raw.piano08; // 솔 음원 파일 리소스 ID
                break;
            case "라":
                soundResId = R.raw.piano10; // 라 음원 파일 리소스 ID
                break;
            case "미":
                soundResId = R.raw.piano05; // 미 음원 파일 리소스 ID
                break;
            case "레":
                soundResId = R.raw.piano03; // 레 음원 파일 리소스 ID
                break;
            case "error":
                soundResId = R.raw.error; // error 음원 파일 리소스 ID
                break;
        }


        // MediaPlayer를 초기화하고 음원 재생
        MediaPlayer mediaPlayer = MediaPlayer.create(this, soundResId);
        mediaPlayer.start();
    }


    private void showGameOverDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Game Over!"); // 팝업 창 제목 설정
        builder.setMessage("게임 오버!").setCancelable(false); // 팝업 창 메시지 설정

        // "다시하기" 버튼 추가 및 클릭 리스너 설정
        builder.setPositiveButton("다시하기", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss(); // 팝업 창 닫기
                restartGame(); // 게임을 다시 시작하는 메서드 호출
            }
        });

        // "메뉴창으로 가기" 버튼 추가 및 클릭 리스너 설정
        builder.setNegativeButton("메뉴창으로 가기", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss(); // 팝업 창 닫기
//                goToMainMenu(); // 메인 메뉴로 이동하는 메서드 호출
            }
        });

        // AlertDialog 생성 및 표시
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
}

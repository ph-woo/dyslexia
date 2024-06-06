package com.example.eye_reading;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ShopActivity extends UserKeyActivity {
    private LinearLayout characterContainer;
    private Set<String> ownedCharacters = new HashSet<>();
    private String currentCharacter;
    private int bookmarks = 0;
    private DatabaseReference databaseReference;

    private TextView bookmarkCountText;


    String userkey="";


    // 현재 캐릭터 정보 뷰 선언
    private ImageView currentCharacterImage;
    private TextView currentCharacterName;
    private TextView currentCharacterDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);



//        Intent shopIntent = getIntent();
//        if (shopIntent != null && shopIntent.hasExtra("USERNAME")) {
//            nickname= shopIntent.getStringExtra("USERNAME");
//
//            Log.d("HomeAct", "Received nickname: " + nickname);
//        } else {
//            Log.e("HomeAct", "No nickname provided");
//        }
//
//        if (shopIntent != null && shopIntent.hasExtra("USERKEY")) {
//            userkey= shopIntent.getStringExtra("USERKEY");
//
//            Log.d("HomeAct", "Received userkey: " + userkey);
//        } else {
//            Log.e("HomeAct", "No userkey provided");
//        }

        userkey = getUserId();
        if (userkey != null) {
            // userId를 사용하여 필요한 작업 수행
            Log.d("MainActivity", "User ID: " + userkey);
        } else {
            // userId가 null이면 로그인 화면으로 이동
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }

        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> {
            Intent HomeIntent = new Intent(ShopActivity.this, HomeActivity.class);
//            HomeIntent.putExtra("USERNAME", nickname);
            HomeIntent.putExtra("USERKEY", userkey);
            startActivity(HomeIntent);
        });

        bookmarkCountText = findViewById(R.id.bookmark_count);


        databaseReference = FirebaseDatabase.getInstance("https://song-62299-default-rtdb.firebaseio.com/").getReference();
        loadBookmarkCount();
        characterContainer = findViewById(R.id.character_list);
//        currentCharacter = "강아지";
//        ownedCharacters = new HashSet<>();
//        ownedCharacters.add("강아지");
//        ownedCharacters.add("판다");

        // current character views 초기화
        currentCharacterImage = findViewById(R.id.current_character_image);
        currentCharacterName = findViewById(R.id.current_character_name);
        currentCharacterDescription = findViewById(R.id.current_character_description);


        fetchingCharacterData();
        addCharacterItems();
        System.out.println(currentCharacter);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void addCharacterItems() {
        List<Character> characters = getCharacters();
        characterContainer.removeAllViews();

        for (Character character : characters) {
            RelativeLayout characterLayout = (RelativeLayout) LayoutInflater.from(this)
                    .inflate(R.layout.character_item, characterContainer, false);

            ImageView characterImage = characterLayout.findViewById(R.id.character_image);
            TextView characterName = characterLayout.findViewById(R.id.character_name);
            TextView characterDescription = characterLayout.findViewById(R.id.character_description);
            ImageView bookmarkIcon = characterLayout.findViewById(R.id.character_bookmark_icon);
            TextView characterPrice = characterLayout.findViewById(R.id.character_price);
            TextView characterStatus = characterLayout.findViewById(R.id.character_status);

            characterImage.setImageResource(character.getImageResId());
            characterName.setText(character.getName());
            characterDescription.setText(character.getDescription());

            if (ownedCharacters.contains(character.getName())) {
                characterLayout.setBackground(currentCharacter != null && currentCharacter.equals(character.getName()) ?
                        getDrawable(R.drawable.rounded_green) : getDrawable(R.drawable.rounded_orange));
                characterStatus.setText(currentCharacter != null && currentCharacter.equals(character.getName()) ?
                        "장착중" : "보유중");
                characterStatus.setVisibility(View.VISIBLE);
                bookmarkIcon.setVisibility(View.GONE);
                characterPrice.setVisibility(View.GONE);
                characterLayout.setOnClickListener(v -> {
                    if (!character.getName().equals(currentCharacter)) {
                        showEquipDialog(character.getName());
                    }
                });
            } else {
                characterLayout.setBackground(getDrawable(R.drawable.rounded_basic));
                characterStatus.setVisibility(View.GONE);
                bookmarkIcon.setVisibility(View.VISIBLE);
                characterPrice.setText(String.valueOf(character.getPrice()));
                characterPrice.setVisibility(View.VISIBLE);
                characterLayout.setOnClickListener(v -> {
                    if (bookmarks >= character.getPrice()) {
                        showPurchaseDialog(character);
                    } else {
                        showInsufficientBookmarksDialog();
                    }
                });
            }
            characterContainer.addView(characterLayout);
        }
    }
    private void refreshCharacterItems() {
        addCharacterItems();
    }

    private void showEquipDialog(String characterName) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.shop_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setCancelable(false);
        AlertDialog alertDialog = builder.create();

        TextView title = dialogView.findViewById(R.id.title);
        TextView message = dialogView.findViewById(R.id.message);
        Button closeButton = dialogView.findViewById(R.id.close_btn);
        Button actionButton = dialogView.findViewById(R.id.action_btn);

        title.setText("캐릭터 장착");
        message.setText("이 캐릭터를 장착하시겠습니까?");
        actionButton.setText("장착하기");

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentCharacter = characterName;

                System.out.println(currentCharacter);
                databaseReference.child("Users").child(userkey).child("currentCharacter").setValue(currentCharacter);

                updateCurrentCharacterInfo();
                refreshCharacterItems();
                addCharacterItems();
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }

    private void showPurchaseDialog(Character character) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.shop_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setCancelable(false);
        AlertDialog alertDialog = builder.create();

        TextView title = dialogView.findViewById(R.id.title);
        TextView message = dialogView.findViewById(R.id.message);
        Button closeButton = dialogView.findViewById(R.id.close_btn);
        Button actionButton = dialogView.findViewById(R.id.action_btn);

        title.setText("캐릭터 구매");
        message.setText("이 캐릭터를 구매하시겠습니까?");
        actionButton.setText("구매하기");

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bookmarks -= character.getPrice();
                System.out.println(bookmarks);
                readBookmarkCount(bookmarks);
                ownedCharacters.add(character.getName());

                // 데이터베이스에서 'characters'의 크기를 가져와서 해당 인덱스에 추가
                databaseReference.child("Users").child(userkey).child("characters").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        long index = dataSnapshot.getChildrenCount();
                        databaseReference.child("Users").child(userkey).child("characters").child(String.valueOf(index)).setValue(character.getName());
                        refreshCharacterItems(); // 캐릭터 아이템을 새로고침하는 메서드 호출
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // 오류 처리
                    }
                });
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }

    private void showInsufficientBookmarksDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.shop_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setCancelable(false);
        AlertDialog alertDialog = builder.create();

        TextView title = dialogView.findViewById(R.id.title);
        TextView message = dialogView.findViewById(R.id.message);
        Button closeButton = dialogView.findViewById(R.id.close_btn);
        Button actionButton = dialogView.findViewById(R.id.action_btn);

        title.setText("책갈피 부족");
        message.setText("책갈피가 부족해요.");
        closeButton.setText("확인");
        actionButton.setVisibility(View.GONE);

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }

    private List<Character> getCharacters() {
        List<Character> characters = new ArrayList<>();
        characters.add(new Character(R.drawable.character_dog, "강아지", "강아지 강아지", 0));
        characters.add(new Character(R.drawable.character_pig, "돼지", "돼지 돼지", 10));
        characters.add(new Character(R.drawable.character_panda, "판다", "판다 판다", 20));
        characters.add(new Character(R.drawable.character_monkey, "원숭이", "원숭이 원숭이", 30));
        characters.add(new Character(R.drawable.character_giraffe, "기린", "기린 기린", 40));
        characters.add(new Character(R.drawable.character_milkcow, "젖소", "젖소 젖소", 50));
        characters.add(new Character(R.drawable.character_penguin, "펭귄", "펭귄 펭귄", 60));
        characters.add(new Character(R.drawable.character_tiger, "호랑이", "호랑이 호랑이", 70));
        characters.add(new Character(R.drawable.character_zebra, "얼룩말", "얼룩말 얼룩말", 80));
        characters.add(new Character(R.drawable.character_lion, "사자", "사자 사자", 90));
        return characters;
    }


    private void loadBookmarkCount() {
        if (userkey.isEmpty()) {
            Log.e("HomeAct", "User key is empty, cannot load bookmark count");
            return;
        }

        DatabaseReference bookmarkCountRef = databaseReference.child("Users").child(userkey).child("bookmarkcount");
        bookmarkCountRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Long bookmarkCount = dataSnapshot.getValue(Long.class);
                    if (bookmarkCount != null) {
                        bookmarkCountText.setText(String.valueOf(bookmarkCount));
                        bookmarks = Math.toIntExact(bookmarkCount);
                    } else {
                        Log.e("HomeAct", "Bookmark count is null");
                        bookmarkCountText.setText("0");
                    }
                } else {
                    Log.e("HomeAct", "Bookmark count does not exist");
                    bookmarkCountText.setText("0");
                }
            }



            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("HomeAct", "Database error: " + databaseError.getMessage());
                bookmarkCountText.setText("0");
            }
        });
    }

    private void readBookmarkCount(int bookmarks) {
        if (userkey.isEmpty()) {
            Log.e("HomeAct", "User key is empty, cannot update bookmark count");
            return;
        }

        DatabaseReference bookmarkCountRef = databaseReference.child("Users").child(userkey).child("bookmarkcount");
        bookmarkCountRef.setValue(bookmarks)
                .addOnSuccessListener(aVoid -> {
                    Log.d("HomeAct", "Bookmark count updated successfully");
                    bookmarkCountText.setText(String.valueOf(bookmarks));
                })
                .addOnFailureListener(e -> {
                    Log.e("HomeAct", "Failed to update bookmark count", e);
                });
    }


    public void fetchingCharacterData() {
        // 데이터베이스에서 currentCharacter와 ownedCharacters를 가져오기
        databaseReference.child("Users").child(userkey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // currentCharacter 값을 가져와 설정
                if (dataSnapshot.child("currentCharacter").exists()) {
                    currentCharacter = dataSnapshot.child("currentCharacter").getValue(String.class);
                } else {
                    currentCharacter = "기본 캐릭터"; // 기본 값 설정
                }
                System.out.println("현재 캐릭터: " + currentCharacter);

                // ownedCharacters 값을 가져와 설정
                if (dataSnapshot.child("characters").exists()) {
                    ownedCharacters.clear();
                    for (DataSnapshot characterSnapshot : dataSnapshot.child("characters").getChildren()) {
                        // 데이터베이스에서 문자열을 가져옴
                        String characterName = characterSnapshot.getValue(String.class);
                        ownedCharacters.add(characterName);
                    }
                } else {
                    ownedCharacters.clear();
                    ownedCharacters.add("기본 캐릭터1"); // 기본 값 설정
                    ownedCharacters.add("기본 캐릭터2");
                }
                System.out.println("소유한 캐릭터: " + ownedCharacters);

                // 현재 캐릭터 정보를 View에 업데이트
                updateCurrentCharacterInfo();

                // 필요한 경우 추가 작업
                refreshCharacterItems(); // 캐릭터 아이템을 새로고침하는 메서드 호출
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // 데이터베이스 접근 중 오류 발생 시 처리
                System.err.println("데이터 읽기 실패: " + databaseError.getMessage());
            }
        });
    }



    private void updateCurrentCharacterInfo() {
        if (currentCharacter != null) {
            Character character = getCharacterByName(currentCharacter);
            SharedPreferences sharedPreferences = getSharedPreferences("login_state", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.putString("CURRENTCHARACTER", String.valueOf(character));
            editor.apply();
            if (character != null) {
                currentCharacterImage.setImageResource(character.getImageResId());
                currentCharacterName.setText(character.getName());
                currentCharacterDescription.setText(character.getDescription());
            }
        }
    }

    // 캐릭터 이름으로 캐릭터 객체를 가져오는 헬퍼 메서드
    private Character getCharacterByName(String characterName) {
        for (Character character : getCharacters()) {
            if (character.getName().equals(characterName)) {
                return character;
            }
        }
        return null;
    }

    private static class Character {
        private final int imageResId;
        private final String name;
        private final String description;
        private final int price;

        public Character(int imageResId, String name, String description, int price) {
            this.imageResId = imageResId;
            this.name = name;
            this.description = description;
            this.price = price;
        }

        public int getImageResId() {
            return imageResId;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public int getPrice() {
            return price;
        }
    }
}

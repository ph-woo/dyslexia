package com.example.eye_reading;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ShopActivity extends AppCompatActivity {
    private LinearLayout characterContainer;
    private Set<String> ownedCharacters;
    private String currentCharacter;
    private int bookmarks = 25;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }

        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> onBackPressed());

        databaseReference = FirebaseDatabase.getInstance("https://song-62299-default-rtdb.firebaseio.com/").getReference();

        characterContainer = findViewById(R.id.character_list);
        currentCharacter = "강아지";
        ownedCharacters = new HashSet<>();
        ownedCharacters.add("강아지");
        ownedCharacters.add("판다");
        addCharacterItems();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void addCharacterItems() {
        List<Character> characters = getCharacters();

        for (Character character : characters) {
            RelativeLayout characterLayout = (RelativeLayout) LayoutInflater.from(this)
                    .inflate(R.layout.character_item, characterContainer, false);

            ImageView characterImage = characterLayout.findViewById(R.id.character_image);
            TextView characterName = characterLayout.findViewById(R.id.character_name);
            TextView characterDescription = characterLayout.findViewById(R.id.character_description);
            TextView characterPrice = characterLayout.findViewById(R.id.character_price);

            characterImage.setImageResource(character.getImageResId());
            characterName.setText(character.getName());
            characterDescription.setText(character.getDescription());
            characterPrice.setText(String.valueOf(character.getPrice()));

            if (ownedCharacters.contains(character.getName())) {
                characterLayout.setBackground(currentCharacter != null && currentCharacter.equals(character.getName()) ?
                        getDrawable(R.drawable.rounded_blue) : getDrawable(R.drawable.rounded_green));
                characterLayout.setOnClickListener(v -> {
                    if (!character.getName().equals(currentCharacter)) {
                        showEquipDialog(character.getName());
                    }
                });
            } else {
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

    private void showEquipDialog(String characterName) {
        new AlertDialog.Builder(this)
                .setTitle("캐릭터 장착")
                .setMessage("이 캐릭터를 장착하시겠습니까?")
                .setPositiveButton("장착하기", (dialog, which) -> {
                    currentCharacter = characterName;
                    addCharacterItems();
                })
                .setNegativeButton("창 닫기", null)
                .show();
    }

    private void showPurchaseDialog(Character character) {
        new AlertDialog.Builder(this)
                .setTitle("캐릭터 구매")
                .setMessage("이 캐릭터를 구매하시겠습니까?")
                .setPositiveButton("구매하기", (dialog, which) -> {
                    bookmarks -= character.getPrice();
                    ownedCharacters.add(character.getName());
                    addCharacterItems();
                })
                .setNegativeButton("창 닫기", null)
                .show();
    }

    private void showInsufficientBookmarksDialog() {
        new AlertDialog.Builder(this)
                .setTitle("책갈피 부족")
                .setMessage("책갈피가 부족해요.")
                .setPositiveButton("확인", null)
                .show();
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

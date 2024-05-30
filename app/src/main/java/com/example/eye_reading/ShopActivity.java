package com.example.eye_reading;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class ShopActivity extends AppCompatActivity {
    private LinearLayout characterContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }

        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> onBackPressed());

        ImageView navHome = findViewById(R.id.nav_home);
        ImageView navGame = findViewById(R.id.nav_game);
        ImageView navUser = findViewById(R.id.nav_user);

        navGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gameIntent = new Intent(ShopActivity.this, GameActivity.class);
                startActivity(gameIntent);
            }
        });

        characterContainer = findViewById(R.id.character_list);
        addCharacterItems();
    }

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

            characterContainer.addView(characterLayout);
        }
    }

    private List<Character> getCharacters() {
        List<Character> characters = new ArrayList<>();
        characters.add(new Character(R.drawable.character_dog, "강아지", "강아지 강아지", 10));
        characters.add(new Character(R.drawable.character_pig, "돼지", "돼지 돼지", 20));
        characters.add(new Character(R.drawable.character_panda, "판다", "판다 판다", 30));
        characters.add(new Character(R.drawable.character_monkey, "원숭이", "원숭이 원숭이", 40));
        characters.add(new Character(R.drawable.character_giraffe, "기린", "기린 기린", 50));
        characters.add(new Character(R.drawable.character_milkcow, "젖소", "젖소 젖소", 60));
        characters.add(new Character(R.drawable.character_penguin, "펭귄", "펭귄 펭귄", 70));
        characters.add(new Character(R.drawable.character_tiger, "호랑이", "호랑이 호랑이", 80));
        characters.add(new Character(R.drawable.character_zebra, "얼룩말", "얼룩말 얼룩말", 90));
        characters.add(new Character(R.drawable.character_lion, "사자", "사자 사자", 100));
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

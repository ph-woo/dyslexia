package com.example.eye_reading;

public class Song {
    private String title;
    private int isCleared; // -1: 잠김 / 0: 플레이 가능 / 1: 클리어

    public Song(String title, int isCleared) {
        this.title = title;
        this.isCleared = isCleared;
    }

    public String getTitle() {
        return title;
    }

    public int getCleared() {
        return isCleared;
    }

    public void setCleared(int isCleared) {
        this.isCleared = isCleared;
    }
}

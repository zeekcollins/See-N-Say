package edu.uwm.seensay;

import android.media.MediaPlayer;

public class Animal {
    private int imageNum;
    private String soundString;
    private final MediaPlayer animalSoundMP;

    public Animal(int imageNum, String soundString, MediaPlayer animalSoundMP) {
        this.imageNum = imageNum;
        this.soundString = soundString;
        this.animalSoundMP = animalSoundMP;
    }

    public int getImageNum() {
        return imageNum;
    }

    public String getSound(){
        return soundString;
    }

    public MediaPlayer getAnimalSoundMP() {
        return animalSoundMP;
    }
}

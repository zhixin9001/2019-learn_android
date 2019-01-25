package com.example.zhixin.beatbox;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

/**
 * Created by ZhiXin on 2019/1/9.
 */

public class SoundViewModel extends BaseObservable {
    private Sound mSound;
    private BeatBox mBeatBox;

    public SoundViewModel(BeatBox beatBox) {
        mBeatBox = beatBox;
    }

    @Bindable
    public String getTitle() {
        return mSound.getName();
    }

    public Sound getSound() {
        return mSound;
    }

    public void setSound(Sound sound) {
        mSound = sound;
        notifyChange();
    }

    public void onButtonClicked(){
        mBeatBox.play(mSound);
    }
}

package com.example.zhixin.beatbox;

/**
 * Created by ZhiXin on 2019/1/9.
 */

public class Sound {
    public Integer getSoundId() {
        return mSoundId;
    }

    public void setSoundId(Integer soundId) {
        mSoundId = soundId;
    }

    private Integer mSoundId;

    public Sound(String assetPath) {
        mAssetPath = assetPath;
        String[] components = assetPath.split("/");
        String filename = components[components.length - 1];
        mName = filename.replace(".wav", "");
    }

    public String getAssetPath() {
        return mAssetPath;
    }

    private String mAssetPath;

    public String getName() {
        return mName;
    }

    private String mName;
}

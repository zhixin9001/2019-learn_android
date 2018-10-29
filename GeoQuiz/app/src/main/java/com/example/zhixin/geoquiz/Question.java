package com.example.zhixin.geoquiz;

/**
 * Created by ZhiXin on 2018/10/25.
 */

public class Question {
    public int getTextResId() {
        return mTextResId;
    }

    public void setTextResId(int textResId) {
        mTextResId = textResId;
    }

    private int mTextResId;
    private boolean mAnswerTrue;

    public boolean isAnswerTrue() {
        return mAnswerTrue;
    }

    public void setAnswerTrue(boolean answerTrue) {
        mAnswerTrue = answerTrue;
    }

    public Question(int testResId, boolean answerTrue) {
        setTextResId(testResId);
        mAnswerTrue = answerTrue;
    }

}

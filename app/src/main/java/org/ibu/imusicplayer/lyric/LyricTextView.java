/**
 * Copyright 2019 Ibu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ibu.imusicplayer.lyric;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;
import org.ibu.imusicplayer.R;

import java.util.ArrayList;
import java.util.List;

public class LyricTextView extends TextView {
    private static final String TAG = "LyricTextView";

    public LyricPlayer mLyricPlayer;

    private String mLyric;
    private int mTextColor;
    private int mHighlightTextColor;

    private LyricDrawable mLyricDrawable;

    public LyricTextView(Context context) {
        this(context,null);
    }

    public LyricTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LyricTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public LyricTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        // in a condition when a.getIndexCount() is equals to zero (TypedArray a)
        mLyric = "";
        mTextColor = Color.GRAY;
        mHighlightTextColor = Color.RED;

        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.LyricTextView, defStyleAttr, defStyleRes);
        final int N = a.getIndexCount();
        for (int i = 0; i < N; i++) {
            int attr = a.getIndex(i);
            switch (attr) {
                case R.styleable.LyricTextView_lyric:
                    mLyric = a.getString(attr);
                    break;
                case R.styleable.LyricTextView_textColor:
                    mTextColor = a.getColor(attr, Color.GRAY);
                    break;
                case R.styleable.LyricTextView_highlightTextColor:
                    mHighlightTextColor = a.getColor(attr, Color.RED);
                    break;
                default:
                    break;
            }
        }
        mLyricPlayer = new LyricPlayer(context, mLyric);
        a.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
		Log.d(TAG,"onDraw(Canvas canvas)");
        super.onDraw(canvas);
        doOnDraw(canvas);
    }
    float mTouchStartY;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float eventX = event.getX();
        float eventY = event.getY();
        Log.d(TAG, eventX+"/"+eventY);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
				Log.d(TAG,"ACTION_DOWN on ("+eventY+")");
                mTouchStartY = eventY;
                break;
            case MotionEvent.ACTION_MOVE:
				Log.d(TAG,"ACTION_MOVE to ("+eventY+")");
                if(eventY < mTouchStartY - mLyricDrawable.mTextHeight){
                    if(mLyricPlayer.getCurNum() < mLyricPlayer.getProcessedLyricList().size()-7) {
                        mLyricPlayer.setCurNum(mLyricPlayer.getCurNum()+1);
                        Log.d(TAG,"mTouchStartY + 1*mLyricDrawable.mTextHeight");
                        invalidate();
                    }

                }else if(eventY > mTouchStartY + mLyricDrawable.mTextHeight){
                    if(mLyricPlayer.getCurNum() > 3) {
                        mLyricPlayer.setCurNum(mLyricPlayer.getCurNum() - 1);
                        Log.d(TAG, "mTouchStartY - 1*mLyricDrawable.mTextHeight");
                        invalidate();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                Log.d(TAG,"ACTION_UP on ("+eventY+")");
                // chick event
                if (mTouchStartY == eventY){
                    mOnLyricTouchListener.OnLyricTouched();
                }
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void invalidate() {
		Log.d(TAG,"invalidate()");
        super.invalidate();
    }

    private void doOnDraw(Canvas canvas){
		Log.d(TAG,"doOnDraw(Canvas canvas)");
        mLyricDrawable = new LyricDrawable(canvas);
        mLyricDrawable.doOnDraw();
    }

    private class LyricDrawable{
        float mTextHeight;

        float mCenterX;

        Canvas mCanvas;
        LyricDrawable(Canvas canvas){
            mCanvas = canvas;
            Log.d(TAG, "canvas width:"+canvas.getWidth()+"/height:"+ canvas.getHeight());
            mTextHeight = canvas.getHeight() / 7;
            mCenterX = canvas.getWidth()/2;
            Log.d(TAG, "mCenterX="+mCenterX+"/mTextHeight="+mTextHeight);
        }
        List<String> parseLyric(List<String> lyrics, int curNum){
			Log.d(TAG,"parse lyric:"+lyrics.toString()+"/curNum:"+curNum);
            List<String> temp = new ArrayList<>(lyrics);
            for (int i = 0; i < 7; i++) {
                temp.add(0, "");
            }
            for (int i = 0; i < 7; i++) {
                temp.add(temp.size()-1, "");
            }
//            int fromIndex = curNum;
//            if (curNum < 3){
//                for (int i = 0; i < 3-curNum; i++) {
//                    lyrics.add(0, "");
//                }
//                fromIndex = 3;
//            }else{
//                fromIndex = curNum - 3;
//            }
//            if(curNum+3 > temp.size()){
//                for (int i = 0; i < curNum+3-temp.size(); i++) {
//                    lyrics.add(temp.size()-1, "");
//                }
//            }
            return temp.subList(curNum+7-3, curNum+7+3+1);
        }
        void doOnDraw(){
			Log.d(TAG,"doOnDraw()");
            // paint text
            Paint textPaint = new Paint();
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setTextSize(50);
            textPaint.setColor(mTextColor);

            Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
            float baseline = (fontMetrics.bottom - fontMetrics.top)/2 - fontMetrics.bottom;

            List<String> tempLyric;
            if (mLyricPlayer.getProcessedLyricList().size() == 1){
               tempLyric = new ArrayList<>();
                tempLyric.add("");
                tempLyric.add("");
                tempLyric.add("");
                tempLyric.add("歌词不存在");
                tempLyric.add("");
                tempLyric.add("");
                tempLyric.add("");
            }else {
                tempLyric = parseLyric(mLyricPlayer.getProcessedLyricList(), mLyricPlayer.getCurNum());
            }
			Log.d(TAG,"parse lyric finished:"+tempLyric.toString());
            for (int i = 0; i < tempLyric.size(); i++) {
                textPaint.setColor(i==3 ? mHighlightTextColor: mTextColor);
                float centerH = (i*mTextHeight +(i+1)*mTextHeight)/2;
                mCanvas.drawText(tempLyric.get(i), mCenterX, centerH + baseline, textPaint);
            }
        }
    }

    public String getLyric() {
        return mLyric;
    }

    public void setLyric(String mLyric) {
        this.mLyric = mLyric;
        this.mLyricPlayer = new LyricPlayer(mLyric);
        mLyricPlayer.setLyricChangedListener(new LyricPlayer.OnLyricChangedListener(){

            @Override
            public void OnLyricChanged() {
				Log.d(TAG,"lyric changed");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        invalidate();
                    }
                }).start();
            }
        });
    }

    public int getTextColor() {
        return mTextColor;
    }

    public void setTextColor(int mTextColor) {
        this.mTextColor = mTextColor;
    }

    public int getHighlightTextColor() {
        return mHighlightTextColor;
    }

    public void setHighlightTextColor(int mHighlightTextColor) {
        this.mHighlightTextColor = mHighlightTextColor;
    }

    public void pause() {
        mLyricPlayer.pause();
    }

    public void stop(){
        mLyricPlayer.stop();
    }

    public void seekTo(int position){
        mLyricPlayer.seekTo(position);
    }

    OnLyricTouchListener mOnLyricTouchListener;

    public void setLyricTouchListener(OnLyricTouchListener l){
        mOnLyricTouchListener = l;
    }

    public interface OnLyricTouchListener{
        void OnLyricTouched();
    }
}

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
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import org.ibu.imusicplayer.util.DensityUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LyricPlayer {
    private static final String TAG = "LyricPlayer";
    class Line{
        int time;
        String txt;

        public Line(int time, String txt) {
            this.time = time;
            this.txt = txt;
        }
    }

    String lyric;

    List<Line> lineList;

    int curNum = 0;

    boolean isPlaying = false;

    Context context;

    Handler handler;

    DensityUtil densityUtil;
    public LyricPlayer(String lyric){
        this.lyric = lyric;
        this.handler = new Handler();
        densityUtil = new DensityUtil(context);
        initLines();
    }
    public LyricPlayer(Context context, String lyric){
        this.context = context;
        this.lyric = lyric;
        this.handler = new Handler();
        densityUtil = new DensityUtil(context);
        initLines();
    }

    private void initLines(){
        lineList = new ArrayList<>();
        String[] lines = this.lyric.split("\n");
        for (String line: lines) {
            Pattern pattern = Pattern.compile("\\[([0-9]*):([0-9]*)\\.([0-9]*)\\](.+?)");
            Matcher matcher = pattern.matcher(line);
            if(matcher.matches()) {
                String m1 = matcher.group(1);
                String m2 = matcher.group(2);
                String m3 = matcher.group(3);
                String txt = matcher.group(4);
                int time =  Integer.valueOf(m1) * 60 * 1000 + Integer.valueOf(m2) * 1000 + Integer.valueOf(m3);
                lineList.add(new Line(time, txt));
            }
        }

    }
    public String getLyric() {
        return lyric;
    }
    // 排除空行的歌词
    public String getProcessedLyric() {
        StringBuilder builder = new StringBuilder();
        for (Line line: lineList) {
            builder.append(line.txt+"\n");
        }
        return builder.toString();
    }

    List<String> getProcessedLyricList(){
        List<String> list = new ArrayList<>();
        for (Line line: lineList) {
            list.add(line.txt);
        }
        return list;
    }
    int getCurNum(){
        return this.curNum;
    }
    void setCurNum(int num){
        this.curNum = num;
    }
//    public void play(){
//        Log.d(TAG, "start play...");
//        isPlaying = true;
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while(isPlaying && curNum < lineList.size()-1) {
//                    curNum ++;
//                    Log.d(TAG, lineList.get(curNum).time+":"+lineList.get(curNum).txt);
//                    mOnLyricChangedListener.OnLyricChanged();
////                try {
//
////                        handler.post(new Runnable() {
////                            @Override
////                            public void run() {
////                                if(curNum >= 3){
////                                    ScrollView scrollView = ((DetailActivity)context).findViewById(R.id.song_lyric_scroll);
////                                    scrollView.scrollTo(0,densityUtil.dp2px(30*(curNum-3)));
////                                }
////                            }
////                        });
//                    // 延时
////                        int previousTime = lineList.get(curNum).time;
////                        int nextTime = lineList.get(curNum + 1).time;
////                        Thread.sleep(nextTime - previousTime);
//
//                } catch (InterruptedException e) {
//
//                }
//            }
//        }).start();
//    }

    public void pause() {
        isPlaying = false;
    }
    public void stop(){
        isPlaying = false;
        curNum = 0;
    }
    public void seekTo(int position){
        Log.d(TAG, "seekTo position:"+ position);
        for (int i = 0; i < lineList.size(); i++) {
            if (i<lineList.size()-1){
                int previousTime = lineList.get(i).time;
                int nextTime = lineList.get(i+1).time;
                Log.d(TAG, "seekTo ["+ previousTime+", "+nextTime+"]");
                if (position >= previousTime && position < nextTime){
                    curNum = i;
                    Log.d(TAG, "seekTo line:"+ curNum);
                    break;
                }
            }else{
                curNum = i;
                Log.d(TAG, "seekTo line"+ curNum);
            }

        }
        mOnLyricChangedListener.OnLyricChanged();
    }
    /**
     *
     */
    public void setLyricChangedListener(OnLyricChangedListener l){
        mOnLyricChangedListener = l;
    }

    private OnLyricChangedListener mOnLyricChangedListener;

    public interface OnLyricChangedListener{
        void OnLyricChanged();
    }
}

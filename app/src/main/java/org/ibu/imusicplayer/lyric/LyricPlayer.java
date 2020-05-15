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
    public static final String NO_LYRIC =  "[00:00.00]歌词不存在";
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
        if(lyric== null || lyric.equals("")){
            this.lyric = NO_LYRIC;
        }else{
            this.lyric = lyric;
        }

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
        if(lyric== null || lyric.equals("")){
            lineList.add(new Line(0, "歌词不存在"));
            return;
        }
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

    public void setLyricChangedListener(OnLyricChangedListener l){
        mOnLyricChangedListener = l;
    }

    private OnLyricChangedListener mOnLyricChangedListener;

    public interface OnLyricChangedListener{
        void OnLyricChanged();
    }
}

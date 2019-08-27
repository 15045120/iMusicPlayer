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
package org.ibu.imusicplayer;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LyricPlayer {
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
    LyricPlayer(Context context, String lyric){
        this.context = context;
        this.lyric = lyric;
        this.handler = new Handler();
        densityUtil = new DensityUtil(context);
        initLines();
    }
    void initLines(){
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
    // 排除空行的歌词
    public String getProcessedLyric() {
        StringBuilder builder = new StringBuilder();
        for (Line line: lineList) {
            builder.append(line.txt+"\n");
        }
        return builder.toString();
    }

    List getProcessedLyricList(){
        List<String> list = new ArrayList<>();
        for (Line line: lineList) {
            list.add(line.txt);
        }
        return list;
    }
    int getCurNum(){
        return this.curNum;
    }
    void play(){
//        isPlaying = true;
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while(isPlaying && curNum < lineList.size()-1) {
//                    try {
//                        Log.d("LYRIC_PLAY", lineList.get(curNum).time+":"+lineList.get(curNum).txt);
//                        handler.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                if(curNum >= 3){
//                                    ScrollView scrollView = ((DetailActivity)context).findViewById(R.id.song_lyric_scroll);
//                                    scrollView.scrollTo(0,densityUtil.dp2px(30*(curNum-3)));
//                                }
//                            }
//                        });
//                        // 延时
//                        int previousTime = lineList.get(curNum).time;
//                        int nextTime = lineList.get(curNum + 1).time;
//                        Thread.sleep(nextTime - previousTime);
//                    } catch (InterruptedException e) {
//
//                    }
//                    curNum ++;
//                }
//            }
//        }).start();
    }

    void pause() {
//        isPlaying = false;
    }
    void stop(){
//        isPlaying = false;
//        curNum = 0;
    }
    void seekTo(int msec){
//        for (int i = 0; i < lineList.size(); i++) {
//            if(lineList.get(i).time > msec){
//                curNum = i;
//                ScrollView scrollView = ((DetailActivity)context).findViewById(R.id.song_lyric_scroll);
//                scrollView.scrollTo(0,densityUtil.dp2px(30*(curNum-3)));
//                break;
//            }
//        }
    }
}

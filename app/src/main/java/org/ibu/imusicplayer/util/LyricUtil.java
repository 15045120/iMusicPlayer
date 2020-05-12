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
package org.ibu.imusicplayer.util;

import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 歌词工具类
 */
public final class LyricUtil {
    private LyricUtil(){}

    /* 将歌词[00:01.840]括号中的数字转换成毫秒 */
    public static int decodeTime(String time){
        Pattern pattern = Pattern.compile("\\[([0-9]*):([0-9]*)\\.([0-9]*)\\]");
        Matcher matcher = pattern.matcher(time);
        if(matcher.matches()) {
            String m1 = matcher.group(1);
            String m2 = matcher.group(2);
            String m3 = matcher.group(3);
            Log.d("IMUSICPLAYER", m1 + ":" + m2 + "." + m3);
            return Integer.valueOf(m1) * 60 * 1000 + Integer.valueOf(m2) * 1000 + Integer.valueOf(m3);
        }
        return 0;
    }

    /* 将毫秒转换成歌曲进度条数值02:33 */
    public static String encodeTime(int time){
        String m1 = "00";
        String m2 = "00";
        if(time / 60000 != 0){
            m1  = Integer.toString(time / 60000);
            if((time - (time/60000)*60000) / 1000 != 0){
                m2 = Integer.toString((time - (time/60000)*60000) / 1000);
            }
        }else{
            if(time / 1000 != 0){
                m2 = Integer.toString(time / 1000);
            }
        }
        return (m1.length()!=2?("0"+m1):m1) + ":" + (m2.length()!=2?("0"+m2):m2);
    }

}

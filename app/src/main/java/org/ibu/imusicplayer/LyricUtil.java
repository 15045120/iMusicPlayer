package org.ibu.imusicplayer;

import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LyricUtil {
    private LyricUtil(){}

    static int decodeTime(String time){
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
    static String encodeTime(int time){
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

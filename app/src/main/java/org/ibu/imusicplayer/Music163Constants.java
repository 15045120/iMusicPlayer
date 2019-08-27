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

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * music163常量类
 */
final class Music163Constants {
    private Music163Constants(){}
    /* music163网络请求API*/
    final static String music163SearchUrl = "https://music.163.com/api/search/get/web?limit=20&type=1&s=arg_s";
    final static String music163AlbumUrl = "https://music.163.com/api/song/detail?ids=[arg_id]";
    final static String music163Mp3Url = "https://music.163.com/api/song/enhance/player/url?br=128000&ids=[arg_id]";
    final static String music163LyricUrl = "https://music.163.com/api/song/lyric?lv=0&tv=0&id=arg_id";
    /* music163请求响应中用到的字段*/
    final static String RESPONSE_DATA_RESULT = "result";
    final static String RESPONSE_DATA_SONGS = "songs";
    final static String RESPONSE_DATA_ID = "id";
    final static String RESPONSE_DATA_ALBUM = "album";
    final static String RESPONSE_DATA_PICURL = "picUrl";
    final static String RESPONSE_DATA_ARTISTS = "artists";
    final static String RESPONSE_DATA_NAME = "name";
    final static String RESPONSE_DATA_LRC = "lrc";
    final static String RESPONSE_DATA_LYRIC = "lyric";
    final static String RESPONSE_DATA_DATA = "data";
    final static String RESPONSE_DATA_URL = "url";
    /* 网络请求响应流转换成字符串 */
    static String convertStreamToString(InputStream is){
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        try{
            while ((line = reader.readLine())!=null){
                sb.append(line+"\n");
            }
        }catch (IOException e){
            Log.d("IMUSICPLAYER_ERROR", e.getMessage());
        }finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

}

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
import java.util.List;

/**
 * qingtingFM常量
 */
public class QingtingFMConstants {
    private QingtingFMConstants(){}
    /* qingtingFM网络请求API*/
    final static String qingtingFMCategoryUrl = "https://rapi.qingting.fm/categories/arg_category/channels?with_total=true&page=1&pagesize=50";
    final static String qingtingFMPlayUrl = "https://lhttp.qingting.fm/live/arg_channel/64k.mp3";
    /* qingtingFM请求响应中用到的字段*/
    final static String RESPONSE_DATA_DATA = "Data";
    final static String RESPONSE_DATA_ITEMS = "items";
    final static String RESPONSE_DATA_CONTENTID = "content_id";
    final static String RESPONSE_DATA_COVER = "cover";
    final static String RESPONSE_DATA_DESCRIPTION = "description";
    final static String RESPONSE_DATA_TITLE = "title";
    /* qingtingFM目录用到的字段*/
    final static String QINGTING_FM_CATEGORIES = "{\n" +
            "        \"root\": [{\"id\": 0, \"title\": \"中央\"}, {\"id\": 1, \"title\": \"地方\"}, {\"id\": 2, \"title\": \"网络\"}],\n" +
            "        \"sub\": [\n" +
            "            [{\"id\": 409, \"title\": \"全部\"}],\n" +
            "            [{\"id\": 3, \"title\": \"北京\"}, {\"id\": 5, \"title\": \"天津\"}, {\"id\": 7, \"title\": \"河北\"}, {\"id\": 83, \"title\": \"上海\"},\n" +
            "        {\"id\": 19, \"title\": \"山西\"}, {\"id\": 31, \"title\": \"内蒙古\"}, {\"id\": 44, \"title\": \"辽宁\"}, {\"id\": 59, \"title\": \"吉林\"},\n" +
            "        {\"id\": 69, \"title\": \"黑龙江\"}, {\"id\": 85, \"title\": \"江苏\"}, {\"id\": 99, \"title\": \"浙江\"}, {\"id\": 111, \"title\": \"安徽\"},\n" +
            "        {\"id\": 129, \"title\": \"福建\"}, {\"id\": 139, \"title\": \"江西\"}, {\"id\": 151, \"title\": \"山东\"}, {\"id\": 169, \"title\": \"河南\"},\n" +
            "        {\"id\": 187, \"title\": \"湖北\"}, {\"id\": 202, \"title\": \"湖南\"}, {\"id\": 217, \"title\": \"广东\"}, {\"id\": 239, \"title\": \"广西\"},\n" +
            "        {\"id\": 254, \"title\": \"海南\"}, {\"id\": 257, \"title\": \"重庆\"}, {\"id\": 259, \"title\": \"四川\"}, {\"id\": 281, \"title\": \"贵州\"},\n" +
            "        {\"id\": 291, \"title\": \"云南\"}, {\"id\": 316, \"title\": \"陕西\"}, {\"id\": 327, \"title\": \"甘肃\"}, {\"id\": 351, \"title\": \"宁夏\"},\n" +
            "        {\"id\": 357, \"title\": \"新疆\"}, {\"id\": 308, \"title\": \"西藏\"}, {\"id\": 342, \"title\": \"青海\"}\n" +
            "            ],\n" +
            "            [{\"id\": 407, \"title\": \"全部\"}]\n" +
            "      ]\n" +
            "    }";
    final static String QINGTING_FM_CATEGORIES_ROOT = "root";
    final static String QINGTING_FM_CATEGORIES_SUB = "sub";
    final static String QINGTING_FM_CATEGORIES_ID = "id";
    final static String QINGTING_FM_CATEGORIES_TITLE = "title";
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

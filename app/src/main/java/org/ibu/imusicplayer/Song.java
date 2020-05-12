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

import android.graphics.Bitmap;

import java.io.Serializable;

/**
 * 歌曲实体类
 */
public class Song implements Serializable {
    private String id;      //歌曲ID
    private String title;   //歌曲标题
    private String singer;  //歌手名
    private String epname;  //专辑名
    private String picUrl;  //专辑封面URL
    private transient Bitmap bitmap;  //专辑封面
    private String mp3Url;  //音频源URL
    private String lyric;   //歌词

    /**
     * 同一个id的歌曲视为同一首歌
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        return id.equalsIgnoreCase(((Song)obj).id);
    }

    public Song(String id, String title, String singer, String epname, String picUrl) {
        this.id = id;
        this.title = title;
        this.singer = singer;
        this.epname = epname;
        this.picUrl = picUrl;
    }

    public Song(String id, String title, String singer, String epname, String picUrl, String mp3Url) {
        this.id = id;
        this.title = title;
        this.singer = singer;
        this.epname = epname;
        this.picUrl = picUrl;
        this.mp3Url = mp3Url;
    }
    public Song(String id, String title, String singer, String epname, String picUrl, String mp3Url, String lyric) {
        this.id = id;
        this.title = title;
        this.singer = singer;
        this.epname = epname;
        this.picUrl = picUrl;
        this.mp3Url = mp3Url;
        this.lyric = lyric;
    }
    public String getMp3Url() {
        return mp3Url;
    }

    public void setMp3Url(String mp3Url) {
        this.mp3Url = mp3Url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public String getEpname() {
        return epname;
    }

    public void setEpname(String epname) {
        this.epname = epname;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    public String getLyric() {
        return lyric;
    }

    public void setLyric(String lyric) {
        this.lyric = lyric;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}

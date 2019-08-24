package org.ibu.imusicplayer;

import java.io.Serializable;

public class Song implements Serializable {
    private String id;
    private String title;
    private String singer;
    private String epname;
    private String picUrl;
    private String mp3Url;

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
}

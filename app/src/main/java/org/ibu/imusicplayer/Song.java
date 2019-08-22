package org.ibu.imusicplayer;

public class Song {
    private String id;
    private String title;
    private String singer;
    private String epname;
    private String picUrl;

    public Song(String id, String title, String singer, String epname, String picUrl) {
        this.id = id;
        this.title = title;
        this.singer = singer;
        this.epname = epname;
        this.picUrl = picUrl;
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

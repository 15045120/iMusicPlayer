package org.ibu.imusicplayer;

final class Music163Contants {
    private Music163Contants(){}

    final static String music163SearchUrl = "https://music.163.com/api/search/get/web?limit=20&type=1&s=arg_s";
    final static String music163AlbumUrl = "https://music.163.com/api/song/detail?ids=[arg_id]";
    final static String music163Mp3Url = "https://music.163.com/api/song/enhance/player/url?br=128000&ids=[arg_id]";
    final static String music163LyricUrl = "https://music.163.com/api/song/lyric?lv=0&tv=0&id=arg_id";

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
}

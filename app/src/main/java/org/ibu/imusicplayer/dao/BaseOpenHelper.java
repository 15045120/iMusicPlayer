package org.ibu.imusicplayer.dao;

import org.ibu.imusicplayer.Song;

import java.util.List;

public interface BaseOpenHelper {
    void insert(Song song);
    void delete(String id);
    Song exist(String id);
    List<Song> queryAll();
}

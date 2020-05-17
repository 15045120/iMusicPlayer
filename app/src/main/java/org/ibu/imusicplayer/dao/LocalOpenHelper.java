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
package org.ibu.imusicplayer.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.MediaStore;
import android.util.Log;
import org.ibu.imusicplayer.Song;

import java.util.ArrayList;
import java.util.List;

public class LocalOpenHelper implements BaseOpenHelper{

    Context mContext;

    public LocalOpenHelper(Context context){
        mContext = context;
    }
    @Override
    public void insert(Song song) {

    }

    @Override
    public void delete(String id) {

    }

    @Override
    public void delete(Song song) {

    }

    @Override
    public Song exist(String id) {
        List<Song> songList = new ArrayList<>();
        Cursor cursor = mContext.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if(cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                if(id.equals(Integer.toString(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID))))){
                    String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    if(artist.equals("<unknown>")){
                        artist = "未知歌手";
                    }
                    return new Song(Integer.toString(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID))),
                            cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)),
                            artist,
                            cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)),
                            null,
                            cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
                }

                cursor.moveToNext();
            }
        }
        return null;
    }

    @Override
    public List<Song> queryAll() {
        List<Song> songList = new ArrayList<>();
        Cursor cursor = mContext.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                        null,
                                        null,
                                        null,
                                                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if(cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                if(artist.equals("<unknown>")){
                    artist = "未知歌手";
                }
                songList.add(new Song(Integer.toString(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID))),
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)),
                        artist,
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)),
                        null,
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))));
                cursor.moveToNext();
            }
        }
        return songList;
    }
}

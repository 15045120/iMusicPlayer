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
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import org.ibu.imusicplayer.Song;

import java.util.ArrayList;
import java.util.List;

/**
 * 操作下载表的数据库帮助类
 */
public class DownloadOpenHelper extends SQLiteOpenHelper implements BaseOpenHelper{
    private static final String TAG = "CollectOpenHelper";

    private static final int DATABASE_VERSION = 2;
    private static final String TABLE_NAME = "download";
    private static final String DATABASE_NAME = "imusicplayer4download.db";

    public DownloadOpenHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE download(id VACHAR(16) PRIMARY KEY,title VARCHAR(64),singer VARCHAR(64),epname VARCHAR(64),picUrl VARCHAR(64),mp3Url VARCHAR(64))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // when database version is changed, you need modify table here.
    }
    public void insert(Song song){
        SQLiteDatabase db = getWritableDatabase();
        Log.d("TAG","INSERT INTO download(id,title,singer,epname,picUrl,mp3Url) VALUES("
                +song.getId()+","+song.getTitle()+","+song.getSinger()+","+song.getPicUrl()+","+song.getEpname()+","+song.getMp3Url()+")");
        db.execSQL("INSERT INTO download(id,title,singer,epname,picUrl,mp3Url) VALUES(?,?,?,?,?,?)",
                new String[]{song.getId(),song.getTitle(),song.getSinger(),song.getEpname(),song.getPicUrl(),song.getMp3Url()});
    }
    public void delete(String id) {
        Log.d(TAG, "delete "+id);
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM download WHERE id = ?",
                new String[]{id});
    }
    public Song exist(String id){
        Log.d(TAG, "exist "+id);
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM download WHERE id = ?",
                new String[]{id});
        if(cursor.moveToFirst()){
            return new Song(cursor.getString(cursor.getColumnIndex("id")),
                    cursor.getString(cursor.getColumnIndex("title")),
                    cursor.getString(cursor.getColumnIndex("singer")),
                    cursor.getString(cursor.getColumnIndex("epname")),
                    cursor.getString(cursor.getColumnIndex("picUrl")),
                    cursor.getString(cursor.getColumnIndex("mp3Url"))
            );
        }
        cursor.close();
        return null;
    }
    public List<Song> queryAll(){
        Log.d(TAG, "queryAll ");
        List<Song> songList = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM download",null);
        while (cursor.moveToNext()){
            songList.add(new Song(cursor.getString(cursor.getColumnIndex("id")),
                    cursor.getString(cursor.getColumnIndex("title")),
                    cursor.getString(cursor.getColumnIndex("singer")),
                    cursor.getString(cursor.getColumnIndex("epname")),
                    cursor.getString(cursor.getColumnIndex("picUrl")),
                    cursor.getString(cursor.getColumnIndex("mp3Url"))));
        }

        cursor.close();
        return songList;
    }
}

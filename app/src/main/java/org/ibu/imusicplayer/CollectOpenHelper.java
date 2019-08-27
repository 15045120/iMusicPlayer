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

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * 操作收藏表的数据库帮助类
 */
public class CollectOpenHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2;
    private static final String COLLECT_TABLE_NAME = "collect";
    private static final String DATABASE_NAME = "imusicplayer.db";
    CollectOpenHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE collect(id VACHAR(16) PRIMARY KEY,title VARCHAR(64),singer VARCHAR(64),epname VARCHAR(64),picUrl VARCHAR(64))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // when database version is changed, you need modify table here.
    }
    public void insert(Song song){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO collect(id,title,singer,epname,picUrl) VALUES(?,?,?,?,?)",
                new String[]{song.getId(),song.getTitle(),song.getSinger(),song.getEpname(),song.getPicUrl()});
    }
    public void delete(String id) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM collect WHERE id = ?",
                new String[]{id});
    }
    public Song exist(String id){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM collect WHERE id = ?",
                new String[]{id});
        if(cursor.moveToFirst()){
            return new Song(cursor.getString(cursor.getColumnIndex("id")),
                    cursor.getString(cursor.getColumnIndex("title")),
                    cursor.getString(cursor.getColumnIndex("singer")),
                    cursor.getString(cursor.getColumnIndex("epname")),
                    cursor.getString(cursor.getColumnIndex("picUrl"))
                    );
        }
        cursor.close();
        return null;
    }
    public List<Song> queryAll(){
        List<Song> songList = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM collect",null);
        while (cursor.moveToNext()){
            songList.add(new Song(cursor.getString(cursor.getColumnIndex("id")),
                    cursor.getString(cursor.getColumnIndex("title")),
                    cursor.getString(cursor.getColumnIndex("singer")),
                    cursor.getString(cursor.getColumnIndex("epname")),
                    cursor.getString(cursor.getColumnIndex("picUrl"))));
        }

        cursor.close();
        return songList;
    }
}

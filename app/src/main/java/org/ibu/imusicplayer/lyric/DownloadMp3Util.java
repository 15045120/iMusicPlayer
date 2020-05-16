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
package org.ibu.imusicplayer.lyric;

import android.app.Activity;
import android.app.Notification;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import org.ibu.imusicplayer.EventListeners;
import org.ibu.imusicplayer.R;
import org.ibu.imusicplayer.Song;
import org.ibu.imusicplayer.player.DetailActivity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.ibu.imusicplayer.Music163Constants.*;

/**
 * 下载音频文件工具类
 */
public class DownloadMp3Util {
    private static final String TAG = "DownloadMp3Util";

    public final static String IMUSICPLAYER_MP3_DIR = "/storage/emulated/0/iMusicPlayer/source/";
    public final static String IMUSICPLAYER_ALBUM_DIR = "/storage/emulated/0/iMusicPlayer/album/";
    public final static String IMUSICPLAYER_LYRIC_DIR = "/storage/emulated/0/iMusicPlayer/lyric/";

    File mMp3Dir;
    File mAlbumDir;
    File mLyricDir;

    Activity mContext;

    Song mSong;
    public DownloadMp3Util(Activity context, Song song){
        mMp3Dir = new File(IMUSICPLAYER_MP3_DIR);
        mAlbumDir = new File(IMUSICPLAYER_ALBUM_DIR);
        mLyricDir = new File(IMUSICPLAYER_LYRIC_DIR);
        if(!mMp3Dir.exists()){
            mMp3Dir.mkdirs();
        }
        if(!mAlbumDir.exists()){
            mAlbumDir.mkdirs();
        }
        if(!mLyricDir.exists()){
            mLyricDir.mkdirs();
        }
        mSong = song;
        mContext = context;
    }

    class DownLoadMp3AsyncTask extends AsyncTask<String, Integer, Boolean> {
        private EventListeners mmContext;
        DownLoadMp3AsyncTask(EventListeners context){
            mmContext = context;
        }

        /* 下载主要实现方法 */
        @Override
        protected Boolean doInBackground(String... params) {
            try {
                // 下载mp3
                URL url = new URL(params[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                InputStream mis = connection.getInputStream();
                File tempDir = new File(mMp3Dir + "/" + mSong.getTitle() + ".mp3");
                Log.d(TAG,mMp3Dir + mSong.getTitle() + ".mp3");
                FileOutputStream fos = new FileOutputStream(tempDir);
                byte[] buffer = new byte[1024];
                int mCount = 0;
                while((mCount = mis.read(buffer)) > 0){
                    fos.write(buffer);
                }
                mis.close();
                fos.close();

                // 下载封面
                Bitmap bitmap = mSong.getBitmap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                InputStream albumInputStream = new ByteArrayInputStream(baos.toByteArray());
                Log.d(TAG, mAlbumDir + "/" + mSong.getTitle() + ".jpg");
                File tempAlbumDir = new File(mAlbumDir + "/" + mSong.getTitle() + ".jpg");
                FileOutputStream albumFOS = new FileOutputStream(tempAlbumDir);
                buffer = new byte[1024];
                mCount = 0;
                while((mCount = albumInputStream.read(buffer)) > 0){
                    albumFOS.write(buffer);
                }
                albumFOS.flush();
                albumInputStream.close();
                albumFOS.close();
                // 扫描媒体库
                MediaScannerConnection.scanFile((Activity)mmContext, new String[]{tempDir.getAbsolutePath()}, null,
                        new MediaScannerConnection.OnScanCompletedListener(){

                            @Override
                            public void onScanCompleted(String path, Uri uri) {
                                Log.d(TAG, "onScanCompleted");
                            }
                        });
                return true;
            }catch (Exception e){
                return false;
            }
        }
        /* 下载完成通知 */
        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            DetailActivity activity = (DetailActivity)mmContext;
            mmContext.onDownloadFinished(mSong);
        }
    }

    public boolean download(){
        boolean result = false;
        String lyric = mSong.getLyric();
        if(mSong.getMp3Url() == null || lyric == null){
            return false;
        }else{
            try {
                // 下载歌词
                File tempLyricDir = new File(mLyricDir + "/" + mSong.getTitle() + ".lrc");
                FileOutputStream lyricFOS = new FileOutputStream(tempLyricDir);
                lyricFOS.write(lyric.getBytes());
                lyricFOS.flush();
                lyricFOS.close();
                DownLoadMp3AsyncTask  downLoadMp3AsyncTask= new DownLoadMp3AsyncTask((EventListeners)mContext);
                downLoadMp3AsyncTask.execute(mSong.getMp3Url(), mSong.getPicUrl());
                result = true;
            }catch(IOException e){
                Log.d("IMUSICPLAYER_ERROR",e.getMessage());
                e.printStackTrace();
            }
            return result;
        }
    }
    public void search(){
        // 查找歌词
        new Thread(new Runnable() {
            @Override
            public void run() {
                String lyricUrl = music163LyricUrl.replace("arg_id", mSong.getId());
                Log.d("IMUSICPLAYER_LYRIC", lyricUrl);
                try{
                    URL url = new URL(lyricUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.connect();
                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        InputStream inputStream = connection.getInputStream();
                        String result = convertStreamToString(inputStream);
                        Log.d("IMUSICPLAYER_LYRIC", result);
                        JSONObject obj = new JSONObject(result);
                        String tempLyric = "";
                        try {
                            tempLyric = obj.getJSONObject(RESPONSE_DATA_LRC).getString(RESPONSE_DATA_LYRIC);
                        }catch (JSONException e){
                            Log.d("SEARCH_ERROR", e.getMessage());
                            e.printStackTrace();
                        }
                        final String lyric = tempLyric;
//                        LyricPlayer lyricPlayer = new LyricPlayer(mContext, lyric);
                        mSong.setLyric(lyric);
                        // 请求音乐封面
                        URL mUrl = new URL(mSong.getPicUrl());
                        HttpURLConnection mConnection = (HttpURLConnection) mUrl.openConnection();
                        mConnection.setRequestMethod("GET");
                        mConnection.connect();
                        int mResponseCode = mConnection.getResponseCode();
                        if (mResponseCode == HttpURLConnection.HTTP_OK) {
                            InputStream mInputStream = mConnection.getInputStream();
                            final Bitmap bitmap = BitmapFactory.decodeStream(mInputStream);
                            mSong.setBitmap(bitmap);
                            mContext.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ((EventListeners)mContext).onSearchFinished(mSong);
                                }
                            });
                        }
                    }else{
//                        DetailActivity.this.runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                Toast.makeText(DetailActivity.this, "歌曲文件不存在",Toast.LENGTH_SHORT).show();
//                            }
//                        });
                    }
                }catch(Exception e){
                    Log.d("SEARCH_ERROR", e.getMessage());
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public InputStream readAlbumImage(){
        InputStream is = null;
        try {
            is = new FileInputStream(new File(mAlbumDir + "/" + mSong.getTitle() + ".jpg"));
        }catch (FileNotFoundException e){
            return mContext.getClass().getResourceAsStream("/res/drawable/ic_default_artwork.png");
        }finally {
            return is;
        }
    }

    public String readLyric(){
        String content = "";
        InputStream is = null;
        try{
            is = new FileInputStream(new File(mLyricDir + "/" + mSong.getTitle() + ".lrc"));
            byte[] bytes = new byte[1024];
            ByteArrayOutputStream byteArray =  new ByteArrayOutputStream();
            int len = 0;
            while(-1 != (len = is.read(bytes))){
                byteArray.write(bytes, 0, len);
            }
            if(is != null){
                is.close();
            }
            content =  byteArray.toString();
        }catch(FileNotFoundException e0){
            e0.printStackTrace();
            return "";
        } finally {
            return content;
        }
    }
}

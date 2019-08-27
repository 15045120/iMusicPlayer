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
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.*;

/**
 * 下载音频文件工具类
 */
public class DownloadMp3Util {

    final static String IMUSICPLAYER_MP3_DIR = "/storage/emulated/0/iMusicPlayer/source/";
    final static String IMUSICPLAYER_ALBUM_DIR = "/storage/emulated/0/iMusicPlayer/album/";
    final static String IMUSICPLAYER_LYRIC_DIR = "/storage/emulated/0/iMusicPlayer/lyric/";

    File mMp3Dir;
    File mAlbumDir;
    File mLyricDir;

    Context mContext;

    Song mSong;
    DownloadMp3Util(Context context, Song song){
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
        private Context mmContext;
        DownLoadMp3AsyncTask(Context context){
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
                File tempDir = new File(mMp3Dir + "/" + mSong.getSinger() + "-" + mSong.getTitle() + ".mp3");
                Log.d("IMUSICPLAYER_DOWNLOAD",mMp3Dir + mSong.getSinger() + "-" + mSong.getTitle() + ".mp3");
                FileOutputStream fos = new FileOutputStream(tempDir);
                int one = 0;
                while ((one = mis.read()) != -1) {
                    fos.write(one);
                }
                mis.close();
                fos.close();
                // 下载封面
                URL mUrl = new URL(params[1]);
                HttpURLConnection mConnection = (HttpURLConnection) mUrl.openConnection();
                mConnection.setRequestMethod("GET");
                mConnection.connect();
                InputStream albumInputStream = mConnection.getInputStream();
                Log.d("IMUSICPLAYER_DOWNLOAD", mAlbumDir + "/" + mSong.getSinger() + "-" + mSong.getTitle() + ".jpg");
                File tempAlbumDir = new File(mAlbumDir + "/" + mSong.getSinger() + "-" + mSong.getTitle() + ".jpg");
                FileOutputStream albumFOS = new FileOutputStream(tempAlbumDir);
                int mOne = 0;
                while ((mOne = albumInputStream.read()) != -1) {
                    albumFOS.write(mOne);
                }
                albumFOS.flush();
                albumInputStream.close();
                albumFOS.close();
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
            if(result){
                activity.downloadIcon.setVisibility(View.GONE);
                activity.downloadOpenHelper.insert(mSong);
                Toast.makeText(activity, mSong.getSinger()+"-"+mSong.getTitle()+".mp3"+"已保存至"+IMUSICPLAYER_MP3_DIR, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(activity, "歌曲下载失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public boolean downloadToStorage(String lyric){
        boolean result = false;
        if(mSong.getMp3Url() == null || lyric == null){
            return false;
        }else{
            try {
                // 下载歌词
                File tempLyricDir = new File(mLyricDir + "/" + mSong.getSinger() + "-" + mSong.getTitle() + ".lrc");
                FileOutputStream lyricFOS = new FileOutputStream(tempLyricDir);
                lyricFOS.write(lyric.getBytes());
                lyricFOS.flush();
                lyricFOS.close();
                DownLoadMp3AsyncTask  downLoadMp3AsyncTask= new DownLoadMp3AsyncTask(mContext);
                downLoadMp3AsyncTask.execute(mSong.getMp3Url(), mSong.getPicUrl());
                result = true;
            }catch(IOException e){
                Log.d("IMUSICPLAYER_ERROR",e.getMessage());
                e.printStackTrace();
            }
            return result;
        }
    }

    public InputStream readAlbumImage(){
        InputStream is = null;
        try {
            is = new FileInputStream(new File(mAlbumDir + "/" + mSong.getSinger() + "-" + mSong.getTitle() + ".jpg"));
        }catch (FileNotFoundException e){

        }finally {
            return is;
        }
    }

    public String readLyric(){
        String content = "";
        InputStream is = null;
        try{
            is = new FileInputStream(new File(mLyricDir + "/" + mSong.getSinger() + "-" + mSong.getTitle() + ".lrc"));
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
        } finally {
            return content;
        }
    }
}

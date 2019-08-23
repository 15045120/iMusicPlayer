package org.ibu.imusicplayer;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadMp3Util {

    final static String IMUSICPLAYER_DOWNLOAD_DIR = "/storage/emulated/0/iMusicPlayer/download/";
    File mRootDir;
    Song mSong;
    DownloadMp3Util(Context context, Song song){
        mRootDir = new File(IMUSICPLAYER_DOWNLOAD_DIR);
        if(!mRootDir.exists()){
            mRootDir.mkdirs();
        }
        mSong = song;
    }

    public boolean downloadToStorage(){
        if(mSong.getMp3Url() == null){
            return false;
        }else{
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // 下载
                        URL url = new URL(mSong.getMp3Url());
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        InputStream mis = connection.getInputStream();
                        File tempDir = new File(mRootDir + "/" + mSong.getTitle() + ".mp3");
                        Log.d("IMUSICPLAYER_DOWNLOAD",mRootDir + "/" + mSong.getTitle() + ".mp3");
                        FileOutputStream fos = new FileOutputStream(tempDir);
                        int one = 0;
                        while ((one = mis.read()) != -1) {
                            fos.write(one);
                        }
                        mis.close();
                        fos.close();
                        // 写入数据库
                    }catch (IOException e){
                        Log.d("IMUSICPLAYER_ERROR",e.getMessage());
                        e.printStackTrace();
                    }
                }
            }).start();
            return true;
        }
    }
}

package org.ibu.imusicplayer.lyric;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import org.ibu.imusicplayer.EventListeners;
import org.ibu.imusicplayer.R;
import org.ibu.imusicplayer.Song;
import org.ibu.imusicplayer.player.DetailActivity;

import java.io.*;

public class LocalMp3Util {
    Activity mContext;
    Song mSong;

    public LocalMp3Util(Activity context, Song song){
        mSong = song;
        mContext = context;
    }

    public void search(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = BitmapFactory.decodeStream(getClass().getResourceAsStream("/res/drawable/ic_launcher.png"));
                mSong.setBitmap(bitmap);
                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((EventListeners)mContext).onSearchFinished(mSong);
                    }
                });
            }
        }).start();
    }
}

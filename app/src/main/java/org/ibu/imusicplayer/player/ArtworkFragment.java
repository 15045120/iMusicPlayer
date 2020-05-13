package org.ibu.imusicplayer.player;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import org.ibu.imusicplayer.R;
import org.ibu.imusicplayer.lyric.DownloadMp3Util;
import org.ibu.imusicplayer.Song;
import org.ibu.imusicplayer.dao.CollectOpenHelper;
import org.ibu.imusicplayer.dao.DownloadOpenHelper;

public class ArtworkFragment extends SimpleFragment {
    private static final String TAG = "ArtworkFragment";

    ImageView songPicImageView;         // 专辑封面
//    ImageView collectIcon;              // 收藏按钮
//    ImageView downloadIcon;             // 下载按钮
//    TextView downloadIconText;          // 下载按钮文字

    Song mSong;
    /* 数据库操作帮助类*/
    CollectOpenHelper dbHelper;
    DownloadOpenHelper downloadOpenHelper;
    /* android6以上动态申请读写权限请求码*/
    int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 2;

    Activity detailActivity;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        detailActivity = getActivity();
        Bundle args = getArguments();
        mSong = (Song) args.get("song");
        View view = inflater.inflate(R.layout.fragment_artwork, null);
        // 初始化歌曲封面
        songPicImageView = view.findViewById(R.id.song_pic);
        // 初始化是否收藏
        dbHelper = new CollectOpenHelper(detailActivity);
//        collectIcon = view.findViewById(R.id.collect_icon);
//        // 初始化下载按钮
//        downloadIcon = view.findViewById(R.id.download_icon);
        downloadOpenHelper = new DownloadOpenHelper(detailActivity);
        // 初始化下载按钮文字
//        downloadIconText = view.findViewById(R.id.download_icon_text);
//        // 监听收藏按钮
//        collectIcon.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if(dbHelper.exist(mSong.getId()) == null) {
//                    dbHelper.insert(mSong);
//                    collectIcon.setImageResource(R.drawable.ic_collected_red_24dp);
//                    Toast.makeText(detailActivity, "已收藏", Toast.LENGTH_SHORT).show();
//                }else {
//                    dbHelper.delete(mSong.getId());
//                    collectIcon.setImageResource(R.drawable.ic_collect_black_24dp);
//                    Toast.makeText(detailActivity, "取消收藏", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//
//        // 初始化下载按钮
//        downloadIcon.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (ContextCompat.checkSelfPermission(detailActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                        != PackageManager.PERMISSION_GRANTED) {
//                    //申请WRITE_EXTERNAL_STORAGE权限
//                    ActivityCompat.requestPermissions(detailActivity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                            WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
//                }else{
//                    if(downloadOpenHelper.exist(mSong.getId()) == null) {
//                        Toast.makeText(detailActivity, "正在后台下载歌曲，请耐心等待", Toast.LENGTH_LONG).show();
//                        downloadIconText.setVisibility(View.VISIBLE);
//                        // 开始下载
//                        DownloadMp3Util downloadMp3Util = new DownloadMp3Util(detailActivity, mSong);
//                        downloadMp3Util.download();
//                    }else {
//                        Toast.makeText(detailActivity, "歌曲已下载", Toast.LENGTH_SHORT).show();
//                    }
//
//                }
//            }
//        });
        //update(args);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        Log.d(TAG, "onAttach");
        super.onAttach(context);
    }

    void download(){
//        if(downloadOpenHelper.exist(mSong.getId()) == null) {
//            Toast.makeText(detailActivity, "正在后台下载歌曲，请耐心等待", Toast.LENGTH_LONG).show();
//            downloadIconText.setVisibility(View.VISIBLE);
//            // 开始下载
//            DownloadMp3Util downloadMp3Util = new DownloadMp3Util(detailActivity, mSong);
//            downloadMp3Util.download();
//        }else {
//            Toast.makeText(detailActivity, "歌曲已下载", Toast.LENGTH_SHORT).show();
//        }
    }
    @Override
    void update(Bundle bundle) {
        Log.d(TAG, "update");
        mSong = (Song) bundle.get("song");
//        if(mSong.getBitmap() == null){
//            Bitmap bitmap = BitmapFactory.decodeStream(getClass().getResourceAsStream("/res/drawable/ic_launcher.png"));
//            Bitmap bitmap =  BitmapFactory.decodeResource(detailActivity.getApplicationContext().getResources(), R.mipmap.ic_launcher);
//            mSong.setBitmap(bitmap);
//        }
        songPicImageView.setImageBitmap(mSong.getBitmap());
//        // 初始化是否收藏
//        if(dbHelper.exist(mSong.getId()) == null) {
//            collectIcon.setImageResource(R.drawable.ic_collect_black_24dp);
//        }else {
//            collectIcon.setImageResource(R.drawable.ic_collected_red_24dp);
//        }
//        // 插入歌曲
//        boolean downloaded = bundle.getBoolean("downloaded",false);
//        if (downloaded){
//            downloadOpenHelper.insert(mSong);
//            downloadIconText.setVisibility(View.INVISIBLE);
//        }
//        // 从已下载歌词中加载
//        if(downloadOpenHelper.exist(mSong.getId()) == null) {
//            downloadIcon.setImageResource(R.drawable.ic_file_download_black_24dp);
//        }else {
//            downloadIcon.setImageResource(R.drawable.ic_file_download_red_24dp);
//        }
    }
}

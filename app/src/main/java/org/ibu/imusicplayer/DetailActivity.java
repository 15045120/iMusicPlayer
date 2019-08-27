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

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static org.ibu.imusicplayer.DownloadMp3Util.IMUSICPLAYER_MP3_DIR;
import static org.ibu.imusicplayer.LyricUtil.encodeTime;
import static org.ibu.imusicplayer.Music163Constants.*;

/**
 * 歌曲详情和播放页面Activity
 */
public class DetailActivity extends AppCompatActivity {
    /* Activity传入参数 */
    final static String DETAIL_CURRENT_SONG = "currentSong";
    final static String DETAIL_SONG_LIST = "songList";
    /* Activity传入参数接收器 */
    Song mSong;
    List<Song> mSongList;
    /* 音乐播放类 */
    MediaPlayer mediaPlayer;
    /* 播放进度条 */
    SeekBar seekBar;
    TextView seekBarCurrentValue;
    TextView seekBarMaxValue;
    /* timer执行定时任务，播放进度条每秒更新1次 */
    Timer timer;
    /* 加载图标 */
    LinearLayout loadingBlock;
    RelativeLayout loadedBlock;
    /* android6以上动态申请读写权限请求码*/
    int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 2;
    /* 数据库操作帮助类*/
    CollectOpenHelper dbHelper;
    DownloadOpenHelper downloadOpenHelper;
    /* 是否正在播放 */
    boolean isPlaying = false;
    /* 歌词播放器 */
    LyricPlayer lyricPlayer;
    /* 页面视图 */
    TextView songTitleTextView;         // 歌曲标题
    TextView songSingerEpnameTextView;  // 歌手名-专辑名
    TextView songLyricTextView;         // 歌词
    ImageView songPicImageView;         // 专辑封面
    ImageView collectIcon;              // 收藏按钮
    ImageView downloadIcon;             // 下载按钮
    ImageView playIcon;                 // 播放按钮

    /**
     * 初始化seekBar
     * 问题：使用正则表达式提取歌词最后一个时间（这里不准确）
     *       String lastTime = lyric.substring(lyric.lastIndexOf('['), lyric.lastIndexOf(']')+1);
     *       maxValue = decodeTime(lastTime);
     */
    void initSeekBar(){
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            // todo 监听滑动条滑动事件不要重写这个方法，因为调用setProgress时也会调用这个方法
            @Override
            public void onProgressChanged(final SeekBar seekBar, final int i, boolean b) {
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(final SeekBar seekBar) {
                if(isPlaying){
                    if(mediaPlayer == null){
                        searchMp3Url();
                    }else{
                        final int i = seekBar.getProgress();
                        mediaPlayer.seekTo(i);
                        lyricPlayer.seekTo(i);
                        mediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                            @Override
                            public void onSeekComplete(MediaPlayer m) {
                                // 移除所有任务
                                if(timer !=null) {
                                    timer.purge();
                                    timer = null;
                                }

                                mediaPlayer.start();
                                lyricPlayer.play();
                                timer = new Timer();
                                timer.schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        if(mediaPlayer!=null) {
                                            try {
                                                seekBar.setProgress(mediaPlayer.getCurrentPosition());
                                            }catch (IllegalStateException e){
                                                Log.d("IMUSICPLAYER_ILLEAGL", "mediaPlayer throws IllegalStateException for in end state");
                                            }
                                        }
                                    }
                                },0, 1000);
                            }
                        });
                        seekBarCurrentValue.setText(encodeTime(i));
                    }
                }
            }
        });
    }

    /**
     * 初始化视图内容
     */
    void initViewContent(){
        // 初始化歌曲名信息
        songTitleTextView.setText(mSong.getTitle());
        songSingerEpnameTextView.setText(mSong.getSinger()+" - "+mSong.getEpname());
        // 初始化是否收藏
        if(dbHelper.exist(mSong.getId()) == null) {
            collectIcon.setImageResource(R.drawable.ic_collect_black_24dp);
        }else {
            collectIcon.setImageResource(R.drawable.ic_collected_red_24dp);
        }
        // 初始化seekBar
        seekBarCurrentValue.setText("0:00");
        seekBarMaxValue.setText("0:00");
        // 从已下载歌词中加载
        if(downloadOpenHelper.exist(mSong.getId()) != null){
            searchLyricAndAlbumImageFromStorage();
        }else {
           searchLyricAndAlbumImageFromNetwork();
        }

    }
    /**
     * 从本地中加载歌词和专辑封面
     */
    void searchLyricAndAlbumImageFromStorage(){
        DownloadMp3Util downloadMp3Util = new DownloadMp3Util(DetailActivity.this, mSong);
        // 初始化封面
        InputStream mInputStream = downloadMp3Util.readAlbumImage();
        final Bitmap bitmap = BitmapFactory.decodeStream(mInputStream);
        songPicImageView.setImageBitmap(bitmap);
        // 初始化歌词
        lyricPlayer = new LyricPlayer(DetailActivity.this, downloadMp3Util.readLyric());
        songLyricTextView.setText(lyricPlayer.getProcessedLyric());

        initSeekBar();
        loadingBlock.setVisibility(View.GONE);
        loadedBlock.setVisibility(View.VISIBLE);
    }

    String mLyricForDownload;
    /**
     * 从网络中加载歌词和专辑封面
     */
    void searchLyricAndAlbumImageFromNetwork(){
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
                        final String lyric = obj.getJSONObject(RESPONSE_DATA_LRC).getString(RESPONSE_DATA_LYRIC);
                        mLyricForDownload = lyric;
                        // 请求音乐封面
                        URL mUrl = new URL(mSong.getPicUrl());
                        HttpURLConnection mConnection = (HttpURLConnection) mUrl.openConnection();
                        mConnection.setRequestMethod("GET");
                        mConnection.connect();
                        int mResponseCode = mConnection.getResponseCode();
                        if (mResponseCode == HttpURLConnection.HTTP_OK) {
                            InputStream mInputStream = mConnection.getInputStream();
                            final Bitmap bitmap = BitmapFactory.decodeStream(mInputStream);
                            DetailActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    songPicImageView.setImageBitmap(bitmap);
                                }
                            });
                        }
                        // 在子线程中更新UI
                        DetailActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // 初始化歌词
                                lyricPlayer = new LyricPlayer(DetailActivity.this, lyric);
                                songLyricTextView.setText(lyricPlayer.getProcessedLyric());

                                initSeekBar();
                                loadingBlock.setVisibility(View.GONE);
                                loadedBlock.setVisibility(View.VISIBLE);
                            }
                        });
                    }else{
                        DetailActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(DetailActivity.this, "歌曲文件不存在",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }catch(Exception e){
                    Log.d("SEARCH_ERROR", e.getMessage());
                    e.printStackTrace();
                }
            }
        }).start();
    }

    class PlayButtonListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            // 显示播放按钮
            if(!isPlaying){
                Log.d("IMUSICPLAYER_MP3","isPlaying=false");
                //暂停转为播放状态
                if(mediaPlayer!=null){
                    // 更新按钮图片
                    playIcon.setImageResource(R.drawable.ic_pause_black_24dp);
                    isPlaying = true;
                    mediaPlayer.start();
                    lyricPlayer.play();
                    // 定时任务，每一秒更新一下seekBar
                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if(mediaPlayer!=null) {
                                try {
                                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                                    seekBarCurrentValue.setText(encodeTime(mediaPlayer.getCurrentPosition()));
                                }catch (IllegalStateException e){
                                    Log.d("IMUSICPLAYER_ILLEAGL", "mediaPlayer throws IllegalStateException for in end state");
                                }
                            }
                        }
                    },0, 1000);
                    return;
                }else{//刚进入该页面
                    searchMp3Url();
                }
            }else{// 显示暂停按钮
                Log.d("IMUSICPLAYER_MP3","isPlaying=true");
                if(mediaPlayer != null && mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                    lyricPlayer.pause();
                    // 更新按钮图片
                    playIcon.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                    isPlaying = false;
                    // 移除所有任务
                    if(timer !=null) {
                        timer.purge();
                        timer = null;
                    }
                }
            }
        }
    }
    View.OnClickListener playButtonListener;
    View.OnClickListener nextButtonListener;

    /**
     * 初始化视图
     */
    void initView(){
        // 初始化歌曲名信息
        songTitleTextView = findViewById(R.id.song_title);
        songSingerEpnameTextView = findViewById(R.id.song_singer_epname);
        // 初始化歌词
        songLyricTextView = findViewById(R.id.song_lyric_text);
        // 使歌词能滚动
        songLyricTextView.setMovementMethod(ScrollingMovementMethod.getInstance());

//        lyricListView = findViewById(R.id.song_lyric_list);
        // 初始化歌曲封面
        songPicImageView = findViewById(R.id.song_pic);
        // 初始化是否收藏
        dbHelper = new CollectOpenHelper(this);
        collectIcon = findViewById(R.id.collect_icon);
        // 初始化seekBar
        seekBar = findViewById(R.id.song_seekbar);
        seekBarCurrentValue = findViewById(R.id.song_seekbar_current_value);
        seekBarMaxValue = findViewById(R.id.song_seekbar_max_value);
        // 初始化下载按钮
        downloadIcon = findViewById(R.id.download_icon);
        downloadOpenHelper = new DownloadOpenHelper(this);
        // 初始化播放暂停按钮
        playIcon = findViewById(R.id.play_icon);
        // 初始化视图内容
        initViewContent();
        // 监听收藏按钮
        collectIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(dbHelper.exist(mSong.getId()) == null) {
                    dbHelper.insert(mSong);
                    collectIcon.setImageResource(R.drawable.ic_collected_red_24dp);
                    Toast.makeText(DetailActivity.this, "已收藏", Toast.LENGTH_SHORT).show();
                }else {
                    dbHelper.delete(mSong.getId());
                    collectIcon.setImageResource(R.drawable.ic_collect_black_24dp);
                    Toast.makeText(DetailActivity.this, "取消收藏", Toast.LENGTH_SHORT).show();
                }
            }
        });
        // 初始化下载按钮
        downloadIcon.setVisibility(View.INVISIBLE);
        downloadIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(DetailActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    //申请WRITE_EXTERNAL_STORAGE权限
                    ActivityCompat.requestPermissions(DetailActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
                }else{
                    Toast.makeText(DetailActivity.this, "正在后台下载歌曲，请耐心等待", Toast.LENGTH_LONG).show();
                    // 下载Mp3文件至本地
                    DownloadMp3Util downloadMp3Util = new DownloadMp3Util(DetailActivity.this, mSong);
                    if (downloadMp3Util.downloadToStorage(mLyricForDownload)) {
                        downloadIcon.setVisibility(View.GONE);
                        downloadOpenHelper.insert(mSong);
                        Toast.makeText(DetailActivity.this, mSong.getSinger()+"-"+mSong.getTitle()+".mp3"+"已保存至"+IMUSICPLAYER_MP3_DIR, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(DetailActivity.this, "歌曲下载失败", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        // 播放暂停按钮监听
        playButtonListener = new PlayButtonListener();
        playIcon.setOnClickListener(playButtonListener);
        // 上一首按钮
        ImageView previousButton = findViewById(R.id.skip_previous_icon);
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int cursor = mSongList.indexOf(mSong);
                cursor = (cursor+mSongList.size()-1) % mSongList.size();
                mSong = null;
                mSong = mSongList.get(cursor);
                if(mediaPlayer != null){
                    isPlaying = false;
                    seekBar.setProgress(0);
                    playIcon.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                    mediaPlayer.release();
                    mediaPlayer = null;
                    // 移除所有任务
                    if(timer !=null) {
                        timer.purge();
                        timer = null;
                    }
                }
                initViewContent();
                playButtonListener.onClick(null);
            }
        });
        // 下一首按钮
        ImageView nextButton = findViewById(R.id.skip_next_icon);
        nextButtonListener = new NextButtonListener();
        nextButton.setOnClickListener(nextButtonListener);
    }
    class NextButtonListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            int cursor = mSongList.indexOf(mSong);
            cursor = (cursor+mSongList.size()+1) % mSongList.size();
            mSong = null;
            mSong = mSongList.get(cursor);
            if(mediaPlayer != null){
                isPlaying = false;
                seekBar.setProgress(0);
                playIcon.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                mediaPlayer.release();
                mediaPlayer = null;
                // 移除所有任务
                if(timer !=null) {
                    timer.purge();
                    timer = null;
                }
            }
            initViewContent();
            playButtonListener.onClick(null);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 隐藏顶部标题
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        // 初始化加载图标
        loadingBlock = findViewById(R.id.loading_block);
        loadedBlock = findViewById(R.id.loaded_block);
        loadedBlock.setVisibility(View.INVISIBLE);
        // 初始化Song和songList
        mSong = (Song) getIntent().getSerializableExtra(DETAIL_CURRENT_SONG);
        mSongList = (ArrayList<Song>) getIntent().getSerializableExtra(DETAIL_SONG_LIST);
        // 初始化back button
        ImageView backIcon = findViewById(R.id.back_for_detail);
        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DetailActivity.this.finish();
            }
        });
        // 初始化View
        initView();
    }
    /**
     * 动态申请权限后回调方法
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == WRITE_EXTERNAL_STORAGE_REQUEST_CODE){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){// 授权成功
                // 下载Mp3文件至本地
                DownloadMp3Util downloadMp3Util = new DownloadMp3Util(DetailActivity.this, mSong);
                if (downloadMp3Util.downloadToStorage(mLyricForDownload)) {
                    downloadIcon.setVisibility(View.GONE);
                    downloadOpenHelper.insert(mSong);
                    Toast.makeText(DetailActivity.this, mSong.getSinger()+"-"+mSong.getTitle()+".mp3"+"已保存至"+IMUSICPLAYER_MP3_DIR, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(DetailActivity.this, "歌曲下载失败", Toast.LENGTH_SHORT).show();
                }
            }else{// 授权失败
                Toast.makeText(DetailActivity.this, "请允许存储权限，下载歌曲", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 从本地加载音频源
     */
    void searchMp3UrlFromStorage(){
        try {
            String mp3Url = downloadOpenHelper.exist(mSong.getId()).getMp3Url();
            // 设置MP3url
            mSong.setMp3Url(mp3Url);
            Log.d("IMUSICPLAYER_MP3_URL", "使用本地资源"+IMUSICPLAYER_MP3_DIR + mSong.getSinger()+"-"+mSong.getTitle() + ".mp3");
            downloadIcon.setVisibility(View.GONE);
            // 初始化mediaPlayer
            Uri mp3Uri = Uri.fromFile(new File(IMUSICPLAYER_MP3_DIR + mSong.getSinger()+"-"+mSong.getTitle() + ".mp3")); // initialize Uri here
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(getApplicationContext(), mp3Uri);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(final MediaPlayer m) {
                    // 获取音频时间
                    int maxValue = mediaPlayer.getDuration();
                    seekBar.setMax(maxValue);
                    seekBarMaxValue.setText(encodeTime(maxValue));
                    // 更新按钮图片
                    playIcon.setImageResource(R.drawable.ic_pause_black_24dp);
                    isPlaying = true;
                    // 开始播放
                    mediaPlayer.start();
                    lyricPlayer.play();
                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if (mediaPlayer != null) {
                                try {
                                    // 调用mediaPlayer.release()后进入到END状态，
                                    // mediaPlayer.getCurrentPosition()会出现IllegalStateException
                                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                                    seekBarCurrentValue.setText(encodeTime(mediaPlayer.getCurrentPosition()));
                                } catch (IllegalStateException e) {
                                    Log.d("IMUSICPLAYER_ILLEAGL", "mediaPlayer throws IllegalStateException for in end state");
                                }
                            }
                        }
                    }, 0, 1000);
                }
            });
            // mediaPlayer播放完自动加载下一首
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    Log.d("IMUSICPLAYER_COMPLETE", "总时长："+mp.getDuration());
                    nextButtonListener.onClick(null);
                }
            });
        }catch (Exception e){
            Log.d("IMUSICPLAYER_MP3", e.getMessage());
            e.printStackTrace();
        }
    }
    /**
     * 从本地（已下载歌曲，download表中含有的）加载音频源
     */
    void searchMp3UrlFromNetwork(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String searchMp3Url = music163Mp3Url.replace("arg_id", mSong.getId());
                Log.d("IMUSICPLAYER_MP3", searchMp3Url);
                try {
                    URL url = new URL(searchMp3Url);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.connect();
                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        InputStream inputStream = connection.getInputStream();
                        String result = convertStreamToString(inputStream);
                        Log.d("IMUSICPLAYER_MP3", result);
                        JSONObject obj = new JSONObject(result);
                        final JSONObject songObj = obj.getJSONArray(RESPONSE_DATA_DATA).getJSONObject(0);
                        // 通过HTTP流媒体从远程URL播放
                        String mp3Url = songObj.getString(RESPONSE_DATA_URL);
                        // url可能是null字符串
                        if (mp3Url != null && !mp3Url.trim().equalsIgnoreCase("null")) {
                            Log.d("IMUSICPLAYER_MP3_URL", "使用网络资源"+songObj.getString(RESPONSE_DATA_URL));
                            // 设置MP3url
                            mSong.setMp3Url(mp3Url);
                            // 初始化mediaPlayer
                            mediaPlayer = new MediaPlayer();
                            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                            mediaPlayer.setDataSource(mp3Url);
                            mediaPlayer.prepareAsync();
                            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(final MediaPlayer m) {
                                    // 设置MP3url
                                    DetailActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            // 数据库中不存在才下载
                                            if (downloadOpenHelper.exist(mSong.getId()) == null) {
                                                Log.d("IMUSICPLAYER_DOWNLOAD", "" + (downloadOpenHelper.exist(mSong.getId()) == null));
                                                downloadIcon.setVisibility(View.VISIBLE);
                                            }
                                        }
                                    });
                                    // 获取音频时间
                                    int maxValue = mediaPlayer.getDuration();
                                    seekBar.setMax(maxValue);
                                    seekBarMaxValue.setText(encodeTime(maxValue));
                                    // 更新按钮图片
                                    playIcon.setImageResource(R.drawable.ic_pause_black_24dp);
//                                        playText.setText("暂停");
                                    isPlaying = true;
                                    // 开始播放
                                    mediaPlayer.start();
                                    lyricPlayer.play();
                                    timer = new Timer();
                                    timer.schedule(new TimerTask() {
                                        @Override
                                        public void run() {
                                            if (mediaPlayer != null) {
                                                try {
                                                    // 调用mediaPlayer.release()后进入到END状态，
                                                    // mediaPlayer.getCurrentPosition()会出现IllegalStateException
                                                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                                                    seekBarCurrentValue.setText(encodeTime(mediaPlayer.getCurrentPosition()));
                                                } catch (IllegalStateException e) {
                                                    Log.d("IMUSICPLAYER_ILLEAGL", "mediaPlayer throws IllegalStateException for in end state");
                                                }
                                            }
                                        }
                                    }, 0, 1000);
                                }
                            });
                            // mediaPlayer播放完自动加载下一首
                            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mp) {
                                    Log.d("IMUSICPLAYER_COMPLETE", "总时长："+mp.getDuration());
                                    nextButtonListener.onClick(null);
                                }
                            });
                        } else {
                            DetailActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(DetailActivity.this, "歌曲文件不存在", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        DetailActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(DetailActivity.this, "歌曲文件不存在", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    // 在子线程中跟新UI

                } catch (Exception e) {
                    Log.d("IMUSICPLAYER_MP3", e.getMessage());
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 加载音频源
     */
    void searchMp3Url(){
        if(downloadOpenHelper.exist(mSong.getId()) != null){
            searchMp3UrlFromStorage();
        }else {
            searchMp3UrlFromNetwork();
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mediaPlayer != null){
            //mediaPlayer不用了需要销毁
            mediaPlayer.release();
            mediaPlayer = null;
            // 移除所有任务
            if(timer !=null) {
                timer.purge();
                timer = null;
            }
            lyricPlayer.stop();
        }
    }
}

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
package org.ibu.imusicplayer.player;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.*;
import org.ibu.imusicplayer.*;
import org.ibu.imusicplayer.dao.BaseOpenHelper;
import org.ibu.imusicplayer.dao.OpenHelperFactory;
import org.ibu.imusicplayer.lyric.DownloadMp3Util;
import org.ibu.imusicplayer.dao.CollectOpenHelper;
import org.ibu.imusicplayer.dao.DownloadOpenHelper;
import org.ibu.imusicplayer.lyric.LyricPlayer;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static org.ibu.imusicplayer.lyric.DownloadMp3Util.IMUSICPLAYER_MP3_DIR;
import static org.ibu.imusicplayer.util.LyricUtil.encodeTime;
import static org.ibu.imusicplayer.Music163Constants.*;

/**
 * 歌曲详情和播放页面Activity
 */
public class DetailActivity extends AppCompatActivity implements EventListeners {
    private static final String TAG = "DetailActivity";

    Song mSong;
    List<Song> mSongList;
    /* 歌词播放器 */
    LyricPlayer lyricPlayer;
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
    LinearLayout loadedBlock;
    /* android6以上动态申请读写权限请求码*/
    int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 2;
    /* 数据库操作帮助类*/
    CollectOpenHelper dbHelper;
    BaseOpenHelper downloadOpenHelper;
    /* 是否正在播放 */
    boolean isPlaying = false;
    /* 页面视图 */
    TextView songTitleTextView;         // 歌曲标题
    TextView songSingerEpnameTextView;  // 歌手名-专辑名
    ImageView playIcon;                 // 播放按钮

    LyricFragment lyricFragment;
    SimpleFragment artworkFragment;
    FrameLayout frameLayout;
    SimpleFragment[] fragments = new SimpleFragment[2];
    int fragmentCount = 0;

    final static int REPEAT_ALL = 1;    // 顺序播放
    final static int REPEAT_ONE = 2;    // 单曲循环
    final static int SHUFFLE = 4;       // 随机播放
    int playMode = 0;
    int[] playModes = new int[]{REPEAT_ALL,
            REPEAT_ONE,
            SHUFFLE};
    int[] playModeResources = new int[]{R.drawable.ic_repeat_black_24dp,
            R.drawable.ic_repeat_one_black_24dp,
            R.drawable.ic_shuffle_black_24dp};
    String[] playModeToasts = new String[]{"顺序播放",
            "单曲循环",
            "随机播放"};

    String dbType;
    /**
     * 初始化seekBar
     * 问题：使用正则表达式提取歌词最后一个时间（这里不准确）
     *       String lastTime = lyric.substring(lyric.lastIndexOf('['), lyric.lastIndexOf(']')+1);
     *       maxValue = decodeTime(lastTime);
     */
    void initSeekBar(){
        Log.d(TAG, "initSeekBar");
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
                        //lyricFragment.songLyricTextView.seekTo(i);
                        mediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                            @Override
                            public void onSeekComplete(MediaPlayer m) {
                                // 移除所有任务
                                if(timer !=null) {
                                    timer.purge();
                                    timer = null;
                                }

                                mediaPlayer.start();
                                //lyricFragment.songLyricTextView.seekTo(mediaPlayer.getCurrentPosition());
                                timer = new Timer();
                                timer.schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        try {
                                            DetailActivity.this.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if(mediaPlayer!=null) {
                                                        seekBar.setProgress(mediaPlayer.getCurrentPosition());
                                                    }
                                                }
                                            });
                                        }catch (IllegalStateException e){
                                            Log.d("IMUSICPLAYER_ILLEAGL", "mediaPlayer throws IllegalStateException for in end state");
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
        Log.d(TAG, "initViewContent");
        // 初始化歌曲名信息
        songTitleTextView.setText(mSong.getTitle());
        songSingerEpnameTextView.setText(mSong.getSinger()+" - "+mSong.getEpname());
        // 初始化seekBar
        seekBarCurrentValue.setText("0:00");
        seekBarMaxValue.setText("0:00");

        if (dbType.equals(OpenHelperFactory.DB_TYPE.DB_TYPE_NETWORK)){
            DownloadMp3Util downloadMp3Util = new DownloadMp3Util(DetailActivity.this, mSong);
            downloadMp3Util.search();
        }else if(downloadOpenHelper.exist(mSong.getId()) != null){
            DownloadMp3Util downloadMp3Util = new DownloadMp3Util(this, mSong);
            // 初始化封面
            InputStream mInputStream = downloadMp3Util.readAlbumImage();
            final Bitmap bitmap = BitmapFactory.decodeStream(mInputStream);
            mSong.setBitmap(bitmap);
            // 初始化歌词
            mSong.setLyric(downloadMp3Util.readLyric());
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Bitmap bitmap = BitmapFactory.decodeStream(getClass().getResourceAsStream("/res/drawable/ic_default_artwork.png"));
                    mSong.setBitmap(bitmap);
                    DetailActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((EventListeners)DetailActivity.this).onSearchFinished(mSong);
                        }
                    });
                }
            }).start();
        }else{
            Bitmap bitmap = BitmapFactory.decodeStream(getClass().getResourceAsStream("/res/drawable/ic_default_artwork.png"));
            mSong.setBitmap(bitmap);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    DetailActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((EventListeners)DetailActivity.this).onSearchFinished(mSong);
                        }
                    });
                }
            }).start();
        }
    }

    @Override
    public void onSearchFinished(Song song) {
        Log.d(TAG, "onSearchFinished");
        Bundle bundle = new Bundle();
        bundle.putSerializable("song", song);

        artworkFragment.update(bundle);
        lyricFragment.update(bundle);


        mSong = song;
        loadingBlock.setVisibility(View.GONE);
        loadedBlock.setVisibility(View.VISIBLE);
        initSeekBar();
        lyricFragment.songLyricTextView.setLyric(song.getLyric());
    }

    @Override
    public void onDownloadFinished(Song song) {
        downloadOpenHelper.insert(song);
        Bundle bundle = new Bundle();
        bundle.putSerializable("song", song);
        bundle.putBoolean("downloaded", true);
        artworkFragment.update(bundle);
        mSong = song;
        Toast.makeText(this, mSong.getSinger()+"-"+mSong.getTitle()+".mp3"+"已保存至"+IMUSICPLAYER_MP3_DIR, Toast.LENGTH_LONG).show();
    }

    class PlayButtonListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            // 显示播放按钮
            if(!isPlaying){
                Log.d(TAG,"isPlaying=false");
                //暂停转为播放状态
                if(mediaPlayer!=null){
                    // 更新按钮图片
                    playIcon.setImageResource(R.drawable.ic_pause_black_24dp);
                    
                    ((ArtworkFragment)artworkFragment).updateDownloadIcon();
                    isPlaying = true;
                    mediaPlayer.start();
                    ((ArtworkFragment)artworkFragment).startRotateArtwork();
                    // 定时任务，每一秒更新一下seekBar
                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                                try {
                                    DetailActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if(mediaPlayer!=null) {
                                                seekBar.setProgress(mediaPlayer.getCurrentPosition());
                                                seekBarCurrentValue.setText(encodeTime(mediaPlayer.getCurrentPosition()));
                                                lyricFragment.songLyricTextView.seekTo(mediaPlayer.getCurrentPosition());
                                            }
                                        }
                                    });
                                }catch (IllegalStateException e){
                                    Log.d("IMUSICPLAYER_ILLEAGL", "mediaPlayer throws IllegalStateException for in end state");
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
                    lyricFragment.songLyricTextView.pause();
                    ((ArtworkFragment)artworkFragment).stopRotateArtwork();
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

    void changeFragment(){
        Log.d(TAG, "changeFragment");
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.hide(fragments[fragmentCount % 2]);
        fragmentCount += 1;
        transaction.show(fragments[fragmentCount % 2]);
        transaction.commit();
    }
    /**
     * 初始化视图
     */
    void initView(){
        Log.d(TAG, "initView");
        // 初始化 fragment
        Bundle bundle1 = new Bundle();
        bundle1.putSerializable("song", mSong);
        artworkFragment = new ArtworkFragment();
        artworkFragment.setArguments(bundle1);

        lyricFragment = new LyricFragment();
        lyricFragment.setArguments(bundle1);

        fragments[0] = artworkFragment;
        fragments[1] = lyricFragment;

        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();

        transaction.add(R.id.song_lyric_scroll, lyricFragment);
        transaction.add(R.id.song_lyric_scroll, artworkFragment);
        transaction.show(artworkFragment).hide(lyricFragment);
        transaction.commit();
        // 初始化歌曲名信息
        songTitleTextView = findViewById(R.id.song_title);
        songSingerEpnameTextView = findViewById(R.id.song_singer_epname);
        frameLayout = findViewById(R.id.song_lyric_scroll);
        frameLayout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                changeFragment();
            }
        });

        // 初始化seekBar
        seekBar = findViewById(R.id.song_seekbar);
        seekBarCurrentValue = findViewById(R.id.song_seekbar_current_value);
        seekBarMaxValue = findViewById(R.id.song_seekbar_max_value);
        // 初始化下载按钮
        downloadOpenHelper = OpenHelperFactory.getOpenHelper(this,
                                OpenHelperFactory.DB_TYPE.DB_TYPE_DOWNLOAD);
        // 初始化播放暂停按钮
        playIcon = findViewById(R.id.play_icon);
        // 初始化视图内容
        initViewContent();
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
        // 初始化playmode
        final ImageView playModeView = findViewById(R.id.play_mode_icon);
        playModeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playMode = (playMode + 1) % playModes.length;
                playModeView.setImageResource(playModeResources[playMode]);
                Toast.makeText(DetailActivity.this, playModeToasts[playMode], Toast.LENGTH_SHORT).show();
            }
        });
    }
    class NextButtonListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            int cursor = mSongList.indexOf(mSong);
            switch (playMode){
                case 0:
                    cursor = (cursor+mSongList.size()+1) % mSongList.size();break;
                case 1:
                    cursor = cursor;break;
                case 2:
                    double random = Math.random();
                    cursor = new Double(random * mSongList.size()).intValue();break;
            }
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
        Log.d(TAG, "onCreate");
        // 隐藏顶部标题
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        // 初始化加载图标
        loadingBlock = findViewById(R.id.loading_block);
        loadedBlock = findViewById(R.id.loaded_block);
        loadedBlock.setVisibility(View.INVISIBLE);
        downloadOpenHelper = new DownloadOpenHelper(this);
        // 初始化Song和songList
        dbType = getIntent().getStringExtra(BundleConstants.DB_TYPE);
        mSong = (Song) getIntent().getSerializableExtra(BundleConstants.DETAIL_CURRENT_SONG);
        mSongList = (ArrayList<Song>) getIntent().getSerializableExtra(BundleConstants.DETAIL_SONG_LIST);
        // 初始化back button
        ImageView backIcon = findViewById(R.id.back_for_detail);
        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DetailActivity.this.finish();
            }
        });
        // 初始化back button
        ImageView queueMusicView = findViewById(R.id.queue_music_icon);
        queueMusicView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DetailActivity.this.finish();
            }
        });
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
                ((ArtworkFragment)artworkFragment).download();
            }else{// 授权失败
                Toast.makeText(DetailActivity.this, "请允许存储权限", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 从本地加载音频源
     */
    void searchMp3UrlFromStorage(){
        Log.d(TAG, "searchMp3UrlFromStorage");
        try {
            String mp3Url = mSong.getMp3Url();
            Log.d(TAG, "使用本地资源"+mp3Url);
            ((ArtworkFragment)artworkFragment).downloadIcon.setVisibility(View.GONE);
            // 初始化mediaPlayer
            Uri mp3Uri = Uri.fromFile(new File(mp3Url)); // initialize Uri here
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
                    ((ArtworkFragment)artworkFragment).startRotateArtwork();
                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                                try {
                                    // 调用mediaPlayer.release()后进入到END状态，
                                    // mediaPlayer.getCurrentPosition()会出现IllegalStateException
                                    DetailActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (mediaPlayer != null) {
                                                seekBar.setProgress(mediaPlayer.getCurrentPosition());
                                                seekBarCurrentValue.setText(encodeTime(mediaPlayer.getCurrentPosition()));
                                            }
                                        }
                                    });

                                } catch (IllegalStateException e) {
                                    Log.d("IMUSICPLAYER_ILLEAGL", "mediaPlayer throws IllegalStateException for in end state");
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
                                                ((ArtworkFragment)artworkFragment).updateDownloadIcon();
                                            }
                                        }
                                    });
                                    // 获取音频时间
                                    int maxValue = mediaPlayer.getDuration();
                                    seekBar.setMax(maxValue);
                                    seekBarMaxValue.setText(encodeTime(maxValue));
                                    // 更新按钮图片
                                    playIcon.setImageResource(R.drawable.ic_pause_black_24dp);
                                    ((ArtworkFragment)artworkFragment).updateDownloadIcon();
                                    isPlaying = true;
                                    // 开始播放
                                    mediaPlayer.start();
                                    ((ArtworkFragment)artworkFragment).startRotateArtwork();
                                    timer = new Timer();
                                    timer.schedule(new TimerTask() {
                                        @Override
                                        public void run() {
                                                try {
                                                    // 调用mediaPlayer.release()后进入到END状态，
                                                    // mediaPlayer.getCurrentPosition()会出现IllegalStateException
                                                    DetailActivity.this.runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            if (mediaPlayer != null) {
                                                                int position = mediaPlayer.getCurrentPosition();
                                                                seekBar.setProgress(position);
                                                                seekBarCurrentValue.setText(encodeTime(position));
                                                                lyricFragment.songLyricTextView.seekTo(position);
                                                            }
                                                        }
                                                    });

                                                } catch (IllegalStateException e) {
                                                    Log.d("IMUSICPLAYER_ILLEAGL", "mediaPlayer throws IllegalStateException for in end state");
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
        if(dbType.equals(OpenHelperFactory.DB_TYPE.DB_TYPE_LOCAL)){
            searchMp3UrlFromStorage();
        }else if(dbType.equals(OpenHelperFactory.DB_TYPE.DB_TYPE_NETWORK)) {
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
            //lyricFragment.songLyricTextView.stop();
        }
    }
}

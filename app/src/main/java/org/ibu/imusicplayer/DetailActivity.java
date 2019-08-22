package org.ibu.imusicplayer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.*;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import static org.ibu.imusicplayer.LyricUtil.encodeTime;
import static org.ibu.imusicplayer.MainActivity.convertStreamToString;
import static org.ibu.imusicplayer.Music163Contants.*;


public class DetailActivity extends AppCompatActivity {
    final static String DETAIL_SONG_ID = "id";
    final static String DETAIL_SONG_TITLE = "title";
    final static String DETAIL_SONG_SINGER= "singer";
    final static String DETAIL_SONG_EPNAME = "epname";
    final static String DETAIL_SONG_PICURL = "picUrl";

    String songId;

    MediaPlayer mediaPlayer;
    SeekBar seekBar;
    TextView seekBarCurrentValue;
    TextView seekBarMaxValue;

    // 初始化seekBar
    void initSeekBar(String lyric){
//        // 使用正则表达式提取歌词最后一个时间（这里不准确）
//        String lastTime = lyric.substring(lyric.lastIndexOf('['), lyric.lastIndexOf(']')+1);
//        maxValue = decodeTime(lastTime);
        seekBarMaxValue.setText("0:00");
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
                        mediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                            @Override
                            public void onSeekComplete(MediaPlayer m) {
                                // 移除所有任务
                                if(timer !=null) {
                                    timer.purge();
                                    timer = null;
                                }

                                mediaPlayer.start();
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
    Timer timer;

    LinearLayout loadingBlock;
    LinearLayout loadedBlock;

    CollectOpenHelper dbHelper;

    boolean isPlaying = false;
    ImageView playIcon;
    TextView playText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 隐藏顶部标题
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        loadingBlock = findViewById(R.id.loading_block);
        loadedBlock = findViewById(R.id.loaded_block);
        loadedBlock.setVisibility(View.INVISIBLE);
        // 初始化songId
        songId = getIntent().getStringExtra(DETAIL_SONG_ID);
        final String title = getIntent().getStringExtra(DETAIL_SONG_TITLE);
        final String singer = getIntent().getStringExtra(DETAIL_SONG_SINGER);
        final String epname = getIntent().getStringExtra(DETAIL_SONG_EPNAME);
        // 初始化歌曲名信息
        TextView songTitleTextView = findViewById(R.id.song_title);
        TextView songSingerEpnameTextView = findViewById(R.id.song_singer_epname);
        songTitleTextView.setText(getIntent().getStringExtra(DETAIL_SONG_TITLE));
        songSingerEpnameTextView.setText(getIntent().getStringExtra(DETAIL_SONG_SINGER)+" - "+getIntent().getStringExtra(DETAIL_SONG_EPNAME));
        // 初始化歌词
        final TextView songLyricTextView = findViewById(R.id.song_lyric);
        // 使歌词能滚动
        songLyricTextView.setMovementMethod(ScrollingMovementMethod.getInstance());
        // 初始化歌曲封面
        final ImageView songPicImageView = findViewById(R.id.song_pic);
        final String picUrl = getIntent().getStringExtra(DETAIL_SONG_PICURL);
        // 初始化是否收藏
        dbHelper = new CollectOpenHelper(this);
        final TextView collectedText = findViewById(R.id.collected_text);
        final ImageView collectButton = findViewById(R.id.collect_icon);
        if(dbHelper.exist(songId) == null) {
            collectedText.setText("收藏");
            collectButton.setImageResource(R.drawable.ic_star_border_black_24dp);
        }else {
            collectedText.setText("已收藏");
            collectButton.setImageResource(R.drawable.ic_star_black_24dp);
        }
        // 监听收藏按钮
        collectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(dbHelper.exist(songId) == null) {
                    dbHelper.insert(new Song(songId, title, singer, epname, picUrl));
                    collectedText.setText("已收藏");
                    collectButton.setImageResource(R.drawable.ic_star_black_24dp);
                }else {
                    dbHelper.delete(songId);
                    collectedText.setText("收藏");
                    collectButton.setImageResource(R.drawable.ic_star_border_black_24dp);
                }
            }
        });
        // 初始化是否下载
//        final TextView downloadedText = findViewById(R.id.downloaded_text);
//        final ImageView downloadButton = findViewById(R.id.download_icon);
//        downloadButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if(dbHelper.exist(songId) == null) {
//                    downloadedText.setText("已下载");
//                    downloadButton.setImageResource(R.drawable.ic_file_download_black_24dp);
//                }else {
//                    downloadedText.setText("下载");
//                    downloadButton.setImageResource(R.drawable.ic_download_finished);
//                }
//            }
//        });
        // 初始化seekBar
        seekBar = findViewById(R.id.song_seekbar);
        seekBarCurrentValue = findViewById(R.id.song_seekbar_current_value);
        seekBarCurrentValue.setText("0:00");
        seekBarMaxValue = findViewById(R.id.song_seekbar_max_value);
        // 查找歌词
        new Thread(new Runnable() {
            @Override
            public void run() {
                String lyricUrl = music163LyricUrl.replace("arg_id", songId);
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
                        // 请求音乐封面
                        URL mUrl = new URL(picUrl);
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
                                songLyricTextView.setText(lyric);
                                initSeekBar(lyric);
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
                    // 在子线程中跟新UI

                }catch(Exception e){
                    Log.d("SEARCH_ERROR", e.getMessage());
                    e.printStackTrace();
                }
            }
        }).start();
        // 播放暂停按钮监听
        playIcon = findViewById(R.id.play_icon);
        playText = findViewById(R.id.play_text);
        LinearLayout playBlock = findViewById(R.id.play_icon_block);
        playBlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 显示播放按钮
                if(!isPlaying){
                    Log.d("IMUSICPLAYER_MP3","isPlaying=false");
                    //暂停转为播放状态
                    if(mediaPlayer!=null){
                        // 更新按钮图片
                        playIcon.setImageResource(R.drawable.ic_pause_circle_outline_black_24dp);
                        playText.setText("暂停");
                        isPlaying = true;
                        mediaPlayer.start();
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
                        // 更新按钮图片
                        playIcon.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
                        playText.setText("播放");
                        isPlaying = false;
                        // 移除所有任务
                        if(timer !=null) {
                            timer.purge();
                            timer = null;
                        }
                    }
                }

            }
        });
    }

    void searchMp3Url(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String searchMp3Url = music163Mp3Url.replace("arg_id", songId);
                Log.d("IMUSICPLAYER_MP3", searchMp3Url);
                try{
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
                        if(mp3Url !=null && !mp3Url.trim().equalsIgnoreCase("null")){
                            Log.d("IMUSICPLAYER_MP3_URL", songObj.getString(RESPONSE_DATA_URL));

                            // 初始化mediaPlayer
                            mediaPlayer = new MediaPlayer();
                            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                            mediaPlayer.setDataSource(mp3Url);
                            mediaPlayer.prepareAsync();
                            mediaPlayer.setLooping(true);
                            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(final MediaPlayer m) {
                                    // 获取音频时间
                                    int maxValue = mediaPlayer.getDuration();
                                    seekBar.setMax(maxValue);
                                    seekBarMaxValue.setText(encodeTime(maxValue));
                                    // 更新按钮图片
                                    playIcon.setImageResource(R.drawable.ic_pause_circle_outline_black_24dp);
                                    playText.setText("暂停");
                                    isPlaying = true;
                                    // 开始播放
                                    mediaPlayer.start();
                                    timer = new Timer();
                                    timer.schedule(new TimerTask() {
                                        @Override
                                        public void run() {
                                            if(mediaPlayer!=null) {
                                                try {
                                                    // 调用mediaPlayer.release()后进入到END状态，
                                                    // mediaPlayer.getCurrentPosition()会出现IllegalStateException
                                                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                                                    seekBarCurrentValue.setText(encodeTime(mediaPlayer.getCurrentPosition()));
                                                }catch (IllegalStateException e){
                                                    Log.d("IMUSICPLAYER_ILLEAGL", "mediaPlayer throws IllegalStateException for in end state");
                                                }
                                            }
                                        }
                                    },0, 1000);
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
                    }else{
                        DetailActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(DetailActivity.this, "歌曲文件不存在",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    // 在子线程中跟新UI

                }catch(Exception e){
                    Log.d("IMUSICPLAYER_MP3", e.getMessage());
                    e.printStackTrace();
                }
            }
        }).start();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mediaPlayer != null){
            mediaPlayer.release();
            mediaPlayer = null;
            // 移除所有任务
            if(timer !=null) {
                timer.purge();
                timer = null;
            }
        }
    }
}

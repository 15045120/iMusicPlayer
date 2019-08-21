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
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.ibu.imusicplayer.MainActivity.convertStreamToString;
import static org.ibu.imusicplayer.Music163Contants.*;


public class DetailActivity extends AppCompatActivity {
    final static String DETAIL_SONG_ID = "id";
    final static String DETAIL_SONG_TITLE = "title";
    final static String DETAIL_SONG_SINGER= "singer";
    final static String DETAIL_SONG_EPNAME = "epname";
    final static String DETAIL_SONG_PICURL = "picUrl";

    String songId;

    LinearLayout playButton;
    LinearLayout pauseButton;

    MediaPlayer mediaPlayer;
    SeekBar seekBar;
    TextView seekBarCurrentValue;
    TextView seekBarMaxValue;
    int maxValue;
 
    // 初始化seekBar
    void initSeekBar(String lyric){
        String lastTime = lyric.substring(lyric.lastIndexOf('['), lyric.lastIndexOf(']')+1);
        maxValue = decodeTime(lastTime);
        seekBar.setMax(maxValue);
        seekBarMaxValue.setText(encodeTime(maxValue));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(final SeekBar seekBar, final int i, boolean b) {
                if(mediaPlayer == null){
                    searchMp3Url();
                }else{
                    mediaPlayer.seekTo(i);
                    mediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                        @Override
                        public void onSeekComplete(MediaPlayer m) {
                            m.start();
                            seekBar.setProgress(i);
                            
                        }
                    });
                    seekBarCurrentValue.setText(encodeTime(i));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
    int decodeTime(String time){
        Pattern pattern = Pattern.compile("\\[([0-9]*):([0-9]*)\\.([0-9]*)\\]");
        Matcher matcher = pattern.matcher(time);
        if(matcher.matches()) {
            String m1 = matcher.group(1);
            String m2 = matcher.group(2);
            String m3 = matcher.group(3);
            Log.d("IMUSICPLAYER", m1 + ":" + m2 + "." + m3);
            return Integer.valueOf(m1) * 60 * 1000 + Integer.valueOf(m2) * 1000 + Integer.valueOf(m3);
        }
        return 0;
    }
    String encodeTime(int time){
        String m1 = "00";
        String m2 = "00";
        if(time / 60000 != 0){
            m1  = Integer.toString(time / 60000);
            if((time - (time/60000)*60000) / 1000 != 0){
                m2 = Integer.toString((time - (time/60000)*60000) / 1000);
            }
        }else{
            if(time / 1000 != 0){
                m2 = Integer.toString(time / 1000);
            }
        }
        return (m1.length()!=2?("0"+m1):m1) + ":" + (m2.length()!=2?("0"+m2):m2);
    }

    LinearLayout loadingBlock;
    LinearLayout loadedBlock;
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
        // 初始化seekBar
        seekBar = findViewById(R.id.song_seekbar);
        seekBarCurrentValue = findViewById(R.id.song_seekbar_current_value);
        seekBarCurrentValue.setText("0:00");
        seekBarMaxValue = findViewById(R.id.song_seekbar_max_value);

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
                        Toast.makeText(DetailActivity.this, "歌曲文件不存在",Toast.LENGTH_SHORT).show();
                    }
                    // 在子线程中跟新UI

                }catch(Exception e){
                    Log.d("SEARCH_ERROR", e.getMessage());
                    e.printStackTrace();
                }
            }
        }).start();



        playButton = findViewById(R.id.play_icon);
        pauseButton = findViewById(R.id.pause_icon);




        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("IMUSICPLAYER_MP3","click play button");
                //暂停转为播放状态
                if(mediaPlayer!=null && !mediaPlayer.isPlaying()){
                    mediaPlayer.start();
                    return;
                }
                if(mediaPlayer == null){//刚进入该页面
                    searchMp3Url();
                }else if(mediaPlayer.isPlaying()){// 正在播放状态，什么也不处理

                }

            }
        });
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaPlayer != null && mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
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
                        final JSONObject songObj = ((JSONArray)obj.get("data")).getJSONObject(0);
                        // 通过HTTP流媒体从远程URL播放
                        if(songObj.getString("url") !=null){
                            Log.d("searchMp3Url", songObj.getString("url"));
                            String mp3Url = songObj.getString("url");
                            // 初始化mediaPlayer
                            mediaPlayer = new MediaPlayer();
                            mediaPlayer.reset();
                            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                            mediaPlayer.setDataSource(mp3Url);
                            mediaPlayer.prepareAsync();
                            mediaPlayer.setLooping(true);
                            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mediaPlayer) {
                                    mediaPlayer.start();

                                }
                            });

                        }else{
                            Toast.makeText(DetailActivity.this, "歌曲文件不存在",Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(DetailActivity.this, "歌曲文件不存在",Toast.LENGTH_SHORT).show();
                    }
                    // 在子线程中跟新UI

                }catch(Exception e){
                    Log.d("SEARCH_ERROR", e.getMessage());
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
        }
    }
}

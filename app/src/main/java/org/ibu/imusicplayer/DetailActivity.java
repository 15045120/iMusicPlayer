package org.ibu.imusicplayer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.ibu.imusicplayer.MainActivity.convertStreamToString;

public class DetailActivity extends AppCompatActivity {
    public final static String SONG_INFO = "songInfo";
    private final static String music163AlbumUrl = "https://music.163.com/api/song/detail?ids=[arg_id]";
    private final static String music163Mp3Url = "https://music.163.com/api/song/enhance/player/url?br=128000&ids=[arg_id]";
    int songId;

    ImageView playButton;
    ImageView pauseButton;

    MediaPlayer mediaPlayer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 隐藏顶部标题
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        final TextView songTitleTextView = findViewById(R.id.song_title);
        final TextView songSingerTextView = findViewById(R.id.song_singer);
        final TextView songEpnameTextView = findViewById(R.id.song_epname);
        final ImageView songPicImageView = findViewById(R.id.song_pic);



        playButton = findViewById(R.id.play_icon);
        pauseButton = findViewById(R.id.pause_icon);

        songId = getIntent().getIntExtra(SONG_INFO, -1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                String searchUrl = music163AlbumUrl.replace("arg_id", Integer.toString(songId));
                Log.d("SEARCH", searchUrl);
                try{
                    URL url = new URL(searchUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.connect();
                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        InputStream inputStream = connection.getInputStream();
                        String result = convertStreamToString(inputStream);
                        Log.d("SEARCH", result);
                        JSONObject obj = new JSONObject(result);
                        final JSONObject songObj = ((JSONArray)obj.get("songs")).getJSONObject(0);

                                URL mUrl = new URL(songObj.getJSONObject("album").getString("picUrl"));
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
                        DetailActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    songTitleTextView.setText(songObj.getString("name"));
                                    songSingerTextView.setText(songObj.getJSONObject("album").getJSONArray("artists").getJSONObject(0).getString("name"));
                                    songEpnameTextView.setText(songObj.getJSONObject("album").getString("name"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }else{
                        Toast.makeText(DetailActivity.this, "歌曲文件不存在",Toast.LENGTH_SHORT);
                    }
                    // 在子线程中跟新UI

                }catch(Exception e){
                    Log.d("SEARCH_ERROR", e.getMessage());
                    e.printStackTrace();
                }
            }
        }).start();

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("SEARCH","click play button");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String searchUrl = music163Mp3Url.replace("arg_id", Integer.toString(songId));
                        Log.d("SEARCH", searchUrl);
                        try{
                            URL url = new URL(searchUrl);
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            connection.setRequestMethod("GET");
                            connection.connect();
                            int responseCode = connection.getResponseCode();
                            if (responseCode == HttpURLConnection.HTTP_OK) {
                                InputStream inputStream = connection.getInputStream();
                                String result = convertStreamToString(inputStream);
                                Log.d("SEARCH", result);
                                JSONObject obj = new JSONObject(result);
                                final JSONObject songObj = ((JSONArray)obj.get("data")).getJSONObject(0);
                                // 通过HTTP流媒体从远程URL播放
                                if(songObj.getString("url") !=null){
                                    Log.d("SEARCH", songObj.getString("url"));
                                    String mp3Url = songObj.getString("url");
                                    mediaPlayer = new MediaPlayer();
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
                                    Toast.makeText(DetailActivity.this, "歌曲文件不存在",Toast.LENGTH_SHORT);
                                }
                            }else{
                                Toast.makeText(DetailActivity.this, "歌曲文件不存在",Toast.LENGTH_SHORT);
                            }
                            // 在子线程中跟新UI

                        }catch(Exception e){
                            Log.d("SEARCH_ERROR", e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaPlayer != null && mediaPlayer.isPlaying()){
                    mediaPlayer.stop();
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mediaPlayer != null){
            mediaPlayer.release();
        }
    }
}

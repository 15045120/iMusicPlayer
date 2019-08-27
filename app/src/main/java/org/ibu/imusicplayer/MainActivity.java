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
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.ibu.imusicplayer.Music163Constants.*;

/**
 * 歌曲搜索页面Activity
 */
public class MainActivity extends AppCompatActivity {

    ListView songListView;
    LinearLayout loadingBlock;
    LinearLayout menuBlock;

    List<Song> mSongList;
    @Override
    protected void onStart() {
        super.onStart();
        menuBlock.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 隐藏顶部标题
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 初始化搜索输入框
        final EditText songNameInput = findViewById(R.id.song_name_input);
        songNameInput.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        // 初始化正在加载图标
        loadingBlock = findViewById(R.id.main_loading_block);
        loadingBlock.setVisibility(View.INVISIBLE);
        // 初始化ListView
        songListView = findViewById(R.id.song_name_list);
        // 自定义更多按钮
        menuBlock = findViewById(R.id.menu_block);
        ImageView moreIcon = findViewById(R.id.more_icon);
        moreIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(menuBlock.getVisibility() == View.INVISIBLE){
                    menuBlock.setVisibility(View.VISIBLE);
                }else{
                    menuBlock.setVisibility(View.INVISIBLE);
                }
            }
        });
        // 点击更多图标中的收藏按钮
        TextView collectButton = findViewById(R.id.menu_collect_text);
        collectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), CollectActivity.class);
                startActivity(intent);
            }
        });
        // 点击更多图标中的下载按钮
        TextView downloadButton = findViewById(R.id.menu_download_text);
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), DownloadActivity.class);
                startActivity(intent);
            }
        });
        // 点击更多图标中的关于按钮
        TextView aboutButton = findViewById(R.id.menu_about_text);
        aboutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AboutActivity.class);
                startActivity(intent);
            }
        });
        // 点击更多图标中的分享按钮
        TextView shareButton = findViewById(R.id.menu_share_text);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent textIntent = new Intent(Intent.ACTION_SEND);
                textIntent.setType("text/plain");
                textIntent.putExtra(Intent.EXTRA_TEXT, "分享“爱音乐”-https://github.com/15045120/iMusicPlayer");
                startActivity(Intent.createChooser(textIntent, "分享"));
            }
        });
        // 监听输入框按下搜索
        songNameInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                // 按下搜索键
                if(actionId == EditorInfo.IME_ACTION_SEARCH){
                    if(songNameInput.getText().toString()!= null && !songNameInput.getText().toString().trim().equals("")){
                        Log.d("IMUSICPLAYER_SEARCH", "按下搜索按钮");
                        loadingBlock.setVisibility(View.VISIBLE);
                        final List<String> songIdList = new ArrayList<>();
                        mSongList = new ArrayList<Song>();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                // 搜索歌曲
                                String searchUrl = music163SearchUrl.replace("arg_s", songNameInput.getText());
                                Log.d("IMUSICPLAYER_SEARCH", searchUrl);
                                try{
                                    URL url = new URL(searchUrl);
                                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                    connection.setRequestMethod("GET");
                                    connection.connect();
                                    int responseCode = connection.getResponseCode();
                                    if (responseCode == HttpURLConnection.HTTP_OK) {
                                        InputStream inputStream = connection.getInputStream();
                                        String result = convertStreamToString(inputStream);
                                        Log.d("IMUSICPLAYER_SEARCH", result);
                                        JSONObject obj = new JSONObject(result);
                                        JSONArray songArray = (JSONArray) ((JSONObject)obj.get(RESPONSE_DATA_RESULT)).get(RESPONSE_DATA_SONGS);
                                        Log.d("IMUSICPLAYER_SEARCH", songArray.toString());

                                        for (int i = 0; i < songArray.length(); i++) {
                                            songIdList.add(Integer.toString(songArray.getJSONObject(i).getInt("id")));
                                        }
                                    }
                                    // 查找歌曲专辑
                                    for (String songId: songIdList) {
                                        String albumUrl = music163AlbumUrl.replace("arg_id", songId);
                                        Log.d("IMUSICPLAYER_ALBUM", albumUrl);
                                        URL mUrl = new URL(albumUrl);
                                        HttpURLConnection mConnection = (HttpURLConnection) mUrl.openConnection();
                                        mConnection.setRequestMethod("GET");
                                        mConnection.connect();
                                        int mResponseCode = mConnection.getResponseCode();
                                        if (mResponseCode == HttpURLConnection.HTTP_OK) {
                                            InputStream mInputStream = mConnection.getInputStream();
                                            String mResult = convertStreamToString(mInputStream);
                                            Log.d("IMUSICPLAYER_ALBUM", mResult);
                                            JSONObject mObj = new JSONObject(mResult);
                                            final JSONObject mSongObj = ((JSONArray) mObj.get(RESPONSE_DATA_SONGS)).getJSONObject(0);
                                            String title = mSongObj.getString(RESPONSE_DATA_NAME);
                                            String singer = mSongObj.getJSONObject(RESPONSE_DATA_ALBUM).getJSONArray(RESPONSE_DATA_ARTISTS).getJSONObject(0).getString(RESPONSE_DATA_NAME);
                                            String epname = mSongObj.getJSONObject(RESPONSE_DATA_ALBUM).getString(RESPONSE_DATA_NAME);
                                            String picUrl = mSongObj.getJSONObject(RESPONSE_DATA_ALBUM).getString(RESPONSE_DATA_PICURL);
                                            mSongList.add(new Song(songId, title, singer, epname, picUrl));
                                        }

                                    }
                                    // 在子线程中更新UI
                                    MainActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            ArrayAdapter adapter = new SongAdapter(MainActivity.this, mSongList);
                                            songListView.setAdapter(adapter);
                                            loadingBlock.setVisibility(View.GONE);
                                        }
                                    });
                                }catch(Exception e){
                                    Log.d("IMUSICPLAYER_ERROR", e.getMessage());
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }else{
                        Toast.makeText(MainActivity.this, "输入不能为空", Toast.LENGTH_SHORT).show();
                    }

                }
                return false;
            }
        });

    }
    class SongAdapter extends ArrayAdapter<Song> {
        private List<Song> songList;
        SongAdapter(Context context, List list){
            super(context,0, list);
            songList = list;
        }
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.song_name_list, null);
            // add listener to connect
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("IMUSICPLAYER_SEARCH", "click song "+position);
                    Song songObj = songList.get(position);
                    Intent intent = new Intent(getApplicationContext(), DetailActivity.class);
                    intent.putExtra(DetailActivity.DETAIL_CURRENT_SONG, songObj);
                    intent.putExtra(DetailActivity.DETAIL_SONG_LIST, (Serializable) mSongList);
                    startActivity(intent);
                }
            });
            // 填充ListView数据
            Song songObj = songList.get(position);
            TextView titleTextView = convertView.findViewById(R.id.song_item_title);
            TextView singerEpnameTextView = convertView.findViewById(R.id.song_item_singer_epname);
            titleTextView.setText(songObj.getTitle());
            singerEpnameTextView.setText(songObj.getSinger()+" - "+songObj.getEpname());
            return convertView;
        }
    }

}

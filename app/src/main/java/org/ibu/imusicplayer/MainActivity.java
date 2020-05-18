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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import org.ibu.imusicplayer.dao.OpenHelperFactory;
import org.ibu.imusicplayer.player.DetailActivity;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * 歌曲搜索页面Activity
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    ListView songListView;

    List<Song> mSongList;
    /* android6以上动态申请读写权限请求码*/
    int READ_WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 1;

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart()");
        super.onStart();
    }
    /**
     * 动态申请权限后回调方法
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode == READ_WRITE_EXTERNAL_STORAGE_REQUEST_CODE){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){// 授权成功
//                toSongListActivity(OpenHelperFactory.DB_TYPE.DB_TYPE_LOCAL);
            }else{// 授权失败
                Toast.makeText(this, "请允许存储权限", Toast.LENGTH_SHORT).show();
                this.finish();
            }
        }
    }
    void toSongListActivity(String dbType){
        Log.d(TAG, "toSongListActivity():"+dbType);
        Intent intent = new Intent(getApplicationContext(), SongListActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(BundleConstants.DB_TYPE, dbType);
        intent.putExtras(bundle);
        startActivity(intent);
    }
    private void ensureLocalPermission(){
        Log.d(TAG, "ensureLocalPermission()");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    READ_WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        // 隐藏顶部标题
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ensureLocalPermission();
        initView();
    }

    void initView(){
        Log.d(TAG, "initView()");
        // 初始化搜索输入框
        final EditText songNameInput = findViewById(R.id.song_name_input);
        songNameInput.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        // 初始化正在加载图标
        final LinearLayout loadingBlock = findViewById(R.id.main_loading_block);
        loadingBlock.setVisibility(View.INVISIBLE);
        // 初始化ListView
        songListView = findViewById(R.id.song_name_list);
        // 自定义更多按钮
        ImageView downloadedIcon = findViewById(R.id.icon_downloaded);
        downloadedIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
				Log.d(TAG, "click btn localsongs");
                toSongListActivity(OpenHelperFactory.DB_TYPE.DB_TYPE_LOCAL);
            }
        });
        // 自定义更多按钮
//        ImageView collectIcon = findViewById(R.id.icon_collectlist);
//        collectIcon.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                toSongListActivity(OpenHelperFactory.DB_TYPE.DB_TYPE_COLLECT);
//            }
//        });
        // 监听输入框按下搜索
        songNameInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                // 按下搜索键
                if(actionId == EditorInfo.IME_ACTION_SEARCH){
                    if(songNameInput.getText().toString()!= null && !songNameInput.getText().toString().trim().equals("")){
                        Log.d(TAG, "click keyboard search");
                        loadingBlock.setVisibility(View.VISIBLE);
                        final List<String> songIdList = new ArrayList<>();
                        mSongList = new ArrayList<Song>();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                // 搜索歌曲
                                String searchUrl = Music163Constants.music163SearchUrl.replace("arg_s", songNameInput.getText());
                                Log.d(TAG, searchUrl);
                                try{
                                    URL url = new URL(searchUrl);
                                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                    connection.setRequestMethod("GET");
                                    connection.connect();
                                    int responseCode = connection.getResponseCode();
                                    if (responseCode == HttpURLConnection.HTTP_OK) {
                                        InputStream inputStream = connection.getInputStream();
                                        String result =  Music163Constants.convertStreamToString(inputStream);
                                        Log.d(TAG, result);
                                        JSONObject obj = new JSONObject(result);
                                        JSONArray songArray = (JSONArray) ((JSONObject)obj.get(Music163Constants.RESPONSE_DATA_RESULT)).get(Music163Constants.RESPONSE_DATA_SONGS);
                                        Log.d(TAG, songArray.toString());

                                        for (int i = 0; i < songArray.length(); i++) {
                                            songIdList.add(Integer.toString(songArray.getJSONObject(i).getInt("id")));
                                        }
                                    }
                                    // 查找歌曲专辑
                                    for (String songId: songIdList) {
                                        String albumUrl = Music163Constants.music163AlbumUrl.replace("arg_id", songId);
                                        Log.d(TAG, albumUrl);
                                        URL mUrl = new URL(albumUrl);
                                        HttpURLConnection mConnection = (HttpURLConnection) mUrl.openConnection();
                                        mConnection.setRequestMethod("GET");
                                        mConnection.connect();
                                        int mResponseCode = mConnection.getResponseCode();
                                        if (mResponseCode == HttpURLConnection.HTTP_OK) {
                                            InputStream mInputStream = mConnection.getInputStream();
                                            String mResult = Music163Constants.convertStreamToString(mInputStream);
                                            Log.d(TAG, mResult);
                                            JSONObject mObj = new JSONObject(mResult);
                                            final JSONObject mSongObj = ((JSONArray) mObj.get(Music163Constants.RESPONSE_DATA_SONGS)).getJSONObject(0);
                                            String title = mSongObj.getString(Music163Constants.RESPONSE_DATA_NAME);
                                            String singer = mSongObj.getJSONObject(Music163Constants.RESPONSE_DATA_ALBUM).getJSONArray(Music163Constants.RESPONSE_DATA_ARTISTS).getJSONObject(0).getString(Music163Constants.RESPONSE_DATA_NAME);
                                            String epname = mSongObj.getJSONObject(Music163Constants.RESPONSE_DATA_ALBUM).getString(Music163Constants.RESPONSE_DATA_NAME);
                                            String picUrl = mSongObj.getJSONObject(Music163Constants.RESPONSE_DATA_ALBUM).getString(Music163Constants.RESPONSE_DATA_PICURL);
                                            mSongList.add(new Song(songId, title, singer, epname, picUrl));
                                        }

                                    }
									Log.d(TAG, "search finished:"+mSongList.toString());
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
                                    Log.d(TAG, "search fail:"+e.getMessage());
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
			Log.d(TAG, "getView():position:"+position);
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.song_name_list, null);
            // add listener to connect
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "click song "+position);
                    Song songObj = songList.get(position);
                    Intent intent = new Intent(getApplicationContext(), DetailActivity.class);
                    intent.putExtra(BundleConstants.DB_TYPE, OpenHelperFactory.DB_TYPE.DB_TYPE_NETWORK);
                    intent.putExtra(BundleConstants.DETAIL_CURRENT_SONG, songObj);
                    intent.putExtra(BundleConstants.DETAIL_SONG_LIST, (Serializable) mSongList);
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

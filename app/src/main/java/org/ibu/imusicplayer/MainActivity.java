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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.ibu.imusicplayer.Music163Contants.*;

public class MainActivity extends AppCompatActivity {



    public static String convertStreamToString(InputStream is){
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        try{
            while ((line = reader.readLine())!=null){
                sb.append(line+"\n");
            }
        }catch (IOException e){
            Log.d("SEARCH_ERROR", e.getMessage());
        }finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
    ListView songListView;
    LinearLayout loadingBlock;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 隐藏顶部标题
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText songNameInput = findViewById(R.id.song_name_input);
        songNameInput.setImeOptions(EditorInfo.IME_ACTION_SEARCH);

        songListView = findViewById(R.id.song_name_list);
        loadingBlock = findViewById(R.id.main_loading_block);
        loadingBlock.setVisibility(View.INVISIBLE);
        // add send action
        songNameInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                // 按下搜索键
                if(actionId == EditorInfo.IME_ACTION_SEARCH){
                    if(songNameInput.getText().toString()!= null && !songNameInput.getText().toString().trim().equals("")){
                        Log.d("IMUSICPLAYER_SEARCH", "click");
                        loadingBlock.setVisibility(View.VISIBLE);
                        final List<String> songIdList = new ArrayList<>();
                        final List<JSONObject> songList = new ArrayList<>();
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
                                            songList.add(mSongObj);
                                        }

                                    }
                                    // 在子线程中更新UI
                                    MainActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            ArrayAdapter adapter = new SongAdapter(MainActivity.this, songList);
                                            songListView.setAdapter(adapter);
                                            loadingBlock.setVisibility(View.GONE);
                                        }
                                    });
                                }catch(Exception e){
                                    Log.d("SEARCH_ERROR", e.getMessage());
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
    class SongAdapter extends ArrayAdapter<JSONObject> {
        private List<JSONObject> songList;
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
                    JSONObject songObj = songList.get(position);
                    Intent intent = new Intent(getApplicationContext(), DetailActivity.class);
                    try{
                        String songId = Integer.toString(songObj.getInt(RESPONSE_DATA_ID));
                        String title = songObj.getString(RESPONSE_DATA_NAME);
                        String singer = songObj.getJSONObject(RESPONSE_DATA_ALBUM).getJSONArray(RESPONSE_DATA_ARTISTS).getJSONObject(0).getString(RESPONSE_DATA_NAME);
                        String epname = songObj.getJSONObject(RESPONSE_DATA_ALBUM).getString(RESPONSE_DATA_NAME);
                        String picUrl = songObj.getJSONObject(RESPONSE_DATA_ALBUM).getString(RESPONSE_DATA_PICURL);
                        intent.putExtra(DetailActivity.DETAIL_SONG_ID, songId);
                        intent.putExtra(DetailActivity.DETAIL_SONG_TITLE, title);
                        intent.putExtra(DetailActivity.DETAIL_SONG_SINGER, singer);
                        intent.putExtra(DetailActivity.DETAIL_SONG_EPNAME, epname);
                        intent.putExtra(DetailActivity.DETAIL_SONG_PICURL, picUrl);
                        startActivity(intent);
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            });
            try {
                JSONObject songObj = songList.get(position);
                String title = songObj.getString(RESPONSE_DATA_NAME);
                String singer = songObj.getJSONObject(RESPONSE_DATA_ALBUM).getJSONArray(RESPONSE_DATA_ARTISTS).getJSONObject(0).getString(RESPONSE_DATA_NAME);
                String epname = songObj.getJSONObject(RESPONSE_DATA_ALBUM).getString(RESPONSE_DATA_NAME);
                TextView titleTextView = convertView.findViewById(R.id.song_item_title);
                TextView singerEpnameTextView = convertView.findViewById(R.id.song_item_singer_epname);
                titleTextView.setText(title);
                singerEpnameTextView.setText(singer+" - "+epname);
            }catch (JSONException e){
                Log.d("ALBUM_ERROR", e.getMessage());
                e.printStackTrace();
            }
            return convertView;
        }
    }

}

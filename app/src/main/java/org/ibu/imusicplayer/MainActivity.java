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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
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

public class MainActivity extends AppCompatActivity {

    private final static String music163SearchUrl = "https://music.163.com/api/search/get/web?limit=20&type=1&s=arg_s";

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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 隐藏顶部标题
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText songNameInput = findViewById(R.id.song_name_input);
        songNameInput.setImeOptions(EditorInfo.IME_ACTION_SEARCH);

        songListView = findViewById(R.id.song_name_list);
        // add send action
        songNameInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH){   Log.d("SEARCH", "click");
                    final List<JSONObject> songList = new ArrayList<>();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String searchUrl = music163SearchUrl.replace("arg_s", songNameInput.getText());
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
                                    JSONArray songArray = (JSONArray) ((JSONObject)obj.get("result")).get("songs");
                                    Log.d("SEARCH", songArray.toString());

                                    for (int i = 0; i < songArray.length(); i++) {
                                        songList.add(songArray.getJSONObject(i));
                                    }

                                }
                                // 在子线程中跟新UI
                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ArrayAdapter adapter = new SongAdapter(MainActivity.this, songList);
                                        songListView.setAdapter(adapter);
                                    }
                                });
                            }catch(Exception e){
                                Log.d("SEARCH_ERROR", e.getMessage());
                                e.printStackTrace();
                            }
                        }
                    }).start();
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
                    Log.d("SEARCH", "click song "+position);
                    JSONObject songObj = songList.get(position);
                    Intent intent = new Intent(getApplicationContext(), DetailActivity.class);
                    int songId = -1;
                    try{
                        songId = songObj.getInt("id");
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                    intent.putExtra(DetailActivity.SONG_INFO, songId);
                    startActivity(intent);
                }
            });
            try {
                JSONObject songObj = songList.get(position);
                JSONArray artistArray = (JSONArray) songObj.get("artists");
                TextView textView = convertView.findViewById(R.id.song_name_text);
                textView.setText((String) songObj.get("name") + "-" + ((JSONObject) artistArray.get(0)).get("name"));
            }catch (JSONException e){
                Log.d("SEARCH_ERROR", e.getMessage());
                e.printStackTrace();
            }
            return convertView;
        }
    }

}

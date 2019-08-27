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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.Serializable;
import java.util.List;

/**
 * 已收藏页面Activity
 */
public class DownloadActivity extends AppCompatActivity {
    ListView downloadSongListView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        getSupportActionBar().hide();
        // 初始化back button
        ImageView backIcon = findViewById(R.id.back_for_download);
        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DownloadActivity.this.finish();
            }
        });
        downloadSongListView = findViewById(R.id.download_song_name_list);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 查找所有收藏信息
        DownloadOpenHelper dbHelper = new DownloadOpenHelper(this);
        List<Song> songList = dbHelper.queryAll();
        ArrayAdapter adapter = new CollectSongAdapter(DownloadActivity.this, songList);
        downloadSongListView.setAdapter(adapter);
    }

    class CollectSongAdapter extends ArrayAdapter<Song> {
        private List<Song> songList;
        CollectSongAdapter(Context context, List list){
            super(context,0, list);
            songList = list;
        }
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.download_song_name_list, null);
            // add listener to connect
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("IMUSICPLAYER_COLLECT", "click song "+position);
                    Song songObj = songList.get(position);
                    Intent intent = new Intent(getApplicationContext(), DetailActivity.class);
                    intent.putExtra(DetailActivity.DETAIL_CURRENT_SONG, songObj);
                    intent.putExtra(DetailActivity.DETAIL_SONG_LIST, (Serializable) songList);
                    startActivity(intent);
                }
            });
            Song songObj = songList.get(position);
            TextView titleTextView = convertView.findViewById(R.id.download_song_item_title);
            TextView singerEpnameTextView = convertView.findViewById(R.id.download_song_item_singer_epname);
            titleTextView.setText(songObj.getTitle());
            singerEpnameTextView.setText(songObj.getSinger()+" - "+songObj.getEpname());
            return convertView;
        }
    }

}

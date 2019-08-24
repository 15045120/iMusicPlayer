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
import android.widget.ListView;
import android.widget.TextView;

import java.io.Serializable;
import java.util.List;


public class CollectActivity extends AppCompatActivity {
    ListView collectSongListView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collect);
        getSupportActionBar().hide();
//        this.setTitle("收藏夹");
        collectSongListView = findViewById(R.id.collect_song_name_list);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 查找所有收藏信息
        CollectOpenHelper dbHelper = new CollectOpenHelper(this);
        List<Song> songList = dbHelper.queryAll();
        ArrayAdapter adapter = new CollectSongAdapter(CollectActivity.this, songList);
        collectSongListView.setAdapter(adapter);
    }

    class CollectSongAdapter extends ArrayAdapter<Song> {
        private List<Song> songList;
        CollectSongAdapter(Context context, List list){
            super(context,0, list);
            songList = list;
        }
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.collect_song_name_list, null);
            // add listener to connect
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("IMUSICPLAYER_SEARCH", "click song "+position);
                    Song songObj = songList.get(position);
                    Intent intent = new Intent(getApplicationContext(), DetailActivity.class);
                    intent.putExtra(DetailActivity.DETAIL_CURRENT_SONG, songObj);
                    intent.putExtra(DetailActivity.DETAIL_SONG_LIST, (Serializable) songList);
                    startActivity(intent);
                }
            });
            Song songObj = songList.get(position);
            TextView titleTextView = convertView.findViewById(R.id.collect_song_item_title);
            TextView singerEpnameTextView = convertView.findViewById(R.id.collect_song_item_singer_epname);
            titleTextView.setText(songObj.getTitle());
            singerEpnameTextView.setText(songObj.getSinger()+" - "+songObj.getEpname());
            return convertView;
        }
    }

}

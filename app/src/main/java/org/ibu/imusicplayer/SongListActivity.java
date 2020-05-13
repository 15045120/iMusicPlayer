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
import org.ibu.imusicplayer.dao.BaseOpenHelper;
import org.ibu.imusicplayer.dao.OpenHelperFactory;
import org.ibu.imusicplayer.player.DetailActivity;

import java.io.Serializable;
import java.util.List;

public class SongListActivity extends AppCompatActivity {
    private static final String TAG = "SongListActivity";

    private String dbType;
    private BaseOpenHelper dbHelper;
    ListView songListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        // 隐藏顶部标题
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_songlist);

        Bundle bundle = getIntent().getExtras();
        dbType = bundle.getString(MainActivity.DB_TYPE);
        initView();
    }
    void initView(){
        Log.d(TAG, "initView");
        // 初始化标题
        TextView title = findViewById(R.id.song_db_type);
        title.setText(dbType);
        // 初始化back button
        ImageView backIcon = findViewById(R.id.back_for_songlist);
        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SongListActivity.this.finish();
            }
        });
        songListView = findViewById(R.id.collect_song_name_list);
    }
    @Override
    protected void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();
        dbHelper = OpenHelperFactory.getOpenHelper(this, dbType);
        List<Song> songList = dbHelper.queryAll();
        ArrayAdapter adapter = new SongAdapter(this, songList);
        songListView.setAdapter(adapter);
    }

    class SongAdapter extends ArrayAdapter<Song> {
        private List<Song> songList;
        SongAdapter(Context context, List list){
            super(context,0, list);
            songList = list;
        }
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_song, null);
            // add listener to connect
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "click song "+position);
                    Song songObj = songList.get(position);
                    Intent intent = new Intent(getApplicationContext(), DetailActivity.class);
                    intent.putExtra(MainActivity.DB_TYPE, OpenHelperFactory.DB_TYPE_LOCAL);
                    intent.putExtra(DetailActivity.DETAIL_CURRENT_SONG, songObj);
                    intent.putExtra(DetailActivity.DETAIL_SONG_LIST, (Serializable) songList);
                    startActivity(intent);
                }
            });
            Song songObj = songList.get(position);
            TextView titleTextView = convertView.findViewById(R.id.song_item_title);
            TextView singerEpnameTextView = convertView.findViewById(R.id.song_item_singer_epname);
            titleTextView.setText(songObj.getTitle());
            singerEpnameTextView.setText(songObj.getSinger()+" - "+songObj.getEpname());
            return convertView;
        }
    }

}
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
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.ibu.imusicplayer.dao.BaseOpenHelper;
import org.ibu.imusicplayer.dao.OpenHelperFactory;
import org.ibu.imusicplayer.lyric.DownloadMp3Util;
import org.ibu.imusicplayer.player.DetailActivity;
import org.w3c.dom.Text;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SongListActivity extends AppCompatActivity {
    private static final String TAG = "SongListActivity";

    private String dbType;
    private BaseOpenHelper dbHelper;
    ListView songListView;
    SongListAdapter songAdapter;
    LinearLayout deleteBlock;

    String[] editTexts = new String[]{"编辑",
            "取消"};
    int editCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        // 隐藏顶部标题
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_songlist);

        Bundle bundle = getIntent().getExtras();
        dbType = bundle.getString(BundleConstants.DB_TYPE);
        initView();
    }
    void initView(){
        Log.d(TAG, "initView()");
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
        deleteBlock = findViewById(R.id.song_delete_block);
        deleteBlock.setVisibility(View.GONE);
        final TextView editView = findViewById(R.id.song_edit_text);
        editView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editCount = (editCount + 1) % editTexts.length;
                editView.setText(editTexts[editCount]);
                changeDeletePanel();
            }
        });
        final TextView selectAllView = findViewById(R.id.song_select_all);
        selectAllView.setOnClickListener(new View.OnClickListener() {
            int selected = 0;
            String[] selectTexts = new String[]{"全选",
                    "取消全选"};
            @Override
            public void onClick(View v) {
				Log.d(TAG, "click selectAll");
                selected = (selected + 1) % 2;
                for (int i = 0; i < songAdapter.mSongList.size(); i++) {
                    songAdapter.mSongList.get(i).put(SongListAdapter.SELECTED_STATE,
                            selected);
                    songAdapter.notifyDataSetChanged();
                }
				Log.d(TAG, "selectAll change to:"+selectTexts[selected]);
                selectAllView.setText(selectTexts[selected]);
            }
        });
        final TextView deleteView = findViewById(R.id.song_delete);
        deleteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
				Log.d(TAG, "click delete");
                List<Map> deleteList = new ArrayList<>();
                List newList = new ArrayList(songAdapter.mSongList);
                for (int i = 0; i < songAdapter.mSongList.size(); i++) {
                    int selected = (int)songAdapter.mSongList.get(i).get(SongListAdapter.SELECTED_STATE);
                    if(selected == SelectImageView.SELECTED){
                        deleteList.add(songAdapter.mSongList.get(i));
                    }
                }
				Log.d(TAG, "delete all:"+deleteList.toString());
                doSongDelete(deleteList);
                songAdapter.mSongList.removeAll(deleteList);
                editView.callOnClick();
            }
        });

    }
    void doSongDelete(List<Map> deleteList){
		Log.d(TAG, "delete all:"+deleteList.toString());
        BaseOpenHelper downloadOpenHelper = OpenHelperFactory.getOpenHelper(this,
                OpenHelperFactory.DB_TYPE.DB_TYPE_DOWNLOAD);

        for(Map map: deleteList){
            Song song = (Song) map.get(SongListAdapter.SONG);
            DownloadMp3Util downloadMp3Util = new DownloadMp3Util(this, song);
            // 删除
            downloadOpenHelper.delete(song);
            downloadMp3Util.remove();
//            if (downloadOpenHelper.exist(song.getId()) != null){
//                downloadOpenHelper.delete(song);
//                downloadMp3Util.remove();
//            }else{
//                downloadMp3Util.remove();
//            }
        }
    }

    void changeDeletePanel(){
        Log.d(TAG, "changeDeletePanel():to:"+editCount);
        if(editCount == 0) {
            deleteBlock.setVisibility(View.GONE);
            for (int i = 0; i < songAdapter.mSongList.size(); i++) {
                songAdapter.mSongList.get(i).put(SongListAdapter.VISIBILITY_STATE, View.GONE);
            }
        }else if(editCount == 1){
            deleteBlock.setVisibility(View.VISIBLE);
            for (int i = 0; i < songAdapter.mSongList.size(); i++) {
                songAdapter.mSongList.get(i).put(SongListAdapter.VISIBILITY_STATE, View.VISIBLE);
            }
        }
    }
    @Override
    protected void onStart() {
        Log.d(TAG, "onStart()");
        super.onStart();
        dbHelper = OpenHelperFactory.getOpenHelper(this, dbType);
        List<Song> songList = dbHelper.queryAll();

        List<Map<String, Object>> listitem = new ArrayList<Map<String, Object>>();
        for (Song song: songList) {
            Map<String, Object> songitem = new HashMap<String, Object>();
            songitem.put(SongListAdapter.SELECTED_STATE, SelectImageView.NOT_SELECTED);
            songitem.put(SongListAdapter.VISIBILITY_STATE, View.GONE);
            songitem.put(SongListAdapter.SONG, song);
            listitem.add(songitem);
        }
        songAdapter = new SongListAdapter(this, listitem);
        songListView.setAdapter(songAdapter);
        songListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "click song "+position);
                if(editCount == 1) {
                    int selected = ((int)songAdapter.mSongList.get(position).get(SongListAdapter.SELECTED_STATE) +1)%2;
                    songAdapter.mSongList.get(position).put(SongListAdapter.SELECTED_STATE,
                            selected);
                    songAdapter.notifyDataSetChanged();
                }
                else if(editCount == 0) {
                    Song songObj = (Song)songAdapter.mSongList.get(position).get(SongListAdapter.SONG);
                    Intent intent = new Intent(getApplicationContext(), DetailActivity.class);
                    intent.putExtra(BundleConstants.DB_TYPE, dbType);
                    intent.putExtra(BundleConstants.DETAIL_CURRENT_SONG, songObj);
                    intent.putExtra(BundleConstants.DETAIL_SONG_LIST, (Serializable) songAdapter.mList);
                    startActivity(intent);
                }
            }
        });
    }
    public class SongListAdapter extends BaseAdapter {
        final static String SELECTED_STATE = "selected";
        final static String SONG = "song";
        final static String VISIBILITY_STATE = "visibility";

        private List<Map<String, Object>> mSongList;
        private List<Song> mList;
        private Context mContext;

        public SongListAdapter(Context context,List<Map<String, Object>> list) {
            mSongList = list;
            mContext = context;
            mList = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                mList.add((Song)list.get(i).get(SONG));
            }
        }
        @Override
        public int getCount() {
            return mSongList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
			Log.d(TAG, "getView():position:"+position);
            ViewHolder holder = null;
            if (convertView == null) {
                holder=new ViewHolder();
                convertView = LayoutInflater.from(mContext).inflate(R.layout.list_song, null);
                holder.mSelectImageView = convertView.findViewById(R.id.song_select_icon);
                holder.mSongTitle = convertView.findViewById(R.id.song_item_title);
                holder.mSongEpname = convertView.findViewById(R.id.song_item_singer_epname);
                convertView.setTag(holder);
            }else {
                holder = (ViewHolder)convertView.getTag();
            }
            LayoutInflater.from(mContext).inflate(R.layout.list_song, null);
            holder.mSelectImageView.setSelected((int)mSongList.get(position).get(SELECTED_STATE));
            holder.mSelectImageView.setVisibility((int)mSongList.get(position).get(VISIBILITY_STATE));

            Song song = (Song)mSongList.get(position).get(SONG);
            holder.mSongTitle.setText(song.getTitle());
            holder.mSongEpname.setText(song.getSinger()+" - "+song.getEpname());
            return convertView;
        }
        public class ViewHolder{
            public SelectImageView mSelectImageView;
            public TextView mSongTitle;
            public TextView mSongEpname;
        };
    }
}
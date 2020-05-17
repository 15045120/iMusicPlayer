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
import org.ibu.imusicplayer.player.DetailActivity;
import org.w3c.dom.Text;

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
    SongAdapter songAdapter;
    LinearLayout deleteBlock;

    String[] editTexts = new String[]{"编辑",
            "取消"};
    int editCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        // 隐藏顶部标题
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_songlist);

        Bundle bundle = getIntent().getExtras();
        dbType = bundle.getString(BundleConstants.DB_TYPE);
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
                selected = (selected + 1) % 2;
                for (int i = 0; i < songAdapter.songSelectIconList.size(); i++) {
                    songAdapter.songSelectIconList.get(i).setSelected(selected == 0 ? SelectImageView.NOT_SELECTED: SelectImageView.SELECTED);
                }
                selectAllView.setText(selectTexts[selected]);
            }
        });
        final TextView deleteView = findViewById(R.id.song_delete);
        deleteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Song> deleteList = new ArrayList<>();
                for (int i = 0; i < songAdapter.songSelectIconList.size(); i++) {
                    int selected = songAdapter.songSelectIconList.get(i).getSelected();
                    if(selected == SelectImageView.SELECTED){
                        deleteList.add(songAdapter.mSongList.get(i));
                    }
                }
                editView.callOnClick();
            }
        });

    }

    void changeDeletePanel(){
        Log.d(TAG, "changeDeletePanel");
        if(editCount == 0) {
            deleteBlock.setVisibility(View.GONE);
            for (int i = 0; i < songAdapter.songSelectIconList.size(); i++) {
                songAdapter.songSelectIconList.get(i).setVisibility(View.GONE);
            }
        }else if(editCount == 1){
            deleteBlock.setVisibility(View.VISIBLE);
            for (int i = 0; i < songAdapter.songSelectIconList.size(); i++) {
                songAdapter.songSelectIconList.get(i).setVisibility(View.VISIBLE);
            }
        }
    }
    @Override
    protected void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();
        dbHelper = OpenHelperFactory.getOpenHelper(this, dbType);
        List<Song> songList = dbHelper.queryAll();

        List<Map<String, Object>> listitem = new ArrayList<Map<String, Object>>();
        for (Song song: songList) {
            Map<String, Object> songitem = new HashMap<String, Object>();
            songitem.put("selectIcon", R.drawable.ic_file_not_selected);
            songitem.put("title", song.getTitle());
            songitem.put("detail", song.getSinger()+" - "+song.getEpname());
            listitem.add(songitem);
        }
        songAdapter = new SongAdapter(getApplicationContext(),listitem,
                R.layout.list_song,new String[]{"selectIcon","title","detail"},
                new int[]{R.id.song_select_icon,R.id.song_item_title,R.id.song_item_singer_epname},
                songList);
        songListView.setAdapter(songAdapter);
//        songListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//
//            }
//        });
    }

    class SongAdapter extends SimpleAdapter {
        private List<Song> mSongList;
        List<SelectImageView> songSelectIconList;
        SongAdapter(Context context, List<? extends Map<String, ?>> data,
                                 int resource, String[] from, int[] to,
        List list){
            super(context, data, resource,from, to);
            mSongList = list;
            songSelectIconList =  new ArrayList<>();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            Log.d(TAG, "SongAdapter::getView::"+position);
            View view = super.getView(position, convertView, parent);
            if(songSelectIconList.size() == 0 || position >= songSelectIconList.size()){
                Log.d(TAG, "SongAdapter::getView::firstLoaded");
                SelectImageView songSelectIcon = view.findViewById(R.id.song_select_icon);
                songSelectIcon.setSelected(SelectImageView.NOT_SELECTED);
                songSelectIconList.add(position, songSelectIcon);
            }
            // add listener to connect
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "click song "+position);
                    if(editCount == 1) {
                        int selected = (songSelectIconList.get(position).getSelected() +1)%2;
                        songSelectIconList.get(position).setSelected(selected);
                    }
                    else if(editCount == 0) {
                        Song songObj = mSongList.get(position);
                        Intent intent = new Intent(getApplicationContext(), DetailActivity.class);
                        intent.putExtra(BundleConstants.DB_TYPE, dbType);
                        intent.putExtra(BundleConstants.DETAIL_CURRENT_SONG, songObj);
                        intent.putExtra(BundleConstants.DETAIL_SONG_LIST, (Serializable) mSongList);
                        startActivity(intent);
                    }
                }
            });
            return view;
        }
    }

}
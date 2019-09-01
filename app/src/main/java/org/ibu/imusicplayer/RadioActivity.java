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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.ibu.imusicplayer.QingtingFMConstants.*;

/**
 * Created by ibu on 2019/9/1.
 */
public class RadioActivity extends AppCompatActivity {
    ListView radioListView;
    private List<List<RadioFMCategories>> mCategroiesList;
    ListView categoriesListView;
    int mCategoriesNum;

    LinearLayout rootCategoryLayout;
    LinearLayout subCategoryLayout;
    ImageView openCategoryIcon;
    TextView currentCategoryText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radio);
        getSupportActionBar().hide();
        // 初始化back button
        ImageView backIcon = findViewById(R.id.back_for_radio);
        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RadioActivity.this.finish();
            }
        });
        // 初始化顶级目录
        TextView centerTextView = findViewById(R.id.menu_radio_category_center);
        centerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCategoriesNum = 0;
                ArrayAdapter adapter = new RadioFMCategoriesAdapter(RadioActivity.this, mCategroiesList.get(mCategoriesNum));
                categoriesListView.setAdapter(adapter);
                categoriesListView.setDividerHeight(0);
            }
        });
        TextView localTextView = findViewById(R.id.menu_radio_category_local);
        localTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCategoriesNum = 1;
                ArrayAdapter adapter = new RadioFMCategoriesAdapter(RadioActivity.this, mCategroiesList.get(mCategoriesNum));
                categoriesListView.setAdapter(adapter);
                categoriesListView.setDividerHeight(0);
            }
        });
        TextView networkTextView = findViewById(R.id.menu_radio_category_newwork);
        networkTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCategoriesNum = 2;
                ArrayAdapter adapter = new RadioFMCategoriesAdapter(RadioActivity.this, mCategroiesList.get(mCategoriesNum));
                categoriesListView.setAdapter(adapter);
                categoriesListView.setDividerHeight(0);
            }
        });
        // 初始化次级目录
        mCategoriesNum = 0;
        mCategroiesList = new ArrayList<>();
        try {
            JSONObject categoryObj = new JSONObject(QINGTING_FM_CATEGORIES);
            JSONArray jsonArray = categoryObj.getJSONArray(QINGTING_FM_CATEGORIES_SUB);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONArray mJsonArray = jsonArray.getJSONArray(i);
                List<RadioFMCategories> mList = new ArrayList<>();
                for (int j = 0; j < mJsonArray.length(); j++) {
                    JSONObject mJsonObj = mJsonArray.getJSONObject(j);
                    RadioFMCategories fmCategories = new RadioFMCategories(mJsonObj.getString(QINGTING_FM_CATEGORIES_ID), mJsonObj.getString(QINGTING_FM_CATEGORIES_TITLE));
                    mList.add(fmCategories);
                }
                mCategroiesList.add(mList);
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
        // categoriesListView
        categoriesListView = findViewById(R.id.radio_category_list);
        ArrayAdapter adapter = new RadioFMCategoriesAdapter(RadioActivity.this, mCategroiesList.get(mCategoriesNum));
        categoriesListView.setAdapter(adapter);
        // radioListView
        radioListView = findViewById(R.id.radio_list);
        // currentCategoryText
        currentCategoryText = findViewById(R.id.current_category_text);
        // 显示隐藏category
        rootCategoryLayout = findViewById(R.id.menu_radio_category_root_block);
        rootCategoryLayout.setVisibility(View.GONE);
        currentCategoryText.setText("中央/全部");
        // openCategoryIcon
        openCategoryIcon = findViewById(R.id.open_category_icon);
        LinearLayout openCategoryLayout = findViewById(R.id.open_category_icon_block);
        openCategoryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(rootCategoryLayout.getVisibility() == View.VISIBLE){
                    rootCategoryLayout.setVisibility(View.GONE);
                    openCategoryIcon.setImageResource(R.drawable.ic_down_category);
                }else{
                    rootCategoryLayout.setVisibility(View.VISIBLE);
                    openCategoryIcon.setImageResource(R.drawable.ic_up_category);
                }
            }
        });


       searchRadioByCategoriesId(mCategroiesList.get(0).get(0).getId());
    }
    void searchRadioByCategoriesId(final String id){
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<RadioFM> radioFMList = new ArrayList<>();
                Log.d("IMUSICPLAYER_RADIO", qingtingFMCategoryUrl.replace("arg_category",id));
                try {
                    URL url = new URL(qingtingFMCategoryUrl.replace("arg_category",id));
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.connect();
                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        InputStream inputStream = connection.getInputStream();
                        String result = convertStreamToString(inputStream);
                        Log.d("IMUSICPLAYER_RADIO", result);
                        JSONObject resultObj = new JSONObject(result);
                        JSONArray radioArray = resultObj.getJSONObject(RESPONSE_DATA_DATA).getJSONArray(RESPONSE_DATA_ITEMS);
                        for (int i = 0; i < radioArray.length(); i++) {
                            JSONObject radioObj = radioArray.getJSONObject(i);
                            String contentId = radioObj.getString(RESPONSE_DATA_CONTENTID);
                            String title = radioObj.getString(RESPONSE_DATA_TITLE);
                            String cover = radioObj.getString(RESPONSE_DATA_COVER);
                            String description = radioObj.isNull(RESPONSE_DATA_DESCRIPTION)?title:radioObj.getString(RESPONSE_DATA_DESCRIPTION);
                            if(contentId != null && title != null && cover != null && description != null) {
                                radioFMList.add(new RadioFM(contentId, title, cover, description));
                            }
                        }
                        RadioActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.d("IMUSICPLAYER_RADIO", radioFMList.toString());
                                ArrayAdapter adapter = new RadioFMAdapter(RadioActivity.this, radioFMList);
                                radioListView.setAdapter(adapter);
                                rootCategoryLayout.setVisibility(View.GONE);
                            }
                        });
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    Log.d("IMUSICPLAYER_RADIO", e.getMessage());
                }
            }
        }).start();
    }
    class RadioFMCategoriesAdapter extends ArrayAdapter<RadioFMCategories> {
        private List<RadioFMCategories> categoriesList;
        RadioFMCategoriesAdapter(Context context, List list){
            super(context,0, list);
            categoriesList = list;
        }
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.radio_category_list, null);
            // add listener to connect
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    RadioFMCategories radioFMCategoriesObj = categoriesList.get(position);
                    if(mCategoriesNum == 0){
                        currentCategoryText.setText("中央/"+radioFMCategoriesObj.getTitle());
                    }else if(mCategoriesNum == 1){
                        currentCategoryText.setText("地方/"+radioFMCategoriesObj.getTitle());
                    }else {
                        currentCategoryText.setText("网络/"+radioFMCategoriesObj.getTitle());
                    }
                    searchRadioByCategoriesId(radioFMCategoriesObj.getId());
                }
            });
            RadioFMCategories radioFMCategoriesObj = categoriesList.get(position);
            TextView titleTextView = convertView.findViewById(R.id.radio_category_item_title);
            titleTextView.setText(radioFMCategoriesObj.getTitle());
            return convertView;
        }
    }
    MediaPlayer mediaPlayer;
    class RadioFMAdapter extends ArrayAdapter<RadioFM> {
        private List<RadioFM> radioList;
        RadioFMAdapter(Context context, List list){
            super(context,0, list);
            radioList = list;
        }
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.radio_list, null);
            // add listener to connect
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        Log.d("IMUSICPLAYER_RADIO", "click song " + position);
                        RadioFM radioFMObj = radioList.get(position);
                        if(mediaPlayer != null){
                            //mediaPlayer不用了需要销毁
                            mediaPlayer.release();
                            mediaPlayer = null;
                        }
                        mediaPlayer = new MediaPlayer();
                        mediaPlayer.setDataSource(qingtingFMPlayUrl.replace("arg_channel", radioFMObj.getContentId()));
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                    }catch (IOException e){
                        Log.d("IMUSICPLAYER_RADIO", e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
            final RadioFM radioFMObj = radioList.get(position);
//            final ImageView coverImageView = convertView.findViewById(R.id.radio_item_cover);
            TextView titleTextView = convertView.findViewById(R.id.radio_item_title);
            TextView descriptionTextView = convertView.findViewById(R.id.radio_item_description);
            titleTextView.setText(radioFMObj.getTitle());
            descriptionTextView.setText(radioFMObj.getDescription());
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try{
//                        URL mUrl = new URL(radioFMObj.getCover());
//                        HttpURLConnection mConnection = (HttpURLConnection) mUrl.openConnection();
//                        mConnection.setRequestMethod("GET");
//                        mConnection.connect();
//                        int mResponseCode = mConnection.getResponseCode();
//                        if (mResponseCode == HttpURLConnection.HTTP_OK) {
//                            InputStream mInputStream = mConnection.getInputStream();
//                            final Bitmap bitmap = BitmapFactory.decodeStream(mInputStream);
//                            coverImageView.setImageBitmap(bitmap);
//                        }
//                    }catch (Exception e){
//                        Log.d("IMUSICPLAYER_RADIO", e.getMessage());
//                        e.printStackTrace();
//                    }
//                }
//            }).start();
            return convertView;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mediaPlayer != null){
            //mediaPlayer不用了需要销毁
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}

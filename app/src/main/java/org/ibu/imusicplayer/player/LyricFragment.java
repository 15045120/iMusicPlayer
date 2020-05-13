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
package org.ibu.imusicplayer.player;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.ibu.imusicplayer.R;
import org.ibu.imusicplayer.Song;
import org.ibu.imusicplayer.lyric.LyricTextView;


public class LyricFragment extends SimpleFragment {
    private static final String TAG = "LyricFragment";
    LyricTextView songLyricTextView; // 歌词

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        Bundle args = getArguments();
        View view = inflater.inflate(R.layout.fragment_lyric, null);
        songLyricTextView = view.findViewById(R.id.song_lyric_text);
        final DetailActivity activity = (DetailActivity)getActivity();
        songLyricTextView.setLyricTouchListener(new LyricTextView.OnLyricTouchListener() {
            @Override
            public void OnLyricTouched() {
                activity.changeFragment();
            }
        });
        return view;
    }

    @Override
    void update(Bundle bundle) {
        Log.d(TAG, "update");
        Song mSong = (Song) bundle.get("song");
        songLyricTextView.setLyric(mSong.getLyric());
    }
}

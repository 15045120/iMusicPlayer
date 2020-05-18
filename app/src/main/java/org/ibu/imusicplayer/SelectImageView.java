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
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

public class SelectImageView  extends ImageView {
		private static final String TAG = "SelectImageView";
		
        final static int NOT_SELECTED = 0;
        final static int SELECTED = 1;


        int selected = NOT_SELECTED;

        public SelectImageView(Context context) {
            super(context);
        }

        public SelectImageView(Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
        }

        public SelectImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        public SelectImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
        }

        public int getSelected() {
            return selected;
        }

        public void setSelected(int selected) {
			Log.d(TAG, "setSelected():"+selected);
            if (selected == NOT_SELECTED) {
                setImageResource(R.drawable.ic_file_not_selected);
            } else {
                setImageResource(R.drawable.ic_file_selected);
            }
            this.selected = selected;

        }
    }
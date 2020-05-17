package org.ibu.imusicplayer;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class SelectImageView  extends ImageView {
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
            if (selected == NOT_SELECTED) {
                setImageResource(R.drawable.ic_file_not_selected);
            } else {
                setImageResource(R.drawable.ic_file_selected);
            }
            this.selected = selected;

        }
    }
package org.ibu.imusicplayer.dao;

import android.content.Context;

public class OpenHelperFactory {
    public class DB_TYPE{
        public static final String DB_TYPE_COLLECT = "我喜欢";
        public static final String DB_TYPE_DOWNLOAD = "下载";
        public static final String DB_TYPE_HISTORY = "最近播放";
        public static final String DB_TYPE_LOCAL = "本地歌曲";
        public static final String DB_TYPE_NETWORK = "网络歌曲";
    }

    public static BaseOpenHelper getOpenHelper(Context context, String dbType){
        BaseOpenHelper dbHelper = null;
        switch (dbType){
            case DB_TYPE.DB_TYPE_COLLECT:
                dbHelper = new CollectOpenHelper(context);break;
            case DB_TYPE.DB_TYPE_DOWNLOAD:
                dbHelper = new DownloadOpenHelper(context);break;
            case DB_TYPE.DB_TYPE_HISTORY:
                dbHelper = new HistoryOpenHelper(context);break;
            case DB_TYPE.DB_TYPE_LOCAL:
                dbHelper = new LocalOpenHelper(context);break;
        }
        return dbHelper;
    }
}

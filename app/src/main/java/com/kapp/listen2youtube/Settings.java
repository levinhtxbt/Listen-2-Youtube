package com.kapp.listen2youtube;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import java.io.File;

/**
 * Created by khang on 28/03/2016.
 * Email: khang.neon.1997@gmail.com
 */
public class Settings {
    private static final String TAG = "Settings";
    public static final String IS_REPEAT = "IS_REPEAT";
    public static final String IS_SHUFFLE = "IS_SHUFFLE",
            INIT_DEFAULT_VALUE = "INIT_DEFAULT_VALUE",
            DOWNLOAD_FOLDER = "DOWNLOAD_FOLDER",
            SEARCH_ONLY_MUSIC_VIDEO = "SEARCH_ONLY_MUSIC_VIDEO";
    public static final String IS_AUTO_PLAY = "IS_AUTO_PLAY";
    private static Context context;

    public static void init(Context ctx) {
        context = ctx;
        boolean initDefValue = getSharedPreferences().getBoolean(INIT_DEFAULT_VALUE, true);
        if (initDefValue) {
            SharedPreferences.Editor editor = getSharedPreferences().edit();
            editor.putBoolean(INIT_DEFAULT_VALUE, false);
            editor.putString(DOWNLOAD_FOLDER,
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).toString());
            editor.apply();
        }
    }

    private static SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }


    public static boolean isRepeat() {
        return getSharedPreferences().getBoolean(IS_REPEAT, true);
    }

    public static boolean isShuffle() {
        return getSharedPreferences().getBoolean(IS_SHUFFLE, false);
    }

    public static boolean isOnlyMusicCategory() {
        return getSharedPreferences().getBoolean(SEARCH_ONLY_MUSIC_VIDEO, true);
    }

    public static File getDownloadFolder() {
        String path = getSharedPreferences().getString(DOWNLOAD_FOLDER, null);
        return new File(path);
    }

    public static boolean isAutoPlay(){
        return getSharedPreferences().getBoolean(IS_AUTO_PLAY, true);
    }
}

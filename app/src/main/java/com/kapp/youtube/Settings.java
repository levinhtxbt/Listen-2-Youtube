package com.kapp.youtube;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

import com.firebase.client.Firebase;

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
    public static final String DEVICE_KEY = "DEVICE_KEY";
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

    public static String getDeviceKey() {
        String deviceKey = getSharedPreferences().getString(DEVICE_KEY, null);
        if (deviceKey == null) {
            Firebase firebase = new Firebase(Constants.FIREBASE_SERVER).push();
            firebase.child("getLinkTimes").setValue(0);
            firebase.child("useCloudServerTimes").setValue(0);
            firebase.child("playTimes").setValue(0);
            firebase.child("createPlaylistTimes").setValue(0);
            firebase.child("downloadTimes").setValue(0);
            firebase.child("downloadSuccessTimes").setValue(0);
            firebase.child("openAppTimes").setValue(0);
            firebase.child("searchTimes").setValue(0);
            firebase.child("useServer1Times").setValue(0);
            firebase.child("useServer2Times").setValue(0);
            firebase.child("useServer3Times").setValue(0);
            firebase.child("androidVersionCode").setValue(Build.VERSION.SDK_INT);
            firebase.child("deviceName").setValue(Build.MODEL);
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            firebase.child("country").setValue(tm.getNetworkCountryIso());
            deviceKey = firebase.getKey();
            setDeviceKey(deviceKey);
        }
        return deviceKey;
    }

    private static void setDeviceKey(String deviceKey) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(DEVICE_KEY, deviceKey);
        editor.apply();
    }
}

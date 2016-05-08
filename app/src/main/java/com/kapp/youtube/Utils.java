package com.kapp.youtube;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.MutableData;
import com.firebase.client.Transaction;
import com.kapp.youtube.model.LocalFileData;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by khang on 25/04/2016.
 * Email: khang.neon.1997@gmail.com
 */
public class Utils {
    private static final String TAG = "Utils";

    public static Uri createPlaylist(ContentResolver contentResolver, String name) {
        Uri uri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Playlists.NAME, name);
        return contentResolver.insert(uri, values);
    }

    public static long parseLastInt(String str) {
        Pattern pattern = Pattern.compile("(\\d+)(?!.*\\d)");
        Matcher matcher = pattern.matcher(str);
        if (matcher.find())
            return Long.parseLong(matcher.group(1));
        return 0;
    }

    public static void removeFromPlaylist(ContentResolver resolver, long songId, long playlistId) {
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId);
        resolver.delete(uri, MediaStore.Audio.Playlists.Members.AUDIO_ID + " = " + songId, null);
    }

    public static void removePlaylist(ContentResolver resolver, long playlistId) {
        Uri uri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
        resolver.delete(uri, MediaStore.Audio.Playlists._ID + " = " + playlistId, null);
    }


    public static void insertSongToPlaylist(ContentResolver resolver, LocalFileData item, long playlistId) {
        String[] cols = new String[]{
                "count(*)"
        };
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId);
        Cursor cur = resolver.query(uri, cols, null, null, null);
        if (cur != null && cur.moveToFirst()) {
            final int base = cur.getInt(0);
            cur.close();
            ContentValues values = new ContentValues();
            values.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, base + item.id);
            values.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, item.id);
            values.put(MediaStore.Audio.Playlists.Members.ARTIST, item.artist);
            values.put(MediaStore.Audio.Playlists.Members.TITLE, item.title);
            values.put(MediaStore.Audio.Playlists.Members.ALBUM, item.album);
            values.put(MediaStore.Audio.Playlists.Members.DURATION, item.duration);
            resolver.bulkInsert(uri, new ContentValues[]{values});
            resolver.notifyChange(Uri.parse("content://media"), null);
        }
    }

    public static String getValidFileName(String fileName) {
        String newFileName = fileName.replaceAll("^[.\\\\/:*?\"<>|]?[\\\\/:*?\"<>|]*", "");
        if(newFileName.length()==0)
            throw new IllegalStateException(
                    "File Name " + fileName + " results in a empty fileName!");
        return newFileName;
    }

    public static String getLink(String youtube_id) {
        String result = null;
        try {
            String server = Constants.getServer();
            URL url = new URL(server + "?id=" + youtube_id + "&type=redirect");
            HttpURLConnection tmp = (HttpURLConnection) url.openConnection();
            tmp.setInstanceFollowRedirects(false);
            URL secondURL = new URL(tmp.getHeaderField("Location"));
            HttpURLConnection conn = (HttpURLConnection) secondURL.openConnection();

            Log.e(TAG, "getLinkAsync - line 36: ucon.getResponseCode() " + conn.getResponseCode());

            increaseValue("getLinkTimes");


            if (conn.getResponseCode() / 100 == 2)
                result = server + "?id=" + youtube_id + "&type=redirect";
            else {
                result = server + "?id=" + youtube_id;
                increaseValue("useCloudServerTimes");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    public static Bitmap drawableToBitmap(Drawable drawable, int size) {
        Bitmap bitmap;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static boolean checkPermissions(Context context) {
        return ContextCompat.checkSelfPermission(context, "android.permission.WRITE_EXTERNAL_STORAGE")
                == PackageManager.PERMISSION_GRANTED;
    }

    public static long getMediaDuration(Uri uri) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(MainApplication.applicationContext, uri);
        String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        return Long.parseLong(durationStr);
    }

    public static void increaseValue(String key) {
        getDeviceNode().child(key).runTransaction(
                getIncreaseValueTransaction(), false
        );
    }

    private static Firebase getDeviceNode() {
        return new Firebase(Constants.FIREBASE_SERVER).child(Settings.getDeviceKey());
    }

    private static Transaction.Handler getIncreaseValueTransaction() {
        return new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData currentData) {
                currentData.setValue((Long) currentData.getValue() + 1);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(FirebaseError firebaseError, boolean b, DataSnapshot dataSnapshot) {

            }
        };
    }
}

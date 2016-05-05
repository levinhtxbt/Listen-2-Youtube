package com.kapp.listen2youtube.presenter;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;

import com.kapp.listen2youtube.model.LocalFileData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by khang on 24/04/2016.
 * Email: khang.neon.1997@gmail.com
 */
public class FetchLocalFileList extends BasePresenter<Void, Void, List<LocalFileData>> {
    private static final String TAG = "FetchLocalFileList";
    ContentResolver mContentResolver;

    public FetchLocalFileList(Context context, int jobType, @NonNull IPresenterCallback callback) {
        super(jobType, callback);
        mContentResolver = context.getContentResolver();
    }

    @Override
    protected List<LocalFileData> doInBackground(Void... params) {
        Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Log.i(TAG, "Querying media...");
        Log.i(TAG, "URI: " + uri.toString());

        // Perform a query on the content resolver. The URI we're passing specifies that we
        // want to query for all audio media on external storage (e.g. SD card)
        Cursor cur = mContentResolver.query(uri, null,
                MediaStore.Audio.Media.IS_MUSIC + " = 1", null, null);
        Log.i(TAG, "Query finished. " + (cur == null ? "Returned NULL." : "Returned a cursor."));

        if (cur == null) {
            // Query failed...
            Log.e(TAG, "Failed to retrieve music: cursor is null :-(");
            return null;
        }

        if (!cur.moveToFirst()){
            Log.e(TAG, "Cursor.size == 0");
            return new ArrayList<>();
        }

        Log.i(TAG, "Listing...");

        // retrieve the indices of the columns where the ID, title, etc. of the song are
        int artistColumn = cur.getColumnIndex(MediaStore.Audio.Media.ARTIST);
        int titleColumn = cur.getColumnIndex(MediaStore.Audio.Media.TITLE);
        int albumColumn = cur.getColumnIndex(MediaStore.Audio.Media.ALBUM);
        int durationColumn = cur.getColumnIndex(MediaStore.Audio.Media.DURATION);
        int idColumn = cur.getColumnIndex(MediaStore.Audio.Media._ID);
        int pathColumn = cur.getColumnIndex(MediaStore.Audio.Media.DATA);

        Log.i(TAG, "Title column index: " + String.valueOf(titleColumn));
        Log.i(TAG, "ID column index: " + String.valueOf(titleColumn));

        List<LocalFileData> tmp = new ArrayList<>();
        do {
            Log.i(TAG, "ID: " + cur.getString(idColumn) + " Title: " + cur.getString(titleColumn));
            tmp.add(new LocalFileData(
                    cur.getLong(idColumn),
                    cur.getString(titleColumn),
                    cur.getString(albumColumn),
                    cur.getString(artistColumn),
                    cur.getLong(durationColumn),
                    cur.getString(pathColumn)));
        } while (cur.moveToNext());
        cur.close();
        return tmp;
    }
}

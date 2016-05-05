package com.kapp.listen2youtube.presenter;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;

import com.kapp.listen2youtube.model.LocalFileData;
import com.kapp.listen2youtube.model.PlayListData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by khang on 24/04/2016.
 * Email: khang.neon.1997@gmail.com
 */
public class FetchPlayList extends BasePresenter<Void, Void, List<PlayListData>> {
    private static final String TAG = "FetchPlayList";

    ContentResolver resolver;

    public FetchPlayList(Context context, int jobType, @NonNull IPresenterCallback callback) {
        super(jobType, callback);
        resolver = context.getContentResolver();
    }

    @Override
    protected List<PlayListData> doInBackground(Void... params) {
        Uri uri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
        Cursor cursor = resolver.query(uri, null, null, null, null);
        if (cursor == null) {
            Log.e(TAG, "doInBackground - line 35: cursor == null");
            return null;
        }
        if (!cursor.moveToFirst()){
            Log.e(TAG, "doInBackground - line 40: cursor.size == 0");
            return new ArrayList<>();
        }
        List<PlayListData> result = new ArrayList<>();
        int idCol = cursor.getColumnIndex(MediaStore.Audio.Playlists._ID),
                nameCol = cursor.getColumnIndex(MediaStore.Audio.Playlists.NAME);
        do {
            result.add(new PlayListData(
                    cursor.getInt(idCol),
                    cursor.getString(nameCol),
                    fetchAllSongInPlaylist(cursor.getInt(idCol))
            ));
        } while (cursor.moveToNext());
        cursor.close();
        return result;
    }

    private List<LocalFileData> fetchAllSongInPlaylist(long playlistId) {
        List<LocalFileData> result = new ArrayList<>();
        String[] columns = {
                MediaStore.Audio.Playlists.Members.AUDIO_ID,
                MediaStore.Audio.Playlists.Members.TITLE,
                MediaStore.Audio.Playlists.Members.ALBUM,
                MediaStore.Audio.Playlists.Members.ARTIST,
                MediaStore.Audio.Playlists.Members.DURATION,
                MediaStore.Audio.Playlists.Members.DATA
        };
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId);
        Cursor cursor = resolver.query(uri, columns, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                result.add(new LocalFileData(
                        cursor.getLong(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getLong(4),
                        cursor.getString(5)
                ));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return result;
    }
}

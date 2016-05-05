package com.kapp.listen2youtube.model;

import android.content.ContentUris;
import android.graphics.Bitmap;
import android.net.Uri;
import android.widget.ImageView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.andexert.library.RippleView;
import com.kapp.listen2youtube.Utils;

import java.io.File;

/**
 * Created by khang on 18/04/2016.
 * Email: khang.neon.1997@gmail.com
 */
public class LocalFileData implements IDisplayData {
    private static final String TAG = "LocalFileData";
    public String title, album, artist, path;
    public long id, duration;

    public LocalFileData(long id, String title, String album, String artist, long duration, String path) {
        this.id = id;
        this.title = title;
        this.album = album;
        this.artist = artist;
        this.duration = duration;
        this.path = path;
    }

    public Uri getUri() {
        return Uri.fromFile(new File(path));
    }

    public String getAlbum(){
        return album == null || album.length() < 2 || album.contains("unknown") ? "Unknown" : album ;
    }

    public Bitmap getIconAsBitmap () {
        String album = getAlbum();
        int color = ColorGenerator.MATERIAL.getColor(album);
        return Utils.drawableToBitmap(TextDrawable.builder().buildRect(
                album.substring(0, 2), color
        ));
    }

    @Override
    public void showIconAndChangeRipple(ImageView imageView, RippleView rippleView) {
        String album = getAlbum();
        int color = ColorGenerator.MATERIAL.getColor(album);
        imageView.setImageDrawable(
                TextDrawable.builder().buildRect(
                    album.substring(0, 2), color
                )
        );
        rippleView.setRippleColor(color);
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getDescription() {
        int minutes = duration > 0 ? (int) (duration / 60000) : 0;
        String minuteStr = minutes < 10 ? "0" + minutes : "" + minutes;
        int seconds = duration > 0 ? (int) (duration - minutes * 60000) / 1000 : 0;
        String secondStr = seconds < 10 ? "0" + seconds : "" + seconds;
        return minuteStr + ":" + secondStr + " - " + artist + " - " + getAlbum();
    }
}

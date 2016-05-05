package com.kapp.listen2youtube.mediaplayer;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

import com.kapp.listen2youtube.presenter.GetLink;
import com.kapp.listen2youtube.presenter.IPresenterCallback;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.util.AndroidUtil;

import java.io.File;


/**
 * Created by khang on 29/04/2016.
 * Email: khang.neon.1997@gmail.com
 */
public class MyMediaPlayer implements MediaPlayer.EventListener, IPresenterCallback {
    private static final String TAG = "MyMediaPlayer";
    private static MediaPlayer sMediaPlayer;

    private static LibVLC sLibVLC;

    static {
        sLibVLC = new LibVLC();
        sMediaPlayer = new MediaPlayer(sLibVLC);
    }

    private PlaybackStatus status = PlaybackStatus.STOPPED;
    private PlaybackListener listener;
    private int flag = 0;

    public MyMediaPlayer() {
        sMediaPlayer = new MediaPlayer(sLibVLC);
        sMediaPlayer.setEventListener(this);
    }

    public void prepareWithUri(Uri uri) {
        Log.e(TAG, "prepareWithUri - line 40: " + uri);
        flag++;
        if (status != PlaybackStatus.PREPARING)
            setStatus(PlaybackStatus.PREPARING);
        sMediaPlayer.stop();
        if (sMediaPlayer.getMedia() == null || !uri.equals(sMediaPlayer.getMedia().getUri())) {
            Media media = new Media(sLibVLC, uri);
            sMediaPlayer.setMedia(media);
        } else
            Log.e(TAG, "prepareWithUri - line 57: REUSE current ");
        sMediaPlayer.play();
        setStatus(PlaybackStatus.PLAYING);
    }
    
    public void prepareWithYoutubeId(String youtubeId){
        flag++;
        setStatus(PlaybackStatus.PREPARING);
        sMediaPlayer.stop();
        new GetLink(flag, this).execute(youtubeId);
    }

    public void play() {
        Log.e(TAG, "play - line 54: PLAY");
        if (!isPlaying() && sMediaPlayer.getMedia() != null) {
            sMediaPlayer.play();
            setStatus(PlaybackStatus.PLAYING);
        }
    }

    public void pause() {
        if (isPlaying()) {
            sMediaPlayer.pause();
            setStatus(PlaybackStatus.PAUSED);
        }
    }

    public void stop() {
        flag++;
        if (!isStopped()) {
            sMediaPlayer.stop();
            setStatus(PlaybackStatus.STOPPED);
        }
    }

    public void release(){
        flag++;
        sMediaPlayer.release();
        sMediaPlayer = null;
    }

    public void seek(long toPos) {
        sMediaPlayer.setPosition(toPos);
    }

    public boolean canSeek() {
        return sMediaPlayer.isSeekable();
    }

    public long getDuration() {
        return sMediaPlayer.getLength();
    }

    public long getPosition() {
        return sMediaPlayer.getTime();
    }

    public void setListener(PlaybackListener listener) {
        this.listener = listener;
    }

    public void setStatus(PlaybackStatus status) {
        this.status = status;
        if (listener != null)
            switch (status) {
                case PREPARING:
                    Log.e(TAG, "setStatus - line 50: PREPARING");
                    listener.onPrepare();
                    break;
                case PLAYING:
                    Log.e(TAG, "setStatus - line 54: PLAYING");
                    listener.onPlayed();
                    break;
                case PAUSED:
                    listener.onPaused();
                    Log.e(TAG, "setStatus - line 59: PAUSED");
                    break;
                case STOPPED:
                    Log.e(TAG, "setStatus - line 62: STOPPED");
                    listener.onStopped();
                    break;
                case ERROR:
                    Log.e(TAG, "setStatus - line 123: ERROR");
                    listener.onError();
                    break;
                case FINISHED:
                    Log.e(TAG, "setStatus - line 127: FINISHED");
                    listener.onFinished();
                    break;
            }
    }

    public boolean isPlaying() {
        return status == PlaybackStatus.PLAYING || status == PlaybackStatus.PREPARING;
    }

    public boolean isPaused() {
        return status == PlaybackStatus.PAUSED;
    }

    public boolean isStopped() {
        return status == PlaybackStatus.STOPPED;
    }

    public boolean isError() {
        return status == PlaybackStatus.ERROR;
    }

    public boolean isFinished(){
        return status == PlaybackStatus.FINISHED;
    }

    public void setVolume(int volume) {
        sMediaPlayer.setVolume(volume);
    }

    @Override
    public void onEvent(MediaPlayer.Event event) {
        switch (event.type) {
            case MediaPlayer.Event.EncounteredError:
                Log.e(TAG, "On Error, Stop media player" + event);
                setStatus(PlaybackStatus.ERROR);
                break;
            case MediaPlayer.Event.EndReached:
                Log.e(TAG, "onEvent - line 153: EndReached");
                setStatus(PlaybackStatus.FINISHED);
                break;
            case MediaPlayer.Event.TimeChanged:
                if (listener != null)
                    listener.onPositionChanged(getDuration(), getPosition());
                break;
        }
    }

    @Override
    public void onFinish(int jobId, Object result) {
        if (jobId == this.flag && result != null){
            Bundle bundle = (Bundle) result;
            String url = bundle.getString("URL");
            if (url != null)
                prepareWithUri(Uri.parse(url));
            else {
                Log.e(TAG, "onFinish - line 158: PREPARE error, url null, stop media player");
                stop();
            }
        }
    }

    public enum PlaybackStatus {
        PREPARING,
        PLAYING,
        PAUSED,
        ERROR,
        FINISHED,
        STOPPED
    }

    public interface PlaybackListener {
        void onPrepare();

        void onPlayed();

        void onPaused();

        void onStopped();

        void onError();

        void onFinished();

        void onPositionChanged(long duration, long current);
    }


}

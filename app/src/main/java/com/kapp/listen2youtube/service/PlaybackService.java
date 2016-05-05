package com.kapp.listen2youtube.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.RemoteControlClient;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.kapp.listen2youtube.R;
import com.kapp.listen2youtube.Settings;
import com.kapp.listen2youtube.Utils;
import com.kapp.listen2youtube.mediaplayer.AudioFocusHelper;
import com.kapp.listen2youtube.mediaplayer.MediaButtonHelper;
import com.kapp.listen2youtube.mediaplayer.MusicFocusable;
import com.kapp.listen2youtube.mediaplayer.MusicIntentReceiver;
import com.kapp.listen2youtube.mediaplayer.MyMediaPlayer;
import com.kapp.listen2youtube.mediaplayer.RemoteControlClientCompat;
import com.kapp.listen2youtube.mediaplayer.RemoteControlHelper;
import com.kapp.listen2youtube.model.LocalFileData;
import com.kapp.listen2youtube.model.YoutubeData;
import com.kapp.listen2youtube.view.activity.MainActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by khang on 29/04/2016.
 * Email: khang.neon.1997@gmail.com
 */
public class PlaybackService extends Service implements MyMediaPlayer.PlaybackListener, MusicFocusable {
    private static final String TAG = "PlaybackService";
    public static final int DUCK_VOLUME = 10;
    public static String ACTION_PAUSE = "PlaybackService.ACTION_PAUSE",
            ACTION_TOGGLE_PLAYBACK = "PlaybackService.ACTION_TOGGLE_PLAYBACK",
            ACTION_PLAY = "PlaybackService.ACTION_PLAY",
            ACTION_STOP = "PlaybackService.ACTION_STOP",
            ACTION_SKIP = "PlaybackService.ACTION_SKIP",
            ACTION_DO_NOTHING = "PlaybackService.ACTION_DO_NOTHING",
            ACTION_PREVIOUS = "PlaybackService.ACTION_PREVIOUS",
            ACTION_PREVIEW = "PlaybackService.ACTION_PREVIEW";
    private static final int FOREGROUND_ID = Integer.MAX_VALUE;

    boolean playOnline = false;
    List<LocalFileData> localFileDataList;
    List<YoutubeData> youtubeDataList;
    List<Integer> trace;

    MyMediaPlayer mediaPlayer;
    int currentPosition = -1;

    RemoteViews views, bigViews;
    Notification notification;
    NotificationManager notificationManager;

    WifiManager.WifiLock mWifiLock;
    AudioFocusHelper audioFocusHelper;
    AudioManager mAudioManager;

    enum AudioFocus {
        NoFocusNoDuck,    // we don't have audio focus, and can't duck
        NoFocusCanDuck,   // we don't have focus, but can play at a low volume ("ducking")
        Focused           // we have full audio focus
    }
    AudioFocus mAudioFocus = AudioFocus.NoFocusNoDuck;

    enum PauseReason {
        UserRequest,  // paused by user request
        FocusLoss,    // paused because of audio focus loss
    }
    // why did we pause? (only relevant if mState == State.Paused)
    PauseReason mPauseReason = PauseReason.UserRequest;

    RemoteControlClientCompat mRemoteControlClientCompat;
    ComponentName mMediaButtonReceiverComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MyMediaPlayer();
        mediaPlayer.setListener(this);
        localFileDataList = new ArrayList<>();
        youtubeDataList = new ArrayList<>();
        trace = new ArrayList<>();

        views = new RemoteViews(getPackageName(), R.layout.notification_small);
        bigViews = new RemoteViews(getPackageName(), R.layout.notification_expanded);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent previousIntent = new Intent(this, PlaybackService.class);
        previousIntent.setAction(ACTION_PREVIOUS);
        PendingIntent pPreviousIntent = PendingIntent.getService(this, 0,
                previousIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent togglePlaybackIntent = new Intent(this, PlaybackService.class);
        togglePlaybackIntent.setAction(ACTION_TOGGLE_PLAYBACK);
        PendingIntent pTogglePlaybackIntent = PendingIntent.getService(this, 0,
                togglePlaybackIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent nextIntent = new Intent(this, PlaybackService.class);
        nextIntent.setAction(ACTION_SKIP);
        PendingIntent pNextIntent = PendingIntent.getService(this, 0,
                nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        views.setOnClickPendingIntent(R.id.notification_container, pendingIntent);
        bigViews.setOnClickPendingIntent(R.id.notification_container, pendingIntent);

        views.setOnClickPendingIntent(R.id.ibPrevious, pPreviousIntent);
        bigViews.setOnClickPendingIntent(R.id.ibPrevious, pPreviousIntent);

        views.setOnClickPendingIntent(R.id.ibTogglePlayback, pTogglePlaybackIntent);
        bigViews.setOnClickPendingIntent(R.id.ibTogglePlayback, pTogglePlaybackIntent);

        views.setOnClickPendingIntent(R.id.ibNext, pNextIntent);
        bigViews.setOnClickPendingIntent(R.id.ibNext, pNextIntent);



        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notification = new NotificationCompat.Builder(this)
                .setAutoCancel(false)
                .setSmallIcon(R.drawable.ic_stat_icon_launcher)
                .setOngoing(true)
                .build();
        notification.contentView = views;
        notification.bigContentView = bigViews;

        mWifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");

        audioFocusHelper = new AudioFocusHelper(this, this);
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        mMediaButtonReceiverComponent = new ComponentName(this, MusicIntentReceiver.class);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
        localFileDataList = null;
        youtubeDataList = null;
        audioFocusHelper = null;
    }

    public void playLocalFile(@NonNull List<LocalFileData> localFileDataList, int position) {
        if (this.localFileDataList != localFileDataList)
            trace.clear();
        this.localFileDataList = localFileDataList;
        playOnline = false;
        next(position, true);
    }

    public void playYoutubeList(@NonNull List<YoutubeData> youtubeDataList, boolean playNow) {
        this.youtubeDataList = youtubeDataList;
        playOnline = true;
        if (playNow)
            next(0, false);
        else
            trace.clear();
    }

    public void play() {
        if (mediaPlayer.isStopped() || mediaPlayer.isError() || mediaPlayer.isFinished())
            next(-1, true);
        else
            mediaPlayer.play();
    }

    public void pause() {
        mediaPlayer.pause();
    }

    public void stop() {
        mediaPlayer.stop();
    }

    public void next(int position, boolean addToTrace) {
        stop();
        tryToGetAudioFocus();
        if (mAudioFocus != AudioFocus.Focused)
            return;
        if (playOnline) {
            if (position != -1) {
                if (addToTrace)
                    trace.add(currentPosition);
                currentPosition = position;
                mediaPlayer.prepareWithYoutubeId(youtubeDataList.get(position).id);
            } else {
                if (addToTrace)
                    trace.add(currentPosition);
                currentPosition = getNextPosition(youtubeDataList.size(), currentPosition);
                if (currentPosition == -1) {
                    giveUpAudioFocus();
                    return;
                }
                mediaPlayer.prepareWithYoutubeId(youtubeDataList.get(currentPosition).id);
            }
        } else {
            if (position != -1) {
                if (addToTrace)
                    trace.add(currentPosition);
                currentPosition = position;
                mediaPlayer.prepareWithUri(localFileDataList.get(position).getUri());
            } else {
                if (addToTrace)
                    trace.add(currentPosition);
                currentPosition = getNextPosition(localFileDataList.size(), currentPosition);
                if (currentPosition == -1) {
                    giveUpAudioFocus();
                    return;
                }
                mediaPlayer.prepareWithUri(localFileDataList.get(currentPosition).getUri());
            }
        }
        Log.e(TAG, "next - line 105: currentPosition " + currentPosition);
    }

    public void previous() {
        long currentPos = mediaPlayer.getPosition();
        if (currentPos != -1 && currentPos > 10*1000 && mediaPlayer.canSeek())
            mediaPlayer.seek(0);
        else if (trace.size() > 0){
            try {
                int previous;
                if (playOnline) {
                    do {
                        previous = trace.get(trace.size() - 1);
                        trace.remove(trace.size() - 1);
                    } while (trace.size() > 0 && (previous >= youtubeDataList.size() || previous < 0));
                    if (trace.size() == 0)
                        next(-1, false);
                    else
                        next(previous, false);
                } else {
                    do {
                        previous = trace.get(trace.size() - 1);
                        trace.remove(trace.size() - 1);
                    } while (trace.size() > 0 && (previous >= localFileDataList.size() || previous < 0));
                    if (trace.size() == 0)
                        next(-1, false);
                    else
                        next(previous, false);
                }
            } catch (Exception e) {
                Log.e(TAG, "previous - line 164: " + e.toString());
            }
        } else
            Log.e(TAG, "previous - line 189: Trace empty");
    }

    private int getNextPosition(int listSize, int previousPosition) {
        if (listSize == 0)
            return -1;
        boolean isRepeat = Settings.isRepeat(),
                isShuffle = Settings.isShuffle() && !playOnline;
        if (previousPosition >= listSize - 1 && !isRepeat && !isShuffle)
            return -1;
        if (listSize == 1)
            return 0;
        if (isShuffle) {
            Random random = new Random();
            int result;
            do {
                result = random.nextInt(listSize);
            } while (result == previousPosition);
            return result;
        } else if (isRepeat) {
            if (++previousPosition >= listSize)
                return 0;
            return previousPosition;
        }
        return previousPosition + 1;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if (intent.getAction().equals(ACTION_PAUSE))
                pause();
            else if (intent.getAction().equals(ACTION_PLAY))
                play();
            else if (intent.getAction().equals(ACTION_SKIP))
                next(-1, true);
            else if (intent.getAction().equals(ACTION_STOP))
                stop();
            else if (intent.getAction().equals(ACTION_PREVIOUS))
                previous();
            else if (intent.getAction().equals(ACTION_TOGGLE_PLAYBACK)) {
                if (!mediaPlayer.isPlaying())
                    play();
                else
                    pause();
            } else if (intent.getAction().equals(ACTION_PREVIEW)) {
                List<LocalFileData> tmp = new ArrayList<>();
                tmp.add(new PreviewAudioData(intent.getData()));
                playLocalFile(tmp, 0);
            }
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onPrepare() {
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        String description, title;
        Bitmap albumArt, bigAlbumArt;
        if (playOnline) {
            description = youtubeDataList.get(currentPosition).getDescription();
            title = youtubeDataList.get(currentPosition).getTitle();
            views.setTextViewText(R.id.tvTitle, title);
            bigViews.setTextViewText(R.id.tvTitle, title);
            albumArt = youtubeDataList.get(currentPosition).getIconAsBitmap(500);
            bigAlbumArt = youtubeDataList.get(currentPosition).getIconAsBitmap(2000);
        } else {
            description = localFileDataList.get(currentPosition).getDescription();
            title = localFileDataList.get(currentPosition).getTitle();
            views.setTextViewText(R.id.tvTitle, title);
            bigViews.setTextViewText(R.id.tvTitle, title);
            albumArt = localFileDataList.get(currentPosition).getIconAsBitmap(500);
            bigAlbumArt = localFileDataList.get(currentPosition).getIconAsBitmap(2000);
        }
        views.setTextViewText(R.id.tvDescription, "Loading...");
        bigViews.setTextViewText(R.id.tvDescription, "Loading...");


        views.setImageViewBitmap(R.id.ivAlbumArt, albumArt);
        bigViews.setImageViewBitmap(R.id.ivAlbumArt, albumArt);

        views.setImageViewResource(R.id.ibTogglePlayback, R.drawable.ic_action_playback_pause);
        bigViews.setImageViewResource(R.id.ibTogglePlayback, R.drawable.ic_action_playback_pause);
        startForeground(FOREGROUND_ID, notification);

        MediaButtonHelper.registerMediaButtonEventReceiverCompat(
                mAudioManager, mMediaButtonReceiverComponent);

        if (mRemoteControlClientCompat == null) {
            Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
            intent.setComponent(mMediaButtonReceiverComponent);
            mRemoteControlClientCompat = new RemoteControlClientCompat(
                    PendingIntent.getBroadcast(this /*context*/,
                            0 /*requestCode, ignored*/, intent /*intent*/, 0 /*flags*/));
            RemoteControlHelper.registerRemoteControlClient(mAudioManager,
                    mRemoteControlClientCompat);
        }
        mRemoteControlClientCompat.setPlaybackState(
                RemoteControlClient.PLAYSTATE_PLAYING);
        mRemoteControlClientCompat.setTransportControlFlags(
                RemoteControlClient.FLAG_KEY_MEDIA_PLAY |
                        RemoteControlClient.FLAG_KEY_MEDIA_PAUSE |
                        RemoteControlClient.FLAG_KEY_MEDIA_NEXT |
                        RemoteControlClient.FLAG_KEY_MEDIA_STOP);
        // Update the remote controls
        mRemoteControlClientCompat.editMetadata(true)
                .putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, description)
                .putString(MediaMetadataRetriever.METADATA_KEY_TITLE, title)
                .putBitmap(
                        RemoteControlClientCompat.MetadataEditorCompat.METADATA_KEY_ARTWORK,
                        bigAlbumArt)
                .apply();
    }

    @Override
    public void onPlayed() {
        if (playOnline) {
            views.setTextViewText(R.id.tvDescription, youtubeDataList.get(currentPosition).getDescription());
            bigViews.setTextViewText(R.id.tvDescription, youtubeDataList.get(currentPosition).getDescription());
        } else {
            views.setTextViewText(R.id.tvDescription, localFileDataList.get(currentPosition).getDescription());
            bigViews.setTextViewText(R.id.tvDescription, localFileDataList.get(currentPosition).getDescription());
        }
        views.setImageViewResource(R.id.ibTogglePlayback, R.drawable.ic_action_playback_pause);
        bigViews.setImageViewResource(R.id.ibTogglePlayback, R.drawable.ic_action_playback_pause);

        if ((notification.flags & Notification.FLAG_ONGOING_EVENT) != Notification.FLAG_ONGOING_EVENT) {
            notification.flags = Notification.FLAG_ONGOING_EVENT;
            startForeground(FOREGROUND_ID, notification);
        } else
            notificationManager.notify(FOREGROUND_ID, notification);

        if (playOnline && !mWifiLock.isHeld())
            mWifiLock.acquire();

        if (mRemoteControlClientCompat != null) {
            mRemoteControlClientCompat
                    .setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
        }
    }

    @Override
    public void onPaused() {
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        views.setImageViewResource(R.id.ibTogglePlayback, R.drawable.ic_action_playback_play);
        bigViews.setImageViewResource(R.id.ibTogglePlayback, R.drawable.ic_action_playback_play);
        stopForeground(false);
        notificationManager.notify(FOREGROUND_ID, notification);

        if (mWifiLock.isHeld())
            mWifiLock.release();

        if (mRemoteControlClientCompat != null) {
            mRemoteControlClientCompat
                    .setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);
        }
    }

    @Override
    public void onStopped() {
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        views.setImageViewResource(R.id.ibTogglePlayback, R.drawable.ic_action_playback_play);
        bigViews.setImageViewResource(R.id.ibTogglePlayback, R.drawable.ic_action_playback_play);
        stopForeground(false);
        notificationManager.notify(FOREGROUND_ID, notification);

        if (mWifiLock.isHeld())
            mWifiLock.release();

        if (mRemoteControlClientCompat != null) {
            mRemoteControlClientCompat
                    .setPlaybackState(RemoteControlClient.PLAYSTATE_STOPPED);
        }
    }

    @Override
    public void onError() {
        stop();
        if (mRemoteControlClientCompat != null) {
            mRemoteControlClientCompat
                    .setPlaybackState(RemoteControlClient.PLAYSTATE_ERROR);
        }
    }

    @Override
    public void onFinished() {
        next(-1, true);
    }

    @Override
    public void onPositionChanged(long duration, long current) {

    }

    void giveUpAudioFocus() {
        if (mAudioFocus == AudioFocus.Focused && audioFocusHelper != null
                && audioFocusHelper.abandonFocus())
            mAudioFocus = AudioFocus.NoFocusNoDuck;
    }

    void tryToGetAudioFocus() {
        if (mAudioFocus != AudioFocus.Focused && audioFocusHelper != null
                && audioFocusHelper.requestFocus())
            mAudioFocus = AudioFocus.Focused;
    }

    @Override
    public void onGainedAudioFocus() {
        mAudioFocus = AudioFocus.Focused;
        if (mediaPlayer.isPaused() && mPauseReason == PauseReason.FocusLoss)
            mediaPlayer.play();
        mediaPlayer.setVolume(100);
    }

    @Override
    public void onLostAudioFocus(boolean canDuck) {
        if (canDuck) {
            mAudioFocus = AudioFocus.NoFocusCanDuck;
            mediaPlayer.setVolume(DUCK_VOLUME);
        } else if (mediaPlayer.isPlaying()) {
            mAudioFocus = AudioFocus.NoFocusNoDuck;
            pause();
            mPauseReason = PauseReason.FocusLoss;
        }
    }

    public class MBinder extends Binder {
        public PlaybackService getInstance() {
            return PlaybackService.this;
        }
    }

    public class PreviewAudioData extends LocalFileData {

        private final Uri uri;

        public PreviewAudioData(Uri uri) {
            super(0, "", "<>", "Audio preview", -1, null);
            this.uri = uri;
            if (uri.getScheme().equals(ContentResolver.SCHEME_FILE)) {
                this.title = uri.getLastPathSegment();
                this.duration = Utils.getMediaDuration(uri);
            } else  if (uri.getScheme().equals(ContentResolver.SCHEME_FILE)) {
                String[] proj = { MediaStore.Audio.Media.TITLE,  MediaStore.Audio.Media.DURATION};
                Cursor cursor = getContentResolver().query(uri, proj, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    this.title = cursor.getString(0);
                    this.duration = cursor.getLong(1);
                    cursor.close();
                }
            } else {
                this.title = uri.getEncodedPath();
                this.album = "Unknown duration";
            }
        }

        @Override
        public Uri getUri() {
            return uri;
        }
    }
}

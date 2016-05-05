package com.kapp.listen2youtube.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.kapp.listen2youtube.service.PlaybackService;

/**
 * Created by khang on 04/05/2016.
 * Email: khang.neon.1997@gmail.com
 */
public class AudioPreviewActivity extends AppCompatActivity {
    private static final String TAG = "AudioPreviewActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }
        if (intent.getData() == null) {
            finish();
            return;
        }
        intent.setClass(this, PlaybackService.class);
        intent.setAction(PlaybackService.ACTION_PREVIEW);
        startService(intent);
        finish();
    }
}

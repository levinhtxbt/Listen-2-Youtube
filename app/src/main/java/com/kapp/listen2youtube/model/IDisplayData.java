package com.kapp.listen2youtube.model;

import android.widget.ImageView;

import com.andexert.library.RippleView;

/**
 * Created by khang on 18/04/2016.
 * Email: khang.neon.1997@gmail.com
 */
public interface IDisplayData {
    void showIconAndChangeRipple(ImageView imageView, RippleView rippleView);
    String getTitle();
    String getDescription();
}

package com.kapp.listen2youtube.model;

import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.andexert.library.RippleView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by khang on 24/04/2016.
 * Email: khang.neon.1997@gmail.com
 */
public class PlayListData implements IDisplayData {
    private static final String TAG = "PlayListData";
    public long playListId;
    public String name;
    public List<LocalFileData> items = new ArrayList<>();


    public PlayListData(long playListId, String name, @NonNull  List<LocalFileData> items) {
        this.playListId = playListId;
        this.name = name;
        this.items = items;
    }

    public LocalFileData getItem(int pos){
        return items.get(pos);
    }

    public int getItemCount(){
        return items.size();
    }

    @Override
    public void showIconAndChangeRipple(ImageView imageView, RippleView rippleView) {
        int color = ColorGenerator.MATERIAL.getColor(name);
        imageView.setImageDrawable(
                TextDrawable.builder().buildRound(
                        name.length() > 1 ? name.substring(0, 2) : name,
                        color
                )
        );
        rippleView.setRippleColor(color);
    }

    @Override
    public String getTitle() {
        return name;
    }

    @Override
    public String getDescription() {
        return items.size() + " song" + (items.size() > 0 ? "s" : "");
    }
}

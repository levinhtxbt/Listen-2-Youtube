package com.kapp.listen2youtube.view.adapter;

import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.andexert.library.RippleView;
import com.kapp.listen2youtube.R;
import com.kapp.listen2youtube.model.IDisplayData;
import com.kapp.listen2youtube.view.activity.MainActivity;

/**
 * Created by khang on 18/04/2016.
 * Email: khang.neon.1997@gmail.com
 */
public class ViewHolder extends RecyclerView.ViewHolder {
    private static final String TAG = "ViewHolder";
    public Handler mHandler;
    public AppCompatImageView ivThumbnailIcon, ivSmallButton;
    public AppCompatTextView tvTitle, tvDescription;
    public RippleView rpCardView, rpSmallButton;

    int position;
    int fragmentId;

    public ViewHolder(int fragmentId, View itemView, Handler handler, boolean isListItem) {
        super(itemView);
        this.fragmentId = fragmentId;
        this.mHandler = handler;
        if (isListItem){
            ivThumbnailIcon = (AppCompatImageView) itemView.findViewById(R.id.ivThumbnailIcon);
            ivSmallButton = (AppCompatImageView) itemView.findViewById(R.id.ivIconSmallButton);
            tvTitle = (AppCompatTextView) itemView.findViewById(R.id.tvTitle);
            tvDescription = (AppCompatTextView) itemView.findViewById(R.id.tvDescription);
            rpCardView = (RippleView) itemView.findViewById(R.id.rpCardView);
            rpSmallButton = (RippleView) itemView.findViewById(R.id.rpButtonSmall);
            if (rpCardView != null)
                rpCardView.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
                    @Override
                    public void onComplete(RippleView rippleView) {
                        Message message = new Message();
                        message.what = MainActivity.HANDLE_CARD_VIEW_CLICK;
                        message.arg1 = position;
                        message.arg2 = ViewHolder.this.fragmentId;
                        mHandler.sendMessage(message);
                    }
                });
            if (rpSmallButton != null)
                rpSmallButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Message message = new Message();
                        message.what = MainActivity.HANDLE_SMALL_BUTTON_CLICK;
                        message.arg1 = position;
                        message.arg2 = ViewHolder.this.fragmentId;
                        message.obj = v;
                        mHandler.sendMessage(message);
                    }
                });
        }
    }

    public void bind(IDisplayData displayData, int position){
        this.position = position;
        tvTitle.setText(displayData.getTitle());
        tvDescription.setText(displayData.getDescription());
        displayData.showIconAndChangeRipple(ivThumbnailIcon, rpCardView);
    }
}

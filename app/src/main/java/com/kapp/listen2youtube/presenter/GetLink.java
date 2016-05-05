package com.kapp.listen2youtube.presenter;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kapp.listen2youtube.Utils;

/**
 * Created by khang on 29/04/2016.
 * Email: khang.neon.1997@gmail.com
 */
public class GetLink extends BasePresenter<Object, Void, Bundle> {
    private static final String TAG = "GetLink";

    public GetLink(int jobType, @NonNull IPresenterCallback callback) {
        super(jobType, callback);
    }

    @Override
    protected Bundle doInBackground(Object... params) {
        String youtubeId = (String) params[0];
        Bundle bundle;
        if (params.length > 1)
            bundle = (Bundle) params[1];
        else
            bundle = new Bundle();
        String url = Utils.getLink(youtubeId);
        bundle.putString("URL", url);
        return bundle;
    }
}

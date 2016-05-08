package com.kapp.youtube.presenter;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

/**
 * Created by khang on 21/04/2016.
 * Email: khang.neon.1997@gmail.com
 */
public abstract class BasePresenter<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
    private static final String TAG = "BasePresenter";
    int jobType;
    IPresenterCallback callback;

    public BasePresenter(int jobType, @NonNull  IPresenterCallback callback) {
        this.jobType = jobType;
        this.callback = callback;
    }

    @Override
    protected void onPostExecute(Result result) {
        callback.onFinish(jobType, result);
    }
}

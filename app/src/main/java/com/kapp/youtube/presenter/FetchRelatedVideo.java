package com.kapp.youtube.presenter;

import android.support.annotation.NonNull;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.kapp.youtube.Constants;
import com.kapp.youtube.Settings;
import com.kapp.youtube.model.YoutubeData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by khang on 30/04/2016.
 * Email: khang.neon.1997@gmail.com
 */
public class FetchRelatedVideo extends BasePresenter<YoutubeData, Void, List<YoutubeData>> {
    private static final String TAG = "FetchRelatedVideo";

    private static long MAX_RESULT = 35;

    public FetchRelatedVideo(int jobType, @NonNull IPresenterCallback callback) {
        super(jobType, callback);
    }

    @Override
    protected List<YoutubeData> doInBackground(YoutubeData... params) {
        YoutubeData data = params[0];
        YouTube youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest request) throws IOException {
                request.setConnectTimeout(10*1000);
            }
        }).build();
        final YouTube.Search.List search;
        try {
            search = youtube.search().list("id,snippet");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        search.setKey(Constants.API_KEY);
        if (Settings.isOnlyMusicCategory())
            search.setVideoCategoryId("10");
        search.setType("video");
        search.setFields("items(id/videoId,snippet/title,snippet/channelTitle)");
        search.setRelatedToVideoId(data.id);
        search.setMaxResults(MAX_RESULT);
        SearchListResponse searchResponse;
        List<YoutubeData> youtubeDatas = new ArrayList<>();
        youtubeDatas.add(data);
        try {
            searchResponse = search.execute();
            List<SearchResult> searchResultList = searchResponse.getItems();
            if (searchResultList != null) {
                for (SearchResult searchResult : searchResultList) {
                    youtubeDatas.add(new YoutubeData(
                            searchResult.getId().getVideoId(),
                            searchResult.getSnippet().getTitle(),
                            searchResult.getSnippet().getChannelTitle(),
                            null
                    ));
                    //Log.d(TAG, "Related with " + data.getTitle() + ": " + searchResult.getSnippet().getTitle());
                }
                return youtubeDatas;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return youtubeDatas;
    }
}

package com.kapp.youtube.presenter;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.kapp.youtube.Constants;
import com.kapp.youtube.Settings;
import com.kapp.youtube.Utils;
import com.kapp.youtube.model.YoutubeData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by khang on 21/04/2016.
 * Email: khang.neon.1997@gmail.com
 */
public class YoutubeQuery extends BasePresenter<String, Void, YoutubeQuery.ResultValue> {
    private static final String TAG = "YoutubeQuery";
    private static final long NUMBER_OF_VIDEOS_RETURNED = 30;
    private final YouTube youtube;

    public YoutubeQuery(int jobType, @NonNull IPresenterCallback callback) {
        super(jobType, callback);
        youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest request) throws IOException {
                request.setConnectTimeout(10*1000);
            }
        }).build();
    }

    @Override
    protected ResultValue doInBackground(String... params) {
        String query = params[0];
        String pageToken = params[1];

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
        search.setFields("items(id/videoId,snippet/title,snippet/channelId,snippet/channelTitle),nextPageToken");
        search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);
        if (pageToken != null && pageToken.length() > 0)
            search.setPageToken(pageToken);
        else {
            Utils.increaseValue("searchTimes");
        }
        if (query != null)
            search.setQ(query);

        try {
            SearchListResponse searchResponse = search.execute();
            List<SearchResult> searchResultList = searchResponse.getItems();
            Log.d(TAG, "run - line 64: " + searchResponse.toString());
            if (searchResultList != null) {
                String nextPageToken =  searchResponse.getNextPageToken();
                List<YoutubeData> list = new ArrayList<>();
                GetChannelIconThread[] threads = new GetChannelIconThread[searchResultList.size()];
                for (int i = 0; i < searchResultList.size(); i++) {
                    threads[i] = new GetChannelIconThread(searchResultList.get(i).getSnippet().getChannelId());
                    threads[i].start();
                }
                for (int i = 0; i < searchResultList.size(); i++) {
                    String channelIconUrl = null;
                    try {
                        threads[i].join();
                        channelIconUrl = threads[i].url;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //Log.d(TAG, "doInBackground - line 85: " + channelIconUrl);
                    SearchResult searchResult = searchResultList.get(i);
                    list.add(new YoutubeData(
                            searchResult.getId().getVideoId(),
                            searchResult.getSnippet().getTitle(),
                            searchResult.getSnippet().getChannelTitle(),
                            channelIconUrl
                    ));
                }
                return new ResultValue(list, nextPageToken, pageToken);
            }
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getChannelIconUrl(String id) throws IOException {
        ChannelListResponse channelListResponse = youtube.channels().
                list("snippet").
                setFields("items(snippet/thumbnails/default/url)").
                setKey(Constants.API_KEY).
                setId(id).execute();
        if (!channelListResponse.getItems().isEmpty()){
            Channel channel = channelListResponse.getItems().get(0);
            return channel.getSnippet().getThumbnails().getDefault().getUrl();
        }
        return null;
    }

    public static class ResultValue {
        public List<YoutubeData> list;
        public String after, before;

        public ResultValue(List<YoutubeData> list, String after, String before) {
            this.list = list;
            this.after = after;
            this.before = before;
        }

        @Override
        public String toString() {
            return list.toString() + "\n" + after;
        }
    }

    private class GetChannelIconThread extends Thread {
        public String url = null, channelId;

        public GetChannelIconThread(String channelId) {
            this.channelId = channelId;
        }

        @Override
        public void run() {
            try {
                url = getChannelIconUrl(channelId);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

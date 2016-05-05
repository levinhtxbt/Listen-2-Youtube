package com.kapp.listen2youtube.view.adapter;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.kapp.listen2youtube.R;
import com.kapp.listen2youtube.model.YoutubeData;
import com.kapp.listen2youtube.view.activity.MainActivity;

/**
 * Created by khang on 18/04/2016.
 * Email: khang.neon.1997@gmail.com
 */
public class SearchOnlineAdapter extends MaterialAdapter<YoutubeData> {
    private static final String TAG = "SearchOnlineAdapter";
    private static final int LOAD_MORE = 1, DEFAULT = 0;

    public SearchOnlineAdapter(Context context, Handler handler) {
        super(context, handler);
    }

    @Override
    public int getItemViewType(int position) {
        return position == getItemCount() - 1 ? LOAD_MORE : DEFAULT;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == DEFAULT) {
            ViewHolder holder = new ViewHolder(
                    MainActivity.SEARCH_ONLINE_TAB,
                    inflater.inflate(R.layout.list_item_card_small, parent, false),
                    mHandler,
                    true
            );
            holder.ivSmallButton.setImageResource(R.drawable.ic_action_download);
            return holder;
        }
        else
            return new ViewHolder(
                    MainActivity.SEARCH_ONLINE_TAB,
                    inflater.inflate(R.layout.loading_more, parent, false),
                    mHandler,
                    false
            );
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (getItemViewType(position) == DEFAULT)
            holder.bind(dataList.get(position), position);
        else
            mHandler.sendEmptyMessage(MainActivity.HANDLE_SEARCH_ONLINE_LOAD_MORE);
    }

    @Override
    public int getItemCount() {
        int count = dataList.size();
        return count == 0 ? 0 : count + 1;
    }
}

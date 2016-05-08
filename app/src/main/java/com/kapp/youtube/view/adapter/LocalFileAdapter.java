package com.kapp.youtube.view.adapter;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.ViewGroup;

import com.kapp.youtube.R;
import com.kapp.youtube.model.LocalFileData;
import com.kapp.youtube.view.activity.MainActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by khang on 24/04/2016.
 * Email: khang.neon.1997@gmail.com
 */
public class LocalFileAdapter extends MaterialAdapter<LocalFileData> {
    private static final String TAG = "LocalFileAdapter";

    private String filterText = null;
    private List<Integer> positionFiltered = new ArrayList<>();

    public LocalFileAdapter(Context context, Handler handler) {
        super(context, handler);
    }

    public void filter(String query) {
        this.filterText = query;
        if (query == null)
            positionFiltered.clear();
        else {
            query = query.toLowerCase();
            for (int i = 0; i < dataList.size(); i++) {
                String title = dataList.get(i).getTitle().toLowerCase(),
                        description = dataList.get(i).getDescription().toLowerCase();
                if (title.contains(query) || description.contains(query))
                    positionFiltered.add(i);
            }
        }
        notifyDataSetChanged__();
    }

    @Override
    public void changeDataList(@NonNull List<LocalFileData> list, boolean append) {
        super.changeDataList(list, append);
        if (filterText != null)
            filter(filterText);
    }

    public String getFilterText() {
        return filterText;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder holder = new ViewHolder(
                MainActivity.LOCAL_FILE_TAB,
                inflater.inflate(R.layout.list_item_card_small, parent, false),
                mHandler,
                true
        );
        holder.ivSmallButton.setImageResource(R.drawable.ic_action_add);
        holder.rpCardView.setRippleColorRes(R.color.blue_grey);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (filterText != null)
            position = positionFiltered.get(position);
        holder.bind(dataList.get(position), position);
    }

    @Override
    public int getItemCount() {
        return filterText == null ? dataList.size() : positionFiltered.size();
    }
}

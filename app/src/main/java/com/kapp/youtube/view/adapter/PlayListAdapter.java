package com.kapp.youtube.view.adapter;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.ViewGroup;

import com.kapp.youtube.R;
import com.kapp.youtube.model.LocalFileData;
import com.kapp.youtube.model.PlayListData;
import com.kapp.youtube.view.activity.MainActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by khang on 24/04/2016.
 * Email: khang.neon.1997@gmail.com
 */
public class PlayListAdapter extends MaterialAdapter<PlayListData> {
    private static final String TAG = "PlayListAdapter";
    public static final int TYPE_ADD_PLAYLIST = 0, TYPE_DEFAULT = 1;

    private int selectingPlaylist = -1;

    private String filterText = null;
    private List<Integer> positionFiltered = new ArrayList<>();

    public PlayListAdapter(Context context, Handler handler) {
        super(context, handler);
    }

    @Override
    public void changeDataList(@NonNull List<PlayListData> list, boolean append) {
        if (append)
            dataList.addAll(list);
        else
            dataList = list;
        if (filterText != null)
            filter(filterText);
        else {
            if (append)
                notifyItemRangeInserted__(dataList.size() - list.size() + 1, list.size());
            else
                notifyDataSetChanged__();
        }
    }

    public void removePlaylist(int playListPosition) {
        dataList.remove(playListPosition);
        notifyItemRemoved__(playListPosition + 1);
    }

    public void removeItemInCurrentPlaylist(int pos){
        dataList.get(selectingPlaylist).items.remove(pos);
        notifyItemRemoved__(pos);
    }

    public void selectPlaylist(int id) {
        selectingPlaylist = id;
        if (filterText != null)
            filter(null);
        else
            notifyDataSetChanged__();
    }

    public String getFilterText() {
        return filterText;
    }

    public void filter(String query) {
        this.filterText = query;
        positionFiltered.clear();
        if (query != null)
            if (selectingPlaylist == -1) {
                for (int i = 0; i < dataList.size(); i++)
                    if (dataList.get(i).getTitle().toLowerCase().contains(query.toLowerCase()))
                        positionFiltered.add(i);
            } else {
                List<LocalFileData> selectingList = dataList.get(selectingPlaylist).items;
                for (int i = 0; i < selectingList.size(); i++)
                    if (selectingList.get(i).getTitle().toLowerCase().contains(query.toLowerCase()))
                        positionFiltered.add(i);
            }
        notifyDataSetChanged__();
    }

    public int getSelectingPlaylist() {
        return selectingPlaylist;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 && selectingPlaylist == -1 && filterText == null ?
                TYPE_ADD_PLAYLIST : TYPE_DEFAULT;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder holder = new ViewHolder(
                MainActivity.PLAY_LIST_TAB,
                inflater.inflate(
                        viewType == TYPE_DEFAULT ?
                                R.layout.list_item_card_small : R.layout.add_playlist_item,
                        parent, false),
                mHandler, true
        );
        holder.rpCardView.setRippleColorRes(R.color.yellow);
        holder.ivSmallButton.setImageResource(R.drawable.ic_delete);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (filterText != null)
            position = positionFiltered.get(position);
        if (selectingPlaylist == -1) {
            if (filterText != null)
                holder.bind(dataList.get(position), position + 1);
            else if (position != 0)
                holder.bind(dataList.get(position - 1), position);
        } else
            holder.bind(dataList.get(selectingPlaylist).getItem(position), position);
    }

    @Override
    public int getItemCount() {
        if (filterText != null)
            return positionFiltered.size();
        if (selectingPlaylist == -1)
            return dataList.size() + 1;
        else
            return dataList.get(selectingPlaylist).getItemCount();
    }
}

package com.kapp.listen2youtube.view.adapter;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;

import com.github.florent37.materialviewpager.adapter.RecyclerViewMaterialAdapter;
import com.kapp.listen2youtube.model.IDisplayData;
import com.kapp.listen2youtube.model.LocalFileData;
import com.kapp.listen2youtube.model.YoutubeData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by khang on 20/04/2016.
 * Email: khang.neon.1997@gmail.com
 */
public abstract class MaterialAdapter<T extends IDisplayData> extends RecyclerView.Adapter<ViewHolder> {
    private static final String TAG = "MaterialAdapter";

    List<T> dataList;
    RecyclerViewMaterialAdapter materialAdapter = new RecyclerViewMaterialAdapter(this);
    LayoutInflater inflater;
    Handler mHandler;

    public MaterialAdapter(Context context, Handler handler) {
        this.inflater = LayoutInflater.from(context);
        this.mHandler = handler;
        dataList = new ArrayList<>();
    }

    public RecyclerView.Adapter getMaterialAdapter() {
        return materialAdapter;
    }

    public void changeDataList(@NonNull List<T> list, boolean append){
        if (append) {
            dataList.addAll(list);
            notifyItemRangeInserted__(dataList.size() - list.size(), list.size());
        }
        else {
            dataList = list;
            notifyDataSetChanged__();
        }
    }

    public T getData(int position){
        return dataList.get(position);
    }

    public List<T> getDataList(){
        return dataList;
    }

    public int getDataListSize(){
        return dataList.size();
    }

    public void notifyDataSetChanged__() {
        notifyDataSetChanged();
        materialAdapter.notifyDataSetChanged();
    }

    public void notifyItemInserted__(int position) {
        notifyItemInserted(position);
        materialAdapter.notifyItemInserted(position + materialAdapter.getPlaceholderSize());
    }

    public void notifyItemRemoved__(int position) {
        notifyItemRemoved(position);
        materialAdapter.notifyItemRemoved(position + materialAdapter.getPlaceholderSize());
    }

    public void notifyItemRangeInserted__(int fromIndex, int itemCount){
        notifyItemRangeInserted(fromIndex, itemCount);
        materialAdapter.notifyItemRangeInserted(
                fromIndex + materialAdapter.getPlaceholderSize(),
                itemCount
        );
    }
}
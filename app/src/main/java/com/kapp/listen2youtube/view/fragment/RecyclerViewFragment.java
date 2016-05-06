package com.kapp.listen2youtube.view.fragment;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.github.florent37.materialviewpager.MaterialViewPagerHelper;
import com.kapp.listen2youtube.R;
import com.kapp.listen2youtube.view.adapter.MaterialAdapter;

import jp.wasabeef.recyclerview.animators.FadeInUpAnimator;


public class RecyclerViewFragment extends Fragment {

    public static final String LAYOUT_MANAGER_STATE = "LAYOUT_MANAGER_STATE";
    private MaterialAdapter mAdapter;

    private LinearLayoutManager layoutManager;
    private RecyclerView mRecyclerView;

    public static RecyclerViewFragment newInstance(MaterialAdapter mAdapter) {
        RecyclerViewFragment fragment = new RecyclerViewFragment();
        fragment.setRetainInstance(true);
        fragment.mAdapter = mAdapter;
        Log.e("TAG", "newInstance - line 35: create fragment");
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recyclerview, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        boolean isLandscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        if (isLandscape) {
            layoutManager = new GridLayoutManager(getContext(), 2);
            ((GridLayoutManager) layoutManager).setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    return position == 0 ? 2 : 1;
                }
            });
        } else
            layoutManager = new LinearLayoutManager(getContext());


        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter.getMaterialAdapter());
        FadeInUpAnimator fadeInUpAnimator = new FadeInUpAnimator();
        fadeInUpAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        fadeInUpAnimator.setAddDuration(350);
        fadeInUpAnimator.setRemoveDuration(350);
        fadeInUpAnimator.setSupportsChangeAnimations(true);
        mRecyclerView.setItemAnimator(fadeInUpAnimator);

        MaterialViewPagerHelper.registerRecyclerView(getActivity(), mRecyclerView, null);


        if (savedInstanceState != null) {
            Log.e("TAG", "onViewCreated - line 71: START HANDLER");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.e("TAG", "run - line 75: Handler().postDelayed");
                    final int scrollToPosition = savedInstanceState.getInt(LAYOUT_MANAGER_STATE, 0);
                    layoutManager.smoothScrollToPosition(mRecyclerView, null ,scrollToPosition);
                }
            }, 500);

        }
    }

    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    public RecyclerView.Adapter getAdapter() {
        return mAdapter;
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(LAYOUT_MANAGER_STATE, layoutManager.findLastCompletelyVisibleItemPosition());
        Log.e("TAG", "onSaveInstanceState - line 70: ");
    }
}

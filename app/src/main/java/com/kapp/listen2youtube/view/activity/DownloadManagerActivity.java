package com.kapp.listen2youtube.view.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.andexert.library.RippleView;
import com.kapp.listen2youtube.R;
import com.kapp.listen2youtube.service.DownloadService;

/**
 * Created by khang on 26/04/2016.
 * Email: khang.neon.1997@gmail.com
 */
public class DownloadManagerActivity extends AppCompatActivity implements DownloadService.DownloadListener,
        RippleView.OnRippleCompleteListener {
    private static final String TAG = "DownloadManagerActivity";

    private ServiceConnection serviceConnection;
    private DownloadService serviceInstance;
    private DownloadManagerAdapter adapter;

    TextView title, subtext;
    ProgressBar progressBar;
    View progressBarLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_manager);

        title = (TextView) findViewById(R.id.tvTitle);
        subtext = (TextView) findViewById(R.id.tvSubtext);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBarLayout = findViewById(R.id.progressLayout);
        ((RippleView) findViewById(R.id.rippleRemove)).setOnRippleCompleteListener(this);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DownloadManagerAdapter();
        recyclerView.setAdapter(adapter);



        Intent service = new Intent(this, DownloadService.class);
        service.setAction(DownloadService.ACTION_DO_NOTHING);
        startService(service);
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                serviceInstance = ((DownloadService.DownloadServiceBinder) service).getServiceInstance();
                serviceInstance.setListener(DownloadManagerActivity.this);
                onQueueChange();
                onCurrentDownloadChange();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                serviceInstance = null;
            }
        };
        bindService(service, serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        serviceInstance = null;
        unbindService(serviceConnection);
    }

    @Override
    public void onQueueChange() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onCurrentDownloadChange() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (serviceInstance == null) return;
                if (serviceInstance.downloadingTaskId == -1)
                    progressBarLayout.setVisibility(View.GONE);
                else {
                    title.setText(serviceInstance.downloadingTaskInfo.title);
                    subtext.setText("Received " +
                            serviceInstance.byteCountToString(serviceInstance.downloadedBytes) + "/"
                            + serviceInstance.byteCountToString(serviceInstance.totalBytes) + ". Progress: " +
                            serviceInstance.progress + "%");
                    progressBar.setProgress(serviceInstance.progress);
                    progressBarLayout.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void onComplete(RippleView rippleView) {
        String url = (String) rippleView.getTag();
        if (serviceInstance == null) return;
        if (url == null && serviceInstance.downloadingTaskId != -1) {
            url = serviceInstance.downloadingTaskInfo.url;
            serviceInstance.removeDownloadTask(url);
        } else
            serviceInstance.removeDownloadTask(url);
    }

    private class DownloadManagerAdapter extends RecyclerView.Adapter<DownloadManagerAdapter.ViewHolder> {
        private LayoutInflater layoutInflater = LayoutInflater.from(DownloadManagerActivity.this);

        public DownloadManagerAdapter() {

        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(
                    layoutInflater.inflate(R.layout.item_in_queue, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            if (serviceInstance == null) return;
            DownloadService.DownloadInfo downloadInfo = serviceInstance.queue.get(position);
            holder.title.setText(downloadInfo.title);
            holder.rippleView.setTag(downloadInfo.url);
        }

        @Override
        public int getItemCount() {
            if (serviceInstance == null)
                return 0;
            return serviceInstance.queue.size();
        }


        class ViewHolder extends RecyclerView.ViewHolder {
            TextView title;
            RippleView rippleView;

            public ViewHolder(View itemView) {
                super(itemView);
                title = (TextView) itemView.findViewById(R.id.tvTitle);
                rippleView = (RippleView) itemView.findViewById(R.id.rippleRemove);
                rippleView.setOnRippleCompleteListener(DownloadManagerActivity.this);
            }
        }
    }
}

package com.example.videodetected;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

// Adapter创建目的是“联通”layout与RecyclerView（解耦的一部分 ps：虽然我感觉变得更复杂了）
public class MainAdapter extends RecyclerView.Adapter<MainAdapter.MainViewHolder> {
    private List<Video> videoList;
    private ViewGroup parent;

    public MainAdapter(List<Video> videoList) {
        this.videoList = videoList;
    }

    @NonNull
    @Override
    public MainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 从layout中生成view,并返回ViewHolder
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_items, parent,false);
        MainViewHolder mainViewHolder = new MainViewHolder(itemView);
        return mainViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MainViewHolder holder, int position) {
        // 此处将Video类中的属性关联到本类中（即充当媒介将layout与Video关联）
        // 值得注意的是 Video类中的image仅记录图像id，需要通过setImageResource方法调用
        // 且info属性并不是直接从Video中拿到的，需要稍微处理下
        Video video = videoList.get(position);
        holder.image.setImageResource(video.pic_id);
        holder.upload_time.setText(video.upload_time);
        holder.duration.setText(video.duration);
        holder.title.setText(video.title);
        holder.owner.setText(video.owner);
        // 处理info字符串
        String info = Integer.toString(video.play_num) + " 播放 · "+Integer.toString(video.bullet_num)+"弹幕";
        holder.info.setText(info);
    }

    @Override
    public int getItemCount() {
        // item总数量
        return videoList.size();
    }

    class MainViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView upload_time;
        TextView duration;
        TextView title;
        TextView owner;
        TextView info;

        public MainViewHolder(View itemView) {
            // 此处将layout里面的元素关联到本类
            super(itemView);
            this.image = itemView.findViewById(R.id.video_image);
            this.upload_time = itemView.findViewById(R.id.video_upload_time);
            this.duration = itemView.findViewById(R.id.video_duration);
            this.title = itemView.findViewById(R.id.video_title);
            this.owner = itemView.findViewById(R.id.video_owner);
            this.info = itemView.findViewById(R.id.video_info);
        }
    }
}


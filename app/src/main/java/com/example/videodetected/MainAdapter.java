package com.example.videodetected;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

// Adapter创建目的是“联通”layout与RecyclerView（解耦的一部分 ps：虽然我感觉变得更复杂了）
public class MainAdapter extends RecyclerView.Adapter<MainAdapter.MainViewHolder> {
    private List<Video> videoList;
    private ViewGroup parent;
    private Bitmap bitmap;
    public List<Boolean> isDownloadList;

    public MainAdapter(List<Video> videoList) {
        this.videoList = videoList;
        this.isDownloadList = new ArrayList<>();
    }

    // 响应图片下载完成事件
    Handler myHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case 1:
                    int position = msg.arg1;
//                    isDownloadList.set(position,true);
//                    bitmapList.set(position,msg.getData().getParcelable("myBitmap"));
                    notifyItemChanged(position);
                    break;
                default:
                    break;
            }
        }
    };

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
        // 值得注意的是 Video类中的image仅记录图像src，需要通过HttpFunc.getBitmapFromUrl方法下载网络资源（bimap返回）
        // 且info属性并不是直接从Video中拿到的，需要稍微处理下
        // item是每个元素的整体页面布局
        Video video = videoList.get(position);
        // 拓展isDownLoadList长度并在此处记录图片是否已获取（如果获取则从func的缓存中读取图片）
//        while(isDownloadList.size()<videoList.size()){
//            isDownloadList.add(false);
//        }
//        if(isDownloadList.get(position)){
//            Bitmap bitmap = MyFunction.getBitmapFromCache(video.pic_src);
//            holder.image.setImageBitmap(bitmap);
//        }else{
//            MyFunction.getBitmapFromUrl(video.pic_src,myHandler,position);
//        }
        bitmap = MyFunction.getBitmapFromCache(video.pic_src);
        if (bitmap!=null){
            holder.image.setImageBitmap(bitmap);
        }else{
            MyFunction.getBitmapFromUrl(video.pic_src,myHandler,position);
        }

        holder.upload_time.setText(video.upload_time);
        holder.duration.setText(video.duration);
        holder.title.setText(video.title);
        holder.owner.setText(video.owner);
        // 处理info字符串
        String info = Integer.toString(video.play_num) + " 播放 · "+Integer.toString(video.bullet_num)+"弹幕";
        holder.info.setText(info);
        // 设置点击事件(跳转至详情页，是否可编辑待商榷）
        holder.item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 可能是因为本函数定义时并没有指定具体view界面，因此无法直接使用MainActivity.this进行跳转
                // 故使用getContext函数作为中转，先获取再跳转
                Context context = v.getContext();
//                Toast.makeText(context,video.title,Toast.LENGTH_SHORT).show();
                // 使用bundle中转以实现传递实体类的功能
                Intent i = new Intent(context, DetailsActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("Intent_video",video);
                bundle.putInt("position",holder.getAdapterPosition());
                i.putExtras(bundle);

                // 从MainActivity获得监听函数，并由此调用DetailActivity，这样可以拿到返回结果以刷新数据（真麻烦）
                MainActivity.launcher.launch(i);
            }
        });
        // 设置长按删除事件
        holder.item.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // 由于删除其他元素时，本元素position可能会发生变化，因此要动态获取
                int position = holder.getAdapterPosition();
                removeData(position);
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        // item总数量
        return videoList.size();
    }

    public void removeData(int position){
        videoList.remove(position);
        // 增加删除动画
        notifyItemRemoved(position);
    }

    class MainViewHolder extends RecyclerView.ViewHolder {
        View item;
        ImageView image;
        TextView upload_time;
        TextView duration;
        TextView title;
        TextView owner;
        TextView info;

        public MainViewHolder(View itemView) {
            // 此处将layout里面的元素关联到本类
            super(itemView);
            this.item = itemView.findViewById(R.id.video_item);  //此处定位整个布局
            this.image = itemView.findViewById(R.id.video_image);
            this.upload_time = itemView.findViewById(R.id.video_upload_time);
            this.duration = itemView.findViewById(R.id.video_duration);
            this.title = itemView.findViewById(R.id.video_title);
            this.owner = itemView.findViewById(R.id.video_owner);
            this.info = itemView.findViewById(R.id.video_info);
        }
    }

}


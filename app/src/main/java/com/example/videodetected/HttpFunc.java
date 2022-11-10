package com.example.videodetected;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HttpFunc {
    static List<Video> videoList;
    static Bitmap myBitmap;
    static List<Bitmap> bitmapList = new ArrayList<>();


    public static List<Video> get_video_info(Handler myHandler){
        videoList = new ArrayList<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = "https://api.bilibili.com/x/space/arc/search?mid=364868516&pn=1&ps=10&index=1&order=pubdate&order_avoided=true&jsonp=jsonp";
                OkHttpClient client = new OkHttpClient();
                final Request request = new Request.Builder()
                        .header("user-agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36 Edg/107.0.1418.35")
                        .url(url)
                        .get()
                        .build();
                Call call = client.newCall(request);
                Response response = null;
                try {
                    response = call.execute();
                    String res_string = response.body().string();
                    JSONObject res_json = new JSONObject(res_string);
                    JSONArray vlist = res_json.getJSONObject("data").getJSONObject("list").getJSONArray("vlist");
                    videoList = Json2Videos(vlist,videoList);
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
                // 给MainActivity发消息使其刷新界面(将videoList使用bundle打包传回）
                Message msg = Message.obtain();
                msg.what = 1;
                Bundle bundle = new Bundle();
                bundle.putSerializable("videoList", (Serializable) videoList);
                msg.setData(bundle);
                myHandler.sendMessage(msg);
            }
        }).start();
        return videoList;
    }
    static List<Video> Json2Videos(JSONArray vlist, List<Video> videoList) throws JSONException {
        for(int i=0;i<vlist.length();i++){
            JSONObject v = vlist.getJSONObject(i);
            Video video = new Video(v);
            videoList.add(video);
        }
        return videoList;
    }

    // 从url获取图片信息
    public static Bitmap getBitmapFromUrl(String src,Handler myHandler,int position){
        myBitmap = null;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(src);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream input = connection.getInputStream();
                    myBitmap = BitmapFactory.decodeStream(input);
                    // 将结果储存起来(先拓展）
                    while(bitmapList.size()<=position){
                        bitmapList.add(null);
                    }
                    bitmapList.set(position,myBitmap);
                    // 给MainAdapter传递myBitmap数据并使其刷新特定元素（获取完图片了）
                    Message msg = Message.obtain();
                    msg.what = 1;
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("myBitmap", (Parcelable) myBitmap);
                    msg.setData(bundle);
                    msg.arg1 = position;
                    myHandler.sendMessage(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        return myBitmap;
    }
    // 从缓存获取图片信息
    public static Bitmap getBitmapFromCache(int position){
        return bitmapList.get(position);
    }
}
package com.example.videodetected;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MyFunction {
    static List<Video> videoList;
    static Bitmap myBitmap;
//    static List<Bitmap> bitmapList = new ArrayList<>();
//    // 用map关联图片链接与bitmap信息，更方便从缓存中读取（且不易出错）
//    static Map<String, Integer> map = new HashMap<>();


    public static List<Video> get_video_info(Handler myHandler){
        videoList = new ArrayList<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = "https://api.bilibili.com/x/space/arc/search?mid=123372&pn=1&ps=10&index=1&order=pubdate&order_avoided=true&jsonp=jsonp";
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
                    // 将图像保存在本地，文件名为video网址信息
                    saveBitmap(myBitmap,src);

//                    // 将结果储存起来(先拓展）
//                    while(bitmapList.size()<=position){
//                        bitmapList.add(null);
//                    }
//                    bitmapList.set(position,myBitmap);
//                    // 将该图片网址与bitmapList序号关联起来
//                    map.put(videoList.get(position).pic_src,position);
                    // 给MainAdapter传递myBitmap数据并使其刷新特定元素（获取完图片了）
                    Message msg = Message.obtain();
                    msg.what = 1;
//                    Bundle bundle = new Bundle();
//                    bundle.putParcelable("myBitmap", (Parcelable) myBitmap);
//                    msg.setData(bundle);
                    msg.arg1 = position;
                    myHandler.sendMessage(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        return myBitmap;
    }

    private static void saveBitmap(Bitmap myBitmap,String src) {
        String path = Environment.getExternalStorageDirectory().getPath() + "/Pictures/VideoDetect";
        File file=new File(path);
        FileOutputStream fileOutputStream=null;
        //文件夹不存在，则创建它
        if(!file.exists()){
            file.mkdir();
        }
        try {
            fileOutputStream=new FileOutputStream(path+"/"+md5(src)+".png");
            myBitmap.compress(Bitmap.CompressFormat.JPEG, 80,fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 从本地缓存获取图片信息
    public static Bitmap getBitmapFromCache(String src){
        String dir_path = Environment.getExternalStorageDirectory().getPath() + "/Pictures/VideoDetect/";
        String pic_path = dir_path + md5(src) + ".png";
        // 找不到本地图片则返回null
        try{
            Bitmap bitmap = BitmapFactory.decodeFile(pic_path);
            return bitmap;
        } catch (Exception e){
            return null;
        }
    }

    // 对videoList排序(无返回值，sort似乎直接操作了主页面的List)
    public static List<Video> sort_videoList_by(List<Video> videoList,String sort_by){
        Collections.sort(videoList, new Comparator<Video>() {
            @Override
            public int compare(Video o1, Video o2) {
                int diff = 0;
                try {
                    diff = func_sort_by(o1,o2,sort_by);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                // diff>0 -> 1;diff<0 -> -1; else 0
//                return Integer.compare(diff, 0);
                // 相反
                return Integer.compare(0, diff);
            }
        });
        return null;
    }
    // 设置特定的比较方法
    static int func_sort_by(Video o1,Video o2,String sort_by) throws ParseException {
        int res = 0;
        switch(sort_by){
            case "upload_time":
                // 字符串转时间戳再比较
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date1 = simpleDateFormat.parse(o1.upload_time);
                Date date2 = simpleDateFormat.parse(o2.upload_time);
                long ts1 = date1.getTime();
                long ts2 = date2.getTime();
                res = (int)( ts1 - ts2 );
                break;
            case "duration":
                // 划分“分秒”再进行比较
                String[] split1 = o1.duration.split(":",2);
                String[] split2 = o2.duration.split(":",2);
                int duration1 = Integer.parseInt(split1[0])*60+Integer.parseInt(split1[1]);
                int duration2 = Integer.parseInt(split2[0])*60+Integer.parseInt(split2[1]);
                res = duration1 - duration2;
                break;
            case "play_num":
                res = o1.play_num - o2.play_num;
                break;
            case "bullet_num":
                res = o1.bullet_num - o2.bullet_num;
                break;
        }
        return res;
    }

    // 获取videoList中符合搜索结果的video（前提是已经从网络获取了全部视频信息,否则可能会报错）-> 简单的关键词过滤
    public static List<Video> searchFilter_videoList(String key){
        List<Video> filterVideoList = new ArrayList<>();
        for(Video v: videoList){
            if(v.title.contains(key)){
                filterVideoList.add(v);
            }
        }
        return filterVideoList;
    }


    // 对src进行md5加密（主要是为了保存图片）
    public static String md5(String src){
        try {
            MessageDigest m = MessageDigest.getInstance("MD5");
            m.update(src.getBytes("UTF8"));
            byte[] s = m.digest();
            src = "";
            for (byte b : s) {
                src += Integer.toHexString((0x000000ff & b) | 0xffffff00).substring(6);
            }
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return src;
    }
}
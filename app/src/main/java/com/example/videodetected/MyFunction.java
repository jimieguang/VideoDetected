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
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MyFunction {
    static List<Video> videoList;
    static Bitmap myBitmap;

    public static void get_video_info(Handler myHandler,String uid,String contain,String from){
        videoList = new ArrayList<>();

        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = String.format("https://api.bilibili.com/x/space/arc/search?mid=%s&pn=%s&ps=%s&index=1&order=pubdate&order_avoided=true&jsonp=jsonp",uid,from,contain);
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
                    Json2Videos(vlist,videoList);
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
    }

    // java中传递List使用的地址，因此函数内的更改会变动函数外的List值，因此无须返回值
    static void Json2Videos(JSONArray vlist, List<Video> videoList) throws JSONException {
        for(int i=0;i<vlist.length();i++){
            JSONObject v = vlist.getJSONObject(i);
            Video video = new Video(v);
            videoList.add(video);
        }
    }

    // 从url获取图片信息
    public static void getBitmapFromUrl(String src, Handler myHandler, int position){
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
                    // 将图像保存在本地，文件名为md5加密后的video网址信息
                    saveBitmap(myBitmap,src);
                    // 给MainAdapter传递myBitmap数据并使其刷新特定元素（获取完图片了）
                    Message msg = Message.obtain();
                    msg.what = 1;
                    msg.arg1 = position;
                    myHandler.sendMessage(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private static void saveBitmap(Bitmap myBitmap,String src) {
        String path = Environment.getExternalStorageDirectory().getPath() + "/Pictures/VideoDetect";
        File file=new File(path);
        FileOutputStream fileOutputStream=null;
        float quality = 80;
        if(myBitmap.getByteCount()/1024>2048){
            quality = 50;
        }
        //文件夹不存在，则创建它
        if(!file.exists()){
            file.mkdir();
        }
        try {
            fileOutputStream=new FileOutputStream(path+"/"+md5(src)+".png");
            myBitmap.compress(Bitmap.CompressFormat.JPEG, (int)quality,fileOutputStream);
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
        // 限制图片最大尺寸
        int init_width = 1920;
        int init_height = 1080;
        // 找不到本地图片则返回null
        try{
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;   // 此时无bitmap返回值，避免开辟bitmap内存空间
            BitmapFactory.decodeFile(pic_path,options);
            int imageHeight = options.outHeight;
            int imageWidth = options.outWidth;
            options.inJustDecodeBounds = false;
            // inSampleSize 缩小2的指数倍，因此需要进行处理
            int scale = Math.min(imageWidth/init_width,imageHeight/init_height);
            options.inSampleSize = 1;
            while(scale!=0){
                scale /= 2;
                options.inSampleSize *= 2;
            }
            Bitmap bitmap = BitmapFactory.decodeFile(pic_path,options);
            return bitmap;
        } catch (Exception e){
            return null;
        }
    }

    // 对videoList排序(无返回值，sort似乎直接操作了主页面的List)
    public static void sort_videoList_by(List<Video> videoList, String sort_by){
        videoList.sort((o1, o2) -> {
            int diff = 0;
            try {
                diff = func_sort_by(o1, o2, sort_by);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            // diff>0 -> 1;diff<0 -> -1; else 0
//                return Integer.compare(diff, 0);
            // 相反
            return Integer.compare(0, diff);
        });
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
            m.update(src.getBytes(StandardCharsets.UTF_8));
            byte[] s = m.digest();
            StringBuilder srcBuilder = new StringBuilder();
            for (byte b : s) {
                srcBuilder.append(Integer.toHexString((0x000000ff & b) | 0xffffff00).substring(6));
            }
            src = srcBuilder.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return src;
    }
}
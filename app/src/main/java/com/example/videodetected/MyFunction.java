package com.example.videodetected;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MyFunction {
    static List<Video> videoList;
    static Bitmap myBitmap;
    static HashMap<String,Bitmap> bitmapHashMap = new HashMap<>();

    public static void get_video_info(Handler myHandler,String uid,String contain,String from){
        videoList = new ArrayList<>();

        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = String.format("https://api.bilibili.com/x/space/wbi/arc/search?mid=%s&pn=%s&ps=%s&index=1&order=pubdate&order_avoided=true",uid,from,contain);
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
                // ???MainActivity???????????????????????????(???videoList??????bundle???????????????
                Message msg = Message.obtain();
                msg.what = 1;
                Bundle bundle = new Bundle();
                bundle.putSerializable("videoList", (Serializable) videoList);
                msg.setData(bundle);
                myHandler.sendMessage(msg);
            }
        }).start();
    }

    // java?????????List???????????????????????????????????????????????????????????????List???????????????????????????
    static void Json2Videos(JSONArray vlist, List<Video> videoList) throws JSONException {
        for(int i=0;i<vlist.length();i++){
            JSONObject v = vlist.getJSONObject(i);
            Video video = new Video(v);
            videoList.add(video);
        }
    }

    // ???url??????????????????
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
                    // ??????????????????
                    saveBitmapCache(myBitmap,src);
                    // ???????????????????????????????????????md5????????????video????????????
                    saveBitmapDisk(myBitmap,src);
                    // ???MainAdapter??????myBitmap?????????????????????????????????????????????????????????
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

    private static void saveBitmapDisk(Bitmap myBitmap, String src) {
        if(myBitmap==null)
            return;
        String path = Environment.getExternalStorageDirectory().getPath() + "/Pictures/VideoDetect";
        File file=new File(path);
        FileOutputStream fileOutputStream=null;
        float quality = 80;
        if(myBitmap.getByteCount()/1024>2048){
            quality = 50;
        }
        //?????????????????????????????????
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


    private static void saveBitmapCache(Bitmap myBitmap,String src) {
        // ????????????????????????
        int max_width = 1920;
        int max_height = 1080;
        assert myBitmap != null;
        int imageWidth = myBitmap.getWidth();
        int imageHeight = myBitmap.getHeight();
        int scale = Math.max(imageWidth/max_width,imageHeight/max_height);
        if(scale>1){
            imageWidth /= imageWidth / scale;
            imageHeight /= imageHeight / scale;
        }
        Bitmap BitmapCache = Bitmap.createScaledBitmap(myBitmap,imageWidth,imageHeight,true);
        bitmapHashMap.put(src,BitmapCache);
    }
    public static Bitmap getBitmapFromCache(String src){
        if(bitmapHashMap.containsKey(src)){
            return bitmapHashMap.get(src);
        }
        return null;
    }

    // ?????????????????????????????????
    public static void getBitmapFromDisk(String src, Handler myHandler, int position){
        myBitmap = null;
        new Thread(new Runnable() {
            @Override
            public void run() {
                String dir_path = Environment.getExternalStorageDirectory().getPath() + "/Pictures/VideoDetect/";
                String pic_path = dir_path + md5(src) + ".png";
                // ????????????????????????
                int max_width = 1920;
                int max_height = 1080;
                // ??????????????????????????????null
                File file = new File(pic_path);
                if(!file.exists()){
                    getBitmapFromUrl(src,myHandler,position);
                }else{
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;   // ?????????bitmap????????????????????????bitmap????????????
                    BitmapFactory.decodeFile(pic_path,options);
                    int imageHeight = options.outHeight;
                    int imageWidth = options.outWidth;
                    options.inJustDecodeBounds = false;
                    // inSampleSize ??????2???????????????????????????????????????
                    int scale = Math.min(imageWidth/max_width,imageHeight/max_height);
                    options.inSampleSize = 1;
                    while(scale!=0){
                        scale /= 2;
                        options.inSampleSize *= 2;
                    }
                    Bitmap bitmap = BitmapFactory.decodeFile(pic_path,options);
                    // ??????????????????
                    saveBitmapCache(bitmap,src);
                    // ???MainAdapter??????myBitmap?????????????????????????????????????????????????????????
                    Message msg = Message.obtain();
                    msg.what = 1;
                    msg.arg1 = position;
                    myHandler.sendMessage(msg);
                }
            }
        }).start();
    }

    // ???videoList??????(???????????????sort?????????????????????????????????List)
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
            // ??????
            return Integer.compare(0, diff);
        });
    }
    // ???????????????????????????
    static int func_sort_by(Video o1,Video o2,String sort_by) throws ParseException {
        int res = 0;
        switch(sort_by){
            case "upload_time":
                // ??????????????????????????????
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date1 = simpleDateFormat.parse(o1.upload_time);
                Date date2 = simpleDateFormat.parse(o2.upload_time);
                long ts1 = date1.getTime();
                long ts2 = date2.getTime();
                res = (int)( ts1 - ts2 );
                break;
            case "duration":
                // ?????????????????????????????????
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

    // ??????videoList????????????????????????video??????????????????????????????????????????????????????,????????????????????????-> ????????????????????????
    public static List<Video> searchFilter_videoList(String key){
        List<Video> filterVideoList = new ArrayList<>();
        for(Video v: videoList){
            if(v.title.contains(key)){
                filterVideoList.add(v);
            }
        }
        return filterVideoList;
    }


    // ???src??????md5???????????????????????????????????????
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
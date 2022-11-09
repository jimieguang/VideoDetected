package com.example.videodetected;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.OkHttp;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HttpJson{
    public String title;
    public String owner;
    public String upload_time;
    public String description;
    public String duration;
    public int play_num;
    public int bullet_num;
    public int pic_id;

    public void get_info(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = "https://api.bilibili.com/x/space/arc/search?mid=364868516&pn=1&ps=4&index=1&order=pubdate&order_avoided=true&jsonp=jsonp";
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
                    List<Video> videoList = Json2Videos(vlist);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }
    List<Video> Json2Videos(JSONArray vlist) throws JSONException {
        List<Video> videoList = new ArrayList<>();
        for(int i=0;i<vlist.length();i++){
            JSONObject v = vlist.getJSONObject(0);
            Video video = new Video(v);
            videoList.add(video);
        }
        return videoList;
    }
}
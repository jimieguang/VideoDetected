package com.example.videodetected;

import android.widget.EditText;
import android.widget.ImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.List;

// 继承Serializable类是为了可以通过Intent传递实体类
public class Video implements Serializable {
    public String title;
    public String owner;
    public String upload_time;
    public String description;
    public String duration;
    public int play_num;
    public int bullet_num;
    public String pic_id;

    public Video(String title, String owner, String upload_time, String description, String duration,String pic_id, int play_num, int bullet_num) {
        this.title = title;
        this.owner = owner;
        this.upload_time = upload_time;
        this.description = description;
        this.duration = duration;
        this.pic_id = pic_id;
        this.play_num = play_num;
        this.bullet_num = bullet_num;
    }

    public Video(List<EditText> editTextList, String pic_id){
        // 注意顺序是严格按照Adapter中定义的，不能随意改变
        this.title = String.valueOf(editTextList.get(0).getText());
        this.owner = String.valueOf(editTextList.get(1).getText());
        this.duration = String.valueOf(editTextList.get(2).getText());
        this.upload_time = String.valueOf(editTextList.get(3).getText());
        // 无法从空字符转换为int类型，因此要捕获异常并设置为0
        try{
            this.play_num = Integer.parseInt(String.valueOf(editTextList.get(4).getText()));
        }catch (NumberFormatException e){
            this.play_num = 0;
        }
        try{
            this.bullet_num = Integer.parseInt(String.valueOf(editTextList.get(5).getText()));
        }catch (NumberFormatException e){
            this.bullet_num = 0;
        }
        this.description = String.valueOf(editTextList.get(6).getText());

        this.pic_id = pic_id;
    }

    public Video(JSONObject v) throws JSONException {
        this.title = (String) v.get("title");
        this.owner = (String) v.get("author");
        this.description = (String) v.get("description");
        this.duration = (String) v.get("length");
        this.play_num = (int) v.get("play");
        this.bullet_num = (int) v.get("video_review");
        // upload_time 需要由时间戳转为字符串
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long l = Long.parseLong((String) v.get("created"));
        this.upload_time = sdf.format(l);
        // 图片需要从指定链接下载完返回id
        this.pic_id = pic_id;

    }
}

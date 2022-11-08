package com.example.videodetected;

import android.widget.EditText;
import android.widget.ImageView;

import java.io.Serializable;
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
    public int pic_id;

    public Video(String title, String owner, String upload_time, String description, String duration, int play_num, int bullet_num, int pic_id) {
        this.title = title;
        this.owner = owner;
        this.upload_time = upload_time;
        this.description = description;
        this.duration = duration;
        this.play_num = play_num;
        this.bullet_num = bullet_num;
        this.pic_id = pic_id;
    }

    public Video(List<EditText> editTextList, int pic_id){
        // 注意顺序是严格按照Adapter中定义的，不能随意改变
        this.title = String.valueOf(editTextList.get(0).getText());
        this.owner = String.valueOf(editTextList.get(1).getText());
        this.duration = String.valueOf(editTextList.get(2).getText());
        this.upload_time = String.valueOf(editTextList.get(3).getText());
        this.play_num = Integer.parseInt(String.valueOf(editTextList.get(4).getText()));
        this.bullet_num = Integer.parseInt(String.valueOf(editTextList.get(5).getText()));
        this.description = String.valueOf(editTextList.get(6).getText());

        this.pic_id = pic_id;
    }
}

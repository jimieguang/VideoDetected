package com.example.videodetected;

public class Video {
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
}

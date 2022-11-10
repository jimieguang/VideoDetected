package com.example.videodetected;

import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.klinker.android.sliding.SlidingActivity;

import java.util.ArrayList;
import java.util.List;

public class DetailsActivity extends SlidingActivity {
    private Intent get_intent;
    private Video Intent_video;
    private int position;  // 定位元素位置，回传的时候可以实现局部刷新
    private List<EditText> editTextList;

    @Override
    public void init(Bundle savedInstanceState) {
        get_intent = this.getIntent();
        Intent_video = (Video) get_intent.getSerializableExtra("Intent_video");
        position = (int) get_intent.getIntExtra("position",0);

        // 可能是背景填充相关的颜色（由于没用到所以无所谓，但谨慎起见还是不删了）
        setPrimaryColors(
                ContextCompat.getColor(this, R.color.teal_200),
                ContextCompat.getColor(this, R.color.black)
        );
        // 获取并设置两个layout（头部和内容）
        setHeaderContent(R.layout.details_activity_header);
        setContent(R.layout.details_activity_content);

        // 将传来的video信息写入layout
        init_header(Intent_video);
        init_content(Intent_video);

        // 设置编辑按钮（点击后将 EditText 改为可选中状态，并将该按钮更换为保存按钮）
        setEditFab();

    }
    void init_header(Video video){
        ImageView detail_image = findViewById(R.id.detail_image);
        Bitmap bitmap = MyFunction.getBitmapFromCache(Intent_video.pic_src);
        // 设置bimap图片
        detail_image.setImageBitmap(bitmap);

    }
    void init_content(Video video){
        // 获取EditText元素并将其加入list以待之后使用
        editTextList = new ArrayList<EditText>();
        EditText title = findViewById(R.id.detail_title); editTextList.add(title);
        EditText owner = findViewById(R.id.detail_owner); editTextList.add(owner);
        EditText duration = findViewById(R.id.detail_duration); editTextList.add(duration);
        EditText upload_time = findViewById(R.id.detail_upload_time); editTextList.add(upload_time);
        EditText play_num = findViewById(R.id.detail_play_num); editTextList.add(play_num);
        EditText bullet_num = findViewById(R.id.detail_bullet_num); editTextList.add(bullet_num);
        EditText description = findViewById(R.id.detail_description); editTextList.add(description);

        // 从类中提取并设置
        title.setText(video.title);
        owner.setText(video.owner);
        duration.setText(video.duration);
        upload_time.setText(video.upload_time);
        play_num.setText(Integer.toString(video.play_num));
        bullet_num.setText(Integer.toString(video.bullet_num));
        description.setText(video.description);

    }

    // 设置编辑图标与响应事件
    void setEditFab(){
        setFab(
                ContextCompat.getColor(this, R.color.teal_200),
                R.drawable.ic_edit,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditFabClick(v);
                        setSaveFab();
                    }
                }
        );
    }
    void EditFabClick(View v){
        // xml中设置android:focusable="false"时似乎将触摸焦点也同时关闭了，因此启用时需要两个一起
        for(EditText editText : editTextList){
            editText.setFocusable(true);
            editText.setFocusableInTouchMode(true);
        }
        // 定位一个可编辑元素（title），并将光标置于最后
        EditText title = findViewById(R.id.detail_title);
        title.requestFocus(title.getText().length());
    }
    // 设置保存图标与响应事件
    void setSaveFab(){
        setFab(
                ContextCompat.getColor(this, R.color.teal_200),
                R.drawable.ic_save,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SaveFabClick(v);
                        setEditFab();
                    }
                }
        );
    }
    void SaveFabClick(View v){
        // 关闭元素可编辑属性
        for(EditText editText : editTextList){
            editText.setFocusable(false);
            editText.setFocusableInTouchMode(false);
        }
        // 关闭可能存在的小键盘
        ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(v.getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
        // 将数据存到数据库（待议）

        // 构建新Video类
        Video new_video = new Video(editTextList,Intent_video.pic_src);
        // 返回保存消息，申请刷新数据
        Intent i = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable("new_video",new_video);
        bundle.putInt("position",position);
        i.putExtras(bundle);
        setResult(RESULT_OK,i);
        finish();
    }
}
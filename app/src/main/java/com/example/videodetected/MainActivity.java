package com.example.videodetected;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static ActivityResultLauncher launcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 去除默认标题栏
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        // 状态栏改为全透明
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        // 进入主页
        setContentView(R.layout.main_activity);

        // 点击按钮打开侧边栏
        final ImageView menu_main = findViewById(R.id.menu_main);
        menu_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawerLayout drawer = findViewById(R.id.drawer_layout);
                drawer.openDrawer(GravityCompat.START);
            }
        });

        // 动态设置侧边栏日期显示
        set_dateinfo();

        //页面主题元素 加载/渲染（recyclerview)
        RecyclerView recyclerView = findViewById(R.id.videolist_recycler);

        List<Video> videoList = new ArrayList<>();


        // 设置LayoutManager，不设置无法显示（似乎是为了样式，但样式应该已经在Adapter中绑定了，不甚理解，待议）
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        // 注入Adapter配置
        MainAdapter mainAdapter = new MainAdapter(videoList);
        recyclerView.setAdapter(mainAdapter);

        Handler myHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg){
                switch (msg.what){
                    case 1:
                        videoList.addAll((Collection<? extends Video>) msg.getData().getSerializable("videoList"));
                        mainAdapter.notifyDataSetChanged();
                        break;
                    default:
                        break;
                }
            }
        };
        HttpFunc.get_video_info(myHandler);

        // 设置监听器以拿到DetailActivity返回的数据（代替StartActivityForResult）
        // 值得注意的是，该监听器仅能在OnCreate时创建，因此带来了不便（因为触发函数写在Adapter）
//        List<Video> finalVideoList = videoList;
        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK) {
                    // 局部刷新数据
                    Intent intent_receive = result.getData();
                    Video new_video = (Video) intent_receive.getSerializableExtra("new_video");
                    int position = intent_receive.getIntExtra("position",0);
                    videoList.set(position,new_video);
                    mainAdapter.notifyItemChanged(position);
                }
            }
        });

    }

    // 设置侧边栏日期（需要从数字转为英文）
    void set_dateinfo() {
        String month_string = "Jan-Feb-Mar-Apr-May-Jun-Jul-Aug-Seg-Oct-Nov-Dec";
        String week_string = "Monday-Tuesday-Wednesday-Thursday-Friday-Saturday-Sunday";
        TextView day_view = findViewById(R.id.sides_day);
        TextView month_view = findViewById(R.id.sides_month);
        TextView week_view = findViewById(R.id.sides_week);

        Calendar now = Calendar.getInstance();
        int day = now.get(Calendar.DATE);
        int month = now.get(Calendar.MONTH);   //month从零开始计算
        int week = now.get(Calendar.DAY_OF_WEEK);
        //需要考虑周日是否为一周之始
        if(now.getFirstDayOfWeek() == Calendar.SUNDAY){
            week_string = "Sunday-Monday-Tuesday-Wednesday-Thursday-Friday-Saturday";
        }
        // 从数字转为英文日期表示
        day_view.setText(Integer.toString(day));   // setText 仅接收字符串参数
        // 月份
        String[] month_list = month_string.split("-");
        month_view.setText(month_list[month]);
        // 星期
        String[] week_list = week_string.split("-");
        week_view.setText(week_list[week-1]);
    }
}
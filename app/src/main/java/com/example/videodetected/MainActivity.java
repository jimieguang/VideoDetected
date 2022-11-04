package com.example.videodetected;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import java.time.LocalDate;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_main);
        final ImageView menu_main = findViewById(R.id.menu_main);

        // 点击按钮打开侧边栏
        menu_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawerLayout drawer = findViewById(R.id.drawer_layout);
                drawer.openDrawer(GravityCompat.START);
            }
        });

        // 动态设置侧边栏日期显示
        set_dateinfo();


    }
    // 设置侧边栏日期（需要从数字转为英文）
    void set_dateinfo() {
        TextView day_view = findViewById(R.id.sides_day);
        TextView month_view = findViewById(R.id.sides_month);
        TextView week_view = findViewById(R.id.sides_week);

        Calendar now = Calendar.getInstance();
        int day = now.get(Calendar.DATE);
        int month = now.get(Calendar.MONTH);   //month从零开始计算
        int week = now.get(Calendar.DAY_OF_WEEK);
        //需要考虑周日是否为一周之始
        if(now.getFirstDayOfWeek() == Calendar.SUNDAY){
            week--;
        }
        // 从数字转为英文日期表示
        day_view.setText(Integer.toString(day));   // setText 仅接收字符串参数
        String month_string = "Jan-Feb-Mar-Apr-May-Jun-Jul-Aug-Seg-Oct-Nov-Dec";
        String[] month_list = month_string.split("-");
        month_view.setText(month_list[month]);
        String week_string = "Monday-Tuesday-Wednesday-Thursday-Friday-Saturday-Sunday";
        String[] week_list = week_string.split("-");
        week_view.setText(week_list[week-1]);
    }
}
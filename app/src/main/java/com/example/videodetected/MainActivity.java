package com.example.videodetected;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static ActivityResultLauncher launcher;
    public static List<Video> videoList;
    private MainAdapter mainAdapter;
    private Handler myHandler;
    private SwipeRefreshLayout swipeRefreshLayout; //下拉刷新控件


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 状态栏改为全透明
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        // 获取用户设置
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String name = preferences.getString("name","未设置");
        String uid = preferences.getString("uid","0");
        String contain = preferences.getString("contain","1");
        String from = preferences.getString("from","1");
        // 进入主页
        setContentView(R.layout.main_activity);

        // 设置toolbar（原先的fitsSystemWindows方式会导致软键盘挤压布局，因此改成此方式）
        Toolbar mToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        TextView menu_name = findViewById(R.id.menu_name);
        menu_name.setText(name); // 设置昵称
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        // Rect start
        View tt = findViewById(R.id.videolist_recycler);
        List<Rect> test = new ArrayList<>();
//        test.add(new Rect(0,237,108,2339));
//        test.add(new Rect(0,0,80,800));
        test.add(new Rect(0,801,80,1200));
        tt.setSystemGestureExclusionRects(test);
        //  Rect end
        // 点击按钮打开侧边栏（设置事件）
        final ImageView menu_main = findViewById(R.id.menu_main);
        menu_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawerLayout drawer = findViewById(R.id.drawer_root);
                drawer.openDrawer(GravityCompat.START);
                // 设置侧边栏点击事件(menu元素在侧边栏打开之前是不加载的，因此要在这里设置点击事件）
                set_sides_click_listener(drawer);
            }
        });

        // 动态设置侧边栏日期显示
        set_dateinfo();

        // 设置刷新按钮
        View flush_button = findViewById(R.id.flush_button);
        flush_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uid = preferences.getString("uid","0");
                String contain = preferences.getString("contain","1");
                String from = preferences.getString("from","1");
                MyFunction.get_video_info(myHandler,uid,contain,from);
            }
        });

        //页面主题元素 加载/渲染（recyclerview)
        RecyclerView recyclerView = findViewById(R.id.videolist_recycler);

        videoList = new ArrayList<>();


        // 设置LayoutManager，不设置无法显示（似乎是为了样式，但样式应该已经在Adapter中绑定了，不甚理解，待议）
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        // 注入Adapter配置
        mainAdapter = new MainAdapter(videoList);
        recyclerView.setAdapter(mainAdapter);

        // 接收视频信息获取完成事件（json）
        myHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg){
                switch (msg.what){
                    case 1:
                        videoList.clear();
                        videoList.addAll((Collection<? extends Video>) msg.getData().getSerializable("videoList"));
                        mainAdapter.notifyDataSetChanged();
                        swipeRefreshLayout.setRefreshing(false);
                        break;
                    default:
                        break;
                }
            }
        };
        // 下拉刷新
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(R.color.teal_200);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                String uid = preferences.getString("uid","0");
                String contain = preferences.getString("contain","1");
                String from = preferences.getString("from","1");
                MyFunction.get_video_info(myHandler,uid,contain,from);
//                swipeRefreshLayout.setRefreshing(false);
            }
        });
        MyFunction.get_video_info(myHandler,uid,contain,from);


        // 设置监听器以拿到DetailActivity返回的数据（代替StartActivityForResult）
        // 值得注意的是，该监听器仅能在OnCreate时创建，因此带来了不便（因为触发函数写在Adapter）
        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK) {
                    // 局部刷新数据
                    Intent intent_receive = result.getData();
                    assert intent_receive != null;
                    Video new_video = (Video) intent_receive.getSerializableExtra("new_video");
                    int position = intent_receive.getIntExtra("position",0);
                    videoList.set(position,new_video);
                    mainAdapter.notifyItemChanged(position);
                }
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        // 设置昵称
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String name = preferences.getString("name","未设置");
        TextView menu_name = findViewById(R.id.menu_name);
        menu_name.setText(name);
        // 刷新页面(简单起见就直接点击刷新按钮了）
        View flush_button = findViewById(R.id.flush_button);
        flush_button.performClick();
    }


    private void set_sides_click_listener(DrawerLayout drawer) {
        // 主页
        View menu_index = findViewById(R.id.menu_index);
        menu_index.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 关闭侧边栏
                drawer.closeDrawers();
            }
        });
        // 排序
        View menu_sort = findViewById(R.id.menu_sort);
        menu_sort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 关闭侧边栏
                drawer.closeDrawers();
                final String[] choiceItems = new String[]{"上传日期","时长","播放量","弹幕量"};
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle("排序依据");
                builder.setIcon(R.mipmap.ic_launcher);
                builder.setItems(choiceItems, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch(which){
                            case 0:
                                MyFunction.sort_videoList_by(videoList,"upload_time");
                                break;
                            case 1:
                                MyFunction.sort_videoList_by(videoList,"duration");
                                break;
                            case 2:
                                MyFunction.sort_videoList_by(videoList,"play_num");
                                break;
                            case 3:
                                MyFunction.sort_videoList_by(videoList,"bullet_num");
                                break;
                        }
                        mainAdapter.notifyDataSetChanged();
                    }
                });
                builder.show();
            }
        });
        // 搜索
        View menu_search = findViewById(R.id.menu_search);
        menu_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 关闭侧边栏
                drawer.closeDrawers();
                Dialog dialog = new Dialog(v.getContext());

                // searchView相关设置
                SearchView mSearchView = new SearchView(v.getContext());
                mSearchView.setIconifiedByDefault(false);//搜索图标是否显示在搜索框内
                mSearchView.setSubmitButtonEnabled(false);//设置搜索框展开时是否显示提交按钮，可不显示
                mSearchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);//让键盘的回车键设置成搜索
                mSearchView.setIconified(false);//搜索框是否展开，false表示展开
                mSearchView.setQueryHint("请输入关键字");//设置提示词
                // 触发事件
                Dialog finalDialog = dialog;
                mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    // 提交时关闭dialog
                    public boolean onQueryTextSubmit(String query) {
                        finalDialog.dismiss();
                        return false;
                    }
                    // 输入框变动时更新主页面数据
                    @Override
                    public boolean onQueryTextChange(String newText) {
                        videoList.clear();
                        videoList.addAll(MyFunction.searchFilter_videoList(newText));
                        mainAdapter.notifyDataSetChanged();
                        return false;
                    }
                });
//                AlertDialog相关设置
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle("检索");
                builder.setIcon(R.mipmap.ic_launcher);
                builder.setView(mSearchView);
                builder.setPositiveButton("搜索", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog = builder.show();
            }
        });
        // 设置
        View menu_setting = findViewById(R.id.menu_setting);
        menu_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(v.getContext(),SettingsActivity.class);
                startActivity(i);
            }
        });
        // 关于
        View menu_about = findViewById(R.id.menu_about);
        menu_about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent();
                intent.setAction("android.intent.action.VIEW");
                Uri content_url = Uri.parse("https://github.com/jimieguang/VideoDetected");
                intent.setData(content_url);
                startActivity(intent);
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
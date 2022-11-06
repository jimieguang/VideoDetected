package com.example.videodetected;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;

import com.klinker.android.sliding.SlidingActivity;

public class DetailsActivity extends SlidingActivity {
    private Intent get_intent;
    private Video Intent_video;

    @Override
    public void init(Bundle savedInstanceState) {
        get_intent = this.getIntent();
        Intent_video = (Video) get_intent.getSerializableExtra("Intent_video");
        setTitle("Test");
        setContent(R.id.video_item2);
        setPrimaryColors(
                ContextCompat.getColor(this, R.color.white),
                ContextCompat.getColor(this, R.color.black)
        );
        setImage(R.drawable.ic_about);
        setHeaderContent(R.id.video_item2);

    }
}
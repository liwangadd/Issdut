package com.liwang.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.liwang.issdut.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Nikolas on 2015/5/23.
 */
public class WelcomeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcom_activity);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                startActivity(new Intent(WelcomeActivity.this, HomeActivity.class));
                finish();
            }
        },2000);
    }
}

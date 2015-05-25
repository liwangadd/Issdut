package com.liwang.application;

import android.app.Application;
import android.content.res.AssetManager;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.liwang.issdut.R;

import junit.framework.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import im.fir.sdk.FIR;

/**
 * Created by Nikolas on 2015/5/22.
 */
public class DutApplication extends Application {

    private RequestQueue queue;
    private Properties configProperties = null;

    @Override
    public void onCreate() {
        super.onCreate();
        queue = Volley.newRequestQueue(this);
        initProperties();

//        FIR.init(this);
    }

    public RequestQueue getQueue() {
        return queue;
    }

    public void initProperties() {
        if (configProperties != null) {
            return;
        }
        InputStream in = null;
        try {
            in = getAssets().open(getResources().getString(R.string.properties));
            configProperties = new Properties();
            configProperties.load(in);
        } catch (Exception e) {
            e.printStackTrace();
            configProperties = null;
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public Properties getConfigProperties() {
        return configProperties;
    }

}

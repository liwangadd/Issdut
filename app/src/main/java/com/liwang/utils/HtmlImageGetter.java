package com.liwang.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;
import com.liwang.application.DutApplication;
import com.liwang.issdut.R;

import java.net.URL;

/**
 * Created by Nikolas on 2015/5/22.
 */
public class HtmlImageGetter implements Html.ImageGetter {

    TextView textView;
    Context context;
    onCompleteListener listener;

    public HtmlImageGetter(Context contxt, TextView textView) {
        this.context = contxt;
        this.textView = textView;
    }

    @Override
    public Drawable getDrawable(String paramString) {
        URLDrawable urlDrawable = new URLDrawable(context);
        paramString = paramString.replace("../..", "http://ssdut.dlut.edu.cn");
        ImageGetterAsyncTask getterTask = new ImageGetterAsyncTask(urlDrawable);
        getterTask.execute(paramString);
        return urlDrawable;
    }

    public class ImageGetterAsyncTask extends AsyncTask<String, Void, String> {
        URLDrawable urlDrawable;

        public ImageGetterAsyncTask(URLDrawable drawable) {
            this.urlDrawable = drawable;
        }

        @Override
        protected String doInBackground(String... params) {
            String source = params[0];
            ImageRequest imgRequest = new ImageRequest(source, new Response.Listener<Bitmap>() {
                @Override
                public void onResponse(Bitmap response) {
                    urlDrawable.setBitmap(response);
//                    textView.requestLayout();
                    if (listener != null)
                        listener.complete();
                }

            }, context.getResources().getDisplayMetrics().widthPixels, 0, ImageView.ScaleType.CENTER_INSIDE, Bitmap.Config.RGB_565, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(context, "图片加载失败", Toast.LENGTH_SHORT).show();
                }
            });
            ((DutApplication) context.getApplicationContext()).getQueue().add(imgRequest);
            return "";
        }

    }

    //得到默认的image大小
    public Rect getDefaultImageBounds(Context context) {
        int width = context.getResources().getDisplayMetrics().widthPixels;
        int height = (int) (width * 3 / 4);
        Rect bounds = new Rect(0, 0, width, height);
        return bounds;
    }

    public class URLDrawable extends BitmapDrawable {

        private Bitmap bitmap;
        private int width, height;

        public URLDrawable(Context context) {
            this.setBounds(getDefaultImageBounds(context));
            bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.logo);
            this.width=bitmap.getWidth();
            this.height=bitmap.getHeight();
            textView.requestLayout();
        }

        @Override
        public void draw(Canvas canvas) {
            this.setBounds(0, 0, width, height);
            if (bitmap != null) {
                canvas.drawBitmap(bitmap, 0, 0, null);
            }
        }

        public void setBitmap(Bitmap bitmap) {
            this.bitmap = bitmap;
            this.height = bitmap.getHeight();
            this.width = bitmap.getWidth();
        }
    }

    public interface onCompleteListener {
        void complete();
    }

    public void setListener(onCompleteListener listener) {
        this.listener = listener;
    }
}

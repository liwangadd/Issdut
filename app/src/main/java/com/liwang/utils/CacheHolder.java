package com.liwang.utils;

import android.graphics.Bitmap;
import android.view.View;

/**
 * Created by Nikolas on 2015/5/22.
 */
public class CacheHolder {

    private static Bitmap cacheBitmap;

    public static Bitmap getCacheBitmap() {
        return cacheBitmap;
    }

    public static void setCacheBitmap(Bitmap cacheBitmap) {
        CacheHolder.cacheBitmap = cacheBitmap;
    }

    public static void clearCache() {
        if(cacheBitmap!=null){
            cacheBitmap.recycle();
            cacheBitmap=null;
        }
    }

}

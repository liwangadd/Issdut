package com.liwang.utils;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.liwang.view.LoadToast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Nikolas on 2015/5/22.
 */
public class DownloadFile {

    // sd卡位置
    String sdcard = Environment.getExternalStorageDirectory() + "/";
    // 下载文件的目录
    String filepath = sdcard + "Issdut/";

    private OnDownLoadListener listener;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (listener != null) {
                if (msg.what == 0)
                    listener.success((File) msg.getData().getSerializable("file"));
                else
                    listener.fail();
            }
        }
    };

    public void downLoad(final String urlString) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    URL url = new URL(urlString);
                    HttpURLConnection connection = (HttpURLConnection) url
                            .openConnection();
                    connection.setRequestProperty("Referer",
                            "http://ssdut.dlut.edu.cn/");
                    connection
                            .setRequestProperty("UserAgent",
                                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; flashget)");
                    File dirFile = new File(filepath);
                    if (!dirFile.exists()) {
                        dirFile.mkdir();
                    }
                    String fileName = getFileName(connection);
                    File file = new File(filepath + "/" + fileName);
                    if (file.exists()) {
                        Message message = new Message();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("file", file);
                        message.what = 0;
                        message.setData(bundle);
                        handler.sendMessage(message);
                        return;
                    }
                    file.createNewFile();
                    InputStream istream = connection.getInputStream();
                    OutputStream output = new FileOutputStream(file);
                    byte[] buffer = new byte[1024];
                    int length = -1;
                    while ((length = istream.read(buffer)) != -1) {
                        output.write(buffer, 0, length);
                    }
                    output.flush();
                    output.close();
                    istream.close();
                    Message message = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("file", file);
                    message.what = 0;
                    message.setData(bundle);
                    handler.sendMessage(message);
                } catch (Exception e) {
                    handler.sendEmptyMessage(1);
                }

            }
        }).start();
    }

    private String getFileName(HttpURLConnection conn)
            throws UnsupportedEncodingException {
        String filename = null;
        for (int i = 0; ; i++) {
            String mine = conn.getHeaderField(i);
            if (mine == null)
                break;
            if ("content-disposition".equals(conn.getHeaderFieldKey(i)
                    .toLowerCase())) {
                Matcher m = Pattern.compile(".*filename=(.*)").matcher(
                        mine.toLowerCase());
                if (m.find()) {
                    filename = m.group(1);
                    return URLDecoder.decode(filename, "UTF-8");
                }
            }
        }
        if (filename == null) {
            filename = UUID.randomUUID() + ".tmp";// 创建临时文件名
        }
        return filename;
    }

    public void setOnDownLoadListener(OnDownLoadListener listener) {
        this.listener = listener;
    }

    public interface OnDownLoadListener {
        void success(File file);

        void fail();
    }

}

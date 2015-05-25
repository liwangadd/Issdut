package com.liwang.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import java.io.File;

/**
 * Created by Nikolas on 2015/5/22.
 */
public class OpenFile {
    private static String[][] MIME_MapTable = {
            {".doc", "application/msword"},
            {".docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"},
            {".xls", "application/vnd.ms-excel"},
            {".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"},
            {".ppt", "application/vnd.ms-powerpoint"},
            {".pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation"},
            {".pdf", "application/pdf"}
    };

    public static void openFile(Context context,File file){
        Intent intent=new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        String type=getMIMEType(file);
        intent.setDataAndType(Uri.fromFile(file),type);
        try {
            context.startActivity(intent);
        }catch(Exception e){
            Toast.makeText(context,"sorry文件不能打开，请安装相关应用",Toast.LENGTH_SHORT).show();
        }
    }

    private static String getMIMEType(File file) {

        String type = "*/*";
        String fName = file.getName();
        //获取文件拓展名中点的位置
        int dotIndex = fName.lastIndexOf(".");
        if (dotIndex < 0) {
            return type;
        }
        //获取文件拓展名
        String end = fName.substring(dotIndex, fName.length()).toLowerCase();
        if (end == "") return type;
        //查找mime对照表，根据拓展名得到mime类型
        for (int i = 0; i < MIME_MapTable.length; i++) {
            if (end.equals(MIME_MapTable[i][0]))
                type = MIME_MapTable[i][1];
        }
        return type;
    }
}

package com.htetznaing.magiz;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.OpenableColumns;
import android.webkit.MimeTypeMap;

public class Constants {
    public static String getMimeTypeFromExtension(String extension) {
        String type = null;
        if (extension != null) {
            if (extension.startsWith("."))
                extension = extension.substring(1);
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    public static boolean isAfterAndroid10(){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R;
    }

    public static boolean isAfterAndroid12(){
        return Build.VERSION.SDK_INT >= 32;
    }

    public static String getBaseName(String fileName){
        return fileName.substring(0,fileName.lastIndexOf("."));
    }

    public static String getFileName(Context context,Uri uri){
        Cursor returnCursor = context.getContentResolver().query(uri, null, null, null, null);
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        String fileName = returnCursor.getString(nameIndex);
        returnCursor.close();
        return fileName;
    }


}

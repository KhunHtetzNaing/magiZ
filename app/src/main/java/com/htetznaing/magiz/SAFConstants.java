package com.htetznaing.magiz;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.UriPermission;
import android.net.Uri;
import android.os.Build;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.DocumentsContract;

import androidx.annotation.RequiresApi;
import androidx.documentfile.provider.DocumentFile;

import java.io.File;
import java.util.List;

public class SAFConstants {
    @RequiresApi(api = Build.VERSION_CODES.R)
    public static Uri createInitFolderURI(StorageManager storageManager, String dir){
        if (dir == null || dir.isEmpty())
            return null;

        StorageVolume storageVolume = storageManager.getPrimaryStorageVolume();
        Uri uri = storageVolume.createOpenDocumentTreeIntent().getParcelableExtra(DocumentsContract.EXTRA_INITIAL_URI);
        if (uri==null)
            return null;

        if (new File(storageVolume.getDirectory(),dir).exists()){
            if (dir.contains("/"))
                dir = dir.replace("/","%2F");
            String scheme = uri.toString();
            scheme = scheme.replace("/root/","/document/");
            scheme += "%3A" + dir;
            return Uri.parse(scheme);
        }
        return uri;
    }

    public static DocumentFile findFolder(DocumentFile documentFile, String name){
        if (documentFile==null)
            return null;
        DocumentFile temp = documentFile.findFile(name);
        if (temp==null)
            temp = documentFile.createDirectory(name);
        return temp;
    }

    @SuppressLint("WrongConstant")
    @RequiresApi(api = Build.VERSION_CODES.R)
    public static void saveDirectory(Uri uri, Context context, String name){
        if (uri==null)
            return;
        Uri existing = loadSavedDirectory(context,name);
        if (existing!=null)
            context.getContentResolver().releasePersistableUriPermission(existing, 3);
        context.getContentResolver().takePersistableUriPermission(uri, 3);
    }

    @SuppressLint("WrongConstant")
    @RequiresApi(api = Build.VERSION_CODES.R)
    public static Uri loadSavedDirectory(Context context,String name){
        List<UriPermission> list = context.getContentResolver().getPersistedUriPermissions();
        if (!list.isEmpty()){
            for (UriPermission uri:list) {
                if (uri.getUri().getPath().endsWith(name) && uri.isWritePermission()) {
                    DocumentFile dir = DocumentFile.fromTreeUri(context,uri.getUri());
                    if (dir!=null && dir.exists())
                        return uri.getUri();
                    context.getContentResolver().releasePersistableUriPermission(uri.getUri(), 3);
                }
            }
        }
        return null;
    }
}

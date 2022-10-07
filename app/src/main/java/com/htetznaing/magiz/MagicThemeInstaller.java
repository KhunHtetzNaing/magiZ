package com.htetznaing.magiz;

import android.net.Uri;

import androidx.appcompat.app.AppCompatActivity;

import com.htetznaing.magiz.Async.TaskRunner;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MagicThemeInstaller {
    private AppCompatActivity activity;

    public MagicThemeInstaller(AppCompatActivity activity) {
        this.activity = activity;
    }

    public void install(Uri uri){
        if (Constants.isAfterAndroid10()){

        }

        new TaskRunner().executeAsync(new Callable<String>() {
            private boolean isValidThemeFile(InputStream stream) throws IOException {
                ZipInputStream zis = new ZipInputStream(stream);
                for (ZipEntry e = zis.getNextEntry(); e != null; e = zis.getNextEntry()) {
                    if (e.getName().equals("description.xml")) {
                        zis.close();
                        return true;
                    }
                }
                zis.close();
                return false;
            }
            @Override
            public String call() throws Exception {
                InputStream stream = activity.getContentResolver().openInputStream(uri);
                if (isValidThemeFile(stream)){

                }else return "Corrupted theme file!";
                return null;
            }
        }, new TaskRunner.Callback<String>() {
            @Override
            public void onTaskCompleted(String result) {

            }

            @Override
            public void onTaskFailure(String error) {

            }
        });
    }
}

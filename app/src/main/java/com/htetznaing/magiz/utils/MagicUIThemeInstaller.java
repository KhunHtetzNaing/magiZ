package com.htetznaing.magiz.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.provider.DocumentsContract;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;
import com.htetznaing.magiz.Async.TaskRunner;
import com.htetznaing.magiz.Constants;
import com.htetznaing.magiz.R;
import com.htetznaing.magiz.SAFConstants;
import com.htetznaing.magiz.ZFileExtensions;
import com.htetznaing.magiz.databinding.ProgressDialogBinding;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.concurrent.Callable;

import io.noties.markwon.Markwon;

public class MagicUIThemeInstaller {
    private static final String HONOR_THEME_PKG = "com.hihonor.android.thememanager";
    private final AppCompatActivity activity;
    private final String ROOT_DIR_NAME = "Honor",THEME_DIR_NAME = "Themes",
            ANDROID_DATA = "Android/data",
            ANDROID_13_DATA = ANDROID_DATA+File.separator+HONOR_THEME_PKG,
            afterAndroid10Path = String.format("files/Download/%s/%s",ROOT_DIR_NAME,THEME_DIR_NAME);

    private final File defaultRootPath = new File(Environment.getExternalStorageDirectory(),ROOT_DIR_NAME+File.separator+THEME_DIR_NAME);

    private ActivityResultLauncher<Intent> safResult;
    private Uri theme;
    private DocumentFile honorThemeDocument;

    public MagicUIThemeInstaller(AppCompatActivity activity) {
        this.activity = activity;
        if (Constants.isAfterAndroid10()){
            safResult = activity.registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (Constants.isAfterAndroid10() && result.getResultCode() == Activity.RESULT_OK) {
                            if (result.getData() != null) {
                                String path = result.getData().getData().getPath().split(":")[1];

                                if (Constants.isAfterAndroid12() ? path.equals(ANDROID_13_DATA) : path.equals(ANDROID_DATA)) {
                                    SAFConstants.saveDirectory(result.getData().getData(),activity,Constants.isAfterAndroid12() ? ANDROID_13_DATA : ANDROID_DATA);
                                } else {
                                    new AlertDialog.Builder(activity)
                                            .setTitle(R.string.sorry)
                                            .setMessage(Markwon.create(activity).toMarkdown(activity.getString(R.string.saf_wrong_select,Constants.isAfterAndroid12() ? ANDROID_13_DATA : ANDROID_DATA,path)))
                                            .setPositiveButton(R.string.try_again, (dialog, which) -> install(theme))
                                            .show();
                                    return;
                                }
                                install(theme);
                            } else
                                Toast.makeText(activity, R.string.unknown_error, Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        }
    }

    public void install(Uri theme){
        this.theme = theme;
        // Prepare DocumentFile for Android 10+
        if (Constants.isAfterAndroid10()){
            Uri uri = SAFConstants.loadSavedDirectory(activity,Constants.isAfterAndroid12() ? ANDROID_13_DATA : ANDROID_DATA);
            if (uri!=null){
                honorThemeDocument = DocumentFile.fromTreeUri(activity,uri);
                if (!Constants.isAfterAndroid12())
                    honorThemeDocument = SAFConstants.findFolder(honorThemeDocument,HONOR_THEME_PKG);
                for (String dir:afterAndroid10Path.split("/"))
                    honorThemeDocument = SAFConstants.findFolder(honorThemeDocument,dir);
            }
            if (honorThemeDocument==null){
                requestPermission();
                return;
            }
        }

        AlertDialog progress = new AlertDialog.Builder(activity)
                .setView(ProgressDialogBinding.inflate(LayoutInflater.from(activity)).getRoot())
                .setCancelable(false)
                .show();
        new TaskRunner().executeAsync(new Callable<Boolean>() {
            private OutputStream createOutputStream(String name) throws FileNotFoundException {
                DocumentFile docHWT = honorThemeDocument.findFile(name);
                if (docHWT != null && docHWT.exists())
                    docHWT.delete();
                docHWT = honorThemeDocument.createFile("*/*", name);
                return activity.getContentResolver().openOutputStream(docHWT.getUri());
            }

            @Override
            public Boolean call() throws Exception {
                String name = Constants.getBaseName(Constants.getFileName(activity,theme))+ ZFileExtensions.HNT;
                InputStream inputStream = activity.getContentResolver().openInputStream(theme);
                OutputStream outputStream = Constants.isAfterAndroid10() ? createOutputStream(name) : new FileOutputStream(new File(defaultRootPath,name));
                StreamUtils.writeInputStream(inputStream,outputStream);
                return true;
            }
        }, new TaskRunner.Callback<Boolean>() {
            @Override
            public void onTaskCompleted(Boolean result) {
                progress.dismiss();
                if (result){
                    new AlertDialog.Builder(activity)
                            .setTitle(R.string.congratulations)
                            .setMessage(R.string.success_msg)
                            .setPositiveButton(R.string.open_theme_manager, (dialog, which) -> openThemeManager(activity))
                            .show();
                }else Toast.makeText(activity, R.string.unknown_error, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onTaskFailure(String error) {
                progress.dismiss();
                Toast.makeText(activity, ""+error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @SuppressLint("WrongConstant")
    private void requestPermission() {
        StorageManager storageManager = (StorageManager) Objects.requireNonNull(activity).getSystemService(Context.STORAGE_SERVICE);

        File dir = new File(storageManager.getPrimaryStorageVolume().getDirectory(),ANDROID_13_DATA);

        String msg = dir.exists() ?
                activity.getString(R.string.saf_allow_note,Constants.isAfterAndroid12() ? HONOR_THEME_PKG : ANDROID_DATA) :
                activity.getString(R.string.saf_create_and_allow_note,HONOR_THEME_PKG,HONOR_THEME_PKG);

        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle(R.string.important)
                .setMessage(Markwon.builder(activity).build().toMarkdown(msg))
                .setPositiveButton(R.string.get_started, (dialog12, which) -> {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                    intent.setFlags(195);
                    intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, SAFConstants.createInitFolderURI(
                            storageManager,(Constants.isAfterAndroid12() && dir.exists()) ? ANDROID_13_DATA : ANDROID_DATA));
                    try {
                        safResult.launch(intent);
                    }catch (Exception e){
                        Toast.makeText(activity, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNeutralButton(R.string.copy,null)
                .create();

        dialog.setOnShowListener(dialog1 -> dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener(v -> {
            if (ClipboardUtils.put(activity,HONOR_THEME_PKG))
                Toast.makeText(activity, R.string.toast_copied_to_clipboard, Toast.LENGTH_SHORT).show();
        }));
        dialog.show();
    }

    public static void openThemeManager(Activity activity) {
        Intent intent = activity.getPackageManager().getLaunchIntentForPackage(HONOR_THEME_PKG);
        try {
            if (intent==null)
                return;
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

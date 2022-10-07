package com.htetznaing.magiz;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.htetznaing.magiz.databinding.ActivityMainBinding;
import com.htetznaing.magiz.utils.MagicUIThemeInstaller;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private ActivityResultLauncher<Intent> filePickerResult;
    private Uri selected;
    private MagicUIThemeInstaller themeInstaller;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        filePickerResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::handleResult);
        themeInstaller = new MagicUIThemeInstaller(this);
        binding.pick.setOnClickListener(v -> pickFile(ZFileExtensions.HNT,ZFileExtensions.HWT,ZFileExtensions.ZIP));
        binding.apply.setOnClickListener(v -> {
            if (selected!=null){
                themeInstaller.install(selected);
            }else Snackbar.make(binding.apply, R.string.no_select_notice,Snackbar.LENGTH_SHORT).show();
        });

        binding.dev.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                String id = "100011402500763";
                try {
                    intent.setData(Uri.parse(String.format("fb://profile/%s",id)));
                    startActivity(intent);
                } catch (Exception e) {
                    intent.setData(Uri.parse("https://facebook.com/"+id));
                    startActivity(intent);
                }
        });
    }

    private void handleResult(ActivityResult result){
        if (result.getResultCode() == Activity.RESULT_OK && result.getData()!=null) {
            selected = result.getData().getData();
            binding.pick.setText(Constants.getFileName(this,selected));
        }
    }

    private void pickFile(String... extensions){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        if (extensions.length > 0){
            List<String> mimetypes = new ArrayList<>();
            for (String ext:extensions){
                String mime = Constants.getMimeTypeFromExtension(ext);
                if (mime!=null) {
                    mimetypes.add(mime);
                }
                if (ext.equalsIgnoreCase(ZFileExtensions.ZIP)) {
                    mimetypes.add("application/x-zip");
                    mimetypes.add("multipart/x-zip");
                    mimetypes.add("application/zip");
                    mimetypes.add("application/mix-archive");
                    mimetypes.add("application/java-archive");
                    mimetypes.add("application/x-zip-compressed");
                    mimetypes.add("application/x-compressed");
                    mimetypes.add("application/x-compress");
                    mimetypes.add("application/x-winzip");
                    mimetypes.add("application/octet-stream");
                }
            }
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes.toArray(new String[0]));
        }
        filePickerResult.launch(intent);
    }
}
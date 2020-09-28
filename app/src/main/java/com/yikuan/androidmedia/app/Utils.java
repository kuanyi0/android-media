package com.yikuan.androidmedia.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.util.Log;

import androidx.core.content.FileProvider;

import java.io.File;

/**
 * @author yikuan
 * @date 2020/09/22
 */
public class Utils {
    private static final String TAG = "Utils";
    private static final String FILE_PROVIDER_AUTHORITY = "com.yikuan.androidmedia.app.fileprovider";

    public static void selectFile(Activity activity, String dirPath) {
        if (dirPath.isEmpty()) {
            return;
        }
        File dir = new File(dirPath);
        selectFile(activity, dir);
    }

    public static void selectFile(Activity activity, File dir) {
        if (dir == null) {
            return;
        }
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(activity, FILE_PROVIDER_AUTHORITY, dir);
        } else {
            uri = Uri.fromFile(dir);
        }
        Log.d(TAG, "dir: " + uri);
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setDataAndType(uri, "*/*");
        try {
            activity.startActivityForResult(intent, 0);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "select file error");
        }
    }

    @SuppressLint("NewApi")
    public static String getPathFromUri(Uri uri) {
        String documentId = DocumentsContract.getDocumentId(uri);
        String[] split = documentId.split(":");
        return Environment.getExternalStorageDirectory() + "/" + split[1];
    }
}

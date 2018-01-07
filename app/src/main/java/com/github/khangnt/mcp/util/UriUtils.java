package com.github.khangnt.mcp.util;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;

import java.io.File;
import java.util.List;

public class UriUtils {

    private static final String[] NAME_PROJECTION = {OpenableColumns.DISPLAY_NAME};
    private static final String DOCUMENTS_AUTHORITY = "com.android.externalstorage.documents";
    private static final String DOWNLOADS_AUTHORITY = "com.android.providers.downloads.documents";
    private static final String MEDIA_AUTHORITY = "com.android.providers.media.documents";
    private static final String PATH_TREE = "tree";

    private static final String SD_CARD_PATH = guessSdCardPath();

    public static String getDisplayName(Context context, Uri uri) {
        Cursor cursor = null;

        try {
            cursor = context.getContentResolver().query(uri, NAME_PROJECTION, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int nameColumn = cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME);
                String name = cursor.getString(nameColumn);

                if (name.trim().isEmpty()) {
                    return getFileName(uri);
                } else {
                    return name;
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return getFileName(uri);
    }

    @SuppressLint("SdCardPath")
    private static String guessSdCardPath() {
        if (new File("/storage/sdcard/").exists()) {
            return "/storage/sdcard/";
        }
        if (new File("/sdcard/").exists()) {
            return "/sdcard/";
        }
        if (new File("/mnt/sdcard/").exists()) {
            return "/mnt/sdcard/";
        }
        if (new File("/storage/sdcard0/").exists()) {
            return "/storage/sdcard0/";
        }
        if (new File("/storage/sdcard1/").exists()) {
            return "/storage/sdcard1/";
        }
        if (new File("/storage/extSdCard/").exists()) {
            return "/storage/extSdCard/";
        }
        return null;
    }

    public static String getFileName(Uri uri) {
        List<String> segments = uri.getPathSegments();
        return (segments.isEmpty()) ? "" : segments.get(segments.size() - 1);
    }

    public static String getPathFromUri(Context context, Uri contentUri) {
        if ("file".equals(contentUri.getScheme())) {
            return contentUri.getPath();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            String docProviderPath = getPathFromDocumentProviderUri(context, contentUri);
            if (docProviderPath != null && !docProviderPath.trim().isEmpty()) {
                return docProviderPath;
            }
        }

        if ("content".equals(contentUri.getScheme())) {
            return getPathFromGeneralUri(context, contentUri);
        }

        return null;
    }

    public static String getDirectoryPathFromUri(Uri uri) {
        if ("file".equals(uri.getScheme())) {
            return uri.getPath();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                && isTreeUri(uri)) {
            String treeId = getTreeDocumentId(uri);
            String[] split = treeId.split(":");
            String type = split[0];
            String subPath = split.length == 2 ? split[1] : "";

            if ("primary".equalsIgnoreCase(type)) {
                return Environment.getExternalStorageDirectory() + "/" + subPath;
            } else {
                File file = new File("/storage/" + type);
                if (file.canRead() && file.exists())
                    return new File(file, subPath).getAbsolutePath();
                file = Environment.getExternalStorageDirectory();
                while (file.getParent() != null) {
                    File test = new File(file.getParent(), type);
                    if (test.canRead() && test.exists())
                        return new File(test, subPath).getAbsolutePath();
                    file = file.getParentFile();
                }
                if (SD_CARD_PATH != null && type.matches("^\\w{4}-\\w{4}$")) {
                    file = new File(SD_CARD_PATH, subPath);
                    if (file.canRead() && file.exists()) return file.getAbsolutePath();
                }
                return null;
            }
        }
        return null;
    }


    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static String getPathFromDocumentProviderUri(Context context, Uri uri) {
        if (!DocumentsContract.isDocumentUri(context, uri)) {
            return null;
        }

        if (DOCUMENTS_AUTHORITY.equals(uri.getAuthority())) {
            // External storage provider
            String docId = DocumentsContract.getDocumentId(uri);
            String[] split = docId.split(":");
            String type = split[0];
            String subPath = split.length == 2 ? split[1] : "";

            if ("primary".equalsIgnoreCase(type)) {
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            } else {
                File file = new File("/storage/" + type);
                if (file.canRead() && file.exists())
                    return new File(file, subPath).getAbsolutePath();
                file = Environment.getExternalStorageDirectory();
                while (file.getParent() != null) {
                    File test = new File(file.getParent(), type);
                    if (test.canRead() && test.exists())
                        return new File(test, subPath).getAbsolutePath();
                    file = file.getParentFile();
                }
                if (SD_CARD_PATH != null && type.matches("^\\w{4}-\\w{4}$")) {
                    file = new File(SD_CARD_PATH, subPath);
                    if (file.canRead() && file.exists()) return file.getAbsolutePath();
                }
                return null;
            }
        } else if (DOWNLOADS_AUTHORITY.equals(uri.getAuthority())) {
            // Downloads provider
            String id = DocumentsContract.getDocumentId(uri);
            Uri contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

            return getPathFromGeneralUri(context, contentUri);
        } else if (MEDIA_AUTHORITY.equals(uri.getAuthority())) {
            // Media Provider – assumes that only music URIs will be passed in
            String id = DocumentsContract.getDocumentId(uri).split(":")[1];
            return getPathFromMediaStore(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
        } else {
            return null;
        }
    }

    public static String getTreeDocumentId(Uri documentUri) {
        final List<String> paths = documentUri.getPathSegments();
        if (paths.size() >= 2 && PATH_TREE.equals(paths.get(0))) {
            return paths.get(1);
        }
        throw new IllegalArgumentException("Invalid URI: " + documentUri);
    }

    public static boolean isTreeUri(Uri uri) {
        final List<String> paths = uri.getPathSegments();
        return (paths.size() >= 2 && PATH_TREE.equals(paths.get(0)));
    }

    private static String getPathFromMediaStore(Context context, Uri uri, String id) {
        String[] projection = {MediaStore.MediaColumns.DATA};
        String selection = MediaStore.MediaColumns._ID + " = ?";
        String[] selectionArgs = {id};

        Cursor cur = context.getContentResolver().query(uri, projection, selection,
                selectionArgs, null);

        if (cur == null) {
            return null;
        }

        String path = null;
        if (cur.moveToFirst()) {
            path = cur.getString(cur.getColumnIndex(MediaStore.MediaColumns.DATA));
        }
        cur.close();

        return path;
    }

    private static String getPathFromGeneralUri(Context context, Uri uri) {
        String[] projection = {MediaStore.MediaColumns.DATA};
        Cursor cur = null;

        try {
            cur = context.getContentResolver().query(uri, projection, null, null, null);
            if (cur != null && cur.moveToFirst()) {
                return cur.getString(cur.getColumnIndex(MediaStore.MediaColumns.DATA));
            } else {
                return null;
            }
        } catch (Throwable ignore) {
            ignore.printStackTrace();
        } finally {
            if (cur != null) {
                cur.close();
            }
        }
        return null;
    }
}

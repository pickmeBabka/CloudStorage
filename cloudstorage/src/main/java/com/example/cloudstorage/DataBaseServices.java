package com.example.cloudstorage;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class DataBaseServices {
    private static SQLiteDatabase db;

    public static void setDb(SQLiteDatabase db) {
        DataBaseServices.db = db;
        try {
            db.execSQL("CREATE TABLE IF NOT EXISTS files(fileid TEXT, isUploaded INTEGER NOT NULL, weight INTEGER, PATH TEXT NOT NULL UNIQUE, AlbumName TEXT NOT NULL, part INEGER, isDownloaded INTEGER, thumbFileId TEXT)");
        } catch (SQLException ignored) {
        }
        Log.i("db", "setDb: " + db.getPath());
    }

    public static void MarkFileAsDownloaded(String fileId)
    {
        db.execSQL("UPDATE files SET isDownloaded=1 WHERE fileId='" + fileId + "'");
    }

    public static void AddFileId(String fileId, String path)
    {
        Log.i("Uploaded", "AddFileId: " + path);
        ContentValues cv = new ContentValues();
        cv.put("fileId", fileId);
        Log.i("Uploaded", "AddFileId: " + db.update("files", cv, "path='" + path + "'", null));
    }

    public static void MarkFileAsUploaded(String fileId)
    {
        ContentValues cv = new ContentValues();
        cv.put("isUploaded", 1);
        Log.i("Uploaded", "MarkFileAsUploaded: " + db.update("files", cv, "fileId='" + fileId + "'", null));


    }

    public static String[] GetUnsyncedFiles()
    {
        Cursor c = db.rawQuery("SELECT * FROM files WHERE isUploaded<1", null);
        ArrayList<String> unsyced = new ArrayList<>();

        if(c.moveToFirst())
        {
            unsyced.add(c.getString(Columns.path.ordinal()));
            while(c.moveToNext())
            {
                unsyced.add(c.getString(Columns.path.ordinal()));
            }
        }
        return unsyced.toArray(new String[unsyced.size()]);
    }

    public static boolean IsFileDownloadedByFileId(String fileId)
    {
        Cursor c = db.rawQuery("SELECT * FROM files WHERE fileid='" + fileId + "' AND isDownloaded=1" , null);
        return c.moveToFirst();
    }
    public static boolean IsFileDownloadedByPath(String path)
    {
        Cursor c = db.rawQuery("SELECT * FROM files WHERE path='" + path + "' AND isDownloaded=1" , null);
        return c.moveToFirst();
    }
    public static boolean IsFileUploadedByFileId(String fileId)
    {
        Cursor c = db.rawQuery("SELECT * FROM files WHERE fileid='" + fileId + "' AND isDownloaded=1" , null);
        return c.moveToFirst();
    }
    public static boolean IsFileUploadedByPath(String path)
    {
        Cursor c = db.rawQuery("SELECT * FROM files WHERE path='" + path + "' AND isUploaded=1" , null);
        return c.moveToFirst();
    }

    public static void AddFile(@NonNull StorageFile... files)
    {
        for (StorageFile f:
             files) {

            ContentValues cv = new ContentValues();
            if(f.FileId != null) cv.put(Columns.fileId.name(), f.FileId);
            if(f.path != null) cv.put(Columns.path.name(), f.path);
            if(f.thumbFileId != null) cv.put(Columns.thumbFileId.name(), f.thumbFileId);
            if(f.AlbumName != null) cv.put(Columns.AlbumName.name(), f.AlbumName);
            if(f.weight != null) cv.put(Columns.weight.name(), f.weight);
            cv.put(Columns.isDownloaded.name(), f.isDownloaded);
            cv.put(Columns.isUploaded.name(), f.isUploaded);
            db.insert("files", null, cv);
        }
    }

    public static StorageFile GetFileByPath(String Path)
    {
        Cursor c = db.rawQuery("SELECT * FROM files WHERE path='" + Path + "'" , null);
        if (c.moveToFirst())
        {
            String[] pathParts = c.getString(Columns.path.ordinal()).split("/");
            StorageFile res = new StorageFile(
                    c.isNull(Columns.fileId.ordinal()) ? c.getString(Columns.fileId.ordinal()) : null,
                    c.getInt(Columns.isUploaded.ordinal())== 1,
                    !c.isNull(Columns.isDownloaded.ordinal()),
                    c.isNull(Columns.weight.ordinal()) ? c.getLong(Columns.weight.ordinal()) : null,
                    c.getString(Columns.path.ordinal()),
                    pathParts[pathParts.length - 2],
                    c.isNull(Columns.part.ordinal()) ? c.getInt(Columns.part.ordinal()) : null,
                    c.isNull(Columns.thumbFileId.ordinal()) ? c.getString(Columns.thumbFileId.ordinal()) : null);
            return res;
        }
        c.close();
        return null;
    }

    public static StorageFile GetFileById(String fileId)
    {
        Cursor c = db.rawQuery("SELECT * FROM files WHERE fileid='" + fileId + "'" , null);
        if (c.moveToFirst())
        {
            String[] pathParts = c.getString(Columns.path.ordinal()).split("\\\\");
            StorageFile res = new StorageFile(c.isNull(Columns.fileId.ordinal()) ? c.getString(Columns.fileId.ordinal()) : null,
                    c.getInt(Columns.isUploaded.ordinal())== 1,
                    !c.isNull(Columns.isDownloaded.ordinal()),
                    c.isNull(Columns.weight.ordinal()) ? c.getLong(Columns.weight.ordinal()) : null,
                    c.getString(Columns.path.ordinal()),
                    pathParts[pathParts.length - 2],
                    c.isNull(Columns.part.ordinal()) ? c.getInt(Columns.part.ordinal()) : null,
                    c.isNull(Columns.thumbFileId.ordinal()) ? c.getString(Columns.thumbFileId.ordinal()) : null);
            return res;
        }
        c.close();
        return null;
    }

    public static  boolean IsFileExistsByFileId(String fileId)
    {
        @SuppressLint("Recycle") Cursor c = db.rawQuery("SELECT * FROM files WHERE fileid='" + fileId + "'" , null);
        return  (c.moveToFirst());
    }
    public static  boolean IsFileExistsByPath(String path)
    {
        @SuppressLint("Recycle") Cursor c = db.rawQuery("SELECT * FROM files WHERE path='" + path + "'" , null);
        return  (c.moveToFirst());
    }

    public static void EditStorageFileByPath(String path, String category, String value, boolean isCategoryText)
    {

        String builder = "UPDATE files " + category + " =" +
                (isCategoryText ? "'" : "") + value + (isCategoryText ? "'" : "") +
                "WHERE path='" + path + "'";

        db.execSQL(builder);
    }

    public static class StorageFile
    {
        @Nullable
        String FileId;
        boolean isUploaded;
        boolean isDownloaded;
        @Nullable
        Long weight;
        String path;
        String AlbumName;
        @Nullable
        Integer part;
        String thumbFileId;

        public StorageFile(@Nullable String FileId, boolean isUploaded, boolean isDownloaded, @Nullable Long weight, String path, String AlbumName, @Nullable Integer part, String thumbFileId)
        {
            this.FileId = FileId;
            this.isUploaded = isUploaded;
            this.isDownloaded = isDownloaded;
            this.AlbumName = AlbumName;
            this.part = part;
            this.path = path;
            this.weight = weight;
            this.thumbFileId = thumbFileId;
        }
    }

    public enum Columns
    {
        fileId,
        isUploaded,
        weight,
        path,
        AlbumName,
        part,
        isDownloaded,
        thumbFileId
    }
}

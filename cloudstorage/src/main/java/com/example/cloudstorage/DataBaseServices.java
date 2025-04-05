package com.example.cloudstorage;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DataBaseServices {
    private static SQLiteDatabase db;

    public static void setDb(SQLiteDatabase db) {
        DataBaseServices.db = db;
        try {
            db.execSQL("CREATE TABLE IF NOT EXISTS files(fileid TEXT, isUploaded INTEGER NOT NULL, weight INTEGER, PATH TEXT NOT NULL UNIQUE, AlbumName TEXT NOT NULL, part INEGER, isDownloaded INTEGER)");
        } catch (SQLException ignored) {

        }
        Log.i("db", "setDb: " + db.getPath());
    }

    public static void MarkFileAsDownloaded(String fileId)
    {
        db.execSQL("UPDATE files SET isDownloaded=1 WHERE fileId='" + fileId + "'");
    }

    public static boolean IsFileDownloaded(String fileId)
    {
        Cursor c = db.rawQuery("SELECT * FROM files WHERE fileid='" + fileId + "'" , null);
        if (c.moveToFirst())
        {
            return !c.isNull(Columns.isDownloaded.ordinal());
        }
        return false;
    }

    public static void AddFile(@NonNull StorageFile... files)
    {
        StringBuilder builderColumns = new StringBuilder(), builderValues = new StringBuilder(), builderSQL = new StringBuilder();
        StorageFile file;
        for (int i = 0; i < files.length; i++) {
            file = files[i];
            builderColumns.append("( isUploaded, path, AlbumName");
            builderValues.append(" ( ").append(file.isUploaded).append(", '").append(file.path).append("', '").append(file.AlbumName).append("'");
            if(file.weight != null){
                builderColumns.append(", weight");
                builderValues.append(", ").append(file.weight);
            }
            if(file.part != null)
            {
                builderColumns.append(", part");
                builderValues.append(", ").append(file.part);
            }
            if(file.FileId != null)
            {
                builderColumns.append(", fileid");
                builderValues.append(", '").append(file.FileId).append("'");
            }
            builderColumns.append(")");
            builderValues.append(")");

            builderSQL.append("INSERT INTO files").append(builderColumns).append(" VALUES ").append(builderValues).append(";");
        }
        db.execSQL(builderSQL.toString());
    }

    public static StorageFile GetFileByPath(String Path)
    {
        Cursor c = db.rawQuery("SELECT * FROM files WHERE path='" + Path + "'" , null);
        if (c.moveToFirst())
        {
            String[] pathParts = c.getString(Columns.path.ordinal()).split("\\\\");
            StorageFile res = new StorageFile(c.isNull(Columns.fileId.ordinal()) ? c.getString(Columns.fileId.ordinal()) : null,
                    c.getInt(Columns.isUploaded.ordinal())== 1,
                    !c.isNull(Columns.isDownloaded.ordinal()),
                    c.isNull(Columns.weight.ordinal()) ? c.getLong(Columns.weight.ordinal()) : null,
                    c.getString(Columns.path.ordinal()),
                    pathParts[pathParts.length - 2],
                    c.isNull(Columns.part.ordinal()) ? c.getInt(Columns.part.ordinal()) : null);
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
                    c.isNull(Columns.part.ordinal()) ? c.getInt(Columns.part.ordinal()) : null);
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

        public StorageFile(@Nullable String FileId, boolean isUploaded, boolean isDownloaded, @Nullable Long weight, String path, String AlbumName, @Nullable Integer part)
        {
            this.FileId = FileId;
            this.isUploaded = isUploaded;
            this.isDownloaded = isDownloaded;
            this.AlbumName = AlbumName;
            this.part = part;
            this.path = path;
            this.weight = weight;
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
        isDownloaded
    }
}

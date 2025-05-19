package com.example.cloudstorage;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

public class MediaViewActivity extends AppCompatActivity {
    ImageView img;
    Button btnSend;
    Random rnd = new Random();
    protected String SessionId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_media_view);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        img = findViewById(R.id.img);
        btnSend = findViewById(R.id.btnSend);
        String SessionId = getSharedPreferences(helper_class.Constatns.Cash, MODE_PRIVATE).getString(helper_class.Constatns.CashSessionId, "");

        Intent intent = getIntent();
        String path = intent.getStringExtra("media");

        if(intent.getBooleanExtra("IsNeedToDownload", false))
        {
            String fileId = intent.getStringExtra("FileId");
            String exst = intent.getStringExtra("exst");
            path = fileId + "." + exst;

            if(!DataBaseServices.IsFileDownloadedByFileId(fileId))
            {
                File f = new File(getFilesDir(), path);
                path = f.getPath();
                if(f.exists()) f.delete();

                try {
                    FileOutputStream fileOutputStream = new FileOutputStream(f);

                    CloudServicesApi.UserFileInfo info = CloudServicesApi.GetFileInfo(fileId);
                    int parts = 0;
                    while(parts < info.parts)
                    {
                        fileOutputStream.write(CloudServicesApi.DownloadPart(fileId, parts));
                        parts++;
                        Log.i("download", "onCreate: " + parts);
                    }

                    DataBaseServices.MarkFileAsDownloaded(fileId);
                } catch (Exception e) {
                    Log.e("download" + e.getClass().getName(), "onCreate: ", e);
                }
            }
            String finalPath1 = path;
            runOnUiThread(()->
            {
                if (Objects.equals(finalPath1, "")) finish();
                Glide.with(getApplicationContext()).load(finalPath1).into(img);
                String finalPath = finalPath1;
            });
        }else {
            if (Objects.equals(path, "")) finish();
            Glide.with(getApplicationContext()).load(path).into(img);
            String finalPath = path;
        }
        String finalPath2 = path;
        String finalPath3 = path;
        String finalPath4 = path;
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !DataBaseServices.GetFileByPath(finalPath2).isDownloaded) {
                    new Thread(()->
                    {
                        int i = rnd.nextInt(30000);
                        AtomicReference<Notification.Builder> NotifB = new AtomicReference<>();
                        AtomicReference<NotificationManager> manager = new AtomicReference<>();
                        runOnUiThread(() ->
                        {
                            manager.set((NotificationManager) getSystemService(NOTIFICATION_SERVICE));
                            manager.get().createNotificationChannel(new NotificationChannel("FGG63", "Upload", NotificationManager.IMPORTANCE_NONE));
                            NotifB.set(new Notification.Builder(getApplicationContext(), "FGG63")
                                    .setContentTitle(getResources().getString(R.string.notif_uploading))
                                    .setProgress(100, 0, false)
                                    .setSmallIcon(R.drawable.baseline_backup_24_icon));

                        });
                        String[] pathParts = finalPath3.split("\\\\");
                        pathParts = pathParts[pathParts.length - 1].split("\\.");
                        String exst = pathParts[pathParts.length - 1];
                        FileInputStream iStream;
                        try {
                            Log.i("FilePath", "onClick: " + finalPath3);
                            iStream = new FileInputStream(finalPath3);
                        } catch (FileNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
                        int bufferSize = 32768;
                        byte[] buffer = new byte[bufferSize];

                        int len = 0;
                        int totalLen = 0;
                        long Size = 0;
                        int fullSize = 0;
                        int part = 0;
                        try {
                            fullSize = iStream.available();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        boolean isBigUpload = false;
                        String fileID = "";
                        if (fullSize >= 1048576) {
                            Log.i("len", "onClick: " + totalLen);
                            isBigUpload = true;
                            while (Objects.equals(fileID, "")) {
                                try {
                                    fileID = CloudServicesApi.GetBigPostFileId(exst, SessionId, fullSize);
                                    Log.i("FileID", "onClick: " + fileID);
                                } catch (Exception e) {
                                    Log.e("GetFileID", "onClick: ", e);
                                }
                            }
                        }
                        while (true) {
                            try {
                                if (!((len = iStream.read(buffer)) != -1)) break;
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            byteBuffer.write(buffer, 0, len);
                            totalLen += len;
                            Size += len;

                            if (totalLen == 1048576) {
                                CloudServicesApi.BigUpload(fileID, part, byteBuffer.toByteArray());
                                byteBuffer.reset();
                                totalLen = 0;
                                part++;
                            }
                        }

                        if (isBigUpload) {
                            Log.i("part", "onClick: " + part);
                            CloudServicesApi.BigUpload(fileID, part, byteBuffer.toByteArray());
                            int finalPart = part;
                            int finalFullSize = fullSize;
                            runOnUiThread(()->{
                                manager.get().notify(i, NotifB.get().setProgress(100, (int)(finalPart * 1048576f / finalFullSize * 100), false).build());
                            });
                        } else {
                            CloudServicesApi.POSTLightImg(byteBuffer.toByteArray(), exst, SessionId);
                        }
                        if (fullSize >= 1048576) {
                            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                            retriever.setDataSource(finalPath4);
                            int width = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
                            int height = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
                            try {
                                retriever.release();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            Bitmap ThumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(finalPath4), 100, (int)(100f * ((float)height / width)));
                            ByteArrayOutputStream bos = new ByteArrayOutputStream();
                            ThumbImage.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
                            byte[] bitmapdata = bos.toByteArray();

                            String thumbFileId = null;
                            try{
                                thumbFileId = CloudServicesApi.GetThumbFileId("png", SessionId, bitmapdata.length, fileID);
                            } catch (IOException e) {
                                Log.e("GetThumbFileId", "onClick: ", e);
                            }
                            CloudServicesApi.BigUpload(thumbFileId, 0, bitmapdata);
                        }
                    }).start();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MainActivity.isShowed = false;
    }
}
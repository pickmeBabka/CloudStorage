package com.example.cloudstorage;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.VideoView;

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
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

public class videoViewActivity extends AppCompatActivity {
    VideoView video;
    Button btnSend;
    protected String SessionId;
    Random rnd = new Random();
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_video_view);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        video = findViewById(R.id.videoView);
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
                    }

                    DataBaseServices.MarkFileAsDownloaded(fileId);
                } catch (Exception e) {
                    Log.e("download" + e.getClass().getName(), "onCreate: ", e);
                }
            }
            String finalPath1 = path;
            runOnUiThread(() -> {
                video.setVideoURI(Uri.parse(finalPath1));
                video.seekTo(1);
            });

        }else {
            video.setVideoURI(Uri.parse(path));
            video.seekTo(1);
        }
        video.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(video.isPlaying()) video.pause(); else video.start();
                return false;
            }
        });

        String finalPath = path;
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!DataBaseServices.GetFileByPath(finalPath).isDownloaded)new Thread(()->
                {
                    int i = rnd.nextInt(30000);
                    AtomicReference<Notification.Builder> NotifB = new AtomicReference<>();
                    AtomicReference<NotificationManager> manager = new AtomicReference<>();
                    runOnUiThread(() ->
                    {
                        manager.set((NotificationManager) getSystemService(NOTIFICATION_SERVICE));
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            manager.get().createNotificationChannel(new NotificationChannel("FGG63", "Upload", NotificationManager.IMPORTANCE_NONE));
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            NotifB.set(new Notification.Builder(getApplicationContext(), "FGG63")
                                    .setContentTitle(getResources().getString(R.string.notif_uploading))
                                    .setProgress(100, 0, false)
                                    .setSmallIcon(R.drawable.baseline_backup_24_icon));
                        }

                    });
                    String[] pathParts = finalPath.split("\\\\");
                    pathParts = pathParts[pathParts.length - 1].split("\\.");
                    String exst = pathParts[pathParts.length - 1];
                    FileInputStream iStream = null;
                    try {
                        iStream = new FileInputStream(finalPath);
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
                        isBigUpload = true;
                        runOnUiThread(()->{
                            manager.get().notify(i, NotifB.get().setProgress(100, 0, false).build());
                        });
                        while (Objects.equals(fileID, "")) {
                            try {
                                fileID = CloudServicesApi.GetBigPostFileId(exst, SessionId, fullSize);
                            } catch (Exception e) {
                                Log.e("GetFileID", "onClick: ", e);
                            }
                        }
                    }
                    while (true) {
                        try {
                            if ((len = iStream.read(buffer)) == -1) break;
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
                            int finalFullSize = fullSize;
                            float finalSize = Size;
                            runOnUiThread(()->{
                                manager.get().notify(i, NotifB.get().setProgress(100, (int)(finalSize / finalFullSize * 100), false).build());
                            });
                        }
                    }

                    if (isBigUpload) {
                        CloudServicesApi.BigUpload(fileID, part, byteBuffer.toByteArray());
                        int finalFullSize = fullSize;
                        float finalSize = Size;
                        runOnUiThread(()->{
                            if(finalSize / finalFullSize < 1f) {
                                manager.get().notify(i, NotifB.get().setProgress(100, (int) (finalSize / finalFullSize * 100), false).build());
                            }else {
                                manager.get().cancel(i);
                            }
                        });
                    } else {
                        CloudServicesApi.POSTLightImg(byteBuffer.toByteArray(), exst, SessionId);
                    }
                    Bitmap ThumbImage = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                        try {
                            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                            retriever.setDataSource(finalPath);
                            int width = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
                            int height = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
                            retriever.release();
                            ThumbImage = ThumbnailUtils.createVideoThumbnail(new File(finalPath), new Size(100, 100), null);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        ThumbImage.compress(Bitmap.CompressFormat.PNG, 0, bos);
                        byte[] bitmapdata = bos.toByteArray();

                        String thumbFileId = null;
                        try {
                            thumbFileId = CloudServicesApi.GetThumbFileId("png", SessionId, bitmapdata.length, fileID);
                        } catch (IOException e) {
                            Log.e("GetThumbFileId", "onClick: ", e);
                        }
                        CloudServicesApi.BigUpload(thumbFileId, 0, bitmapdata);
                    }

                }).start();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MainActivity.isShowed = false;
    }
}
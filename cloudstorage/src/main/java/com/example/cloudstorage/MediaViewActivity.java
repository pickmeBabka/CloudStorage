package com.example.cloudstorage;

import android.content.Intent;
import android.net.Uri;
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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class MediaViewActivity extends AppCompatActivity {
    ImageView img;
    Button btnSend;
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
        if (Objects.equals(path, "")) finish();
        Glide.with(getApplicationContext()).load(path).into(img);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(()->
                {
                    String[] pathParts = path.split("\\\\");
                    pathParts = pathParts[pathParts.length - 1].split("\\.");
                    String exst = pathParts[pathParts.length - 1];
                    FileInputStream iStream;
                    try {
                        Log.i("FilePath", "onClick: " + path);
                        iStream = new FileInputStream(path);
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
                    } else {
                        CloudServicesApi.POSTLightImg(byteBuffer.toByteArray(), exst, SessionId);
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
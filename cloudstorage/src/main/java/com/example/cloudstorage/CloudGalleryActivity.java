package com.example.cloudstorage;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQuery;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.Bundle;
import android.util.JsonWriter;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;

import static androidx.recyclerview.widget.RecyclerView.*;
import static com.example.cloudstorage.MainActivity.closeDrawer;
import static com.example.cloudstorage.MainActivity.openDrawer;
import static com.example.cloudstorage.MainActivity.redirectActivity;

public class CloudGalleryActivity extends AppCompatActivity {
    DrawerLayout drawerLayout;
    ImageView menu;
    LinearLayout home, cloudGallary, Settings;
    RecyclerView recyclerView;
    ArrayList<CloudGalleryAdapter.GalleryItem> images;
    CloudGalleryAdapter adapter;
    GridLayoutManager manager;
    TextView lblHome;
    Context context;
    static Boolean isShowed = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cloud_gallery);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String SessionId = getSharedPreferences(helper_class.Constatns.Cash, MODE_PRIVATE).getString(helper_class.Constatns.CashSessionId, "");

        drawerLayout = findViewById(R.id.main);
        menu = findViewById(R.id.menu);
        Settings = findViewById(R.id.Settings);
        home = findViewById(R.id.galary);
        lblHome = findViewById(R.id.lblHome);
        cloudGallary = findViewById(R.id.CloudGallary);
        context = getApplicationContext();
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDrawer(drawerLayout);
            }
        });
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirectActivity(CloudGalleryActivity.this, MainActivity.class);
                finish();
            }
        });
        Settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirectActivity(CloudGalleryActivity.this, SettingsActivity.class);
                finish();
            }
        });
        cloudGallary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeDrawer(drawerLayout);
            }
        });

        recyclerView=findViewById(R.id.gallery_recycler);
        images=new ArrayList<>();
        adapter=new CloudGalleryAdapter(this,images);
        manager=new GridLayoutManager(this,3);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(manager);
        manager.canScrollVertically();
//        recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
//            Long DownTime;
//            float x,y;
//            @Override
//            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
//                if(e.getAction() == MotionEvent.ACTION_DOWN) {
//                    DownTime = e.getEventTime();
//                }
//                if(e.getAction() == MotionEvent.ACTION_UP) {
//                    Long eventTime = e.getEventTime();
//                    lblHome.setText("" + (eventTime - DownTime) + " " + ((eventTime - DownTime) < 100));
//                    x = e.getX(); y = e.getY();
//                    if((eventTime - DownTime) < 100)
//                    {
//                        onTouchEvent(rv, e);
//                    }
//                    return (eventTime - DownTime) < 100;
//                }
//                return false;
//            }
//
//            @Override
//            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
//                if(!isShowed){
//                    CardView img = (CardView) rv.findChildViewUnder(x, y);
//                    if(img != null) startActivity(new Intent(getApplicationContext(), img.getChildAt(0).getTag().toString().contains(".mp4") ? videoViewActivity.class : MediaViewActivity.class).putExtra("media", img.getChildAt(0).getTag().toString()));
//                }
//                isShowed = true;
//            }
//
//            @Override
//            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
//            }
//        });


        new Thread(()->{

            try {
                String[] filesIds = CloudServicesApi.GetMyFiles(SessionId);
                for (String id:
                     filesIds) {
                    CloudServicesApi.UserFileInfo fileInfo = CloudServicesApi.GetFileInfo(id.substring(0, 35));
                    CloudGalleryAdapter.GalleryItem item = new CloudGalleryAdapter.GalleryItem(
                            fileInfo.thumbId != null ? fileInfo.thumbId + "." + CloudServicesApi.GetFileInfo(fileInfo.thumbId).extension : id + "." + fileInfo.extension,
                            fileInfo.thumbId != null ? fileInfo.thumbId : id.substring(0, 35),
                            fileInfo.thumbId != null ? CloudServicesApi.GetFileInfo(fileInfo.thumbId).parts : fileInfo.parts,
                            fileInfo.thumbId != null ? id.substring(0, 35) : null);
                    images.add(item);
                    if(DataBaseServices.IsFileExistsByFileId(fileInfo.FileId))
                    {
                        item.isDownloaded = DataBaseServices.IsFileDownloadedByFileId(fileInfo.FileId);
                        runOnUiThread(() -> adapter.notifyItemChanged(images.size() - 1));
                    }else
                    {
                        DataBaseServices.AddFile(new DataBaseServices.StorageFile(
                                fileInfo.FileId,
                                false,
                                false,
                                fileInfo.length,
                                fileInfo.FileId + "." + fileInfo.extension,
                                "",
                                null,
                                fileInfo.thumbId
                        ));
                    }
                    File f = new File(context.getFilesDir(), fileInfo.FileId + "." + fileInfo.extension);
                    if(!item.isDownloaded && f.exists())
                    {
                        f.delete();
                    }
                }
                runOnUiThread(() ->{
                    recyclerView.getAdapter().notifyDataSetChanged();
                });
                startDownloadFiles();

            } catch (Exception e) {
                Log.e(e.getClass().getName(), "onCreate: ", e);
            }


        }).start();
    }

    private void startDownloadFiles() throws IOException {
            for (int i = 0; i < Math.ceil(images.size() / 5d); i++) {
                File[] files = new File[5];
                FileOutputStream[] filesWriters = new FileOutputStream[5];
                int DownloadedNum = 0;
                while(DownloadedNum != (Math.min(images.size(), 5))){
                    DownloadedNum = 0;
                    for (int j = 0; j < (Math.min(5, images.size() - (i * 5))); j++) {
                    CloudGalleryAdapter.GalleryItem item = images.get(i * 5 + j);
                    if(item.isDownloaded)
                    {
                        DownloadedNum++;
                        if(filesWriters[j] != null)
                        {
                            Log.i("downloadFinal", "startDownloadFiles: " + files[j].length() + " " + item.fileID);
                            filesWriters[j].close();
                            filesWriters[j] = null;
                            files[j] = null;
                            int finalI = i;
                            int finalJ = j;
                            runOnUiThread(() -> adapter.notifyItemChanged(finalI * 5 + finalJ));
                        }
                    }else {
                        if(files[j] == null)files[j] = new File(context.getFilesDir(), item.path);
                        if(filesWriters[j] == null) filesWriters[j] = new FileOutputStream(files[j]);
                        byte[] bytes = CloudServicesApi.DownloadPart(item.fileID, item.downloadedParts);
                        filesWriters[j].write(bytes);
                        Log.i("download", "startDownloadFiles: " + files[j].length() + " " + item.fileID + " " + files[j].getPath());
                        item.IncreaseDownloadedParts();
                        int finalI = i;
                        int finalJ = j;
                        runOnUiThread(() -> adapter.notifyItemChanged(finalI * 5 + finalJ));
                    }
                }
            }
        }
    }
}
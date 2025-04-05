package com.example.cloudstorage;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQuery;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.Bundle;
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
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
    LinearLayout home, setings, cloudGallary;
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
        home = findViewById(R.id.galary);
        setings = findViewById(R.id.settings);
        lblHome = findViewById(R.id.lblHome);
        cloudGallary = findViewById(R.id.CloudGallary);
        context = getApplicationContext();
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDrawer(drawerLayout);
            }
        });
        setings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirectActivity(CloudGalleryActivity.this, SettingsActivity.class);
            }
        });
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirectActivity(CloudGalleryActivity.this, MainActivity.class);
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
        recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            Long DownTime;
            float x,y;
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                if(e.getAction() == MotionEvent.ACTION_DOWN) {
                    DownTime = e.getEventTime();
                }
                if(e.getAction() == MotionEvent.ACTION_UP) {
                    Long eventTime = e.getEventTime();
                    lblHome.setText("" + (eventTime - DownTime) + " " + ((eventTime - DownTime) < 100));
                    x = e.getX(); y = e.getY();
                    if((eventTime - DownTime) < 100)
                    {
                        onTouchEvent(rv, e);
                    }
                    return (eventTime - DownTime) < 100;
                }
                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                if(!isShowed){
                    CardView img = (CardView) rv.findChildViewUnder(x, y);
                    assert img != null;
                    startActivity(new Intent(getApplicationContext(), img.getChildAt(0).getTag().toString().contains(".mp4") ? videoViewActivity.class : MediaViewActivity.class).putExtra("media", img.getChildAt(0).getTag().toString()));
                }
                isShowed = true;
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            }
        });


        new Thread(()->{

            try {
                String[] filesIds = CloudServicesApi.GetMyFiles(SessionId);
                for (String id:
                     filesIds) {
                    CloudServicesApi.UserFileInfo fileInfo = CloudServicesApi.GetFileInfo(id);
                    CloudGalleryAdapter.GalleryItem item = new CloudGalleryAdapter.GalleryItem(fileInfo.FileId + "." + fileInfo.extension,
                            id,
                            fileInfo.parts);
                    images.add(item);
                    if(DataBaseServices.IsFileExistsByFileId(fileInfo.FileId))
                    {
                        item.isDownloaded = DataBaseServices.IsFileDownloaded(fileInfo.FileId);
                    }else
                    {
                        DataBaseServices.AddFile(new DataBaseServices.StorageFile(
                                fileInfo.FileId,
                                false,
                                false,
                                fileInfo.length,
                                fileInfo.FileId + "." + fileInfo.extension,
                                "",
                                null
                        ));
                    }
                    File f = new File(context.getFilesDir(), fileInfo.FileId + "." + fileInfo.extension);
                    if(item.isDownloaded && f.exists())
                    {
                        f.delete();
                    }
                }
                runOnUiThread(() ->{
                    recyclerView.getAdapter().notifyDataSetChanged();
                });
                startDownloadFiles();

            } catch (IOException | JSONException e) {
                throw new RuntimeException(e);
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
                    for (int j = 0; j < (Math.min(images.size(), 5)); j++) {
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
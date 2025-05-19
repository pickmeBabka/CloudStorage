package com.example.cloudstorage;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

import static android.Manifest.permission.POST_NOTIFICATIONS;
import static android.Manifest.permission.READ_MEDIA_IMAGES;
import static android.Manifest.permission.READ_MEDIA_VIDEO;
import static java.util.Arrays.stream;

public class MainActivity extends AppCompatActivity {
    static int PERMISSION_REQUEST_CODE=100;
    DrawerLayout drawerLayout;
    ImageView menu;
    LinearLayout home, cloudGallery, Settings, laySync;
    RecyclerView recyclerView;
    ArrayList<String> images;
    GalleryAdapter adapter;
    GridLayoutManager manager;
    TextView lblHome;
    Button btnSync;
    Random rnd = new Random();
    static Boolean isShowed = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        drawerLayout = findViewById(R.id.main);
        menu = findViewById(R.id.menu);
        home = findViewById(R.id.galary);
        lblHome = findViewById(R.id.lblHome);
        cloudGallery = findViewById(R.id.CloudGallary);
        Settings = findViewById(R.id.Settings);
        laySync = findViewById(R.id.laySync);
        btnSync = findViewById(R.id.btnSync);

        btnSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(()->
                {
                    AtomicReference<Notification.Builder> NotifB = new AtomicReference<>();
                    AtomicReference<NotificationManager> manager = new AtomicReference<>();
                    AtomicReference<Integer> filesSynced = new AtomicReference<Integer>(0);
                    AtomicReference<Integer> allFiles = new AtomicReference<>(0);
                    int NotifId = rnd.nextInt(30000);
                    runOnUiThread(() ->
                    {
                        manager.set((NotificationManager) getSystemService(NOTIFICATION_SERVICE));
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            manager.get().createNotificationChannel(new NotificationChannel("FGG63", "SyncFiles", NotificationManager.IMPORTANCE_NONE));
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            NotifB.set(new Notification.Builder(getApplicationContext(), "FGG63")
                                    .setContentTitle(getResources().getString(R.string.notif_uploading))
                                    .setProgress(100, 0, false)
                                    .setSmallIcon(R.drawable.baseline_backup_24_icon));
                        }
                        manager.get().notify(NotifId, NotifB.get().build());
                    });

                    AtomicReference<Queue<String>> q = new AtomicReference<>(new ArrayDeque<>());
                    q.get().addAll(Arrays.asList(DataBaseServices.GetUnsyncedFiles()));
                    Log.i("ff", "onClick: " + q.get().size());
                    allFiles.set(q.get().size());
                    for (int i = 0; i < SettingsEnums.getNetworkProfile(getApplicationContext()).uploadThreads; i++) {
                        new Thread(() -> {
                            String path = null;
                            while((path = q.get().poll()) != null)
                            {
                                try {
                                    FileInputStream reader = new FileInputStream(new File(path));
                                    int len = 0, part = 0;
                                    long fullSize = 0;

                                    byte[] buf = new byte[4096];
                                    ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
                                    String[] pathParts = path.split("/");
                                    pathParts = pathParts[pathParts.length - 1].split("\\.");
                                    String exst = pathParts[pathParts.length - 1];
                                    String SessionId = getSharedPreferences(helper_class.Constatns.Cash, MODE_PRIVATE).getString(helper_class.Constatns.CashSessionId, "");
                                    String fileId = CloudServicesApi.GetBigPostFileId(exst, SessionId, reader.available());

                                    while((len = reader.read(buf)) > 0){
                                        byteBuffer.write(buf, 0, len);
                                        fullSize+=len;
                                        if(fullSize == 1048576)
                                        {
                                            CloudServicesApi.BigUpload(fileId, part, byteBuffer.toByteArray());
                                            fullSize=0;
                                            part++;
                                            byteBuffer.reset();
                                        }
                                    }
                                    CloudServicesApi.BigUpload(fileId, part, byteBuffer.toByteArray());

                                    if("mp4 mov avi mkv wmv".contains(exst))
                                    {
                                        Bitmap ThumbImage = null;
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                            try {
                                                ThumbImage = ThumbnailUtils.createVideoThumbnail(new File(path), new Size(100, 100), null);
                                            } catch (IOException e) {
                                                Log.e("thumbIO", "onClick: ", e);
                                            }
                                            ByteArrayOutputStream bos = new ByteArrayOutputStream();
                                            ThumbImage.compress(Bitmap.CompressFormat.PNG, 0, bos);
                                            byte[] bitmapdata = bos.toByteArray();

                                            String thumbFileId = null;
                                            try {
                                                thumbFileId = CloudServicesApi.GetThumbFileId("png", SessionId, bitmapdata.length, fileId);
                                            } catch (IOException e) {
                                                Log.e("GetThumbFileId", "onClick: ", e);
                                            }
                                            CloudServicesApi.BigUpload(thumbFileId, 0, bitmapdata);
                                        }
                                    }else if(path != null)
                                    {
                                        Bitmap ThumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(path), 100, 100);
                                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                                        ThumbImage.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
                                        byte[] bitmapdata = bos.toByteArray();

                                        String thumbFileId = null;
                                        try{
                                            thumbFileId = CloudServicesApi.GetThumbFileId("png", SessionId, bitmapdata.length, fileId);
                                        } catch (IOException e) {
                                            Log.e("GetThumbFileId", "onClick: ", e);
                                        }
                                        CloudServicesApi.BigUpload(thumbFileId, 0, bitmapdata);
                                    }
                                    DataBaseServices.AddFileId(fileId, path);
                                    DataBaseServices.MarkFileAsUploaded(fileId);
                                } catch (Exception e) {
                                    Log.e("Sync" + e.getClass().getName(), "onClick: ", e);
                                }
                                filesSynced.set(filesSynced.get()+1);
                                runOnUiThread(() -> manager.get().notify(NotifId, NotifB.get().setProgress(100, (int)(filesSynced.get() / (float)allFiles.get() * 100), false).build()));
                            }
                        }).start();
                    }
                }).start();
            }
        });

        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDrawer(drawerLayout);
            }
        });
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeDrawer(drawerLayout);
            }
        });
        cloudGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirectActivity(MainActivity.this, CloudGalleryActivity.class);
                finish();
            }
        });
        Settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirectActivity(MainActivity.this, SettingsActivity.class);
                finish();
            }
        });

        recyclerView=findViewById(R.id.gallery_recycler);
        images=new ArrayList<>();
        adapter=new GalleryAdapter(this,images);
        manager=new GridLayoutManager(this,3);
        manager.canScrollVertically();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(manager);

        try {
            checkPermissions();
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }

    }

    public static void openDrawer(DrawerLayout drawerLayout)
    {
        drawerLayout.openDrawer(GravityCompat.START);
    }

    public static void closeDrawer(DrawerLayout drawerLayout)
    {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
        {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    public static void redirectActivity(Context activity, Class secondActivity)
    {
        Intent i = new Intent(activity, secondActivity);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(i);
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeDrawer(drawerLayout);
    }

    private void checkPermissions() throws IOException, ParseException {
        int result= ContextCompat.checkSelfPermission(getApplicationContext(),READ_MEDIA_VIDEO);
        if(result== PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getApplicationContext(),POST_NOTIFICATIONS)== PackageManager.PERMISSION_GRANTED){
            new Thread(() -> {
                try {
                    loadImages();
                } catch (IOException e) {
                    Log.e("LoadImagesIO", "checkPermissions: ", e);
                } catch (ParseException e) {
                    Log.e("LoadImagesParse", "checkPermissions: ", e);
                }
            }).start();
        }else{
            ActivityCompat.requestPermissions(this,new String[]{READ_MEDIA_VIDEO, READ_MEDIA_IMAGES, POST_NOTIFICATIONS},PERMISSION_REQUEST_CODE);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0){
            boolean accepted=grantResults[0]==PackageManager.PERMISSION_GRANTED && grantResults[1]==PackageManager.PERMISSION_GRANTED;
            if(accepted){
                new Thread(() -> {
                    try {
                        loadImages();
                    } catch (IOException e) {
                        Log.e("LoadImagesIO", "checkPermissions: ", e);
                    } catch (ParseException e) {
                        Log.e("LoadImagesParse", "checkPermissions: ", e);
                    }
                }).start();
            }else{
                Toast.makeText(this,"You have dined the permission",Toast.LENGTH_LONG).show();
            }
        }else if(permissions.length == 1 && Objects.equals(permissions[0], NOTIFICATION_SERVICE)){
            Log.i("dura", "onRequestPermissionsResult: " + grantResults[0]);
        }
    }
    private void loadImages() throws IOException, ParseException {
        String[] colums={MediaStore.Images.Media.DATA,MediaStore.Images.Media._ID, MediaStore.Images.Media.DATE_TAKEN};
        String order=MediaStore.Images.Media.DATE_TAKEN+" DESC";
        ArrayList<file> media = new ArrayList<>();
        Cursor cursor=getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,colums,null,null,order);
        int count =cursor.getCount();
        int DataCI=cursor.getColumnIndex(MediaStore.Video.Media.DATA), DateCI = cursor.getColumnIndex(MediaStore.Video.Media.DATE_TAKEN);
        Calendar c = Calendar.getInstance();
        int unsinc = 0;

        cursor.moveToPosition(0);
        for(int i=0;i<count;i++){
            cursor.moveToPosition(i);
            media.add(new file (cursor.getInt(DateCI), cursor.getString(DataCI)));
        }
        cursor=getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,colums,null,null,order);
        count =cursor.getCount();
        for(int i=0;i<count;i++){
            cursor.moveToPosition(i);
            media.add(new file (cursor.getInt(DateCI), cursor.getString(DataCI)));
        }
        file[] mediaArray = media.toArray(new file[0]);
        Arrays.sort(mediaArray, new Comparator<file>() {
            @Override
            public int compare(file o1, file o2) {
                return  o1.date.compareTo(o2.date);
            }
        });
        for (file o : mediaArray) {
            Integer key = o.date;
            String[] path = o.path.split("/");
            //Log.i("", "loadImages: " + DataBaseServices.IsFileExistsByPath(o.path) + " " + o.path + " " + DataBaseServices.IsFileUploadedByPath(o.path));
            try {
                if(!DataBaseServices.IsFileExistsByPath(o.path)) {
                    DataBaseServices.AddFile(new DataBaseServices.StorageFile(null,
                            false,
                            false,
                            null,
                            o.path,
                            path[Math.max(path.length - 2, 0)],
                            null,
                            null));
                    unsinc++;
                }
                else {
                    if(!DataBaseServices.IsFileUploadedByPath(o.path)) unsinc++;
                }
            } catch (Exception e) {
                Log.e("addFile_" + e.getClass().getName(), "loadImages: ", e);
            }
            images.add(o.path);
        }
        if (Looper.myLooper() == Looper.getMainLooper()) {
            recyclerView.getAdapter().notifyDataSetChanged();
            if(unsinc > 0) {
                laySync.setVisibility(View.VISIBLE);
                lblHome.setText(getResources().getString(R.string.Main_UnsynchronizedObjects) + ": " + unsinc);
            }
        }
        else {
            int finalUnsinc = unsinc;
            runOnUiThread(() -> {
                recyclerView.getAdapter().notifyDataSetChanged();
                if(finalUnsinc > 0) {
                    laySync.setVisibility(View.VISIBLE);
                    lblHome.setText(getResources().getString(R.string.Main_UnsynchronizedObjects) + ": " + finalUnsinc);
                }
            });
        }
        cursor.close();
    }

    String StringArrayToString(@NonNull String[] array, String sep)
    {
        String res = "";
        for (String s : array) {
            res = s + sep;
        }
        return res;
    }

    class file
    {
        String path;
        Integer date;
        public  file(Integer date, String path)
        {
            this.path = path;
            this.date = date;
        }
    }
}
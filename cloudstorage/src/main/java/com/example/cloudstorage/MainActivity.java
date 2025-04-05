package com.example.cloudstorage;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;

import static android.Manifest.permission.READ_MEDIA_IMAGES;
import static android.Manifest.permission.READ_MEDIA_VIDEO;
import static java.util.Arrays.stream;

public class MainActivity extends AppCompatActivity {
    static int PERMISSION_REQUEST_CODE=100;
    DrawerLayout drawerLayout;
    ImageView menu;
    LinearLayout home, setings, cloudGallary;
    RecyclerView recyclerView;
    ArrayList<String> images;
    GalleryAdapter adapter;
    GridLayoutManager manager;
    TextView lblHome;
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
        setings = findViewById(R.id.settings);
        lblHome = findViewById(R.id.lblHome);
        cloudGallary = findViewById(R.id.CloudGallary);

        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDrawer(drawerLayout);
            }
        });
        setings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirectActivity(MainActivity.this, SettingsActivity.class);
            }
        });
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeDrawer(drawerLayout);
            }
        });
        cloudGallary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirectActivity(MainActivity.this, CloudGalleryActivity.class);
            }
        });

        recyclerView=findViewById(R.id.gallery_recycler);
        images=new ArrayList<>();
        adapter=new GalleryAdapter(this,images);
        manager=new GridLayoutManager(this,3);
        manager.canScrollVertically();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(manager);
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
//                    assert img != null;
//                    startActivity(new Intent(getApplicationContext(), img.getChildAt(0).getTag().toString().contains(".mp4") ? videoViewActivity.class : MediaViewActivity.class).putExtra("media", img.getChildAt(0).getTag().toString()));
//                }
//                isShowed = true;
//            }
//
//            @Override
//            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
//            }
//        });
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
        if(result== PackageManager.PERMISSION_GRANTED){
            loadImages();
        }else{
            ActivityCompat.requestPermissions(this,new String[]{READ_MEDIA_VIDEO, READ_MEDIA_IMAGES},PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0){
            boolean accepted=grantResults[0]==PackageManager.PERMISSION_GRANTED && grantResults[1]==PackageManager.PERMISSION_GRANTED;
            if(accepted){
                try {
                    loadImages();
                } catch (FileNotFoundException e) {
                    Log.e("LoadImages", "onRequestPermissionsResult: ", e);
                } catch (IOException | ParseException e) {
                    throw new RuntimeException(e);
                }
            }else{
                Toast.makeText(this,"You have dined the permission",Toast.LENGTH_LONG).show();
            }
        }else{

        }
    }
    private void loadImages() throws IOException, ParseException {
        String[] colums={MediaStore.Images.Media.DATA,MediaStore.Images.Media._ID, MediaStore.Images.Media.DATE_TAKEN};
        String order=MediaStore.Images.Media.DATE_TAKEN+" DESC";
        HashMap<Integer, String> media = new HashMap<>();
        Cursor cursor=getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,colums,null,null,order);
        int count =cursor.getCount();
        int DataCI=cursor.getColumnIndex(MediaStore.Video.Media.DATA), DateCI = cursor.getColumnIndex(MediaStore.Video.Media.DATE_TAKEN);
        Calendar c = Calendar.getInstance();
        cursor.moveToPosition(0);
        for(int i=0;i<count;i++){
            cursor.moveToPosition(i);
            media.put(cursor.getInt(DateCI), cursor.getString(DataCI));
        }
        cursor=getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,colums,null,null,order);
        count =cursor.getCount();
        for(int i=0;i<count;i++){
            cursor.moveToPosition(i);
            media.put(cursor.getInt(DateCI), cursor.getString(DataCI));
        }

        Object[] keys = media.keySet().toArray();
        Arrays.sort(keys);
        for (Object o : keys) {
            Integer key = (Integer) o;
            String[] path = media.get(key).split("\\\\");
            Log.i("key", "loadImages: " + key + " " + path.length);
            try {
                DataBaseServices.AddFile(new DataBaseServices.StorageFile(null,
                        false,
                        false,
                        null,
                        media.get(key),
                        path[Math.max(path.length - 2, 0)],
                        null));
            } catch (Exception ignored) {

            }
            images.add(media.get(key));
        }
        recyclerView.getAdapter().notifyDataSetChanged();
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
}
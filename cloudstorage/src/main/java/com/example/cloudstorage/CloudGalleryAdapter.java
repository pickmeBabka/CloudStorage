package com.example.cloudstorage;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.jetbrains.annotations.Range;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import static androidx.core.content.ContextCompat.startActivity;

public class CloudGalleryAdapter extends RecyclerView.Adapter<CloudGalleryAdapter.ViewHolder> {

    private Context context;
    private ArrayList<GalleryItem> images_list;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.cloud_galary_item,parent,false);
        view.setTag(parent.getChildCount());
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bar.setProgress(images_list.get(position).progress);
        if(images_list.get(position).isDownloaded) {
            File image_file = new File(context.getFilesDir(), images_list.get(position).path);
            Log.i("filePath", "onBindViewHolder: " + image_file.getPath());
            if (image_file.exists()) {
                Glide.with(context)
                        .load(image_file)
                        .into(holder.image);
                holder.image.setTag(position);
            }
            holder.bar.setVisibility(ProgressBar.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return images_list.size();
    }

    public CloudGalleryAdapter(Context context, ArrayList<GalleryItem> images_list) {
        this.context = context;
        this.images_list = images_list;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private ImageView image;
        private ProgressBar bar;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image=itemView.findViewById(R.id.cloud_gallery_item);
            bar = itemView.findViewById(R.id.cloudGalleryProgressBar);

            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i("gallary_item", "onClick: " + v.getTag() + " " + images_list.get((int)image.getTag()).extension + " " + images_list.get((int)image.getTag()).path + " " + images_list.get((int)image.getTag()).fileID);
                    if(images_list.get((int)image.getTag()).originFileId != null) startActivity(context, new Intent(context,
                                     images_list.get((int)image.getTag()).path.contains(".mp4") ? videoViewActivity.class : MediaViewActivity.class).putExtra("media", image.getTag().toString()).putExtra("IsNeedToDownload", true).putExtra("FileId", images_list.get((int)image.getTag()).originFileId).putExtra("exst", images_list.get((int)image.getTag()).extension),
                            null);
                    else startActivity(context, new Intent(context,
                            images_list.get((int)image.getTag()).path.contains(".mp4") ? videoViewActivity.class : MediaViewActivity.class).putExtra("media", images_list.get((int)image.getTag()).path), null);

                }
            });
        }
    }

    public static class GalleryItem
    {
        Boolean isDownloaded = false;
        String path;
        int progress;
        String fileID;
        int downloadedParts = 0;
        int totalParts;
        String extension;
        String originFileId;
        public GalleryItem(String path, String fileID, int totalParts, String originFileId)
        {
            this.path = path;
            progress = 0;
            this.fileID = fileID;
            this.totalParts = totalParts;
            this.originFileId = originFileId;
            String[] p = path.split("/");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                extension = Arrays.stream(p).toList().getLast().split("\\.")[1];
            }
        }
        public void setProgress(@Range(from = 0, to = 100)int newVal) {progress = newVal; }
        public void IncreaseDownloadedParts() {
            downloadedParts++;
            progress = (int)((float) downloadedParts / totalParts * 100f);
            Log.i(fileID, "IncreaseDownloadedParts: " + downloadedParts + " " + totalParts);
            isDownloaded = downloadedParts >= totalParts;
            if(isDownloaded)
            {
                DataBaseServices.MarkFileAsDownloaded(fileID);
            }
        }
    }
}

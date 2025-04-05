package com.example.cloudstorage;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.jetbrains.annotations.Range;

import java.io.File;
import java.util.ArrayList;

public class CloudGalleryAdapter extends RecyclerView.Adapter<CloudGalleryAdapter.ViewHolder> {

    private Context context;
    private ArrayList<GalleryItem> images_list;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.cloud_galary_item,parent,false);
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
                holder.image.setTag(image_file.getPath());
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
        public GalleryItem(String path, String fileID, int totalParts)
        {
            this.path = path;
            progress = 0;
            this.fileID = fileID;
            this.totalParts = totalParts;
        }
        public void setProgress(@Range(from = 0, to = 100)int newVal) {progress = newVal; }
        public void IncreaseDownloadedParts() {
            downloadedParts++;
            progress = (int)((float) downloadedParts / totalParts * 100f);
            isDownloaded = downloadedParts == totalParts;
            if(isDownloaded)
            {
                DataBaseServices.MarkFileAsDownloaded(fileID);
            }
        }
    }
}

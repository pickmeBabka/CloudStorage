package com.example.cloudstorage;

import android.content.Context;
import android.content.Intent;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;

import static androidx.core.content.ContextCompat.startActivity;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {

    private Context context;
    private ArrayList<String> images_list;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.gallery_item,parent,false);
        return new ViewHolder(view, context);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        File image_file=new File(images_list.get(position));
        if(image_file.exists()){
            Glide.with(context).load(image_file).into(holder.image);
            holder.image.setTag(images_list.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return images_list.size();
    }

    public GalleryAdapter(Context context, ArrayList<String> images_list) {
        this.context = context;
        this.images_list = images_list;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final ImageView image;
        public ViewHolder(@NonNull View itemView, Context context) {
            super(itemView);
            image=itemView.findViewById(R.id.gallery_item);
            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(context, new Intent(context,
                            image.getTag().toString().contains(".mp4") ? videoViewActivity.class : MediaViewActivity.class).putExtra("media", image.getTag().toString()),
                            null);
                }
            });
        }
    }
}

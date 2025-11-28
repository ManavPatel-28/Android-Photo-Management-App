package com.example.andriodapp78.photos.ui;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.andriodapp78.R;
import com.example.andriodapp78.photos.model.Photo;

import java.util.ArrayList;
import java.util.List;

public class PhotoListAdapter extends RecyclerView.Adapter<PhotoListAdapter.ViewHolder> {

    public interface OnPhotoClickListener {
        void onPhotoClick(Photo photo);
    }

    private final List<Photo> photos = new ArrayList<>();
    private final OnPhotoClickListener listener;

    public PhotoListAdapter(List<Photo> initial, OnPhotoClickListener listener) {
        if (initial != null) {
            photos.addAll(initial);
        }
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_photo, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Photo p = photos.get(position);
        holder.bind(p, listener);
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    // ---- helper methods used by AlbumActivity ----

    public void addPhoto(Photo p) {
        photos.add(p);
        notifyItemInserted(photos.size() - 1);
    }

    public void removePhoto(Photo p) {
        int idx = photos.indexOf(p);
        if (idx >= 0) {
            photos.remove(idx);
            notifyItemRemoved(idx);
        }
    }

    public void replaceAll(List<Photo> newList) {
        photos.clear();
        if (newList != null) {
            photos.addAll(newList);
        }
        notifyDataSetChanged();
    }

    // ---- ViewHolder ----

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageThumb;
        private final TextView textCaption;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageThumb = itemView.findViewById(R.id.image_thumb);
            textCaption = itemView.findViewById(R.id.text_caption);
        }

        void bind(Photo photo, OnPhotoClickListener listener) {
            textCaption.setText(photo.getCaption());

            Glide.with(imageThumb.getContext())
                    .load(Uri.parse(photo.getUriString()))
                    .centerCrop()
                    .into(imageThumb);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPhotoClick(photo);
                }
            });
        }
    }
}

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

public class PhotoListAdapter extends RecyclerView.Adapter<PhotoListAdapter.PhotoViewHolder> {

    public interface OnPhotoClickListener {
        void onPhotoClick(Photo photo);
        void onPhotoLongClick(Photo photo);
    }

    // Adapter **owns** this list
    private final List<Photo> photos = new ArrayList<>();
    private final OnPhotoClickListener listener;

    public PhotoListAdapter(List<Photo> initial, OnPhotoClickListener listener) {
        if (initial != null) {
            photos.addAll(initial);   // copy, do NOT share reference
        }
        this.listener = listener;
    }

    /** Add one photo at the end */
    public void addPhoto(Photo p) {
        photos.add(p);
        notifyItemInserted(photos.size() - 1);
    }

    /** Remove a specific photo (by equals) */
    public void removePhoto(Photo p) {
        int idx = photos.indexOf(p);
        if (idx >= 0) {
            photos.remove(idx);
            notifyItemRemoved(idx);
        }
    }

    /** Replace entire list from the album (used in onResume) */
    public void replaceAll(List<Photo> newPhotos) {
        photos.clear();
        if (newPhotos != null) {
            photos.addAll(newPhotos);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_photo, parent, false);
        return new PhotoViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        holder.bind(photos.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imageThumb;
        private final TextView infoText;

        PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            imageThumb = itemView.findViewById(R.id.image_thumb);
            infoText = itemView.findViewById(R.id.text_photo_info);
        }

        void bind(Photo photo, OnPhotoClickListener listener) {
            String uriString = photo.getUriString();

            if (uriString != null && !uriString.isEmpty()) {
                Glide.with(imageThumb.getContext())
                        .load(Uri.parse(uriString))
                        .placeholder(android.R.color.darker_gray)
                        .error(android.R.color.darker_gray)
                        .centerCrop()
                        .into(imageThumb);
            } else {
                imageThumb.setImageResource(android.R.color.darker_gray);
            }

            // Show filename/caption
            infoText.setText(photo.getCaption());

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onPhotoClick(photo);
            });

            itemView.setOnLongClickListener(v -> {
                if (listener != null) listener.onPhotoLongClick(photo);
                return true;
            });
        }
    }
}

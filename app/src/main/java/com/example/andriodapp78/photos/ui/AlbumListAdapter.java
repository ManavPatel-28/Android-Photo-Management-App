package com.example.andriodapp78.photos.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.andriodapp78.R;
import com.example.andriodapp78.photos.model.Album;

import java.util.List;

public class AlbumListAdapter extends RecyclerView.Adapter<AlbumListAdapter.AlbumViewHolder> {

    public interface OnAlbumClickListener {
        void onAlbumClick(Album album);
        void onAlbumLongClick(Album album);
    }

    private final List<Album> albums;
    private final OnAlbumClickListener listener;

    public AlbumListAdapter(List<Album> albums, OnAlbumClickListener listener) {
        this.albums = albums;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_album, parent, false);
        return new AlbumViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
        Album album = albums.get(position);
        holder.bind(album, listener);
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }

    static class AlbumViewHolder extends RecyclerView.ViewHolder {

        private final TextView nameView;
        private final TextView infoView;

        AlbumViewHolder(@NonNull View itemView) {
            super(itemView);
            nameView = itemView.findViewById(R.id.text_album_name);
            infoView = itemView.findViewById(R.id.text_album_info);
        }

        void bind(Album album, OnAlbumClickListener listener) {
            nameView.setText(album.getName());
            int count = album.getPhotos().size();
            String label = count + (count == 1 ? " photo" : " photos");
            infoView.setText(label);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAlbumClick(album);
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onAlbumLongClick(album);
                }
                return true;
            });
        }
    }
}

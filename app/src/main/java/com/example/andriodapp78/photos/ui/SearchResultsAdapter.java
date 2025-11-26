package com.example.andriodapp78.photos.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.andriodapp78.PhotoViewActivity;
import com.example.andriodapp78.R;
import com.example.andriodapp78.SearchActivity;
import com.example.andriodapp78.photos.model.Photo;
import com.example.andriodapp78.photos.model.Tag;
import com.example.andriodapp78.photos.model.PhotoLibrary;
import com.example.andriodapp78.photos.model.Album;

import java.util.List;

public class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.SearchViewHolder> {

    private final List<SearchActivity.SearchResult> results;
    private final Context context;
    private final PhotoLibrary library;

    public SearchResultsAdapter(List<SearchActivity.SearchResult> results,
                                Context context,
                                PhotoLibrary library) {
        this.results = results;
        this.context = context;
        this.library = library;
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_result, parent, false);
        return new SearchViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
        SearchActivity.SearchResult sr = results.get(position);
        holder.bind(sr, context, library);
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    static class SearchViewHolder extends RecyclerView.ViewHolder {

        private final ImageView thumb;
        private final TextView line1;
        private final TextView line2;

        SearchViewHolder(@NonNull View itemView) {
            super(itemView);
            thumb = itemView.findViewById(R.id.image_thumb_search);
            line1 = itemView.findViewById(R.id.text_line1);
            line2 = itemView.findViewById(R.id.text_line2);
        }

        void bind(SearchActivity.SearchResult sr,
                  Context context,
                  PhotoLibrary library) {

            Photo p = sr.photo;

            // Thumbnail
            Uri uri = Uri.parse(p.getUriString());
            Glide.with(context)
                    .load(uri)
                    .centerCrop()
                    .placeholder(android.R.color.darker_gray)
                    .into(thumb);

            // First line: album + caption
            String caption = p.getCaption();
            if (caption == null || caption.isBlank()) {
                caption = "(no caption)";
            }
            line1.setText(sr.albumName + " — " + caption);

            // Second line: tags summary
            List<Tag> tags = p.getTags();
            if (tags == null || tags.isEmpty()) {
                line2.setText("No tags");
            } else {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < tags.size(); i++) {
                    Tag t = tags.get(i);
                    if (i > 0) sb.append(" | ");
                    sb.append(t.getType()).append(" = ").append(t.getValue());
                }
                line2.setText(sb.toString());
            }

            // Click: open PhotoViewActivity at the right album + index
            itemView.setOnClickListener(v -> {
                Album a = library.findAlbum(sr.albumName);
                if (a == null) return;

                Intent intent = new Intent(context, PhotoViewActivity.class);
                intent.putExtra(PhotoViewActivity.EXTRA_ALBUM_NAME, sr.albumName);
                intent.putExtra(PhotoViewActivity.EXTRA_PHOTO_INDEX, sr.indexInAlbum);
                context.startActivity(intent);
            });
        }
    }
}

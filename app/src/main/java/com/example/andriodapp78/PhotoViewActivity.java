package com.example.andriodapp78;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.andriodapp78.photos.model.Album;
import com.example.andriodapp78.photos.model.Photo;
import com.example.andriodapp78.photos.model.PhotoLibrary;
import com.example.andriodapp78.photos.model.Tag;
import com.example.andriodapp78.photos.storage.Storage;

import java.util.ArrayList;
import java.util.List;

public class PhotoViewActivity extends AppCompatActivity {

    public static final String EXTRA_ALBUM_NAME = "album_name";
    public static final String EXTRA_PHOTO_INDEX = "photo_index";

    private PhotoLibrary library;
    private Album album;
    private List<Photo> photos = new ArrayList<>();
    private int index = 0;

    private ImageView imageFull;
    private TextView captionText;
    private TextView tagsText;
    private Button btnPrev;
    private Button btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_view);

        imageFull = findViewById(R.id.image_full);
        captionText = findViewById(R.id.text_caption);
        tagsText = findViewById(R.id.text_tags);
        btnPrev = findViewById(R.id.button_prev);
        btnNext = findViewById(R.id.button_next);

        // Load library and album
        library = Storage.load(this);

        String albumName = getIntent().getStringExtra(EXTRA_ALBUM_NAME);
        index = getIntent().getIntExtra(EXTRA_PHOTO_INDEX, 0);

        if (albumName != null) {
            album = library.findAlbum(albumName);
        }

        if (album == null) {
            Toast.makeText(this, "Album not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        photos = new ArrayList<>(album.getPhotos());
        if (photos.isEmpty()) {
            Toast.makeText(this, "No photos in album", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (index < 0) index = 0;
        if (index >= photos.size()) index = photos.size() - 1;

        // Button handlers
        btnPrev.setOnClickListener(v -> {
            if (index > 0) {
                index--;
                showPhoto();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (index < photos.size() - 1) {
                index++;
                showPhoto();
            }
        });

        // Click on "No tags" / tags line to show tag menu
        tagsText.setOnClickListener(v -> showTagMenuForCurrentPhoto());

        showPhoto();
    }

    private void showPhoto() {
        if (photos.isEmpty()) return;

        Photo p = photos.get(index);

        // Big image via Glide
        Uri uri = Uri.parse(p.getUriString());
        Glide.with(this)
                .load(uri)
                .centerInside()
                .into(imageFull);

        captionText.setText(p.getCaption());
        updateTagsLabel(p);
        updateButtonStates();
    }

    private void updateTagsLabel(Photo p) {
        List<Tag> tags = p.getTags();
        if (tags == null || tags.isEmpty()) {
            tagsText.setText("No tags");
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tags.size(); i++) {
            Tag t = tags.get(i);
            if (i > 0) sb.append(" | ");
            // use type/value names you already have
            sb.append(t.getType()).append(" = ").append(t.getValue());
        }
        tagsText.setText(sb.toString());
    }

    private void updateButtonStates() {
        btnPrev.setEnabled(index > 0);
        btnNext.setEnabled(index < photos.size() - 1);
    }

    // ====== Tag helpers ======

    private Photo getCurrentPhoto() {
        if (photos.isEmpty()) return null;
        return photos.get(index);
    }

    private void showTagMenuForCurrentPhoto() {
        Photo p = getCurrentPhoto();
        if (p == null) return;

        String[] options = {"Add Tag", "Delete Tag", "Cancel"};

        new AlertDialog.Builder(this)
                .setTitle("Tags")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        addTagForCurrentPhoto();
                    } else if (which == 1) {
                        deleteTagForCurrentPhoto();
                    }
                })
                .show();
    }

    private void addTagForCurrentPhoto() {
        Photo p = getCurrentPhoto();
        if (p == null) return;

        final String[] tagTypes = {"person", "location"};

        // Step 1: choose tag type
        new AlertDialog.Builder(this)
                .setTitle("Choose tag type")
                .setItems(tagTypes, (dialog, whichType) -> {
                    String type = tagTypes[whichType];

                    // Step 2: enter tag value
                    final android.widget.EditText input = new android.widget.EditText(this);
                    input.setHint("Enter " + type);

                    new AlertDialog.Builder(this)
                            .setTitle("Add " + type + " tag")
                            .setView(input)
                            .setPositiveButton("OK", (d2, w2) -> {
                                String value = input.getText().toString().trim();
                                if (value.isEmpty()) {
                                    Toast.makeText(this,
                                            "Value cannot be empty",
                                            Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                Tag newTag = new Tag(type, value);

                                if (p.getTags().contains(newTag)) {
                                    Toast.makeText(this,
                                            "Tag already exists",
                                            Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                p.addTag(newTag);
                                Storage.save(this, library);
                                updateTagsLabel(p);
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                })
                .show();
    }

    private void deleteTagForCurrentPhoto() {
        Photo p = getCurrentPhoto();
        if (p == null) return;

        List<Tag> tags = p.getTags();
        if (tags.isEmpty()) {
            Toast.makeText(this,
                    "No tags to delete",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Build list of strings to show
        String[] tagStrings = new String[tags.size()];
        for (int i = 0; i < tags.size(); i++) {
            Tag t = tags.get(i);
            tagStrings[i] = t.getType() + " = " + t.getValue();
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete which tag?")
                .setItems(tagStrings, (dialog, which) -> {
                    Tag toRemove = tags.get(which);
                    p.removeTag(toRemove);
                    Storage.save(this, library);
                    updateTagsLabel(p);
                })
                .show();
    }

    // ====== 3-dot overflow menu (Add/Delete Tag) ======

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_photo_view, menu);
        return true; // show the menu
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add_tag) {
            addTagForCurrentPhoto();
            return true;
        } else if (id == R.id.action_delete_tag) {
            deleteTagForCurrentPhoto();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Storage.save(this, library);
    }
}

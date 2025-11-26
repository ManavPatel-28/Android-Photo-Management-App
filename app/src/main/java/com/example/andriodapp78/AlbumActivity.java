package com.example.andriodapp78;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.andriodapp78.photos.model.Album;
import com.example.andriodapp78.photos.model.Photo;
import com.example.andriodapp78.photos.model.PhotoLibrary;
import com.example.andriodapp78.photos.storage.Storage;
import com.example.andriodapp78.photos.ui.PhotoListAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class AlbumActivity extends AppCompatActivity {

    public static final String EXTRA_ALBUM_NAME = "album_name";

    private PhotoLibrary library;
    private Album album;

    private RecyclerView recyclerPhotos;
    private PhotoListAdapter photoAdapter;

    // SAF OpenDocument picker
    private ActivityResultLauncher<String[]> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        // Load library for THIS activity
        library = Storage.load(this);

        // Find album by name
        String albumName = getIntent().getStringExtra(EXTRA_ALBUM_NAME);
        if (albumName != null) {
            album = library.findAlbum(albumName);
        }

        TextView title = findViewById(R.id.text_album_title);
        if (album != null) {
            title.setText("Album: " + album.getName());
        } else {
            title.setText("Album not found");
        }

        // ----- RecyclerView -----
        recyclerPhotos = findViewById(R.id.recycler_photos);
        recyclerPhotos.setLayoutManager(new LinearLayoutManager(this));

        List<Photo> existing = (album != null) ? album.getPhotos() : new ArrayList<>();

        photoAdapter = new PhotoListAdapter(
                existing,
                new PhotoListAdapter.OnPhotoClickListener() {
                    @Override
                    public void onPhotoClick(Photo photo) {
                        if (album == null || photo == null) {
                            return;
                        }

                        int index = album.getPhotos().indexOf(photo);
                        if (index < 0) {
                            return;
                        }

                        Intent intent = new Intent(AlbumActivity.this, PhotoViewActivity.class);
                        intent.putExtra(PhotoViewActivity.EXTRA_ALBUM_NAME, album.getName());
                        intent.putExtra(PhotoViewActivity.EXTRA_PHOTO_INDEX, index);
                        startActivity(intent);
                    }

                    @Override
                    public void onPhotoLongClick(Photo photo) {
                        showPhotoOptionsDialog(photo);
                    }
                });
        recyclerPhotos.setAdapter(photoAdapter);

        // ----- SAF picker -----
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri == null || album == null) return;

                    try {
                        getContentResolver().takePersistableUriPermission(
                                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } catch (SecurityException ignored) { }

                    String displayName = null;
                    try (Cursor cursor = getContentResolver().query(
                            uri,
                            new String[]{OpenableColumns.DISPLAY_NAME},
                            null, null, null
                    )) {
                        if (cursor != null && cursor.moveToFirst()) {
                            int idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                            if (idx >= 0) displayName = cursor.getString(idx);
                        }
                    } catch (Exception ignored) { }

                    Photo newPhoto = new Photo(uri.toString(), displayName);

                    if (album.containsPhoto(newPhoto)) {
                        Toast.makeText(AlbumActivity.this,
                                "That photo is already in this album",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 1) update model
                    album.addPhoto(newPhoto);
                    Storage.save(AlbumActivity.this, library);

                    // 2) update UI list (adapter’s own copy)
                    photoAdapter.addPhoto(newPhoto);
                }
        );

        // FAB add photo
        FloatingActionButton fabAdd = findViewById(R.id.fab_add_photo);
        fabAdd.setOnClickListener(v -> {
            if (album == null) {
                Toast.makeText(this,
                        "Album not found, cannot add photo",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            pickImageLauncher.launch(new String[]{"image/*"});
        });
    }

    // ====== Photo options (Move / Delete) on long press ======
    private void showPhotoOptionsDialog(Photo photo) {
        String[] options = {"Move Photo", "Delete Photo", "Cancel"};

        new AlertDialog.Builder(this)
                .setTitle("Photo options")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showMovePhotoDialog(photo);
                    } else if (which == 1) {
                        deletePhoto(photo);
                    }
                })
                .show();
    }

    // Choose destination album and move
    private void showMovePhotoDialog(Photo photo) {
        if (album == null || photo == null) return;

        List<Album> allAlbums = library.getAlbums();
        List<Album> targets = new ArrayList<>();

        for (Album a : allAlbums) {
            if (!a.equals(album)) {
                targets.add(a);
            }
        }

        if (targets.isEmpty()) {
            Toast.makeText(this,
                    "No other albums to move to",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String[] names = new String[targets.size()];
        for (int i = 0; i < targets.size(); i++) {
            names[i] = targets.get(i).getName();
        }

        new AlertDialog.Builder(this)
                .setTitle("Move photo to:")
                .setItems(names, (dialog, which) -> {
                    Album dest = targets.get(which);
                    movePhotoToAlbum(photo, dest);
                })
                .show();
    }

    private void movePhotoToAlbum(Photo photo, Album dest) {
        if (album == null || dest == null) return;

        if (dest.containsPhoto(photo)) {
            Toast.makeText(this,
                    "Destination album already has that photo",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Add to destination
        dest.addPhoto(photo);
        // Remove from current
        album.removePhoto(photo);

        // Persist
        Storage.save(this, library);

        // Update current album UI
        photoAdapter.removePhoto(photo);

        Toast.makeText(this,
                "Moved photo to \"" + dest.getName() + "\"",
                Toast.LENGTH_SHORT).show();
    }

    private void deletePhoto(Photo photo) {
        if (album == null || photo == null) return;

        if (!album.containsPhoto(photo)) {
            Toast.makeText(this,
                    "Photo not found in album",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        album.removePhoto(photo);
        Storage.save(this, library);
        photoAdapter.removePhoto(photo);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload from disk in case something changed & refresh adapter
        library = Storage.load(this);
        if (album != null) {
            Album fresh = library.findAlbum(album.getName());
            if (fresh != null) {
                album = fresh;
                photoAdapter.replaceAll(album.getPhotos());
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Storage.save(this, library);
    }
}

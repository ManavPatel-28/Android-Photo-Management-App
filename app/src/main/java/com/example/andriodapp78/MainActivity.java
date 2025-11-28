package com.example.andriodapp78;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.andriodapp78.photos.model.Album;
import com.example.andriodapp78.photos.model.PhotoLibrary;
import com.example.andriodapp78.photos.storage.Storage;
import com.example.andriodapp78.photos.ui.AlbumListAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private PhotoLibrary library;

    // Separate mutable list used by the adapter
    private final List<Album> albums = new ArrayList<>();

    private RecyclerView recyclerView;
    private AlbumListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ----- Toolbar with title + 3-dot menu -----
        Toolbar toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Android Photos App");
        }

        // ----- RecyclerView -----
        recyclerView = findViewById(R.id.recycler_albums);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Load library and fill adapter list
        library = Storage.load(this);
        albums.clear();
        albums.addAll(library.getAlbums());

        adapter = new AlbumListAdapter(albums, new AlbumListAdapter.OnAlbumClickListener() {
            @Override
            public void onAlbumClick(Album album) {
                Intent i = new Intent(MainActivity.this, AlbumActivity.class);
                i.putExtra(AlbumActivity.EXTRA_ALBUM_NAME, album.getName());
                startActivity(i);
            }

            @Override
            public void onAlbumLongClick(Album album) {
                // Optional: keep long-press menu on each album row
                showAlbumOptionsDialog(album);
            }
        });
        recyclerView.setAdapter(adapter);

        // ----- FAB: add album -----
        FloatingActionButton fabAdd = findViewById(R.id.fab_add_album);
        fabAdd.setOnClickListener(v -> showAddAlbumDialog());

        // ----- FAB: open SearchActivity -----
        FloatingActionButton fabSearch = findViewById(R.id.fab_search);
        fabSearch.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            startActivity(intent);
        });
    }

    // When we come back from an album, reload from disk so counts are correct
    @Override
    protected void onResume() {
        super.onResume();
        library = Storage.load(this);
        albums.clear();
        albums.addAll(library.getAlbums());
        adapter.notifyDataSetChanged();
    }

    // ---------- OLD dialog helpers (per-album) ----------

    private void showAlbumOptionsDialog(Album album) {
        String[] options = {"Rename", "Delete", "Cancel"};

        new AlertDialog.Builder(this)
                .setTitle(album.getName())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showRenameDialog(album);
                    } else if (which == 1) {
                        confirmDeleteAlbum(album);
                    }
                })
                .show();
    }

    private void showRenameDialog(Album album) {
        final EditText input = new EditText(this);
        input.setText(album.getName());

        new AlertDialog.Builder(this)
                .setTitle("Rename Album")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newName = input.getText().toString().trim();
                    if (newName.isEmpty()) {
                        Toast.makeText(this,
                                "Album name cannot be empty",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Use helper in PhotoLibrary to avoid duplicates
                    if (!library.renameAlbum(album, newName)) {
                        Toast.makeText(this,
                                "Another album already has that name",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Storage.save(this, library);

                    int idx = albums.indexOf(album);
                    if (idx >= 0) {
                        adapter.notifyItemChanged(idx);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmDeleteAlbum(Album album) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Album")
                .setMessage("Delete album \"" + album.getName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    int index = albums.indexOf(album);
                    if (index < 0) {
                        Toast.makeText(this,
                                "Album not found",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 1) remove from our adapter list
                    albums.remove(index);

                    // 2) remove from model
                    library.removeAlbum(album);

                    // 3) persist + notify adapter
                    Storage.save(this, library);
                    adapter.notifyItemRemoved(index);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAddAlbumDialog() {
        final EditText input = new EditText(this);
        input.setHint("Album name");

        new AlertDialog.Builder(this)
                .setTitle("New Album")
                .setView(input)
                .setPositiveButton("Create", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this,
                                "Album name cannot be empty",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (library.findAlbum(name) != null) {
                        Toast.makeText(this,
                                "Album with that name already exists",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Album a = new Album(name);

                    // 1) update model
                    library.addAlbum(a);
                    Storage.save(this, library);

                    // 2) update adapter list
                    albums.add(a);
                    adapter.notifyItemInserted(albums.size() - 1);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Storage.save(this, library);
    }

    // ---------- Toolbar 3-dot menu: rename/delete album ----------

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_rename_album) {
            showChooseAlbumThenRename();
            return true;
        } else if (id == R.id.action_delete_album) {
            showChooseAlbumThenDelete();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showChooseAlbumThenRename() {
        if (albums.isEmpty()) {
            Toast.makeText(this, "No albums to rename", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] names = new String[albums.size()];
        for (int i = 0; i < albums.size(); i++) {
            names[i] = albums.get(i).getName();
        }

        new AlertDialog.Builder(this)
                .setTitle("Rename which album?")
                .setItems(names, (dialog, which) -> {
                    Album chosen = albums.get(which);
                    showRenameDialog(chosen);
                })
                .show();
    }

    private void showChooseAlbumThenDelete() {
        if (albums.isEmpty()) {
            Toast.makeText(this, "No albums to delete", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] names = new String[albums.size()];
        for (int i = 0; i < albums.size(); i++) {
            names[i] = albums.get(i).getName();
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete which album?")
                .setItems(names, (dialog, which) -> {
                    Album chosen = albums.get(which);
                    confirmDeleteAlbum(chosen);
                })
                .show();
    }
}

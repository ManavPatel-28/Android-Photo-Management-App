package com.example.andriodapp78;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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
                showAlbumOptionsDialog(album);
            }
        });
        recyclerView.setAdapter(adapter);

        // Existing FAB: create album
        FloatingActionButton fabAdd = findViewById(R.id.fab_add_album);
        fabAdd.setOnClickListener(v -> showAddAlbumDialog());

        // NEW FAB: open SearchActivity
        FloatingActionButton fabSearch = findViewById(R.id.fab_search);
        if (fabSearch != null) {
            fabSearch.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);
            });
        }
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

    // ===== toolbar menu for Search (kept – harmless even if you use FAB) =====
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            Intent intent = new Intent(this, SearchActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

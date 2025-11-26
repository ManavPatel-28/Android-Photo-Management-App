package com.example.andriodapp78.photos.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PhotoLibrary implements Serializable {

    private static final long serialVersionUID = 1L;

    // master list of albums
    private final List<Album> albums = new ArrayList<>();

    /** Return the *mutable* list so adapters can modify it. */
    public List<Album> getAlbums() {
        return albums;
    }

    public void addAlbum(Album a) {
        if (a != null && !albums.contains(a)) {
            albums.add(a);
        }
    }

    public void removeAlbum(Album a) {
        albums.remove(a);
    }

    public Album findAlbum(String name) {
        if (name == null) return null;
        String n = name.toLowerCase(Locale.ROOT);
        for (Album a : albums) {
            if (a.getName().toLowerCase(Locale.ROOT).equals(n))
                return a;
        }
        return null;
    }

    public boolean renameAlbum(Album a, String newName) {
        if (a == null || newName == null) return false;
        String target = newName.toLowerCase(Locale.ROOT);

        for (Album x : albums) {
            if (x != a && x.getName().toLowerCase(Locale.ROOT).equals(target))
                return false; // duplicate
        }
        a.setName(newName);
        return true;
    }
}

package com.example.andriodapp78.photos.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Album implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    // master list of photos for this album
    private final List<Photo> photos = new ArrayList<>();

    public Album(String name) {
        setName(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String newName) {
        if (newName == null || newName.trim().isEmpty())
            throw new IllegalArgumentException("Album name cannot be empty");
        this.name = newName.trim();
    }

    /** Return the *mutable* list so adapters can modify it. */
    public List<Photo> getPhotos() {
        return photos;
    }

    public void addPhoto(Photo p) {
        if (p != null && !photos.contains(p)) {
            photos.add(p);
        }
    }

    public void removePhoto(Photo p) {
        photos.remove(p);
    }

    public boolean containsPhoto(Photo p) {
        return photos.contains(p);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Album)) return false;
        Album a = (Album) o;
        return name.equalsIgnoreCase(a.name);
    }

    @Override
    public int hashCode() {
        return name.toLowerCase(Locale.ROOT).hashCode();
    }
}

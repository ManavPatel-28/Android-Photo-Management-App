package com.example.andriodapp78.photos.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class Photo implements Serializable {

    private static final long serialVersionUID = 1L;

    // Where the image actually lives (content://, file://, etc.)
    private final String uriString;

    // Human-readable caption (filename stand-in)
    private String caption = "";

    // Your tag list from earlier project
    private final List<Tag> tags = new ArrayList<>();

    // Main constructor: uri + optional caption
    public Photo(String uriString, String caption) {
        if (uriString == null) {
            throw new IllegalArgumentException("uriString cannot be null");
        }
        this.uriString = uriString;

        if (caption == null || caption.isBlank()) {
            this.caption = deriveCaptionFromUri(uriString);
        } else {
            this.caption = caption.trim();
        }
    }

    // Convenience constructor used in older code: only URI
    public Photo(String uriString) {
        this(uriString, null);
    }

    /** Raw URI string (for loading image) */
    public String getUriString() {
        return uriString;
    }

    /** Caption to show in lists / viewer (filename without path) */
    public String getCaption() {
        return caption;
    }

    /** Allow editing caption later (if you want to support it) */
    public void setCaption(String c) {
        if (c == null || c.isBlank()) {
            this.caption = deriveCaptionFromUri(uriString);
        } else {
            this.caption = c.trim();
        }
    }

    /** Tags API (same as before) */
    public List<Tag> getTags() {
        return Collections.unmodifiableList(tags);
    }

    public void addTag(Tag tag) {
        if (tag != null && !tags.contains(tag)) {
            tags.add(tag);
        }
    }

    public void removeTag(Tag tag) {
        tags.remove(tag);
    }

    /** Helper to turn URI into a nice short name */
    private static String deriveCaptionFromUri(String uri) {
        String s = uri;

        // Drop everything before the last '/'
        int slash = s.lastIndexOf('/');
        if (slash >= 0 && slash < s.length() - 1) {
            s = s.substring(slash + 1);
        }

        // For content URIs we sometimes get weird encodings like "raw%3A..."
        // but if Android gave us a nice DISPLAY_NAME we won’t use this path anyway.

        // Strip extension if present
        int dot = s.lastIndexOf('.');
        if (dot > 0) {
            s = s.substring(0, dot);
        }

        return s;
    }

    // Equality: same underlying URI (case-insensitive)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Photo)) return false;
        Photo p = (Photo) o;
        return uriString.toLowerCase(Locale.ROOT)
                .equals(p.uriString.toLowerCase(Locale.ROOT));
    }

    @Override
    public int hashCode() {
        return Objects.hash(uriString.toLowerCase(Locale.ROOT));
    }
}

package com.example.andriodapp78.photos.model;

import java.io.Serializable;
import java.util.Locale;
import java.util.Objects;

public class Tag implements Serializable {

    private static final long serialVersionUID = 1L;

    // "person" or "location"
    private final String type;
    private final String value;

    public Tag(String type, String value) {
        if (type == null || value == null) {
            throw new IllegalArgumentException("type/value cannot be null");
        }

        String t = type.trim().toLowerCase(Locale.ROOT);
        if (!t.equals("person") && !t.equals("location")) {
            throw new IllegalArgumentException("Invalid tag type: " + type);
        }

        this.type = t;
        this.value = value.trim();
    }

    // Used by other parts of your code
    public String getType() {
        return type;
    }

    // ✅ Alias so PhotoViewActivity.t.getName() compiles
    public String getName() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public String getValueLower() {
        return value.toLowerCase(Locale.ROOT);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tag)) return false;
        Tag tag = (Tag) o;
        return type.equals(tag.type)
                && getValueLower().equals(tag.getValueLower());
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, getValueLower());
    }

    @Override
    public String toString() {
        return type + ": " + value;
    }
}

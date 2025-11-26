package com.example.andriodapp78.photos.storage;

import android.content.Context;

import com.example.andriodapp78.photos.model.PhotoLibrary;

import java.io.*;

public class Storage {

    private static final String FILE_NAME = "photos_library.ser";

    public static PhotoLibrary load(Context ctx) {
        try (ObjectInputStream ois =
                     new ObjectInputStream(ctx.openFileInput(FILE_NAME))) {
            Object obj = ois.readObject();
            if (obj instanceof PhotoLibrary) {
                return (PhotoLibrary) obj;
            }
        } catch (Exception e) {
            e.printStackTrace();   // <-- see errors in Logcat instead of silently ignoring
        }
        return new PhotoLibrary();
    }

    public static void save(Context ctx, PhotoLibrary lib) {
        try (ObjectOutputStream oos =
                     new ObjectOutputStream(ctx.openFileOutput(FILE_NAME, Context.MODE_PRIVATE))) {
            oos.writeObject(lib);
        } catch (Exception e) {
            e.printStackTrace();   // <-- don’t silently ignore save failures
        }
    }
}

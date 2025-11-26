# AndriodApp78

📸 AndroidApp78 — Android Photos Application

CS 213 — Fall 2025
Group 78 • Android Studio • SDK 34 • Java 17

Authors / Team
Name	GitHub
Manav Patel	github.com/Manavpatel2811
Akshar Patel	github.com/aksharpatel1

📱 Overview

This project is an Android port of the JavaFX “Photos” application.
The Android version supports a single user, as the app runs on a personal phone.
All data is saved locally using serialization.

The application provides:

Album management

Photo display (full size + thumbnails)

Tagging system

Moving photos between albums

Search by tags with AND/OR & Auto-Complete

Slideshow (manual navigation)


Project Structure
AndroidApp78/
├── app/
│   ├── src/main/java/com/example/andriodapp78/
│   │   ├── MainActivity.java                 # Home screen
│   │   ├── AlbumActivity.java                # Album photo list
│   │   ├── PhotoViewActivity.java            # Full-size viewer + slideshow
│   │   ├── SearchActivity.java               # Search UI + auto-complete
│   │   ├── photos/model/
│   │   │   ├── Album.java
│   │   │   ├── Photo.java
│   │   │   ├── Tag.java
│   │   │   └── PhotoLibrary.java
│   │   ├── photos/storage/
│   │   │   └── Storage.java
│   │   ├── photos/ui/
│   │   │   ├── AlbumListAdapter.java
│   │   │   ├── PhotoListAdapter.java
│   │   │   ├── SearchResultsAdapter.java
│   │   │   └── ViewHolders.java
│   ├── res/layout/
│   │   ├── activity_main.xml
│   │   ├── activity_album.xml
│   │   ├── activity_photo_view.xml
│   │   ├── activity_search.xml
│   │   ├── item_album.xml
│   │   ├── item_photo.xml
│   │   └── item_search_result.xml
│   ├── res/menu/
│   │   ├── menu_main.xml
│   │   └── menu_photo_view.xml
│   ├── res/values/
│       ├── strings.xml
│       ├── colors.xml
│       └── themes.xml
└── README.md


Data Storage

All data stored via Java serialization inside internal app storage.

Saves automatically on:

Add/Delete Album

Add/Delete Photo

Add/Delete Tag

Move Photo

No user login, so single library file persists across sessions.






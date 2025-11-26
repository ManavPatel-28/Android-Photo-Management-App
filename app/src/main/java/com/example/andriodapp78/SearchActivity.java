package com.example.andriodapp78;

import android.os.Bundle;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.andriodapp78.photos.model.Album;
import com.example.andriodapp78.photos.model.Photo;
import com.example.andriodapp78.photos.model.PhotoLibrary;
import com.example.andriodapp78.photos.model.Tag;
import com.example.andriodapp78.photos.storage.Storage;
import com.example.andriodapp78.photos.ui.SearchResultsAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class SearchActivity extends AppCompatActivity {

    private PhotoLibrary library;

    private AutoCompleteTextView editTagValue1;
    private AutoCompleteTextView editTagValue2;
    private RadioGroup groupType1;
    private RadioGroup groupType2;
    private RadioGroup groupAndOr;
    private Button buttonSearch;
    private RecyclerView recyclerResults;

    private SearchResultsAdapter resultsAdapter;
    private final List<SearchResult> searchResults = new ArrayList<>();

    // auto-complete adapters
    private android.widget.ArrayAdapter<String> personAdapter;
    private android.widget.ArrayAdapter<String> locationAdapter;

    // Simple DTO for a match
    public static class SearchResult {
        public final String albumName;
        public final Photo photo;
        public final int indexInAlbum;

        public SearchResult(String albumName, Photo photo, int indexInAlbum) {
            this.albumName = albumName;
            this.photo = photo;
            this.indexInAlbum = indexInAlbum;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // load full library
        library = Storage.load(this);

        editTagValue1 = findViewById(R.id.edit_tag_value1);
        editTagValue2 = findViewById(R.id.edit_tag_value2);
        groupType1    = findViewById(R.id.group_type1);
        groupType2    = findViewById(R.id.group_type2);
        groupAndOr    = findViewById(R.id.group_and_or);
        buttonSearch  = findViewById(R.id.button_search);
        recyclerResults = findViewById(R.id.recycler_results);

        recyclerResults.setLayoutManager(new LinearLayoutManager(this));
        // use your existing adapter constructor
        resultsAdapter = new SearchResultsAdapter(searchResults, this, library);
        recyclerResults.setAdapter(resultsAdapter);

        setupAutoCompleteAdapters();
        setupTypeRadioListeners();

        buttonSearch.setOnClickListener(v -> runSearch());
    }

    // Build person/location suggestion lists from all tags
    private void setupAutoCompleteAdapters() {
        HashSet<String> persons = new HashSet<>();
        HashSet<String> locations = new HashSet<>();

        for (Album a : library.getAlbums()) {
            for (Photo p : a.getPhotos()) {
                for (Tag t : p.getTags()) {
                    if ("person".equals(t.getType())) {
                        persons.add(t.getValue());
                    } else if ("location".equals(t.getType())) {
                        locations.add(t.getValue());
                    }
                }
            }
        }

        personAdapter = new android.widget.ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                new ArrayList<>(persons)
        );
        locationAdapter = new android.widget.ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                new ArrayList<>(locations)
        );

        // default: first query is person, second is location (matches your XML defaults)
        applyAdapterForField(editTagValue1, true);
        applyAdapterForField(editTagValue2, false);
    }

    private void applyAdapterForField(AutoCompleteTextView field, boolean isPerson) {
        if (isPerson) {
            field.setAdapter(personAdapter);
        } else {
            field.setAdapter(locationAdapter);
        }
    }

    // When the radio buttons change, switch which suggestions we use
    private void setupTypeRadioListeners() {
        groupType1.setOnCheckedChangeListener((group, checkedId) -> {
            boolean isPerson = (checkedId == R.id.radio_person1);
            applyAdapterForField(editTagValue1, isPerson);
        });

        groupType2.setOnCheckedChangeListener((group, checkedId) -> {
            boolean isPerson = (checkedId == R.id.radio_person2);
            applyAdapterForField(editTagValue2, isPerson);
        });
    }

    private void runSearch() {
        String value1 = editTagValue1.getText().toString().trim();
        String value2 = editTagValue2.getText().toString().trim();

        if (value1.isEmpty()) {
            Toast.makeText(this, "Please enter at least tag value 1", Toast.LENGTH_SHORT).show();
            return;
        }

        // Determine tag types (person/location) from radio buttons
        String type1 = (groupType1.getCheckedRadioButtonId() == R.id.radio_person1)
                ? "person" : "location";

        boolean hasSecondTag = !value2.isEmpty()
                && (groupType2.getCheckedRadioButtonId() == R.id.radio_person2
                || groupType2.getCheckedRadioButtonId() == R.id.radio_location2);

        String type2 = null;
        if (hasSecondTag) {
            type2 = (groupType2.getCheckedRadioButtonId() == R.id.radio_person2)
                    ? "person" : "location";
        }

        boolean useAnd = (groupAndOr.getCheckedRadioButtonId() == R.id.radio_and);

        String q1 = value1.toLowerCase(Locale.ROOT);
        String q2 = hasSecondTag ? value2.toLowerCase(Locale.ROOT) : null;

        searchResults.clear();

        for (Album album : library.getAlbums()) {
            List<Photo> photos = album.getPhotos();
            for (int i = 0; i < photos.size(); i++) {
                Photo p = photos.get(i);

                boolean match1 = matchesPhoto(p, type1, q1);
                boolean match2 = hasSecondTag && matchesPhoto(p, type2, q2);

                boolean keep;
                if (!hasSecondTag) {
                    keep = match1;
                } else if (useAnd) {
                    keep = match1 && match2;   // conjunction
                } else {
                    keep = match1 || match2;   // disjunction (OR)
                }

                if (keep) {
                    searchResults.add(new SearchResult(album.getName(), p, i));
                }
            }
        }

        resultsAdapter.notifyDataSetChanged();

        if (searchResults.isEmpty()) {
            Toast.makeText(this, "No matching photos found", Toast.LENGTH_SHORT).show();
        }
    }

    // Case-insensitive prefix match for the given type/value
    private boolean matchesPhoto(Photo p, String type, String queryLower) {
        for (Tag t : p.getTags()) {
            if (!type.equals(t.getType())) continue;
            String valLower = t.getValueLower(); // already lowercase
            if (valLower.startsWith(queryLower)) {
                return true;
            }
        }
        return false;
    }
}

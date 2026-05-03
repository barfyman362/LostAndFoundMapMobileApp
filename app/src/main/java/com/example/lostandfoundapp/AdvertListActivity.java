package com.example.lostandfoundapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class AdvertListActivity extends AppCompatActivity {

    Spinner spinnerFilter;
    ListView listViewAdverts;

    DatabaseHelper dbHelper;
    ArrayList<Advert> adverts;
    ArrayList<String> advertTextList;

    String[] filters = {"All", "Electronics", "Pets", "Wallets", "Keys", "Clothing", "Documents", "Other"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advert_list);

        dbHelper = new DatabaseHelper(this);

        spinnerFilter = findViewById(R.id.spinnerFilter);
        listViewAdverts = findViewById(R.id.listViewAdverts);

        setUpFilterSpinner();

        listViewAdverts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Advert advert = adverts.get(position);

                Intent intent = new Intent(AdvertListActivity.this, AdvertDetailActivity.class);
                intent.putExtra("advertId", advert.id);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        String selectedFilter = spinnerFilter.getSelectedItem().toString();
        loadAdverts(selectedFilter);
    }

    // reloads the list whenever a category is selected
    private void setUpFilterSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                filters
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(adapter);

        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                loadAdverts(filters[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                loadAdverts("All");
            }
        });
    }

    // loads either all adverts or only the chosen category
    private void loadAdverts(String filter) {
        if (filter.equals("All")) {
            adverts = dbHelper.getAllAdverts();
        } else {
            adverts = dbHelper.getAdvertsByCategory(filter);
        }

        advertTextList = new ArrayList<>();

        for (int i = 0; i < adverts.size(); i++) {
            Advert advert = adverts.get(i);

            String text = advert.type + ": " + advert.description
                    + "\nCategory: " + advert.category
                    + "\nLocation: " + advert.location
                    + "\nPosted: " + advert.postedTime;

            advertTextList.add(text);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                advertTextList
        );

        listViewAdverts.setAdapter(adapter);

        if (adverts.isEmpty()) {
            Toast.makeText(this, "No adverts to show", Toast.LENGTH_SHORT).show();
        }
    }
}
package com.example.lostandfoundapp;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.InputStream;

public class AdvertDetailActivity extends AppCompatActivity {

    ImageView imageAdvert;
    TextView textType;
    TextView textName;
    TextView textPhone;
    TextView textDescription;
    TextView textCategory;
    TextView textDate;
    TextView textLocation;
    TextView textPostedTime;
    Button btnRemove;

    DatabaseHelper dbHelper;
    int advertId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advert_detail);

        dbHelper = new DatabaseHelper(this);

        imageAdvert = findViewById(R.id.imageAdvert);
        textType = findViewById(R.id.textType);
        textName = findViewById(R.id.textName);
        textPhone = findViewById(R.id.textPhone);
        textDescription = findViewById(R.id.textDescription);
        textCategory = findViewById(R.id.textCategory);
        textDate = findViewById(R.id.textDate);
        textLocation = findViewById(R.id.textLocation);
        textPostedTime = findViewById(R.id.textPostedTime);
        btnRemove = findViewById(R.id.btnRemove);

        advertId = getIntent().getIntExtra("advertId", -1);

        if (advertId == -1) {
            Toast.makeText(this, "Could not load advert", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadAdvert();

        btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeAdvert();
            }
        });
    }

    // loads advert data from SQLite and displays it on screen
    private void loadAdvert() {
        Advert advert = dbHelper.getAdvert(advertId);

        if (advert == null) {
            Toast.makeText(this, "Advert not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        textType.setText("Type: " + advert.type);
        textName.setText("Name: " + advert.name);
        textPhone.setText("Phone: " + advert.phone);
        textDescription.setText("Description: " + advert.description);
        textCategory.setText("Category: " + advert.category);
        textDate.setText("Date: " + advert.date);
        textLocation.setText("Location: " + advert.location);
        textPostedTime.setText("Posted: " + advert.postedTime);

        if (advert.image != null && !advert.image.isEmpty()) {
            try {
                Uri imageUri = Uri.parse(advert.image);

                InputStream inputStream = getContentResolver().openInputStream(imageUri);

                if (inputStream != null) {
                    inputStream.close();
                    imageAdvert.setImageURI(imageUri);
                }
            } catch (Exception e) {
                imageAdvert.setImageResource(android.R.drawable.ic_menu_report_image);
                Toast.makeText(this, "Could not load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // removes the advert once the item has been dealt with
    private void removeAdvert() {
        boolean deleted = dbHelper.deleteAdvert(advertId);

        if (deleted) {
            Toast.makeText(this, "Advert removed", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Could not remove advert", Toast.LENGTH_SHORT).show();
        }
    }
}
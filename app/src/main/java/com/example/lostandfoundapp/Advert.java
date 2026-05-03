package com.example.lostandfoundapp;

// basic class to hold one lost/found advert
public class Advert {

    int id;
    String type;
    String name;
    String phone;
    String description;
    String category;
    String date;
    String location;
    String image;
    String postedTime;

    // added for the map part of task 9.1P
    double latitude;
    double longitude;

    public Advert(int id, String type, String name, String phone, String description,
                  String category, String date, String location, String image,
                  String postedTime, double latitude, double longitude) {

        this.id = id;
        this.type = type;
        this.name = name;
        this.phone = phone;
        this.description = description;
        this.category = category;
        this.date = date;
        this.location = location;
        this.image = image;
        this.postedTime = postedTime;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
package com.example.finalproject;

import java.io.Serializable;

public class Book implements Serializable {
    private String title;
    private String author;
    private int totalPages;
    private int readPages;
    private float rating;
    private double progress;

    private String imageUri;


    public Book(String title, String author, int totalPages, int readPages, float rating, double progress, String imageUri) {
        this.title = title;
        this.author = author;
        this.totalPages = totalPages;
        this.readPages = readPages;
        this.rating = rating;
        this.progress = progress;
        this.imageUri = imageUri;
    }


    //Getter 메소드
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public int getTotalPages() { return totalPages; }
    public int getReadPages() { return readPages; }
    public float getRating() { return rating; }
    public double getProgress() { return progress; }

    public String getImageUri() { return imageUri; }
}

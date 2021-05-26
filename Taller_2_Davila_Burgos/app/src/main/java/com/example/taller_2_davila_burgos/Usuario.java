package com.example.taller_2_davila_burgos;

public class Usuario {
    String uid;
    String name;
    String lastName;
    String email;
    String password;
    Long document;
    double latitude;
    double longitude;
    boolean disponible;

    public Usuario(String uid, String name, String lastName, String email, String password, Long document, double latitude, double longitude, boolean disponible){
        this.uid = uid;
        this.name = name;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.document = document;
        this.latitude = latitude;
        this.longitude = longitude;
        this.disponible = disponible;
    }
    public Usuario(){

    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getDocument() {
        return document;
    }

    public void setDocument(Long document) {
        this.document = document;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public boolean getDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }
}


package com.example.mrero.accident;

public class Accident {
    private String title;
    private String desc;
    private String image;
    private String username;
    private String address;
    private double lat;
    private double lng;
    private long date;

    public Accident() {

    }

    public Accident(String title, String desc, String image, String username, String address,
                       double lat, double lng, long date) {
        this.title = title;
        this.desc = desc;
        this.image = image;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
        this.username = username;
        this.date = date;
    }

    public Accident(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long dateInMillis) {
        this.date = dateInMillis;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}

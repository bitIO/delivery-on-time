package com.proyectodot.enterprise;

import com.google.android.gms.maps.model.LatLng;

public class RouteWayPoint {
    private String address;
    private String city;
    private String province;
    private String email;
    private String country = "Spain";
    private LatLng latLng;

    public RouteWayPoint() {

    }

    public RouteWayPoint(String address, String city, String province, String email) {
        this.address = address;
        this.city = city;
        this.province = province;
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return address + ", " + city + ", " + province + ", " + country;
    }
}

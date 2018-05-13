package com.proyectodot.enterprise;

public class RouteWayPoint {
    private String address;
    private String city;
    private String province;
    private String country = "Spain";

    public RouteWayPoint() {

    }

    public RouteWayPoint(String address, String city, String province) {
        this.address = address;
        this.city = city;
        this.province = province;
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

    @Override
    public String toString() {
        return address + ", " + city + ", " + province + ", " + country ;
    }
}

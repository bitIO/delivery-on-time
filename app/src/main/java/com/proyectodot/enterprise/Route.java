package com.proyectodot.enterprise;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class Route {
    private String name;
    private ArrayList<RouteWayPoint> waypoints;
    private List<LatLng> polyline;

    public Route() {
    }

    public Route(String name, ArrayList<RouteWayPoint> waypoints) {
        this.name = name;
        this.waypoints = waypoints;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<RouteWayPoint> getWaypoints() {
        return waypoints;
    }

    public void setWaypoints(ArrayList<RouteWayPoint> waypoints) {
        this.waypoints = waypoints;
    }

    public List<LatLng> getPolyline() {
        return polyline;
    }

    public void setPolyline(List<LatLng> polyline) {
        this.polyline = polyline;
    }
}

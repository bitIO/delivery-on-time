package com.proyectodot.enterprise;

import java.util.ArrayList;

public class Route {
    private String name;
    private ArrayList<RouteWayPoint> waypoints;

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
}

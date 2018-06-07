package com.proyectodot.enterprise;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Route {
    private String id;
    private String name;
    private ArrayList<RouteWayPoint> waypoints;
    private List<LatLng> polyline;
    private String assigned;

    public static final Route parse(DataSnapshot snapshot) {
        Route r = new Route();
        r.setId(snapshot.getKey());

        Iterable<DataSnapshot> it = snapshot.getChildren();
        for (DataSnapshot child: it) {
            switch (child.getKey()) {
                case "name":
                    r.setName((String)child.getValue());
                    break;

                case "waypoints":
                    ArrayList<RouteWayPoint> rwps = new ArrayList<>();
                    ArrayList<HashMap<String, Object>> wps = (ArrayList<HashMap<String, Object>>)child.getValue();
                    for (int i = 0; i < wps.size(); i++) {
                        HashMap<String, Object> data = wps.get(i);
                        RouteWayPoint rwp = new RouteWayPoint(
                                data.get("address").toString(),
                                data.get("city").toString(),
                                data.get("province").toString(),
                                data.get("email").toString()
                        );
                        if (data.get("latLng") != null) {
                            rwp.setLatLng(new LatLng(
                                    ((HashMap<String, Double>) data.get("latLng")).get("latitude"),
                                    ((HashMap<String, Double>) data.get("latLng")).get("longitude")
                            ));
                            rwps.add(rwp);
                        }
                    }

                    r.setWaypoints(rwps);
                    break;

                case "polyline":
                    ArrayList<LatLng> p = new ArrayList<>();
                    ArrayList<HashMap<String, Double>> points = (ArrayList<HashMap<String, Double>>)child.getValue();
                    for (int i = 0; i < points.size(); i++) {
                        try {
                            HashMap<String, Double> data = points.get(i);
                            Double lat = data.get("latitude");
                            Double lon = data.get("longitude");
                            p.add(new LatLng(lat, lon));
                        } catch (NumberFormatException e) {
                            Log.e("DOT", e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    r.setPolyline(p);
                    break;

                case "assigned":
                    r.setAssigned((String)child.getValue());
                    break;
                default:
                    break;
            }
        }
        return r;
    }

    public Route() {
    }

    public Route(String name, ArrayList<RouteWayPoint> waypoints) {
        this.name = name;
        this.waypoints = waypoints;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getAssigned() {
        return assigned;
    }

    public void setAssigned(String assigned) {
        this.assigned = assigned;
    }

    @Override
    public String toString() {
        return name + "( contiene " + waypoints.size() + " paradas )";
    }
}

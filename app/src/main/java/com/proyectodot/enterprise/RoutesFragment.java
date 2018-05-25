package com.proyectodot.enterprise;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.android.PolyUtil;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.TravelMode;
import com.woxthebox.draglistview.DragItemAdapter;
import com.woxthebox.draglistview.DragListView;

import org.joda.time.DateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class RoutesFragment extends Fragment implements OnMapReadyCallback, View.OnClickListener {

    private static final int overview = 0;

    FirebaseDatabase database;
    DatabaseReference firebaseData;

    // Map
    GoogleMap mGoogleMap;
    MapView mMapView;
    View mView;

    // List
    private ScrollView lRoutesListScrollView;
    private DragListView lAvailableRoutes;

    // form create route
    ScrollView lRoutesFormCreate;
    TextView tRouteName;
    TextView tAddress;
    TextView tCity;
    TextView tProvince;
    DragListView lRouteWayPoints;
    ArrayList<RouteWayPoint> mRouteWayPoints = new ArrayList<>();
    Button bButtonSaveRoute;

    Route currentRoute;
    ArrayList<Route> availableRoutes;

    public RoutesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_routes, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        database = FirebaseDatabase.getInstance();
        firebaseData = database.getReference("routes/");

        // map
        mMapView = mView.findViewById(R.id.map);
        if (mMapView != null) {
            mMapView.onCreate(null);
            mMapView.onResume();
            mMapView.getMapAsync(this);
        }

        // elements
        lRoutesFormCreate = view.findViewById(R.id.routesFormCreate);
        tRouteName = view.findViewById(R.id.editTextName);
        tAddress = view.findViewById(R.id.editTextAddress);
        tCity= view.findViewById(R.id.editTextCity);
        tProvince= view.findViewById(R.id.editTextProvince);
        bButtonSaveRoute = view.findViewById(R.id.buttonSaveRoute);
        lRouteWayPoints = view.findViewById(R.id.dragListRouteItems);
        lAvailableRoutes= view.findViewById(R.id.dragListAvailableRoutes);
        lRoutesListScrollView = view.findViewById(R.id.routesListScrollView);

        initDragList();

        // listeners
        view.findViewById(R.id.buttonListRoutes).setOnClickListener(this);
        view.findViewById(R.id.buttonNewRoute).setOnClickListener(this);
        view.findViewById(R.id.buttonAddToRoute).setOnClickListener(this);
        view.findViewById(R.id.buttonSaveRoute).setOnClickListener(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        // MapsInitializer.initialize(getContext());
        setupGoogleMapScreenSettings(mGoogleMap);
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mGoogleMap.addMarker(
          new MarkerOptions()
                  .position(new LatLng(40.2959197, -3.8309136))
                  .title("Casa")
                  .snippet("Aquí vivo yo")
        );

        CameraPosition home = CameraPosition.builder()
                .target(new LatLng(40.2959197, -3.8309136))
                .zoom(16)
                .bearing(0)
                .build();
        mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(home));

        getAvailableRoutes();
    }


    @Override
    public void onClick(View v) {
        int buttonId = v.getId();
        Log.d("DOT", "Clicked on view: " + buttonId);
        switch (buttonId) {
            case R.id.buttonListRoutes:
                getAvailableRoutes();
                break;

            case R.id.buttonNewRoute:
                enableRouteCreation();
                break;


            case R.id.buttonAddToRoute:
                Log.d("DOT", "Adding address to route");
                DragItemAdapter routesAdapter = lRouteWayPoints.getAdapter();
                String address = tAddress.getText().toString();
                String city = tCity.getText().toString();
                String province = tProvince.getText().toString();
                RouteWayPoint rwp = new RouteWayPoint(address, city, province);
                routesAdapter.addItem(
                        routesAdapter.getItemCount(),
                        new Pair<>(System.currentTimeMillis(), rwp.toString())
                );
                mRouteWayPoints.add(rwp);
                if (routesAdapter.getItemCount() >= 2) {
                    mGoogleMap.clear();
                    DirectionsResult results = getDirectionsDetails(mRouteWayPoints, TravelMode.DRIVING);
                    if (results != null) {
                        addPolyline(results, mGoogleMap);
                        positionCamera(results.routes[overview], mGoogleMap);
                        addMarkersToMap(results, mGoogleMap);
                    }
                }
                Toast.makeText(
                        getActivity(),
                        getString(R.string.routes_form_create_saved_address),
                        Toast.LENGTH_LONG
                ).show();
                resetRouteWaypointForm();
                break;

            case R.id.buttonSaveRoute:
                Log.d("DOT", "Saving route");
                DragItemAdapter routeAdapter = lRouteWayPoints.getAdapter();
                if (routeAdapter.getItemCount() == 0) {
                    Toast.makeText(v.getContext(), "😅 La ruta esta vacia", Toast.LENGTH_LONG).show();
                }
                currentRoute.setName(tRouteName.getText().toString());
                currentRoute.setWaypoints(mRouteWayPoints);

                // Write a message to the database
                DatabaseReference newRoute = database.getReference("routes/").push();
                newRoute.setValue(currentRoute);

                resetRouteCreation();
                Toast.makeText(
                        getActivity(),
                        getString(R.string.routes_form_created),
                        Toast.LENGTH_LONG
                ).show();
                break;

            default:
                break;
        }
    }

    private void getAvailableRoutes() {
        lRoutesFormCreate.setVisibility(View.GONE);
        lRoutesListScrollView.setVisibility(View.VISIBLE);
        firebaseData.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // get total available quest
                DragItemAdapter availableRoutesAdapter = lAvailableRoutes.getAdapter();
                ((RouteItemAdapter)availableRoutesAdapter).setGoogleMap(mGoogleMap);

                while(availableRoutesAdapter != null && availableRoutesAdapter.getItemCount() > 0) {
                    availableRoutesAdapter.removeItem(0);
                }
                availableRoutes = new ArrayList<>();
                int i = 0;
                for (Iterator it = dataSnapshot.getChildren().iterator(); it.hasNext(); i++) {
                    Route r = Route.parse((DataSnapshot) it.next());
                    availableRoutesAdapter.addItem(i, new Pair<>(System.currentTimeMillis(), r));
                    availableRoutes.add(r);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void initDragList () {
        lRouteWayPoints.setDragListListener(new DragListView.DragListListener() {
            @Override
            public void onItemDragStarted(int position) {
                Toast.makeText(getActivity(), "Start - position: " + position, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemDragging(int itemPosition, float x, float y) {

            }

            @Override
            public void onItemDragEnded(int fromPosition, int toPosition) {
                if (fromPosition != toPosition) {
                    Toast.makeText(getActivity(), "End - position: " + toPosition, Toast.LENGTH_SHORT).show();
                }
            }
        });

        lRouteWayPoints.setLayoutManager(new LinearLayoutManager(getActivity()));
        DragItemAdapter listAdapter = new ItemAdapter(new ArrayList<>(), R.layout.list_item, R.id.image, true);
        lRouteWayPoints.setAdapter(listAdapter, false);
        lRouteWayPoints.setCanDragHorizontally(false);

        lAvailableRoutes.setLayoutManager(new LinearLayoutManager(getActivity()));
        RouteItemAdapter ria = new RouteItemAdapter(new ArrayList<>(), R.layout.list_item, R.id.image, false);
        ria.setFirebaseRoutesRef(firebaseData);
        lAvailableRoutes.setAdapter(ria, false);
        lAvailableRoutes.setCanDragHorizontally(true);

    }

    private DirectionsResult getDirectionsDetails(ArrayList<RouteWayPoint> route, TravelMode mode) {
        DateTime now = new DateTime();
        StringBuilder waypointsBuilder = new StringBuilder();
        String SEPARATOR = "|";
        int routes = route.size();
        for (int i = 1; i < routes - 1; i++) {
            waypointsBuilder.append(route.get(i).toString());
            if (i < routes - 2) {
                waypointsBuilder.append(SEPARATOR);
            }
        }
        try {
            Log.d("DOT", "Requesting directions");
            Log.d("DOT", "Origin: " + mRouteWayPoints.get(0).toString());
            Log.d("DOT", "Destination: " + mRouteWayPoints.get(routes - 1).toString());
            Log.d("DOT", "Waypoints: " + waypointsBuilder.toString());

            return DirectionsApi
                    .newRequest(getGeoContext())
                    .mode(mode)
                    .origin(mRouteWayPoints.get(0).toString())
                    .destination(route.get(routes - 1).toString())
                    .waypoints(waypointsBuilder.toString())
                    .departureTime(now)
                    .await();
        } catch (ApiException e) {
            e.printStackTrace();
            return null;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void addMarkersToMap(DirectionsResult results, GoogleMap mMap) {
//        mMap.addMarker(new MarkerOptions().position(
//                new LatLng(
//                        results.routes[overview].legs[overview].startLocation.lat,
//                        results.routes[overview].legs[overview].startLocation.lng
//                )
//        ).title(results.routes[overview].legs[overview].startAddress));

        Geocoder gCoder = new Geocoder(getContext());
        for (int i = 0; i < mRouteWayPoints.size(); i++) {
            try {
                Log.d("DOT", "Añadiendo marcador: " + i);
                Log.d("DOT", mRouteWayPoints.get(i).toString());
                Address address = gCoder.getFromLocationName(mRouteWayPoints.get(i).toString(), 1).get(0);
                LatLng latlng = new LatLng(address.getLatitude(), address.getLongitude());
                mMap.addMarker(new MarkerOptions().position(latlng).title("Parada " + (i + 1)));
                mRouteWayPoints.get(i).setLatLng(latlng);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

//        mMap.addMarker(new MarkerOptions().position(
//                new LatLng(
//                        results.routes[overview].legs[overview].endLocation.lat,
//                        results.routes[overview].legs[overview].endLocation.lng)
//        ).title(results.routes[overview].legs[overview].startAddress).snippet(getEndLocationTitle(results)));
    }

    private void positionCamera(DirectionsRoute route, GoogleMap mMap) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(route.legs[overview].startLocation.lat, route.legs[overview].startLocation.lng), 12));
    }

    private void addPolyline(DirectionsResult results, GoogleMap mMap) {
        List<LatLng> decodedPath = PolyUtil.decode(results.routes[overview].overviewPolyline.getEncodedPath());
        currentRoute.setPolyline(decodedPath);
        mMap.addPolyline(new PolylineOptions().addAll(decodedPath).color(getContext().getResources().getColor(R.color.colorPrimary)));
    }

    private String getEndLocationTitle(DirectionsResult results){
        return  "Time :"+ results.routes[overview].legs[overview].duration.humanReadable + " Distance :" + results.routes[overview].legs[overview].distance.humanReadable;
    }

    private GeoApiContext getGeoContext() {
        return new GeoApiContext
                .Builder()
                .queryRateLimit(3)
                .connectTimeout(1, TimeUnit.SECONDS)
                .readTimeout(1, TimeUnit.SECONDS)
                .writeTimeout(1, TimeUnit.SECONDS)
                .apiKey(getString(R.string.google_directions_key))
                .build();

    }

    private void setupGoogleMapScreenSettings(GoogleMap mMap) {
        mMap.setBuildingsEnabled(false);
        mMap.setIndoorEnabled(false);
        mMap.setTrafficEnabled(true);

        UiSettings mUiSettings = mMap.getUiSettings();
        mUiSettings.setZoomControlsEnabled(true);
        mUiSettings.setCompassEnabled(true);
        mUiSettings.setMyLocationButtonEnabled(true);
        mUiSettings.setScrollGesturesEnabled(true);
        mUiSettings.setZoomGesturesEnabled(true);
        mUiSettings.setTiltGesturesEnabled(false);
        mUiSettings.setRotateGesturesEnabled(true);
    }

    private void enableRouteCreation() {
        lRoutesFormCreate.setVisibility(View.VISIBLE);
        lRoutesListScrollView.setVisibility(View.GONE);
        currentRoute = new Route();
        mRouteWayPoints = new ArrayList<>();

        tRouteName.setText("");
        mGoogleMap.clear();
        resetRouteWaypointForm();
        lRoutesFormCreate.setVisibility(View.VISIBLE);
    }

    private void resetRouteCreation() {
        currentRoute = null;
        mRouteWayPoints = null;
        lRoutesFormCreate.setVisibility(View.GONE);
        tRouteName.setText("");
        mGoogleMap.clear();
        resetRouteWaypointForm();
    }

    private void resetRouteWaypointForm() {
        tAddress.setText("");
        tCity.setText("");
        tProvince.setText("");
    }

    private void fixtureRouteCreation() {
        RouteWayPoint rwp1 = new RouteWayPoint("Avenida de Carlos V 3", "Mostoles", "Madrid");
        RouteWayPoint rwp2 = new RouteWayPoint("Simon Hernandez 1", "Mostoles", "Madrid");
        RouteWayPoint rwp3 = new RouteWayPoint("Luis Sauquillo 3", "Fuenlabdrada", "Madrid");
        RouteWayPoint rwp4 = new RouteWayPoint("Urbanizacion Nuevo Versalles 30", "Fuenlabdrada", "Madrid");

        DragItemAdapter adapter = lRouteWayPoints.getAdapter();
        adapter.addItem(
                adapter.getItemCount(),
                new Pair<>(System.currentTimeMillis(), rwp1.toString())
        );
        mRouteWayPoints.add(rwp1);
        adapter.addItem(
                adapter.getItemCount(),
                new Pair<>(System.currentTimeMillis(), rwp2.toString())
        );
        mRouteWayPoints.add(rwp2);
        adapter.addItem(
                adapter.getItemCount(),
                new Pair<>(System.currentTimeMillis(), rwp3.toString())
        );
        mRouteWayPoints.add(rwp3);
        adapter.addItem(
                adapter.getItemCount(),
                new Pair<>(System.currentTimeMillis(), rwp4.toString())
        );
        mRouteWayPoints.add(rwp4);
    }
}

package com.proyectodot.enterprise;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.woxthebox.draglistview.DragItemAdapter;
import com.woxthebox.draglistview.DragListView;

import java.util.ArrayList;


public class RoutesFragment extends Fragment implements OnMapReadyCallback, View.OnClickListener {

    GoogleMap mGoogleMap;
    MapView mMapView;
    View mView;

    // form create route
    TextView tRouteName;
    TextView tAddress;
    TextView tCity;
    TextView tProvince;
    DragListView lRouteWayPoints;
    ArrayList<RouteWayPoint> mRouteWayPoints = new ArrayList<>();

    public RoutesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_routes, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // map
        mMapView = mView.findViewById(R.id.map);
        Log.d("DOT", "mMapView is null? " + (mMapView == null));
        if (mMapView != null) {
            mMapView.onCreate(null);
            mMapView.onResume();
            mMapView.getMapAsync(this);
        }

        // elements
        tRouteName = view.findViewById(R.id.editTextName);
        tAddress = view.findViewById(R.id.editTextAddress);
        tCity= view.findViewById(R.id.editTextCity);
        tProvince= view.findViewById(R.id.editTextProvince);

        lRouteWayPoints = view.findViewById(R.id.dragListRouteItems);
        initDragList();

        // listeners
        view.findViewById(R.id.buttonAddToRoute).setOnClickListener(this);
        view.findViewById(R.id.buttonSaveRoute).setOnClickListener(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(getContext());
        mGoogleMap = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.addMarker(
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
    }


    @Override
    public void onClick(View v) {
        int buttonId = v.getId();
        DragItemAdapter adapter = lRouteWayPoints.getAdapter();
        Log.d("DOT", "Clicked on view: " + buttonId);
        Log.d("DOT", "AddToRouteId: " + R.id.buttonAddToRoute);
        switch (buttonId) {
            case R.id.buttonAddToRoute:
                Log.d("DOT", "Adding address to route");
                String address = tAddress.getText().toString();
                String city = tCity.getText().toString();
                String province = tProvince.getText().toString();
                RouteWayPoint rwp = new RouteWayPoint(address, city, province);
                adapter.addItem(
                        adapter.getItemCount(),
                        new Pair<>(System.currentTimeMillis(), rwp.toString())
                );
                mRouteWayPoints.add(rwp);
                break;

            case R.id.buttonSaveRoute:
                Log.d("DOT", "Saving route");
                if (adapter.getItemCount() == 0) {
                    Toast.makeText(v.getContext(), "😅 La ruta esta vacia", Toast.LENGTH_LONG).show();
                }
                Route route = new Route(tRouteName.getText().toString(), mRouteWayPoints);
                // Write a message to the database
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("routes/" + route.getName());
                myRef.setValue(route.getWaypoints());
                break;

            default:
                break;
        }
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
        lRouteWayPoints.setCanDragHorizontally(true);
    }
}

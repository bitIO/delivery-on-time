package com.proyectodot.enterprise;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.GeoApiContext;
import com.google.maps.android.PolyUtil;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.woxthebox.draglistview.DragListView;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DeliverymanFragment extends Fragment implements OnMapReadyCallback, View.OnClickListener {
    private View mView;
    private MapView mapViewDeliveryMan;
    private GoogleMap mGoogleMap;

    private FirebaseDatabase database;
    private DatabaseReference fbDeliveryMen;
    private DatabaseReference fbRoutes;

    private LinearLayout linearLayoutDeliveryManCreate;
    private TextView editTextDeliveryManName;
    private TextView editTextDeliveryManSurame;
    private TextView editTextDeliveryManPhone;
    private TextView editTextDeliveryManEmail;
    private TextView editTextDeliveryManComments;
    private Button buttonDeliveryManSave;

    private DragListView recyclerViewDeliveryMen;
    private Spinner spinnerDeliveryMenRoutes;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_deliveryman, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        database = FirebaseDatabase.getInstance();
        fbDeliveryMen = database.getReference("deliverymen/");
        fbRoutes = database.getReference("routes/");

        linearLayoutDeliveryManCreate = view.findViewById(R.id.linearLayoutDeliveryManCreate);
        editTextDeliveryManName = mView.findViewById(R.id.editTextDeliveryManName);
        editTextDeliveryManSurame = mView.findViewById(R.id.editTextDeliveryManSurame);
        editTextDeliveryManPhone = mView.findViewById(R.id.editTextDeliveryManPhone);
        editTextDeliveryManEmail = mView.findViewById(R.id.editTextDeliveryManEmail);
        editTextDeliveryManComments = mView.findViewById(R.id.editTextDeliveryManComments);
        buttonDeliveryManSave = mView.findViewById(R.id.buttonDeliveryManSave);

        recyclerViewDeliveryMen = mView.findViewById(R.id.recyclerViewDeliveryMen);
        recyclerViewDeliveryMen.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerViewDeliveryMen.setAdapter(
                new DeliveryMenItemAdapter(
                        new ArrayList<>(),
                        R.layout.list_item,
                        R.id.image,
                        false,
                        fbDeliveryMen,
                        fbRoutes
                ),
                false
        );
        spinnerDeliveryMenRoutes = mView.findViewById(R.id.spinnerDeliveryMenRoutes);
        mapViewDeliveryMan = mView.findViewById(R.id.mapViewDeliveryMan);
        if (mapViewDeliveryMan != null) {
            mapViewDeliveryMan .onCreate(null);
            mapViewDeliveryMan .onResume();
            mapViewDeliveryMan .getMapAsync(this);
        }

        view.findViewById(R.id.buttonDeliveryManCreate).setOnClickListener(this);
        view.findViewById(R.id.buttonDeliveryManSave).setOnClickListener(this);
        view.findViewById(R.id.buttonDeliveryManAssign).setOnClickListener(this);

        getAvailableDeliveryMen();
        getAvailableRoutes();

    }

    @Override
    public void onClick(View v) {
        int buttonId = v.getId();
        Log.d("DOT", "Clicked on view: " + buttonId);
        switch (buttonId) {
            case R.id.buttonDeliveryManCreate:
                enableDeliveryManCreate();
                break;
            case R.id.buttonDeliveryManSave:
                saveDeliveryMan();
                break;
            case R.id.buttonDeliveryManAssign:
                assignRoute();
                break;
            default:
                break;
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        setupGoogleMapScreenSettings(mGoogleMap);
        ((DeliveryMenItemAdapter)recyclerViewDeliveryMen.getAdapter()).setGoogleMap(mGoogleMap);
    }

    private void setupGoogleMapScreenSettings(GoogleMap mMap) {
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
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

    private void enableDeliveryManCreate() {
        linearLayoutDeliveryManCreate.setVisibility(View.VISIBLE);
    }

    private void disableDeliveryManCreate() {
        linearLayoutDeliveryManCreate.setVisibility(View.GONE);
    }

    private void saveDeliveryMan() {
        DatabaseReference newDeliveryMan= fbDeliveryMen.push();
        newDeliveryMan.setValue(
                new DeliveryMan(
                    editTextDeliveryManName.getText().toString(),
                    editTextDeliveryManSurame.getText().toString(),
                    editTextDeliveryManPhone.getText().toString(),
                    editTextDeliveryManEmail.getText().toString(),
                    editTextDeliveryManComments.getText().toString()
                ),
                new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        resetDeliveryManForm();
                        disableDeliveryManCreate();
                        Toast.makeText(
                                getActivity(),
                                getString(R.string.deliveryman_button_saved),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );

    }

    private void resetDeliveryManForm() {
        editTextDeliveryManName.setText("");
        editTextDeliveryManSurame.setText("");
        editTextDeliveryManPhone.setText("");
        editTextDeliveryManEmail.setText("");
        editTextDeliveryManComments.setText("");
    }

    private void getAvailableDeliveryMen() {
        DeliveryMenItemAdapter deliveryMenAdapter =  (DeliveryMenItemAdapter) recyclerViewDeliveryMen.getAdapter();
        fbDeliveryMen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                while (deliveryMenAdapter.getItemCount() > 0) {
                    deliveryMenAdapter.removeItem(0);
                }

                int i = 0;
                for (Iterator it = dataSnapshot.getChildren().iterator(); it.hasNext(); i++) {
                    deliveryMenAdapter.addItem(
                            i,
                            new Pair(
                                    System.currentTimeMillis(),
                                    DeliveryMan.parse((DataSnapshot) it.next())
                            )
                    );
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getAvailableRoutes() {
        fbRoutes.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Route> routes = new ArrayList<>();
                for (Iterator it = dataSnapshot.getChildren().iterator(); it.hasNext();) {
                    Route r = Route.parse((DataSnapshot) it.next());
                    if (r.getAssigned() == null) {
                        routes.add(r);
                    }
                }
                spinnerDeliveryMenRoutes.setAdapter(
                        new ArrayAdapter<>(
                                getContext(),
                                android.R.layout.simple_spinner_item,
                                routes.toArray(new Route[routes.size()])
                        )
                );
                spinnerDeliveryMenRoutes.setOnItemSelectedListener  (new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        Route r = (Route) spinnerDeliveryMenRoutes.getAdapter().getItem(position);
                        Log.d("DOT", r.toString());
                        ((TextView)mView.findViewById(R.id.textViewDeliveryManHiddenIdR)).setText(r.getId());
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void assignRoute() {
        TextView tvDm = mView.findViewById(R.id.textViewDeliveryManHiddenIdD);
        TextView tvR = mView.findViewById(R.id.textViewDeliveryManHiddenIdR);

        String deliverManId = tvDm.getText().toString();
        String routeId = tvR.getText().toString();

        fbRoutes.child(routeId).child("assigned").setValue(deliverManId);
        fbDeliveryMen.child(deliverManId).child("assigned").setValue(routeId);
        Toast.makeText(
                getActivity(),
                getString(R.string.deliveryman_button_assigned),
                Toast.LENGTH_LONG
        ).show();

        tvDm.setText("");
        tvR.setText("");

        mView.findViewById(R.id.linearLayoutDeliveryManAssign).setVisibility(View.GONE);
    }
}

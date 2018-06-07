package com.proyectodot.enterprise;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.model.LatLng;
import com.seatgeek.placesautocomplete.DetailsCallback;
import com.seatgeek.placesautocomplete.OnPlaceSelectedListener;
import com.seatgeek.placesautocomplete.PlacesAutocompleteTextView;
import com.seatgeek.placesautocomplete.model.AutocompleteResultType;
import com.seatgeek.placesautocomplete.model.Place;
import com.seatgeek.placesautocomplete.model.PlaceDetails;
import com.woxthebox.draglistview.DragItemAdapter;
import com.woxthebox.draglistview.DragListView;

import java.util.ArrayList;
import java.util.Iterator;


public class ClientsFragment extends Fragment implements View.OnClickListener {
    private FirebaseDatabase database;
    private DatabaseReference fbClients;

    private EditText etName;
    private EditText etSurname;
    private EditText etPhone;
    private EditText etEmail;

    private Place selectedPlace;
    private PlaceDetails selectedPlaceDetails;
    private PlacesAutocompleteTextView placesAutocomplete;

    private DragListView lClients;
    private ArrayList<Client> availableClients;

    public ClientsFragment() {
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
        return inflater.inflate(R.layout.fragment_clients, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        database = FirebaseDatabase.getInstance();
        fbClients = database.getReference("clients/");
        fbClients.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DragItemAdapter availableClientsAdapter = lClients.getAdapter();
                while(availableClientsAdapter != null && availableClientsAdapter.getItemCount() > 0) {
                    availableClientsAdapter.removeItem(0);
                }
                availableClients = new ArrayList<>();
                int i = 0;
                for (Iterator it = dataSnapshot.getChildren().iterator(); it.hasNext(); i++) {
                    DataSnapshot ds = (DataSnapshot)it.next();
                    Client c = ds.getValue(Client.class);
                    c.setId(ds.getKey());

                    availableClientsAdapter.addItem(i, new Pair<>(System.currentTimeMillis(), c));
                    availableClients.add(c);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        etName = view.findViewById(R.id.clientName);
        etSurname = view.findViewById(R.id.clientSurname);
        etPhone  = view.findViewById(R.id.clientPhone);
        etEmail = view.findViewById(R.id.clientEmail);

        lClients = view.findViewById(R.id.dragListClients);
        lClients.setLayoutManager(new LinearLayoutManager(getActivity()));
        ClientItemAdapter listAdapter = new ClientItemAdapter(
                new ArrayList<>(), R.layout.list_item, R.id.image, true
        );
        listAdapter.setFirebaseClientRef(fbClients);
        lClients.setAdapter(listAdapter, false);
        lClients.setCanDragHorizontally(false);

        placesAutocomplete = getView().findViewById(R.id.clientAddress);
        placesAutocomplete.setResultType(AutocompleteResultType.GEOCODE);
        placesAutocomplete.showClearButton(true);
        placesAutocomplete.setOnPlaceSelectedListener(
                new OnPlaceSelectedListener() {
                    @Override
                    public void onPlaceSelected(final Place place) {
                        selectedPlace = place;
                        placesAutocomplete.getDetailsFor(place, new DetailsCallback() {
                            @Override
                            public void onSuccess(PlaceDetails placeDetails) {
                                selectedPlaceDetails = placeDetails;
                            }

                            @Override
                            public void onFailure(Throwable throwable) {

                            }
                        });
                    }
                }
        );

        view.findViewById(R.id.clientSave).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int buttonId = v.getId();
        Log.d("DOT", "Clicked on view: " + buttonId);
        switch (buttonId) {
            case R.id.clientSave:
                Client client = new Client(
                        etName.getText().toString(),
                        etSurname.getText().toString(),
                        etPhone.getText().toString(),
                        etEmail.getText().toString(),
                        selectedPlace.terms.get(0).value,
                        selectedPlace.terms.get(1).value,
                        selectedPlace.terms.get(2).value,
                        new LatLng(
                                selectedPlaceDetails.geometry.location.lat,
                                selectedPlaceDetails.geometry.location.lng
                        )
                );
                fbClients.push().setValue(client, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError == null) {
                            Toast.makeText(
                                    getActivity(),
                                    "👍🏻 Cliente creado correctamente",
                                    Toast.LENGTH_LONG
                            ).show();
                            clearForm();
                        }
                    }
                });
                break;
            default:
                break;
        }
    }

    private void clearForm() {
        etName.setText("");
        etSurname.setText("");
        etPhone.setText("");
        etEmail.setText("");
    }
}

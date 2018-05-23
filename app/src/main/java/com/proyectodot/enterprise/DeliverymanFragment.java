package com.proyectodot.enterprise;

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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.woxthebox.draglistview.DragListView;

import java.util.ArrayList;
import java.util.Iterator;

public class DeliverymanFragment extends Fragment implements OnMapReadyCallback, View.OnClickListener {
    private View mView;

    private FirebaseDatabase database;
    private DatabaseReference firebaseData;

    private LinearLayout linearLayoutDeliveryManCreate;
    private TextView editTextDeliveryManName;
    private TextView editTextDeliveryManSurame;
    private TextView editTextDeliveryManPhone;
    private TextView editTextDeliveryManEmail;
    private TextView editTextDeliveryManComments;
    private Button buttonDeliveryManSave;

    private DragListView recyclerViewDeliveryMen;

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
        firebaseData = database.getReference("deliverymen/");

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
                new ItemAdapter(new ArrayList<>(), R.layout.list_item, R.id.image, false),
                false
        );

        view.findViewById(R.id.buttonDeliveryManCreate).setOnClickListener(this);
        view.findViewById(R.id.buttonDeliveryManSave).setOnClickListener(this);

        getAvailableDeliveryMen();

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
            default:
                break;
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

    }

    private void enableDeliveryManCreate() {
        linearLayoutDeliveryManCreate.setVisibility(View.VISIBLE);
    }

    private void disableDeliveryManCreate() {
        linearLayoutDeliveryManCreate.setVisibility(View.GONE);
    }

    private void saveDeliveryMan() {
        DatabaseReference newDeliveryMan= firebaseData.push();
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
        ItemAdapter deliveryMenAdapter =  (ItemAdapter) recyclerViewDeliveryMen.getAdapter();
        firebaseData.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                while (deliveryMenAdapter.getItemCount() > 0) {
                    deliveryMenAdapter.removeItem(0);
                }

                ArrayList<Pair<Long, String>> items = new ArrayList<>();
                int i = 0;
                for (Iterator it = dataSnapshot.getChildren().iterator(); it.hasNext(); i++) {
                    deliveryMenAdapter.addItem(
                            i,
                            new Pair(
                                    System.currentTimeMillis(),
                                    DeliveryMan.parse((DataSnapshot) it.next()).toString()
                            )
                    );
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}

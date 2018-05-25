package com.proyectodot.enterprise;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.woxthebox.draglistview.DragItemAdapter;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class DeliveryMenItemAdapter extends DragItemAdapter<Pair<String, DeliveryMan>, DeliveryMenItemAdapter.ViewHolder> {

    private int mLayoutId;
    private int mGrabHandleId;
    private boolean mDragOnLongPress;

    private ArrayList<Pair<String, DeliveryMan>> deliveryMenList;
    private GoogleMap googleMap;

    private DatabaseReference fbDeliveryMenRef;
    private DatabaseReference fbRouteRef;

    public DeliveryMenItemAdapter(
            ArrayList<Pair<String, DeliveryMan>> list,
            int layoutId,
            int grabHandleId,
            boolean dragOnLongPress,
            DatabaseReference fbDeliveryMenRef,
            DatabaseReference fbRouteRef

    ) {
        mLayoutId = layoutId;
        mGrabHandleId = grabHandleId;
        mDragOnLongPress = dragOnLongPress;
        deliveryMenList = list;
        this.fbDeliveryMenRef = fbDeliveryMenRef;
        this.fbRouteRef = fbRouteRef;

        setItemList(list);
    }

    public GoogleMap getGoogleMap() {
        return googleMap;
    }

    public void setGoogleMap(GoogleMap googleMap) {
        this.googleMap = googleMap;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(mLayoutId, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        String text = mItemList.get(position).second.toString();
        holder.mText.setText(text);
        holder.itemView.setTag(mItemList.get(position));
    }

    @Override
    public long getUniqueItemId(int position) {
        return new Long(position).longValue();
    }

    class ViewHolder extends DragItemAdapter.ViewHolder {
        TextView mText;

        ViewHolder(final View itemView) {
            super(itemView, mGrabHandleId, mDragOnLongPress);
            mText = itemView.findViewById(R.id.text);
        }

        @Override
        public void onItemClicked(View view) {
            DeliveryMan dm = deliveryMenList.get(this.getPosition()).second;

            Log.d("DOT", "Clicked on " + dm.toString());
            if (dm.getAssigned() == null) {
                Toast.makeText(
                        view.getContext(),
                        view.getContext().getString(R.string.deliveryman_toast_has_no_route),
                        Toast.LENGTH_SHORT
                ).show();
                view.getRootView().findViewById(R.id.linearLayoutDeliveryManAssign).setVisibility(View.VISIBLE);
                view.getRootView().findViewById(R.id.linearLayoutDeliveryManMap).setVisibility(View.GONE);

                ((TextView)view.getRootView().findViewById(R.id.textViewDeliveryManHiddenIdD)).setText(dm.getId());
                if (dm.getAssigned() != null) {
                    ((TextView)view.getRootView().findViewById(R.id.textViewDeliveryManHiddenIdR)).setText(dm.getAssigned());
                }
            } else {
                Toast.makeText(
                        view.getContext(),
                        view.getContext().getString(R.string.deliveryman_toast_has_route),
                        Toast.LENGTH_SHORT
                ).show();
                view.getRootView().findViewById(R.id.linearLayoutDeliveryManAssign).setVisibility(View.GONE);
                view.getRootView().findViewById(R.id.linearLayoutDeliveryManMap).setVisibility(View.VISIBLE);
                showRoute(view, dm.getAssigned());
            }
        }

        @Override
        public boolean onItemLongClicked(View view) {
            DeliveryMan dm = deliveryMenList.get(this.getPosition()).second;

            new AlertDialog.Builder(view.getContext())
                    .setTitle(view.getContext().getString(R.string.deliveryman_dialog_remove_title))
                    .setMessage(view.getContext().getString(R.string.deliveryman_dialog_remove_message, dm.getName()))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(
                            android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                fbDeliveryMenRef.child(dm.getId()).getRef().removeValue(new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        Toast.makeText(
                                                view.getContext(),
                                                view.getContext().getString(R.string.deliveryman_dialog_remove_done),
                                                Toast.LENGTH_SHORT
                                        ).show();
                                    }
                                });
                            }})
                    .setNegativeButton(android.R.string.no, null)
                    .show();
            return true;
        }
    }

    private void showRoute(View view, String routeId) {
        fbRouteRef.child(routeId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Route r = Route.parse(dataSnapshot);
                        googleMap.addPolyline(
                                new PolylineOptions()
                                        .addAll(r.getPolyline())
                                        .color(view.getResources().getColor(R.color.colorPrimary))
                        );
                        LatLngBounds.Builder bounds= new LatLngBounds.Builder();
                        for(LatLng latlong: r.getPolyline()) {
                            bounds.include(latlong);
                        }

                        for (int i = 0; i < r.getWaypoints().size(); i++) {
                            googleMap.addMarker(
                                    new MarkerOptions()
                                            .position(r.getWaypoints().get(i).getLatLng())
                                            .title("Parada " + (i + 1))
                            );
                        }
                        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds.build(), 50);
                        googleMap.animateCamera(cu);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );
    }
}
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
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.maps.android.PolyUtil;
import com.woxthebox.draglistview.DragItemAdapter;

import java.util.ArrayList;
import java.util.List;

public class RouteItemAdapter extends DragItemAdapter<Pair<String, Route>, RouteItemAdapter.ViewHolder> {

    private int mLayoutId;
    private int mGrabHandleId;
    private boolean mDragOnLongPress;

    private ArrayList<Pair<String, Route>> routesList;
    private GoogleMap googleMap;

    private DatabaseReference firebaseRoutesRef;

    public RouteItemAdapter(ArrayList<Pair<String, Route>> list, int layoutId, int grabHandleId, boolean dragOnLongPress) {
        mLayoutId = layoutId;
        mGrabHandleId = grabHandleId;
        mDragOnLongPress = dragOnLongPress;
        routesList = list;

        setItemList(list);
    }

    public void setFirebaseRoutesRef(DatabaseReference ref) {
        this.firebaseRoutesRef = ref;
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

    public void setGoogleMap(GoogleMap googleMap) {
        this.googleMap = googleMap;
    }

    class ViewHolder extends DragItemAdapter.ViewHolder {
        TextView mText;

        ViewHolder(final View itemView) {
            super(itemView, mGrabHandleId, mDragOnLongPress);
            mText = itemView.findViewById(R.id.text);
        }

        @Override
        public void onItemClicked(View view) {
            Toast.makeText(view.getContext(), "Zooming to route", Toast.LENGTH_SHORT).show();
            Route r = routesList.get(this.getPosition()).second;
            Log.d("DOT", "Clicked on " + r.toString());

            googleMap.clear();
            googleMap.addPolyline(new PolylineOptions().addAll(r.getPolyline()).color(view.getResources().getColor(R.color.colorPrimary)));
            LatLngBounds.Builder bounds= new LatLngBounds.Builder();
            for(LatLng latlong: r.getPolyline()) {
                bounds.include(latlong);
            }

            for (int i = 0; i < r.getWaypoints().size(); i++) {
                googleMap.addMarker(new MarkerOptions().position(r.getWaypoints().get(i).getLatLng()).title("Parada " + (i + 1)));
            }
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds.build(), 50);
            googleMap.animateCamera(cu);
        }

        @Override
        public boolean onItemLongClicked(View view) {
            Route r = routesList.get(this.getPosition()).second;

            new AlertDialog.Builder(view.getContext())
                    .setTitle(view.getContext().getString(R.string.routes_dialog_remove_title))
                    .setMessage(view.getContext().getString(R.string.routes_dialog_remove_message, r.getName()))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(
                            android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                firebaseRoutesRef.child(r.getId()).getRef().removeValue(new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        Toast.makeText(
                                                view.getContext(),
                                                view.getContext().getString(R.string.routes_dialog_remove_done),
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
}
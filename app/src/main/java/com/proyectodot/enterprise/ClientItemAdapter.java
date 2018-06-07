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

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.woxthebox.draglistview.DragItemAdapter;

import java.util.ArrayList;

public class ClientItemAdapter extends DragItemAdapter<Pair<String, Client>, ClientItemAdapter.ViewHolder> {

    private int mLayoutId;
    private int mGrabHandleId;
    private boolean mDragOnLongPress;

    private ArrayList<Pair<String, Client>> clientList;

    private DatabaseReference firebaseClientRef;

    public ClientItemAdapter(ArrayList<Pair<String, Client>> list, int layoutId, int grabHandleId, boolean dragOnLongPress) {
        mLayoutId = layoutId;
        mGrabHandleId = grabHandleId;
        mDragOnLongPress = dragOnLongPress;
        clientList = list;

        setItemList(list);
    }

    public void setFirebaseClientRef(DatabaseReference ref) {
        this.firebaseClientRef = ref;
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
            Client c = clientList.get(this.getPosition()).second;
            Log.d("DOT", "Clicked on " + c.toString());
        }

        @Override
        public boolean onItemLongClicked(View view) {
            Client c = clientList.get(this.getPosition()).second;

            new AlertDialog.Builder(view.getContext())
                    .setTitle("Borrado de cliente")
                    .setMessage(
                        "Quiere borrar al cliente/a: " + c.getName() + " " + c.getSurname()
                    )
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(
                            android.R.string.yes, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {
                                    firebaseClientRef.child(c.getId()).getRef().removeValue(new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                            Toast.makeText(
                                                    view.getContext(),
                                                    "Cliente borrado correctamente",
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

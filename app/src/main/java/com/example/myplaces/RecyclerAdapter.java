package com.example.myplaces;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> implements View.OnClickListener {

    private final Context mContext;
    private ArrayList<MarkerModel> mMarkers;
    private final LayoutInflater mLayoutInflater;
    private String delayUrl;
    private TextView nameFromClicked;
    private String passedName;

    public RecyclerAdapter(Context mContext, ArrayList<MarkerModel> mMarkers) {
        this.mContext = mContext;
        mLayoutInflater = LayoutInflater.from(mContext);
        this.mMarkers = mMarkers;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mLayoutInflater.inflate(R.layout.item_locations_list,parent,false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final MarkerModel markerModel = mMarkers.get(position);
        final String holderId = markerModel.getMarkerId();
        holder.title.setText(markerModel.getStreet());
        holder.setItemClickListener(new ItemClickListener() {
            @Override
            public void onClick(View view, int position) {

                Intent intent = new Intent(mContext,InfosWhenClicked.class);
                nameFromClicked = (TextView) view.findViewById(R.id.titleTextViewItem);
                passedName = nameFromClicked.getText().toString();

                Bundle bundle = new Bundle();
                bundle.putString("passedName", passedName);
                bundle.putString("passedId",holderId);
                intent.putExtras(bundle);
                mContext.startActivity(intent);

            }
        });

        Log.d("holder title is",""+holder.title.getText().toString());

        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
        DatabaseReference mRef = mDatabase.getReference("Users");
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        UserModel userModel = ds.getValue(UserModel.class);
                        if (userModel.getEmail().equals(mAuth.getCurrentUser().getEmail())) {
                            if (userModel.getProfilePhoto()!=null && !userModel.getProfilePhoto().equals("")) {
                                delayUrl = userModel.getProfilePhoto();
                                Picasso.get().load(delayUrl).into(holder.thumbnail);
                            }else {
                                holder.thumbnail.setImageResource(R.drawable.add_photo_image);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


    }

    @Override
    public int getItemCount() {
        return mMarkers.size();
    }

    @Override
    public void onClick(View v) {

    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final TextView title;
        public final ImageView thumbnail;
        public final CardView cardView;
        private ItemClickListener itemClickListener;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.titleTextViewItem);
            thumbnail = (ImageView) itemView.findViewById(R.id.circleImageViewItem);
            cardView = (CardView) itemView.findViewById(R.id.card_view);
            itemView.setOnClickListener(this);

        }

        public void setItemClickListener(ItemClickListener itemClickListener) {
            this.itemClickListener  = itemClickListener;
        }

        @Override
        public void onClick(View v) {
            itemClickListener.onClick(v,getAdapterPosition());
        }
    }

    public void updateList (List<MarkerModel> newList) {
        mMarkers = new ArrayList<>();
        mMarkers.addAll(newList);
        notifyDataSetChanged();
    }
}

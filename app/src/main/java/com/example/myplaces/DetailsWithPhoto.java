package com.example.myplaces;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;



public class DetailsWithPhoto extends Fragment {

    public TextView streetName,zipCode,country;
    public static ImageView locationPhoto;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mRef;
    static Button deletePhotoButton;
    static ProgressBar uploadingPhotoProgres;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Bundle bundle = this.getArguments();
        final int passedId = bundle.getInt("keyId",0);

        View view = inflater.inflate(R.layout.fragment_detailswithphoto,container,false);

        streetName = (TextView) view.findViewById(R.id.streetNameTextView);
        zipCode = (TextView) view.findViewById(R.id.zipCodeTextView);
        country = (TextView) view.findViewById(R.id.countryTextView);
        locationPhoto = (ImageView) view.findViewById(R.id.locationPhoto);
        deletePhotoButton = (Button) view.findViewById(R.id.deletePhoto);
        deletePhotoButton.setVisibility(View.INVISIBLE);
        uploadingPhotoProgres = (ProgressBar)view.findViewById(R.id.loadingProgressBarDetails);
        uploadingPhotoProgres.setVisibility(View.INVISIBLE);


        mAuth = FirebaseAuth.getInstance();

        mDatabase = FirebaseDatabase.getInstance();
        mRef = mDatabase.getReference("Users");

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    UserModel userModel = ds.getValue(UserModel.class);
                    if (userModel.getEmail().equals(mAuth.getCurrentUser().getEmail())) {
                        mDatabase=FirebaseDatabase.getInstance();

                        mRef=mDatabase.getReference("markers").child(mAuth.getCurrentUser().getUid());

                        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Log.d("passed id is",""+String.valueOf(passedId));
                                MarkerModel markerModel = dataSnapshot.child(String.valueOf(passedId)).getValue(MarkerModel.class);
                                streetName.setText(markerModel.getStreet());
                                zipCode.setText(markerModel.getZipCode());
                                country.setText(markerModel.getCountry());
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return view;
    }
}

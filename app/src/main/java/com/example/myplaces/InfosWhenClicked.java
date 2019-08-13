package com.example.myplaces;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.squareup.picasso.Picasso;


public class InfosWhenClicked extends AppCompatActivity {

    public static TextView streetInfo,countryInfo,zipCodeinfo;
    public static ImageView imageOfLocation;
    private int currentNumLoc ;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mRef;
    private FirebaseStorage mStorage;
    private StorageReference mStorRef;
    private String markerId;
    private BottomNavigationViewEx bottomNavigationViewEx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_infos_when_clicked);

        Intent intent = new Intent() ;
        Bundle b = getIntent().getExtras();
        markerId = b.getString("passedId");

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mRef = mDatabase.getReference("markers").child(mAuth.getCurrentUser().getUid());

        bottomNavigationViewEx=(BottomNavigationViewEx)findViewById(R.id.bottomNavigationBarInformations);
        bottomNavigationViewEx.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.nav_home_button :
                        Intent intentHome = new Intent(getApplicationContext(),HomeDashBoard.class);
                        startActivity(intentHome);
                        menuItem.setChecked(true);
                        break;
                    case R.id.nav_map_button:
                        Intent intent = new Intent(getApplicationContext(),MapsActivity.class);
                        startActivity(intent);
                        menuItem.setChecked(true);
                        break;
                    case R.id.nav_user_button:
                        Intent intentProfile = new Intent(getApplicationContext(),UserProfile.class);
                        startActivity(intentProfile);
                        menuItem.setChecked(true);
                        break;
                }
                return false;
            }
        });

        streetInfo = (TextView) findViewById(R.id.streetNameTextViewDetails);
        countryInfo=(TextView) findViewById(R.id.countryTextViewDetails);
        zipCodeinfo = (TextView) findViewById(R.id.zipCodeTextViewDetails);
        imageOfLocation = (ImageView) findViewById(R.id.locationPhotoDetails);

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    MarkerModel markerModel = ds.getValue(MarkerModel.class);
                    if (markerModel.getMarkerId().equals(markerId)) {

                        streetInfo.setText(markerModel.getStreet());
                        countryInfo.setText(markerModel.getCountry());
                        zipCodeinfo.setText(markerModel.getZipCode());

                        if (markerModel.getMarkerPhotoUrl()!=null && !markerModel.getMarkerPhotoUrl().equals("")) {
                            Picasso.get().load(markerModel.getMarkerPhotoUrl()).into(imageOfLocation);
                        } else {
                            imageOfLocation.setImageResource(R.drawable.add_photo_image);
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }


    public void deleteLocation(View view) {
        mStorage = FirebaseStorage.getInstance();
        mStorRef = mStorage.getReference();
        dialogAlert();
    }

    public void dialogAlert () {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);

        alertBuilder.setTitle("Delete entry")
                .setMessage("Are you sure you want to delete this location")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        mRef = FirebaseDatabase.getInstance().getReference("markers").child(mAuth.getCurrentUser().getUid());
                        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                for (DataSnapshot ds:dataSnapshot.getChildren()) {
                                    if (ds.getKey().equals(markerId)) {
                                        MarkerModel markerModel = ds.getValue(MarkerModel.class);
                                        if (markerModel.getMarkerPhotoUrl()!=null && !markerModel.getMarkerPhotoUrl().equals("")) {
                                            StorageReference photoRef = mStorage.getReferenceFromUrl(markerModel.getMarkerPhotoUrl());
                                            photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                }
                                            });
                                        }
                                    }
                                }
                                mRef= FirebaseDatabase.getInstance().getReference("Users");
                                mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                            UserModel userModel = ds.getValue(UserModel.class);
                                            if (userModel.getEmail().equals(mAuth.getCurrentUser().getEmail())) {
                                                currentNumLoc = userModel.getNumLocationsAdded();
                                                mRef.child(mAuth.getCurrentUser().getUid()).child("numLocationsAdded").setValue(currentNumLoc-1);
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        mRef.child(markerId).removeValue();
                        Intent intent = new Intent(getApplicationContext(),HomeDashBoard.class);
                        startActivity(intent);
                    }
                });

        alertBuilder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Log.d("CLICKED ","NO");
            }
        });
        alertBuilder.setIcon(R.drawable.ic_delete);
        alertBuilder.show();
    }
}

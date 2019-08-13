package com.example.myplaces;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import static com.example.myplaces.DetailsWithPhoto.deletePhotoButton;
import static com.example.myplaces.DetailsWithPhoto.locationPhoto;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private BottomNavigationViewEx bottomNavigationViewEx;
    private int markerCount;
    static int markerPosition;
    Geocoder geocoder;
    FloatingActionButton addLocationButton, deleteLocationButton;
    private MarkerModel currentMarker;
    private FirebaseDatabase mDataBase;
    private DatabaseReference mRef;
    private FirebaseAuth mAuth;
    private FrameLayout fragmentFrameDetails;
    private FirebaseStorage mStorage;
    private StorageReference mStorRef;
    private String locationPhotoUrl;
    private boolean exist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        markerCount = 0;
        addLocationButton = (FloatingActionButton) findViewById(R.id.addLocationButton);
        addLocationButton.setVisibility(View.INVISIBLE);
        deleteLocationButton = (FloatingActionButton) findViewById(R.id.clearMap);
        deleteLocationButton.setVisibility(View.INVISIBLE);
        mAuth = FirebaseAuth.getInstance();

        bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavigationBar_maps);
        bottomNavigationViewEx.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.nav_home_button:
                        Intent intentHome = new Intent(getApplicationContext(), HomeDashBoard.class);
                        startActivity(intentHome);
                        break;
                    case R.id.nav_map_button:
                        return false;
                    case R.id.nav_user_button:
                        Intent intentUserProfile = new Intent(getApplicationContext(), UserProfile.class);
                        startActivity(intentUserProfile);
                        break;
                }

                return false;
            }
        });

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mapFragment.getMapAsync(this);
    }

    public void postitionToCurrentLocation(Location location) {
        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f));
    }

    public void positionToCurentAndAskForUpade() {
        Location currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        postitionToCurrentLocation(currentLocation);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                }
            }
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if (markerCount == 0) {
                    markerCount++;
                    addLocationButton.setVisibility(View.VISIBLE);
                    deleteLocationButton.setVisibility(View.VISIBLE);

                    MarkerOptions myMarker = new MarkerOptions().position(latLng).title("Location you want to save");
                    mMap.addMarker(myMarker);

                    geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

                    try {

                        List<Address> adressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);

                        if (adressList != null && adressList.size() > 0) {

                            currentMarker = new MarkerModel();

                            if (adressList.get(0).getCountryName() == null) {
                                currentMarker.setCountry("Country unknown");
                            } else {
                                currentMarker.setCountry(adressList.get(0).getCountryName());
                            }

                            if (adressList.get(0).getThoroughfare() == null) {
                                currentMarker.setStreet("Street unknown");
                            } else {
                                currentMarker.setStreet(adressList.get(0).getThoroughfare());
                            }
                            if (adressList.get(0).getPostalCode() == null) {
                                currentMarker.setZipCode("Zip Code unknown");
                            } else {
                                currentMarker.setZipCode(adressList.get(0).getPostalCode());
                            }
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        });

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                postitionToCurrentLocation(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if (Build.VERSION.SDK_INT < 23) {
            positionToCurentAndAskForUpade();
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1);
            } else {
                positionToCurentAndAskForUpade();
            }
        }
    }

    public void addLocation(final View view) {

        mDataBase = FirebaseDatabase.getInstance();
        mRef = mDataBase.getReference("Users");

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    UserModel userModel = ds.getValue(UserModel.class);
                    if (userModel.getEmail().equals(mAuth.getCurrentUser().getEmail())) {

                        int numberLocation = userModel.getNumLocationsAdded() + 1;
                        int priority = userModel.getMarkerPriority();
                        mRef.child(mAuth.getCurrentUser().getUid()).child("numLocationsAdded").setValue(numberLocation);
                        markerPosition = Integer.parseInt(getRandomId());
                        mRef = FirebaseDatabase.getInstance().getReference("markers");
                        currentMarker.setMarkerId(String.valueOf(markerPosition));
                        currentMarker.setMarkerPriority(priority);
                        mRef.child(mAuth.getCurrentUser().getUid()).child(String.valueOf(markerPosition)).setValue(currentMarker);
                        mRef = FirebaseDatabase.getInstance().getReference("Users");
                        mRef.child(mAuth.getCurrentUser().getUid()).child("markerPriority").setValue(priority + 1);
                        mRef = FirebaseDatabase.getInstance().getReference("Users");
                        fragmentFrameDetails = (FrameLayout) findViewById(R.id.frameInfos);
                        Bundle bundle = new Bundle();
                        bundle.putInt("keyId", markerPosition);
                        DetailsWithPhoto detailsWithPhoto = new DetailsWithPhoto();
                        detailsWithPhoto.setArguments(bundle);
                        FragmentManager manager = getSupportFragmentManager();
                        FragmentTransaction transaction = manager.beginTransaction();
                        transaction.add(R.id.frameInfos, detailsWithPhoto, "detailsWithPhoto");
                        transaction.commit();
                        addLocationButton.setVisibility(View.INVISIBLE);
                        deleteLocationButton.setVisibility(View.INVISIBLE);
                        fragmentFrameDetails.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void clearMap(View view) {
        mMap.clear();
        markerCount = 0;
        deleteLocationButton.setVisibility(View.INVISIBLE);
        addLocationButton.setVisibility(View.INVISIBLE);
    }

    public void changeLocationPhoto(View view) {

        Intent i = new Intent(
                Intent.ACTION_GET_CONTENT);
        i.setType("image/jpeg");
        i.putExtra(Intent.EXTRA_LOCAL_ONLY, true);

        startActivityForResult(i.createChooser(i, "Insert Picture"), 1);

    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, final Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        if (resultCode == RESULT_OK && reqCode == 1) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);

                DetailsWithPhoto.uploadingPhotoProgres.setVisibility(View.VISIBLE);

                mDataBase = FirebaseDatabase.getInstance();
                mRef = mDataBase.getReference("Users");

                mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            final UserModel userModel = ds.getValue(UserModel.class);
                            if (userModel.getEmail().equals(mAuth.getCurrentUser().getEmail())) {

                                mStorage = FirebaseStorage.getInstance();
                                mStorRef = mStorage.getReference("markers").child(mAuth.getCurrentUser().getUid()).child(String.valueOf(markerPosition));

                                mStorRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        DetailsWithPhoto.locationPhoto.setImageBitmap(selectedImage);
                                        DetailsWithPhoto.uploadingPhotoProgres.setVisibility(View.INVISIBLE);
                                        Toast.makeText(getApplicationContext(), "Image uploaded", Toast.LENGTH_SHORT).show();
                                        deletePhotoButton.setVisibility(View.VISIBLE);
                                        mStorRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                locationPhotoUrl = uri.toString();
                                                mStorage = FirebaseStorage.getInstance();
                                                mRef = mDataBase.getReference("markers").child(mAuth.getCurrentUser().getUid()).child(String.valueOf(markerPosition)).child("markerPhotoUrl");
                                                mRef.setValue(locationPhotoUrl);
                                            }
                                        });
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_LONG).show();
            }

        } else {
            Toast.makeText(getApplicationContext(), "You haven't picked Image", Toast.LENGTH_LONG).show();
        }
    }


    public void deletePhoto(View view) {
        mStorRef = mStorage.getReference();
        dialogAlert();
    }

    public void dialogAlert() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);

        alertBuilder.setTitle("Delete entry")
                .setMessage("Are you sure you want to delete photo")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        mRef = FirebaseDatabase.getInstance().getReference("markers").child(mAuth.getCurrentUser().getUid());
                        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                    if (ds.getKey().equals(String.valueOf(markerPosition))) {
                                        MarkerModel markerModel = ds.getValue(MarkerModel.class);
                                        StorageReference photoRef = mStorage.getReferenceFromUrl(markerModel.getMarkerPhotoUrl());
                                        photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(getApplicationContext(), "Image deleted", Toast.LENGTH_SHORT).show();
                                                locationPhoto.setImageResource(R.drawable.add_photo_image);
                                                deletePhotoButton.setVisibility(View.INVISIBLE);
                                            }
                                        });
                                    }
                                }
                                mRef.child(String.valueOf(markerPosition)).child("markerPhotoUrl").setValue("");
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }
                });

        alertBuilder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Log.d("CLICKED ", "NO");
            }
        });
        alertBuilder.setIcon(R.drawable.ic_delete);
        alertBuilder.show();
    }

    private String getRandomId() {

        Random random = new Random();
        String randomNumber = String.format("%06d", random.nextInt(1000000));
        Log.d("MyApp", "Generated Password : " + randomNumber);

        while (checkExistOfId(randomNumber)) {
            randomNumber = String.format("%06d", random.nextInt(1000000));
        }
        return randomNumber;
    }

    private boolean checkExistOfId(final String passedId) {
        mDataBase = FirebaseDatabase.getInstance();
        mRef = mDataBase.getReference("markers").child(mAuth.getCurrentUser().getUid());

        exist = false;

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        if (ds.getKey().equals(passedId)) {
                            exist = true;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return exist;
    }
}

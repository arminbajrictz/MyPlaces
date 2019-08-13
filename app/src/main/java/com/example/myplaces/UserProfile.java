package com.example.myplaces;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
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
import com.google.firebase.storage.UploadTask;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.squareup.picasso.Picasso;
import java.io.FileNotFoundException;
import java.io.InputStream;
import static com.example.myplaces.EditUserProfile.myImage;
import static com.example.myplaces.EditUserProfile.phonenumber;

public class UserProfile extends AppCompatActivity {

    static ImageView profilePhoto;
    private BottomNavigationViewEx bottomNavigationViewEx ;
    private FirebaseAuth mAuth;
    FrameLayout fragmentFrame ;
    private Button editInfo;
    static TextView name,phone,email,numLoc;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mRef;
    private FirebaseStorage mStorage;
    private StorageReference mStorRef;
    private String pictureUrl;
    static String defNum,defName;
    private String  urlOfImageDelay;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        profilePhoto = (ImageView) findViewById(R.id.profilePhoto);
        mAuth = FirebaseAuth.getInstance();
        editInfo = (Button) findViewById(R.id.editProfileButton);
        name =(TextView) findViewById(R.id.fullNameTextView);
        phone =(TextView) findViewById(R.id.phoneNumberTextView);
        email =(TextView) findViewById(R.id.emailTextView);
        numLoc =(TextView) findViewById(R.id.locationCounterTextView);

        defNum = "+123 456 789";
        defName = "John Doe";

        initiUserProfile();

        mStorage = FirebaseStorage.getInstance();
        mStorRef=mStorage.getReference();

        bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavigationBarUsers);
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
                        menuItem.setChecked(true);
                        break;
                }
                return false;
            }
        });

    }


    private boolean isValidMobile(String phone) {
        return android.util.Patterns.PHONE.matcher(phone).matches();
}


    public void callUser (View view) {

            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + phone.getText().toString()));
            startActivity(callIntent);
    }

        public void sendEmail(View view) {
            Intent i = new Intent(android.content.Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_EMAIL  , new String[]{mAuth.getCurrentUser().getEmail()});
            i.putExtra(Intent.EXTRA_SUBJECT, "My Places App default subject");
            i.putExtra(Intent.EXTRA_TEXT   , "");
            startActivity(Intent.createChooser(i, "Email:"));
        }


    public void changeProfilePic (View view) {

        Intent i = new Intent(
                Intent.ACTION_GET_CONTENT);
        i.setType("image/jpeg");
        i.putExtra(Intent.EXTRA_LOCAL_ONLY,true);

        startActivityForResult(i.createChooser(i,"Insert Picture"),1);
    }


    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);


        if (resultCode == RESULT_OK && reqCode==1) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                myImage.setImageBitmap(selectedImage);
                mStorRef = mStorage.getReference("users").child(mAuth.getCurrentUser().getUid()).child("profilePic");
                mStorRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(getApplicationContext(),"Image updated",Toast.LENGTH_SHORT).show();
                        mStorRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                mStorRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        pictureUrl = uri.toString();
                                        mDatabase = FirebaseDatabase.getInstance();
                                        mRef = mDatabase.getReference("Users");
                                        mRef.child(mAuth.getCurrentUser().getUid()).child("profilePhoto").setValue(pictureUrl);

                                    }
                                });
                            }
                        });
                    }
                });
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_LONG).show();
            }

        }else {
            Toast.makeText(getApplicationContext(), "You haven't picked Image",Toast.LENGTH_LONG).show();
        }
    }


    public void cancel(View view) {
        fragmentFrame.setVisibility(View.INVISIBLE);
        editInfo.setVisibility(View.VISIBLE);
        bottomNavigationViewEx.setVisibility(View.VISIBLE);
        fragmentFrame.removeAllViews();

    }

    public void initiUserProfile() {
        mDatabase = FirebaseDatabase.getInstance();
        mRef=mDatabase.getReference("Users");
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    UserModel user = ds.getValue(UserModel.class);

                    if (mAuth.getCurrentUser().getEmail()!= null) {

                        if (user.getEmail().equals(mAuth.getCurrentUser().getEmail())) {

                            email.setText(user.getEmail());

                            if (user.getPhoneNumber() == null || user.getPhoneNumber().equals("")) {
                                phone.setText(defNum);
                            } else {
                                phone.setText(user.getPhoneNumber());
                            }

                            if (user.getFullName() == null || user.getFullName().equals("")) {
                                name.setText(defName);
                            } else {
                                name.setText(user.getFullName());
                            }
                            if (user.getProfilePhoto() == null) {
                                profilePhoto.setImageResource(R.drawable.user);
                            } else {
                                urlOfImageDelay = user.getProfilePhoto();
                                Picasso.get().load( urlOfImageDelay).into(profilePhoto);
                            }
                            numLoc.setText("Locations Added : " + user.getNumLocationsAdded());
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void saveChanges(View view) {

        if (!TextUtils.isEmpty(phonenumber.getText())) {
            if (isValidMobile(phonenumber.getText().toString())) {
                saveCase();
                     } else {
                              Toast.makeText(getApplicationContext(), "Please enter valid phone number", Toast.LENGTH_SHORT).show();
                           }
        }else {
            saveCase();
        }
    }

    public void saveCase() {

        fragmentFrame.setVisibility(View.INVISIBLE);
        editInfo.setVisibility(View.VISIBLE);

        mDatabase = FirebaseDatabase.getInstance();
        mRef = mDatabase.getReference("Users");

        mRef.child(mAuth.getCurrentUser().getUid()).child("fullName").setValue(EditUserProfile.nameFull.getText().toString());
        mRef.child(mAuth.getCurrentUser().getUid()).child("phoneNumber").setValue(EditUserProfile.phonenumber.getText().toString());
        initiUserProfile();

        bottomNavigationViewEx.setVisibility(View.VISIBLE);
        fragmentFrame.removeAllViews();
    }

    public void editUserProfile (View view) {
        bottomNavigationViewEx.setVisibility(View.INVISIBLE);
        editInfo.setVisibility(View.INVISIBLE);
        fragmentFrame = (FrameLayout) findViewById(R.id.frameLayout);
        EditUserProfile editUserProfile = new EditUserProfile();
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.frameLayout,editUserProfile,"editUserProfile");
        transaction.commit();
        fragmentFrame.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.dashboard_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.signOut :
                mAuth.signOut();
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
                break;
        }
        return false;
    }

}

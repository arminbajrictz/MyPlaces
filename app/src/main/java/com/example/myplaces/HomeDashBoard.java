package com.example.myplaces;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class HomeDashBoard extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private FirebaseDatabase mdatabase;
    private DatabaseReference mRef;
    private FirebaseAuth mAuth;
    private BottomNavigationViewEx myBottomNavigation;
    private RecyclerView myRecyclerView;
    static ArrayList<MarkerModel> markersForRecylcer;
    private RecyclerAdapter recyclerAdapter;
    private Button addFirstLocation ;
    private TextView wellcomMessage;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_dash_board);


        mAuth= FirebaseAuth.getInstance();
        markersForRecylcer = new ArrayList<MarkerModel>();
        wellcomMessage = (TextView) findViewById(R.id.welcomeMessage);
        addFirstLocation = (Button) findViewById(R.id.addFirstLocation);
        wellcomMessage.setVisibility(View.INVISIBLE);
        addFirstLocation.setVisibility(View.INVISIBLE);

        mdatabase = FirebaseDatabase.getInstance();
        mRef = mdatabase.getReference("markers").child(mAuth.getCurrentUser().getUid());

        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                myRecyclerView = (RecyclerView)findViewById(R.id.myRecyclerView);
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    MarkerModel markerModel = ds.getValue(MarkerModel.class);
                    markersForRecylcer.add(markerModel);
                }

                if (markersForRecylcer.isEmpty()) {
                    wellcomMessage.setVisibility(View.VISIBLE);
                    addFirstLocation.setVisibility(View.VISIBLE);
                    myRecyclerView.setVisibility(View.INVISIBLE);
                }else {
                    wellcomMessage.setVisibility(View.INVISIBLE);
                    addFirstLocation.setVisibility(View.INVISIBLE);
                    myRecyclerView.setVisibility(View.VISIBLE);
                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
                    myRecyclerView.setLayoutManager(linearLayoutManager);

                    Collections.sort(markersForRecylcer, new Comparator<MarkerModel>() {
                        @Override
                        public int compare(MarkerModel m1, MarkerModel m2) {
                            return m1.getMarkerPriority() - m2.getMarkerPriority();
                        }
                    });

                    recyclerAdapter = new RecyclerAdapter(getApplicationContext(),markersForRecylcer);
                    myRecyclerView.setAdapter(recyclerAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        myBottomNavigation = (BottomNavigationViewEx) findViewById(R.id.bottomNavigationBar);
        myBottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.nav_home_button :
                        menuItem.setChecked(true);
                        break;
                    case R.id.nav_map_button:
                        Intent intent = new Intent(getApplicationContext(),MapsActivity.class);
                        startActivity(intent);
                        menuItem.setChecked(true);
                        break;
                    case R.id.nav_user_button:
                        Intent intentUserProfile = new Intent(getApplicationContext(),UserProfile.class);
                        startActivity(intentUserProfile);
                        menuItem.setChecked(true);
                        break;
                }
                return false;
            }
        });

    }

    public void addFirstLocation(View view) {
        Intent intent = new Intent(getApplicationContext(),MapsActivity.class);
        startActivity(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_bar,menu);

        MenuItem menuItem = menu.findItem(R.id.searchItem);
        searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(this);
        searchView.setQueryHint("Type Adress Here");

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (!markersForRecylcer.isEmpty()) {
            String userInput = newText.toLowerCase();
            ArrayList<MarkerModel> newList = new ArrayList<MarkerModel>();
            for (MarkerModel markerModel : markersForRecylcer) {
                if (markerModel.getStreet().toLowerCase().contains(userInput)) {
                    newList.add(markerModel);
                }
            }
            recyclerAdapter.updateList(newList);
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        searchView.setQuery("", false);
        searchView.clearFocus();
        super.onBackPressed();
    }
}

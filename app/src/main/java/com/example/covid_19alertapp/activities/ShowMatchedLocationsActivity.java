package com.example.covid_19alertapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.covid_19alertapp.R;
import com.example.covid_19alertapp.extras.AddressReceiver;
import com.example.covid_19alertapp.extras.Constants;
import com.example.covid_19alertapp.extras.FetchAddress;
import com.example.covid_19alertapp.extras.Internet;
import com.example.covid_19alertapp.extras.LogTags;
import com.example.covid_19alertapp.extras.Notifications;
import com.example.covid_19alertapp.models.MatchedLocation;
import com.example.covid_19alertapp.roomdatabase.VisitedLocations;
import com.example.covid_19alertapp.roomdatabase.VisitedLocationsDao;
import com.example.covid_19alertapp.roomdatabase.VisitedLocationsDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ShowMatchedLocationsActivity extends AppCompatActivity implements AddressReceiver.AddressView {

    // matched locations model (for recycler-view)
    List<MatchedLocation> matchedLocations = new ArrayList<>();
    int matchedLocationPosition = 0;

    // firebase
    private DatabaseReference firebaseReference;

    // local db
    private VisitedLocationsDatabase roomDatabase;
    private VisitedLocationsDao visitedLocationsDao;

    // retrieved data from local db
    private List<VisitedLocations> retrievedDatas = new ArrayList<>();

    // retrieve and upload progress level
    private double currProgress = 0;
    private int dataSize;

    // Address Fetch
    AddressReceiver addressReceiver = new AddressReceiver(new Handler(), this);

    // UI stuff
    ProgressBar progressBar;
    TextView progressBarText;
    Button retryButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_matched_locations);

        setUI();

        Notifications.removeNotification(Constants.DangerNotification_ID, this);

        // TODO:[Check] fetch locations and match

        // set local db configs
        roomDatabase = VisitedLocationsDatabase.getDatabase(getApplicationContext());
        visitedLocationsDao = roomDatabase.visitedLocationsDao();

        // firebase
        firebaseReference = FirebaseDatabase.getInstance().getReference();

        findMatchedLocations();

    }

    private void setUI() {

        progressBar = findViewById(R.id.progressBar);
        progressBarText = findViewById(R.id.progressBar_text);
        retryButton = findViewById(R.id.retry_btn);

    }

    private void findMatchedLocations() {

        matchedLocationPosition = 0;
        retryButton.setEnabled(false);

        matchedLocations.clear();

        roomDatabase.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {

                // fetch from local db and query firebase
                retrievedDatas = visitedLocationsDao.fetchAll();

                // retrieval from localDB done (30%)
                currProgress = 30;
                progressBar.setProgress((int) currProgress);

                dataSize = retrievedDatas.size();

                if(dataSize==0){
                    // local database empty

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                           progressBarText.setText(getText(R.string.local_db_empty_text));
                           progressBar.setVisibility(View.GONE);

                        }
                    });

                    return;

                }


                for (VisitedLocations currentEntry: retrievedDatas)
                {
                    // format = "latLon_dateTime"
                    String[] splitter = currentEntry.splitPrimaryKey();

                    // firebase query values
                    final String key = currentEntry.getATencodedlatlon();
                    final String dateTime = splitter[1];

                    Log.d(LogTags.MatchFound_TAG, "run: query key = "+key +" date time = "+dateTime);

                    if(!Internet.isInternetAvailable(getApplicationContext())){

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                internetDisconnceted();
                                retryButton.setEnabled(true);
                            }
                        });

                        return;
                    }

                    // query in firebase
                    firebaseReference = FirebaseDatabase.getInstance().getReference().child("infectedLocations").child(key).child(dateTime);
                    firebaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.getValue()!=null){
                                // INFECTED LOCATION MATCH FOUND!

                                // TODO: add to list/recycler view

                                String latLon = key;
                                long count = (long) dataSnapshot.child("count").getValue();

                                MatchedLocation matchedLocation = new MatchedLocation(latLon, dateTime, count);
                                matchedLocations.add(matchedLocation);

                                // start address fetch service
                                addressReceiver.startAddressFetchService(
                                        ShowMatchedLocationsActivity.this,
                                        matchedLocation.getLatitude(),
                                        matchedLocation.getLongitude(),
                                        matchedLocationPosition
                                );

                                matchedLocationPosition++;

                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                            // internet connection lost

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    internetDisconnceted();
                                    retryButton.setEnabled(true);
                                }
                            });

                        }
                    });


                    // keep track of upload progress (30%-60%)
                    currProgress += (double) 30/dataSize;
                    if(currProgress<=100)
                        progressBar.setProgress((int) currProgress);

                    // let the value listener and address fetch service catch up
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Log.d(LogTags.MatchFound_TAG, "run: thread not tired");
                    }

                }

            }
        });

    }

    private void internetDisconnceted() {

        progressBar.setVisibility(View.GONE);
        progressBarText.setText(getText(R.string.internet_disconnected_text));

        Toast.makeText(this, getText(R.string.no_internet_toast), Toast.LENGTH_LONG)
                .show();

    }

    public void retryClicked(View view) {

        progressBar.setProgress(0);
        progressBar.setVisibility(View.VISIBLE);
        progressBarText.setText(getText(R.string.loading_progressbar_text));
        findMatchedLocations();

    }

    private int updateCount = 0;
    @Override
    public void updateAddress(String address, int listPosition) {

        /*
        address received here
         */

        matchedLocations.get(listPosition).setAddress(address);
        Log.d(LogTags.MatchFound_TAG, "updateAddress: address = "+matchedLocations.get(listPosition).toString());

        // keep track of upload progress (60%-100%)
        currProgress += (double) 40/matchedLocations.size();
        if(currProgress<=100)
            progressBar.setProgress((int) currProgress);

        updateCount++;
        if(updateCount>=matchedLocations.size()){

            updateCount = 0;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    retryButton.setEnabled(true);
                    progressBarText.setText(getText(R.string.finished_progressbar_text));
                    progressBar.setVisibility(View.GONE);
                }
            });

        }

    }
}

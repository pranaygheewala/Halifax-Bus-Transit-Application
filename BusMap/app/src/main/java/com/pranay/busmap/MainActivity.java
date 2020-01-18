package com.pranay.busmap;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.transit.realtime.GtfsRealtime;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    RecyclerView list_bus;
    Button btn_all, btn_sel;

    ArrayList<String> busidlist = new ArrayList<>();
    ArrayList<Boolean> buschecklist = new ArrayList<>();

    ArrayList<String> selectedBus = new ArrayList<>();

    private ListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        list_bus = findViewById(R.id.list_bus);
        btn_all = findViewById(R.id.btn_all);
        btn_sel = findViewById(R.id.btn_sel);


        new busInformation().execute();

        btn_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                    return;
                } else {
                    startActivity(new Intent(MainActivity.this, MapsActivity.class));
                }
            }
        });

        btn_sel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                    return;
                } else {
                    for (int i = 0; i < buschecklist.size(); i++) {
                        if (buschecklist.get(i)) {
                            selectedBus.add(busidlist.get(i));
                        }
                    }

                    Log.e("xyz", "onClick: " + selectedBus);
                    if (selectedBus.size() > 0) {

                        Intent i = new Intent(MainActivity.this, MapsActivity.class);
                        i.putExtra("type", "select");
                        i.putStringArrayListExtra("list", selectedBus);
                        startActivity(i);

                    } else {
                        Toast.makeText(MainActivity.this, "Select at least one bus", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }


    class busInformation extends AsyncTask<Void, Void, Void> {
        GtfsRealtime.FeedMessage feed = null;

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("loading...");
            progressDialog.setCancelable(false);
            if (!progressDialog.isShowing()) {
                progressDialog.show();
            }
            buschecklist.clear();
            busidlist.clear();
            selectedBus.clear();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            URL url = null;
            try {
                url = new URL("http://gtfs.halifax.ca/realtime/Vehicle/VehiclePositions.pb");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            try {
                feed = GtfsRealtime.FeedMessage.parseFrom(url.openStream());
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();

            for (GtfsRealtime.FeedEntity entity : feed.getEntityList()) {

                if (entity.hasVehicle()) {

                    busidlist.add(entity.getVehicle().getVehicle().getId());
                    buschecklist.add(false);
                }
            }
            setListData();
        }

        private void setListData() {
            mAdapter = new ListAdapter(getApplicationContext(), busidlist, buschecklist, new OnItemClickListener() {
                @Override
                public void onItemClick(int number) {

                    if (buschecklist.get(number)) {
                        buschecklist.set(number, false);
                    } else {
                        buschecklist.set(number, true);
                    }

                    mAdapter.notifyDataSetChanged();
                }
            });
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
            list_bus.setLayoutManager(mLayoutManager);
            list_bus.setItemAnimator(new DefaultItemAnimator());
            list_bus.setAdapter(mAdapter);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        new busInformation().execute();
    }
}

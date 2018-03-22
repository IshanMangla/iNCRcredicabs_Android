package com.ncr.interns.codecatchers.incredicabs;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.maps.model.Dash;
import com.ncr.interns.codecatchers.incredicabs.Adapter.*;
import com.ncr.interns.codecatchers.incredicabs.NCABdatabase.CabMatesContract;
import com.ncr.interns.codecatchers.incredicabs.NCABdatabase.EmployeeCabMatesDetails;
import com.ncr.interns.codecatchers.incredicabs.NCABdatabase.NcabSQLiteHelper;

import java.util.ArrayList;
import java.util.Calendar;

public class Dashboard extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    LinearLayout linearLayout;
    RecyclerView mRecyclerView;
    ArrayList<EmployeeCabMatesDetails> mList;
    Button checkIn,checkOut,Complaints,request;
    SQLiteDatabase mSqLiteDatabase;
    NcabSQLiteHelper ncabSQLiteHelper;
    Cursor cursor;
    Button button_sos;
    private static final String MY_PREFERENCES = "MyPrefs_login";
    Context context = this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ncabSQLiteHelper = new NcabSQLiteHelper(this);
        mSqLiteDatabase = ncabSQLiteHelper.getWritableDatabase();
        getIdofComponents();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        CabMatesAdapter adapter = new CabMatesAdapter(getCabmatesDetails(),this);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.addItemDecoration(new
                DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        mRecyclerView.setAdapter(adapter);

        cabMatesNotification();//Abhishek Alarm manager

        checkIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: 3/15/2018 CHeckIn Intent
            }
        });

        checkOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: 3/15/2018  CheckOut Intent
            }
        });

        Complaints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Dashboard.this,FeedbackActivity.class);
                startActivity(intent);
            }
        });

        request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Dashboard.this,MainRequestActivity.class);
                startActivity(intent);
            }
        });

        button_sos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: 3/20/2018 By Harshit pandey SOS implementation
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.call_transport){
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("tel:7895305782"));
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_call_transport_one) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("tel:7895305782"));
            startActivity(intent);

        } else if (id == R.id.nav_call_transport_two) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("tel:7895305782"));
            startActivity(intent);

        } else if (id == R.id.nav_app_feedback) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_EMAIL, "gs250365@ncr.com");
            intent.putExtra(Intent.EXTRA_SUBJECT, "Test");
            intent.putExtra(Intent.EXTRA_TEXT," Test Test");
            intent.setType("message/rfc822");
            startActivity(Intent.createChooser(intent, "Choose an email client"));

        } else if (id == R.id.nav_about_developers) {
            // TODO: 3/18/2018 About Page
        } else if(id == R.id.LogOut){
            /*Snackbar snackbar = Snackbar.make(linearLayout,"Logging Out",Snackbar.LENGTH_LONG);
            snackbar.show();
            */
            Intent intent = new Intent(this,Login.class);
            mSqLiteDatabase.execSQL("DELETE FROM "+CabMatesContract.DB_TABLE);
            SharedPreferences sharedPreferences = context.getSharedPreferences(MY_PREFERENCES,Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();
            startActivity(intent);
            finish();
        }


        DrawerLayout drawer =  findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void getIdofComponents(){
        button_sos = findViewById(R.id.button_sos);
        checkIn = findViewById(R.id.button_checkIn);
        checkOut = findViewById(R.id.button_checkOut);
        Complaints = findViewById(R.id.button_complaint);
        request = findViewById(R.id.button_request);
        linearLayout = findViewById(R.id.dashboard_linerarParent);
        mRecyclerView = findViewById(R.id.recyclerView);

    }

    public Cursor getCabmatesDetails(){

        cursor = mSqLiteDatabase.rawQuery("SELECT * FROM "+ CabMatesContract.DB_TABLE,null);
        return cursor;


    }

    public void cabMatesNotification(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 32);
        // TODO: 3/21/2018 Get the Current shift time from SQlite database
        Intent intent1 = new Intent(Dashboard.this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(Dashboard.this, 0,intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) Dashboard.this.getSystemService(ALARM_SERVICE);
        assert am != null;
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);

    }
}

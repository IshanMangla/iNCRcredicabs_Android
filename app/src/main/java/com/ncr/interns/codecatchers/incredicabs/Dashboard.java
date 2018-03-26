package com.ncr.interns.codecatchers.incredicabs;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ncr.interns.codecatchers.incredicabs.Adapter.*;
import com.ncr.interns.codecatchers.incredicabs.NCABdatabase.CabMatesContract;
import com.ncr.interns.codecatchers.incredicabs.NCABdatabase.ContactsContract;
import com.ncr.interns.codecatchers.incredicabs.NCABdatabase.EmployeeCabMatesDetails;
import com.ncr.interns.codecatchers.incredicabs.NCABdatabase.EmployeeContract;
import com.ncr.interns.codecatchers.incredicabs.NCABdatabase.NcabSQLiteHelper;
import com.ncr.interns.codecatchers.incredicabs.NCABdatabase.ShiftContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

public class Dashboard extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    LinearLayout linearLayout;
    RecyclerView mRecyclerView;
    ArrayList<EmployeeCabMatesDetails> mList;
    Button checkIn, checkOut, Complaints, request;
    SQLiteDatabase mSqLiteDatabase;
    NcabSQLiteHelper ncabSQLiteHelper;
    Cursor cursor;
    Button button_sos;
    String number, Pickuptime = "14:10";
    String Employee_Qlid;
    String Employee_Name;
    String Employee_HomeAddress;
    String Employee_Contact_number;
    CabMatesAdapter adapter;
    JSONObject jsonObject;
    String Route_No = null;
    String Pickup_Time = null;
    String Start_Time = null;
    String End_Time = null;

    boolean checkCon;
    String mainUrl = "http://ec2-18-219-151-75.us-east-2.compute.amazonaws.com:8080/NCAB/AndroidService/RoasterDetailsByEmpID"; //
    String loginUrl = "http://ec2-18-219-151-75.us-east-2.compute.amazonaws.com:8080/NCAB/EmployeeService/login-android";
    SharedPreferences sharedPreferences;
    private static final String MY_PREFERENCES = "MyPrefs_login";
    Context context = this;
    private static final String TAG = "Dashboard Debugging";
    private static final int REQUEST_CALL = 1;
    TextView Emp_QLID_textView, Emp_Name_textView, Emp_HomeAddress_textView, Emp_ContactNum_textView;
    TextView Current_shift;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ncabSQLiteHelper = new NcabSQLiteHelper(this);
        mSqLiteDatabase = ncabSQLiteHelper.getWritableDatabase();
        getIdofComponents();
        getEmployeeData();
        Emp_QLID_textView = findViewById(R.id.Emp_QLid);
        Emp_Name_textView = findViewById(R.id.Emp_Name);
        Emp_HomeAddress_textView = findViewById(R.id.Emp_homeAddress);
        Emp_ContactNum_textView = findViewById(R.id.Emp_contactNumber);
        Emp_QLID_textView.setText(String.format("Emp.ID: %s", Employee_Qlid));
        Emp_Name_textView.setText(String.format("Emp Name: %s", Employee_Name));
        Emp_HomeAddress_textView.setText(String.format("Current Address : %s", Employee_HomeAddress));
        Emp_ContactNum_textView.setText(String.format("Contact Number:- %s", Employee_Contact_number));
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

        adapter = new CabMatesAdapter(getCabMatesDetails(), this);

        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mRecyclerView.setAdapter(adapter);
        checkCon = checkConnection(Dashboard.this);

        //<editor-fold desc="Get teh data from database">
        final String query = "select a.cabmatepickuptime, a.routenumber, a.roasterid, a.shiftid, b.starttime, b.endtime  from CabMatesDetails a, ShiftTable b where a.CabMateQlid = ? and a.shiftid = b.shiftid";
        Cursor c = mSqLiteDatabase.rawQuery(query, new String[]{Employee_Qlid.toUpperCase()});
        c.moveToFirst();
        while (!c.isAfterLast()) {
            Pickup_Time = c.getString(c.getColumnIndex(CabMatesContract.COLUMN_CABMATE_PICKUPTIME));
            Start_Time = c.getString(c.getColumnIndex(ShiftContract.COLUMN_START_TIME));
            Route_No = c.getString(c.getColumnIndex(CabMatesContract.COLUMN_CABMATE_ROUTE_NUMBER));
            End_Time = c.getString(c.getColumnIndex(ShiftContract.COLUMN_END_TIME));
            c.moveToNext();
        }
        //</editor-fold>
        String currentShift = "Current Shift is " + Start_Time + " to " + End_Time;
        Current_shift = findViewById(R.id.textView_currentShift);
        Current_shift.setText(currentShift);
       /* cabMatesNotification();//Abhishek Alarm manager
        getCabMateShiftTimeNew();
*/
        checkIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Start_Time == null) {
                    Snackbar snackbar = Snackbar.make(linearLayout, "Server Error", Snackbar.LENGTH_LONG);
                    snackbar.show();
                    return;
                }

                if (checkCon) {
                    Intent checkIn_intent = new Intent(Dashboard.this, CheckIn.class);
                    checkIn_intent.putExtra("pickup", Pickup_Time);
                    checkIn_intent.putExtra("start_time", Start_Time);
                    checkIn_intent.putExtra("route_no", Route_No);
                    startActivity(checkIn_intent);
                } else {
                    final AlertDialog alertDialog = new AlertDialog.Builder(Dashboard.this).create();
                    alertDialog.setTitle("No Connection Available");
                    alertDialog.setMessage("Please Connect to the Internet");
                    alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
                    alertDialog.setButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            alertDialog.cancel();
                        }
                    });
                    alertDialog.show();
                }

            }
        });

        checkOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Start_Time == null) {
                    Snackbar snackbar = Snackbar.make(linearLayout, "Server Error", Snackbar.LENGTH_LONG);
                    snackbar.show();
                    return;
                }

                if (checkCon) {
                    Intent checkOut_intent = new Intent(Dashboard.this, CheckOut.class);
                    startActivity(checkOut_intent);
                } else {
                    final AlertDialog alertDialog = new AlertDialog.Builder(Dashboard.this).create();
                    alertDialog.setTitle("No Connection Available");
                    alertDialog.setMessage("Please Connect to the Internet");
                    alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
                    alertDialog.setButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            alertDialog.cancel();
                        }
                    });
                    alertDialog.show();
                }
            }
        });

        Complaints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Dashboard.this, FeedbackActivity.class);
                startActivity(intent);
            }
        });

        request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Dashboard.this, MainRequestActivity.class);
                startActivity(intent);

            }
        });

        button_sos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ContextCompat.checkSelfPermission(Dashboard.this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(Dashboard.this, new String[]{android.Manifest.permission.CALL_PHONE}, CustomDialogClass.REQUEST_CALL);
                } else {
                    CustomDialogClass cdd = new CustomDialogClass(Dashboard.this, context);
                    cdd.show();
                    cdd.start();
                }
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
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
        if (id == R.id.call_transport) {
            number = "9998764636";
            makePhoneCall(number);
        }
        if (id == R.id.refresh) {
/*
            sharedPreferences = getSharedPreferences(MY_PREFERENCES,Context.MODE_PRIVATE);
            String userQlid = sharedPreferences.getString("user_qlid", "");
            String userPassword = sharedPreferences.getString("user_password", "");
            try {
                jsonObject.put("qlid", userQlid);
                jsonObject.put("password", userPassword);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JsonObjectRequest jsonObjRequest = new JsonObjectRequest(Request.Method.POST, loginUrl, jsonObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            Log.i("VOLLEY", "inside onResponse method: Login");
                            Log.i("VOLLEY", response.toString());

                            try {
                                if (response.getString("success").equalsIgnoreCase("true")) {
                                    mSqLiteDatabase.execSQL("DELETE FROM " + CabMatesContract.DB_TABLE);
                                    parseJSON(response);
                                    Toast.makeText(context, "Refresh Done", Toast.LENGTH_SHORT).show();
                                    adapter = new CabMatesAdapter(getCabMatesDetails(), Dashboard.this);
                                    adapter.notifyDataSetChanged();
                                    // TODO: 3/26/2018 Handel the refresh button
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // Do something when error occurred
                            Log.d("VOLLEY", "Something went wrong");
                            error.printStackTrace();
                        }
                    });

            RESTService.getInstance(Dashboard.this).addToRequestQueue(jsonObjRequest);
*/
        }

        return super.onOptionsItemSelected(item);
    }

    private void parseJSON(JSONObject response) {

        Log.d(TAG, "parseJSON: Response:- " + response);

        try {
            //<editor-fold desc="Yet to Implement">
            JSONArray cabMates = response.getJSONArray("rosterInfo");
            for (int i = 0; i < cabMates.length(); i++) {

                try {
                    JSONObject cabMateJSON = cabMates.getJSONObject(i);
                    String CabMate_Qlid = cabMateJSON.getString("Qlid");
                    String CabMate_name = cabMateJSON.getString("f_name") + " " + cabMateJSON.getString("l_name");
                    String CabMate_contactNumber = cabMateJSON.getString("e_mob");
                    String CabMate_address = cabMateJSON.getString("p_a");
//                    String CabMate_pickupTime = cabMateJSON.getString("pickup_time");
                    ContentValues cabMateValues = new ContentValues();
                    cabMateValues.put(CabMatesContract.COLUMN_CABMATE_QLID, CabMate_Qlid);
                    cabMateValues.put(CabMatesContract.COLUMN_CABMATE_NAME, CabMate_name);
                    cabMateValues.put(CabMatesContract.COLUMN_CABMATE_CONTACT_NUMBER, CabMate_contactNumber);
                    cabMateValues.put(CabMatesContract.COLUMN_CABMATE_ADDRESS, CabMate_address);
                    //                  cabMateValues.put(CabMatesContract.COLUMN_CABMATE_PICKUPTIME,CabMate_pickupTime);
                    mSqLiteDatabase.insert(CabMatesContract.DB_TABLE, null, cabMateValues);
                    adapter = new CabMatesAdapter(getCabMatesDetails(), Dashboard.this);

                    mRecyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();

                    Log.d(TAG, "parseJSON: Data Inserted to Cabmate Table row :- " + i);
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_call_transport_one) {
            number = "9998764636";
            makePhoneCall(number);
            // FIXME: 3/22/2018 Get the real number from DB

        } else if (id == R.id.nav_call_transport_two) {
            number = "7864648383";
            makePhoneCall(number);

        } else if (id == R.id.nav_app_feedback) {
            //<editor-fold desc="Implementation Hidden">
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_EMAIL, "gs250365@ncr.com");
            intent.putExtra(Intent.EXTRA_SUBJECT, "Test");
            intent.putExtra(Intent.EXTRA_TEXT, " Test Test");
            intent.setType("message/rfc822");
            startActivity(Intent.createChooser(intent, "Choose an email client"));
            //</editor-fold>

        } else if (id == R.id.nav_about_developers) {
            startActivity(new Intent(Dashboard.this, AboutPage.class));

        } else if (id == R.id.LogOut) {

            Intent intent = new Intent(this, Login.class);
            mSqLiteDatabase.execSQL("DELETE FROM " + CabMatesContract.DB_TABLE);
            mSqLiteDatabase.execSQL("DELETE FROM " + ShiftContract.DB_TABLE);
            mSqLiteDatabase.execSQL("DELETE FROM " + ContactsContract.DB_TABLE);
            sharedPreferences = context.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();
            startActivity(intent);
            finish();
        }


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void getIdofComponents() {
        button_sos = findViewById(R.id.button_sos);
        checkIn = findViewById(R.id.button_checkIn);
        checkOut = findViewById(R.id.button_checkOut);
        Complaints = findViewById(R.id.button_complaint);
        request = findViewById(R.id.button_request);
        linearLayout = findViewById(R.id.dashboard_linerarParent);
        mRecyclerView = findViewById(R.id.recyclerView);

    }

    //<editor-fold desc="Function to get the Current Cab Mates Details from the Database">
    public Cursor getCabMatesDetails() {
        cursor = mSqLiteDatabase.rawQuery("SELECT * FROM " + CabMatesContract.DB_TABLE, null);
        return cursor;
    }
    //</editor-fold>

    //<editor-fold desc="Function to make the phone call">
    private void makePhoneCall(String number) {
        //String number = mEditTextNumber.getText().toString();
        if (true) {

            if (ContextCompat.checkSelfPermission(Dashboard.this,
                    android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(Dashboard.this,
                        new String[]{android.Manifest.permission.CALL_PHONE}, REQUEST_CALL);
            } else {
                String dial = "tel:" + number;
                startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
            }

        } else {
            Toast.makeText(context, "There might be Some Problem..", Toast.LENGTH_SHORT).show();
            //Toast.makeText(Dashboard.this, "Enter Phone Number", Toast.LENGTH_SHORT).show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Function overRided to ask permission">
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CALL) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                CabMatesAdapter adapter = new CabMatesAdapter();
                makePhoneCall(number);
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }
    //</editor-fold>

    //<editor-fold desc="Method to get CabMatesShift Time">
    public String[] getCabMatesShiftTime() {
        Cursor cursorShiftTime = mSqLiteDatabase.rawQuery("SELECT * FROM " + CabMatesContract.DB_TABLE, null);
        int len = cursor.getCount();
        String shiftTimes_array[] = new String[len];
        int counter = 0;
        while (cursorShiftTime.moveToNext()) {
            String shiftTime = cursorShiftTime.getString(cursorShiftTime.getColumnIndex
                    (CabMatesContract.COLUMN_CABMATE_PICKUPTIME));
            shiftTimes_array[counter] = shiftTime;
            counter++;
        }
        return shiftTimes_array;
    }
    //</editor-fold>

    //<editor-fold desc="method CabmatesNotification">
    public void cabMatesNotification(String Pickuptim) {
        String cabMatesShiftTime = Pickuptim;
        String[] time = cabMatesShiftTime.split(":");
        int hour = Integer.parseInt(time[0].trim());
        int min = Integer.parseInt(time[1].trim());
        if (min >= 30)
            min -= 30;
        else {
            hour -= 1;
            min += 30;
        }
        Log.e("my log alarm", "h- " + hour + "m-" + min);
        //Toast.makeText(context, "h- "+hour+"m-"+min, Toast.LENGTH_SHORT).show();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, min);
        Intent intent1 = new Intent(Dashboard.this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(Dashboard.this, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) Dashboard.this.getSystemService(ALARM_SERVICE);
        assert am != null;
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);


    }
    //</editor-fold>


    //<editor-fold desc="OnStart">
    @Override
    protected void onStart() {
        if (getSharedPreferences(null, MODE_PRIVATE).getBoolean("alarm", true))
            gettingPickuptime();
        else
            getSharedPreferences(null, MODE_PRIVATE).edit().putBoolean("alarm", false).apply();
        super.onStart();

    }
    //</editor-fold>

    //<editor-fold desc="gettingPickupTime">
    private void gettingPickuptime() {
        final JSONObject[] js = new JSONObject[1];
        final String[] pickuptime = new String[1];
        JSONObject jsonBodynot = new JSONObject();
        try {
            String Emp_qlid = getEmployeeQlid();
            jsonBodynot.put("Emp_Qlid", Emp_qlid);
            Log.d(TAG, "gettingPickuptime: Emp_QLID:- " + Emp_qlid);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        JsonObjectRequest jsonObjRequest = new JsonObjectRequest(Request.Method.POST, mainUrl, jsonBodynot, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i("VOLLEY", "inside onResponse method:UnscheduledRequest");
                Log.i("VOLLEY", response.toString());
                try {
                    js[0] = new JSONObject(response.getString("result"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    if (js[0].getString("Pickup_Time") != "") {
                        pickuptime[0] = js[0].getString("Pickup_Time");
                        Pickuptime = pickuptime[0];
                        cabMatesNotification(Pickuptime);

                        // Toast.makeText(Dashboard.this, "Your request is Submitted", Toast.LENGTH_LONG).show();
                    } else {
                        cabMatesNotification("14:30");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                // Do something when error occurred
                Log.d("VOLLEY", "Something went wrong");
                //  Toast.makeText(getActivity(), "Oops..Something Went wrong", Toast.LENGTH_SHORT).show();

                error.printStackTrace();
            }
        });

        RESTService.getInstance(Dashboard.this).addToRequestQueue(jsonObjRequest);


    }
    //</editor-fold>


    public String getEmployeeQlid() {
        sharedPreferences = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        String Employee_Qlid = sharedPreferences.getString("user_qlid", "");
        return Employee_Qlid;
    }

    //<editor-fold desc="Method to get the Current Employee data from the database to show in dashboard">
    public void getEmployeeData() {
        Cursor c = mSqLiteDatabase.rawQuery("SELECT * FROM " + EmployeeContract.DB_TABLE, null);
        while (c.moveToNext()) {
            Employee_Qlid = c.getString(c.getColumnIndex(EmployeeContract.COLUMN_EMP_QLID));
            Employee_Name = c.getString(c.getColumnIndex(EmployeeContract.COLUMN_FIRST_NAME))
                    + c.getString(c.getColumnIndex(EmployeeContract.COLUMN_LAST_NAME));
            Employee_Contact_number = c.getString(c.getColumnIndex(EmployeeContract.COLUMN_CONTACT_NUMBER));
            Employee_HomeAddress = c.getString(c.getColumnIndex(EmployeeContract.COLUMN_HOME_ADDRESS));
        }
        c.close();

    }
    //</editor-fold>


    public static boolean checkConnection(Context context) {
        final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetworkInfo = connMgr.getActiveNetworkInfo();

        if (activeNetworkInfo != null) { // connected to the internet
            //Toast.makeText(context, activeNetworkInfo.getTypeName(), Toast.LENGTH_SHORT).show();

            if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                // connected to wifi
                return true;
            } else if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                // connected to the mobile provider's data plan
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}

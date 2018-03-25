package com.ncr.interns.codecatchers.incredicabs;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.ncr.interns.codecatchers.incredicabs.NCABdatabase.EmployeeContract;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class CheckIn extends AppCompatActivity {
    public static final String DATABASE_NAME= "NCABDatabase";
    String DB_TABLE = "EmployeeData";
    SQLiteDatabase mDatabase;
    String Route_No = null;
    String Pickup_Time = null;
    String Start_Time = null;
    String ipaddress = "ec2-18-219-151-75.us-east-2.compute.amazonaws.com:8080";
    String url = "http://" + ipaddress + "/NCAB/VendorService/checkin";
    String url_roasterinfo = "http://" + ipaddress + "/NCAB/VendorService/RoasterDetailsByEmpID";
    String Emp_Qlid;
    String Check_In=null;
    private static final String MY_PREFERENCES = "MyPrefs_login";
    SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        openScanner();
        Emp_Qlid = getEmployeeQlid();
        JSONObject jsonBodyRequest = new JSONObject();
        try {
            jsonBodyRequest.put("Emp_Qlid", Emp_Qlid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjRequest = new JsonObjectRequest(Request.Method.POST,
                url_roasterinfo,
                jsonBodyRequest,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject js = new JSONObject(response.getString("result"));
                            Route_No = js.getString("Route_No");
                            Pickup_Time = js.getString("Pickup_Time");
                            Start_Time = js.getString("Start_Time");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Server Error", Toast.LENGTH_LONG).show();
                            Intent Dashboard_intent = new Intent(getApplicationContext(), Dashboard.class);
                            startActivity(Dashboard_intent);
                        }
                        Log.i("VOLLEY", "inside onResponse method:doLogin");
                        Log.i("VOLLEY", response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Do something when error occurred
                        Log.d("VOLLEY", "Something went wrong");
                        Toast.makeText(getApplicationContext(), "No Network Connection", Toast.LENGTH_LONG).show();
                        /*Intent Dashboard_intent = new Intent(getApplicationContext(), Dashboard.class);
                        startActivity(Dashboard_intent);*/
                        error.printStackTrace();
                        finish();
                    }
                });
        RESTService.getInstance(getApplicationContext()).addToRequestQueue(jsonObjRequest);

    }

    private void openScanner() {
        IntentIntegrator scanIntegrator = new IntentIntegrator(CheckIn.this);
        scanIntegrator.setPrompt("Scan a barcode");
        scanIntegrator.setBeepEnabled(true);
        scanIntegrator.setOrientationLocked(true);
        scanIntegrator.setBarcodeImageEnabled(true);
        scanIntegrator.setCaptureActivity(CustomScannerActivity.class);
        scanIntegrator.initiateScan();
    }

    @Override
    protected void onActivityResult(final int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        /*As an example in order to get the content of what is scanned you can do the following*/
        if (scanningResult.getContents() != null) {
            if (Pickup_Time != null && Start_Time != null && Route_No != null) {
                String scanContent = scanningResult.getContents().toString();
                Log.e("scanContent", scanContent);
                JSONObject jsonBodyRequest = new JSONObject();
                try {
                    DateFormat date_format = new SimpleDateFormat("yyyy-MM-dd");
                    DateFormat time_format = new SimpleDateFormat("HH:mm");
                    String split[] = Pickup_Time.split(":");
                    String split1[] = Start_Time.split(":");
                    int Pick_hour = Integer.parseInt(split[0]);
                    int Start_Time_Hour = Integer.parseInt(split1[0]);

                    Calendar rightNow = Calendar.getInstance();
                    System.out.println(rightNow);
                    int Hour = rightNow.get(Calendar.HOUR_OF_DAY);
                    System.out.println(Hour);
                    int Minute = rightNow.get(Calendar.MINUTE);
                    jsonBodyRequest.put("Emp_Qlid", Emp_Qlid);
                    jsonBodyRequest.put("Route_No", Route_No);
                    jsonBodyRequest.put("Trip_Date", date_format.format(rightNow.getTime()));
                    jsonBodyRequest.put("Check_in_Time", time_format.format(rightNow.getTime()));
                    jsonBodyRequest.put("Cab_Type", "auto");
                    if (Hour >= Pick_hour && Hour <= Start_Time_Hour) {
                        jsonBodyRequest.put("Trip_Type", "Pick");
                    } else {
                        jsonBodyRequest.put("Trip_Type", "Drop");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JsonObjectRequest jsonObjRequest = new JsonObjectRequest(Request.Method.POST,
                        url,
                        jsonBodyRequest,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                JSONObject js = null;
                                try {
                                    js = new JSONObject(response.getString("result"));
                                    Check_In = js.getString("Check_In");
                                    if(Check_In.equals("Done"))
                                    {

                                        Toast.makeText(getApplicationContext(), "Successful Checkin", Toast.LENGTH_LONG).show();
                                    }
                                    else
                                    {
                                        Toast.makeText(getApplicationContext(), "Already Checkedin", Toast.LENGTH_LONG).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                Log.i("VOLLEY", "inside onResponse method:doLogin");
                                Log.i("VOLLEY", response.toString());


                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // Do something when error occurred
                                Toast.makeText(getApplicationContext(), "No Network Connection", Toast.LENGTH_LONG).show();
                                Intent Dashboard_intent = new Intent(getApplicationContext(), Dashboard.class);
                                startActivity(Dashboard_intent);
                                Log.d("VOLLEY", "Something went wrong");
                                error.printStackTrace();
                            }
                        });
                RESTService.getInstance(getApplicationContext()).addToRequestQueue(jsonObjRequest);
                finish();
                /*Intent Dashboard_intent = new Intent(getApplicationContext(), Dashboard.class);
                startActivity(Dashboard_intent);
*/
            } else {
                Toast.makeText(getApplicationContext(), "CheckIn failed,Contact Admin", Toast.LENGTH_LONG).show();
            }
        }
        else

        {
            finish();
        }
    }
    public String getEmployeeQlid(){
        sharedPreferences = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        String Employee_Qlid = sharedPreferences.getString("user_qlid","");
        return Employee_Qlid;
    }
}

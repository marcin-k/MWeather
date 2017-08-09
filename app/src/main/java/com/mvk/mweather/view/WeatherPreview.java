package com.mvk.mweather.view;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.mvk.mweather.R;
import com.mvk.mweather.model.Weather;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WeatherPreview extends AppCompatActivity {

    private double latitude;
    private double longitude;
    private Weather weather;
    @BindView(R.id.tv1) TextView tv1;
    @BindView(R.id.temp) TextView temp;
    @BindView(R.id.street) TextView streetTV;
    @BindView(R.id.city) TextView cityTV;
    @BindView(R.id.time) TextView timeTV;
    @BindView(R.id.rain) TextView rainTV;

    String TAG = "myTag";

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_preview);

        ButterKnife.bind(this);

        //gets users weather location (latitude, longitude);
        boolean gotLocation = checkLocationPermission();
        //if app could get the location
        if (!gotLocation){
            tv1.setText("Application requires access to your location to display the weather");
        }

        else{
            Log.d(TAG, "lets set Everything up");
            getForecast(latitude, longitude);
            getAddress();
        }
    }

    //************ Checks if location is enabled and prompts user for it ***************************
    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission. ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission. ACCESS_FINE_LOCATION)) {

                new AlertDialog.Builder(this)
                        .setTitle("My location?")
                        .setMessage("We need your weather location to be able to present to you accurate weather forecast")
                        .setPositiveButton("I understand", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(WeatherPreview.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission. ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            longitude = location.getLongitude();
            latitude = location.getLatitude();
            return true;
        }
    }

    //******************** Requests a address details from Google API ******************************
    //gets city name based on your location from google api
    private void getAddress(){
        if(networkAvailability()) {
            String googleApi = "http://maps.googleapis.com/maps/api/geocode/json?latlng="+latitude+","+longitude;

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(googleApi)
                    .build();

            Call call = client.newCall(request);

            //Enqueue - onResponse is executed as a call back once the 2nd thread is finished
            //
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                    alertUserAboutError();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    try {
                        String jsonData = response.body().string();
                        //checks if the response was received successfully
                        if (response.isSuccessful()) {
                            try {
                                parseAddress(jsonData);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            alertUserAboutError();
                        }
                    }catch (IOException e) {
                        Log.e(TAG, "Exception caught ", e);
                    }
                }
            });

        }
        else {

            Toast.makeText(this, "network is not available", Toast.LENGTH_LONG).show();
        }
    }

    //************************ Parse the address from JSON Object **********************************
    //updates the the UI element for city and street
    private void parseAddress(String jsonData) throws JSONException {

        JSONObject address = new JSONObject(jsonData);
        JSONArray itemsDetails = address.getJSONArray("results");
        JSONObject addressComponent = itemsDetails.getJSONObject(0);
        Log.d(TAG, "Results2: "+addressComponent.toString());
        JSONArray addressComponentDetails = addressComponent.getJSONArray("address_components");
        JSONObject streetArray = addressComponentDetails.getJSONObject(1);
        final String street = streetArray.getString("long_name");
        Log.d(TAG, "St: "+street);
        JSONObject cityArray = addressComponentDetails.getJSONObject(3);
        final String city = cityArray.getString("short_name");
        Log.d(TAG, city);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                streetTV.setText(street);
                cityTV.setText(city);
            }
        });
    }
    //******************** Requests a weather forecast from API  ***********************************
    private void getForecast(double latitude, double longitude) {

        //key authentication for the darksky website
        String apiKey = "98339a064c6e514c7ba50d01e76e2e05";
        String forcastUrl = "https://api.darksky.net/forecast/"+apiKey+"/"+latitude+","+longitude;
        Log.d(TAG, "test2");

        if(networkAvailability()) {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(forcastUrl)
                    .build();

            Call call = client.newCall(request);

            //Enqueue - onResponse is executed as a call back once the 2nd thread is finished
            //
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                    alertUserAboutError();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    try {
                        String jsonData = response.body().string();
                        //checks if the response was received successfully
                        if (response.isSuccessful()) {
                            weather = (getWeather(jsonData));
                            //**** Changes to UI requires to be perform on main thread******************
                            //     runOnUiThread method allows run method on activity thread
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateView();
                                }
                            });

                        } else {
                            alertUserAboutError();
                        }
                    }catch (IOException e) {
                        Log.e(TAG, "Exception caught ", e);
                    }
                    catch (JSONException e) {
                        Log.e(TAG, "Exception caught ", e);
                    }
                }
            });

        }
        else {
            Toast.makeText(this, "network is not available", Toast.LENGTH_LONG).show();
        }
    }

    //********************************* Updates UI elements  ***************************************
    private void updateView() {
        temp.setText(weather.getTemperature()+"");
        tv1.setText(weather.getSummary());
        timeTV.setText(weather.getFormattedTime()+"");
        rainTV.setText("Chance of Rain: "+ weather.getPrecipChance()+"%");
//
//        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.activity_weather_preview);
//        relativeLayout.setBackgroundResource(0);
//        //gets the id of a resource in a drawable dir
//        Drawable drawable = ResourcesCompat.getDrawable(getResources(), mForecast.getCurrent().getIconResource(), null);
//        mIconImageView.setImageDrawable(drawable);
    }

    //**************************** Create Weather Object from JSON  ********************************
    private Weather getWeather(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        Log.d(TAG, forecast.toString());
        String timeZone = forecast.getString("timezone");
        JSONObject currently = forecast.getJSONObject("currently");
        Weather weather = new Weather();
        weather.setTime(currently.getLong("time"));
        weather.setIcon(currently.getString("icon"));
        weather.setPrecipChance(currently.getDouble("precipProbability"));
        weather.setSummary(currently.getString("summary"));
        weather.setTemperature(currently.getDouble("temperature"));
        weather.setTimeZone(timeZone);
        return weather;
    }
    //**************************** Check Network Availability **************************************
    private boolean networkAvailability() {
        ConnectivityManager manager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isNetworkAvaiable = false;
        //checks if there is network is present and connected
        if(networkInfo!=null && networkInfo.isConnected()){
            isNetworkAvaiable=true;
        }
        return isNetworkAvaiable;

    }
    //********************************* Error Dialogue *********************************************
    private void alertUserAboutError() {
        AlertDialogFragment dialogue = new AlertDialogFragment();
        dialogue.show(getFragmentManager(), "error_dialog");
    }

}

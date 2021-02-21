package com.rinzler.earthquake.activities;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.*;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.rinzler.earthquake.R;
import com.rinzler.earthquake.model.Earthquake;
import com.rinzler.earthquake.ui.CustomInfoWindow;
import com.rinzler.earthquake.util.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMarkerClickListener{

    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private static final int REQUEST_LOCATION_PERMISSION = 1;

    private RequestQueue requestQueue;

    //alert dialog
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;

    //making the markers colourful
    private BitmapDescriptor[] markerColors;

    //show quake list button
    private Button showListButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //click the show list button to go to another activity which shows all the eathquakes
        showListButton = findViewById(R.id.showList);
        showListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MapsActivity.this, QuakesListsActivity.class));
            }
        });
        //random colors for marker
        markerColors = new BitmapDescriptor[]{
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA),

        };

       requestQueue = Volley.newRequestQueue(this);

       getEarthquakes();

    }
    private void getEarthquakes() {
        //create an instance of the earthquake class
        Earthquake earthquake = new Earthquake();
        //objects
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, Constants.URL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    //inside the arrray features there are objects and arrays which contains objects and arrays within them selves
                    //go inside the features array
                    JSONArray features = response.getJSONArray("features");

                    for (int i=0; i < Constants.LIMIT; i++){
                        //get the object which holds all the properties
                        JSONObject properties = features.getJSONObject(i).getJSONObject("properties");
                       // Log.d("Properties", properties.getString("place"));

                        //get gemetry // coordinates
                        //inside the features array we have object named geometry
                        //go inside the geometry objects
                        JSONObject geometry = features.getJSONObject(i).getJSONObject("geometry");
                        //which then has an array of coordinates
                        JSONArray coordinates = geometry.getJSONArray("coordinates");

                        //get the cordinates thats stores inside the array
                        double lon = coordinates.getDouble(0);
                        double lat = coordinates.getDouble(1);

                      //  Log.d("Quake : ", lon + ", " + lat);
                        //this is all inside the properties object
                        earthquake.setPlace(properties.getString("place"));
                        earthquake.setType(properties.getString("type"));
                        earthquake.setTime(properties.getLong("time"));
                        earthquake.setLat(lat);
                        earthquake.setLon(lon);
                        earthquake.setMagnitude(properties.getDouble("mag"));
                        earthquake.setDetailLink(properties.getString("detail"));

                        //formatting the time from milli seconds to redable
                        DateFormat dateFormat = DateFormat.getDateInstance();
                        String formattedDate = dateFormat.format(new Date(Long.valueOf(properties.getLong("time")))
                        .getTime());

                        //add marker
                        MarkerOptions markerOptions = new MarkerOptions();
                        //generating random colors for markers
                        markerOptions.icon(markerColors[Constants.randomInt(markerColors.length,0)]);
                        markerOptions.title(earthquake.getPlace());
                        markerOptions.position(new LatLng(lat,lon));
                        //add snippet for making more room when we click the position
                        markerOptions.snippet("Magnitude : " + earthquake.getMagnitude() + "\n"
                        + "Date : " + formattedDate);

                        //show red markers and a red cirle for greater magnitude
                        if (earthquake.getMagnitude() >= 2.0){
                            CircleOptions circleOptions = new CircleOptions();
                            circleOptions.center(new LatLng(earthquake.getLat(),earthquake.getLon()));
                            circleOptions.radius(30000);
                            circleOptions.strokeWidth(3.6f);
                            circleOptions.fillColor(Color.RED);
                            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

                            mMap.addCircle(circleOptions);
                        }
                        Marker marker = mMap.addMarker(markerOptions);
                        marker.setTag(earthquake.getDetailLink());
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon),1));


                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        requestQueue.add(jsonObjectRequest);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //force map to use the custom window info instead of the default snippet
        mMap.setInfoWindowAdapter(new CustomInfoWindow(getApplicationContext()));
        //register the listerners inside the map to work
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMarkerClickListener(this);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {

            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {

            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {

            }
        };

        //TODO : check for location permission
        //if permission not already given request for permission
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
            //mMap.setMyLocationEnabled(true);
        }else {
            //if the location permission is already given
           locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            // Add a marker in HOME and move the camera
          //  Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            //LatLng home = new LatLng(location.getLatitude(), location.getLongitude());
            LatLng home = new LatLng(6.46131,79.98042);
            float zoom = 15;
            mMap.addMarker(new MarkerOptions().position(home).title("Current Location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(home, zoom));
        }
    }

    //handle location permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // Check if location permissions are granted and if so enable the
        // location data layer.
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED){
                //if the permission is granted get the location updates
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            }
        }
    }

    //hnadling window clicks
    @Override
    public void onInfoWindowClick(Marker marker) {
      // Toast.makeText(this, marker.getTag().toString(), Toast.LENGTH_SHORT).show();
       getQuakeDetails(marker.getTag().toString());

    }

    //get details of a single earthquake when a certain marker is clicked
    private void getQuakeDetails(String url) {

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                String detailsUrl = "";

                try {
                    //inside the object we have an object which is named as properties
                    JSONObject properties = response.getJSONObject("properties");
                    //inside properties we have an object which is name under products
                    JSONObject products = properties.getJSONObject("products");
                    //inside products we have an array containing nearby cities
                    JSONArray nearbyCities = products.getJSONArray("nearby-cities");

                    for (int i =0; i < nearbyCities.length(); i++){
                        //inside the  nearby cities array there in an object
                        JSONObject contentObj = nearbyCities.getJSONObject(i);
                        //inside  object we have a contents objects
                        JSONObject contents = contentObj.getJSONObject("contents");
                        //inside contents  we have nearbycityjson  object
                        JSONObject nearbyCityJson = contents.getJSONObject("nearby-cities.json");
                        //get the string url from inside the nearbycityjson
                       detailsUrl = nearbyCityJson.getString("url");


                    }
                    Log.d("URL : ", detailsUrl);
                    //todo : call getmoredetails method to open a dialog window which then opens the files which redirects to the url
                    getMoreDetails(detailsUrl);

                } catch (JSONException e) {
                    Toast.makeText(MapsActivity.this, "No Earthquakes in Nearby Places..", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MapsActivity.this, "No Earthquakes in Nearby Places..", Toast.LENGTH_SHORT).show();
            }
        });
        requestQueue.add(jsonObjectRequest);
    }

    private void getMoreDetails(String url){


        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url,
                null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {

                dialogBuilder = new AlertDialog.Builder(MapsActivity.this);
                View view = getLayoutInflater().inflate(R.layout.popup,null);


                //initialize the views in the popup.xml
                Button dismissButton = view.findViewById(R.id.dismissPopup);
                Button dismissButtonTop = view.findViewById(R.id.dismissPopTop);
                TextView popList = view.findViewById(R.id.popList);
                WebView htmlPop = view.findViewById(R.id.htmlWebView);

                StringBuilder stringBuilder = new StringBuilder();
                //TODO : accessing json array which has no name
                if (response != null){
                    for (int i =0; i < response.length(); i++){
                        try {
                            JSONObject citiesObj = response.getJSONObject(i);

                            stringBuilder.append("City : " + citiesObj.getString("name")
                                    + "\n" + "Distance : " + citiesObj.getString("distance")
                                   );

                            stringBuilder.append("\n\n");

                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    popList.setText(stringBuilder);
                    dismissButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                        }
                    });
                    dismissButtonTop.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                        }
                    });
                    //create the dialog and show them
                    dialogBuilder.setView(view);
                    dialog = dialogBuilder.create();
                    dialog.show();
                }else{
                    Toast.makeText(MapsActivity.this, "NO nearby cities..", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MapsActivity.this, "No nearby cities to Show..", Toast.LENGTH_SHORT).show();
            }
        });
        requestQueue.add(jsonArrayRequest);
    }

    //handling marker clicks
    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }
}


package com.rinzler.earthquake.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.rinzler.earthquake.R;
import com.rinzler.earthquake.model.Earthquake;
import com.rinzler.earthquake.util.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class QuakesListsActivity extends AppCompatActivity {

    private List<String> arrayList;
    private ListView listView;
    private RequestQueue requestQueue;
    private ArrayAdapter arrayAdapter;

    private List<Earthquake> quakeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quakes_lists);

        arrayList = new ArrayList<>();
        listView = findViewById(R.id.listView);

        requestQueue = Volley.newRequestQueue(this);
        quakeList = new ArrayList<>();

        getAllQuakes(Constants.URL);
    }

    void getAllQuakes(String url){

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Earthquake earthquake = new Earthquake();
                try {
                    //inside the arrray features there are objects and arrays which contains objects and arrays within them selves
                    //go inside the features array
                    JSONArray features = response.getJSONArray("features");

                    for (int i = 0; i < features.length(); i++){
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

                        arrayList.add(earthquake.getPlace());

                    }
                    //use the array adapter provided by google
                    //get the place and set to the arraylist adapter text
                    arrayAdapter = new ArrayAdapter(QuakesListsActivity.this, android.R.layout.simple_expandable_list_item_1,
                            android.R.id.text1, arrayList);
                    listView.setAdapter(arrayAdapter);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            Toast.makeText(QuakesListsActivity.this, "Clicked Position : " + (i + 1), Toast.LENGTH_SHORT).show();
                        }
                    });
                    arrayAdapter.notifyDataSetChanged();
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
}

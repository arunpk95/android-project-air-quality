package com.example.airquality;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ViewCityStat extends AppCompatActivity {
    JSONObject jsonObject;
    TextView airQualityTv,humidityTv,windSpeedTv,temeperatureTv,titleTv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_city_stat);
        //initiate views
        airQualityTv=findViewById(R.id.airQuality);
        humidityTv = findViewById(R.id.humidity);
        windSpeedTv=findViewById(R.id.windSpeed);
        temeperatureTv = findViewById(R.id.temperature);
        titleTv = findViewById(R.id.title);
        //get selected city data from intent
        try {
            jsonObject = new JSONObject(getIntent().getStringExtra("place"));
            titleTv.setText("Air Quality at "+jsonObject.getString("city"));
            fetchstat();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //fetch stats for the city from API
    public void fetchstat() throws JSONException {
        String url = "https://api.airvisual.com/v2/city?city="+jsonObject.getString("city")+"&state="+jsonObject.getString("state")+"&country="+jsonObject.getString("country")+"&key=c7728efa-9e98-425a-94a8-a0d75d7db254";

        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Fetching Data from API...");
            pDialog.show();

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        pDialog.hide();
                        try {
                            if(response.getString("status").equals("success"))
                            {
                                JSONObject responseJson = new JSONObject(response.toString());
                                JSONObject pollution = responseJson.getJSONObject("data").getJSONObject("current").getJSONObject("pollution");
                                JSONObject weather = responseJson.getJSONObject("data").getJSONObject("current").getJSONObject("weather");

                                airQualityTv.setText(pollution.getString("aqius") + " US AQI");
                                humidityTv.setText(weather.getString("hu")+ " %");
                                temeperatureTv.setText(weather.getString("tp")+" "+"\u2103");
                                windSpeedTv.setText(weather.getString("ws")+" m/s");

                            }
                            else
                            {
                                Toast.makeText(getApplicationContext(),"Sorry, Some error occured",Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(),"Sorry, Some error occured",Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),"Sorry, Some error occured",Toast.LENGTH_LONG).show();
                // hide the progress dialog
                pDialog.hide();
            }
        });

        // Adding request to request queue
        VolleySingleton.getInstance(this).addToRequestQueue(jsonObjReq);
    }

}

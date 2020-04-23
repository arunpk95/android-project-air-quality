package com.example.airquality;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AddNewLocation extends AppCompatActivity {

    Spinner countrySpinner,stateSpinner,citySpinner;
    Button addCity,backBtn;
    String selectedCountry,selectedState,selectedCity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_location);
        //initiate spinners
        countrySpinner = findViewById(R.id.spinnerCountry);
        stateSpinner = findViewById(R.id.spinnerState);
        citySpinner = findViewById(R.id.spinnerCity);
        backBtn = findViewById(R.id.backButton);
        countrySpinner.setEnabled(false);
        stateSpinner.setEnabled(false);
        citySpinner.setEnabled(false);
        addCity = findViewById(R.id.buttonAddCity);
        addCity.setVisibility(View.GONE);
        fetchCountries();
        //onchange listener for spinners
        countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedCountry = adapterView.getItemAtPosition(i).toString();
                fetchState(adapterView.getItemAtPosition(i).toString());//update state if country chnaged
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        stateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedState = adapterView.getItemAtPosition(i).toString();
                fetchCity(selectedCountry,selectedState);//update city if state changed
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedCity = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //Add place on Add button clicked
        //Append Data to Json array stored
        addCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Place place = new Place(selectedCountry,selectedState,selectedCity);
                String spPlaces = SharedPreferenceHelper.getSharedPreferenceString(getApplicationContext(),"places","");
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("country", selectedCountry);
                    jsonObject.put("state", selectedState);
                    jsonObject.put("city", selectedCity);
                    JSONArray jsonArray = new JSONArray();
                    if (spPlaces.equals("")) {
                        jsonArray.put(jsonObject);
                        SharedPreferenceHelper.setSharedPreferenceString(getApplicationContext(),"places",jsonArray.toString());
                    }
                    else
                    {
                        jsonArray = new JSONArray(spPlaces);
                        if(!alreadyExists(jsonArray))
                        {
                            jsonArray.put(jsonObject);
                            SharedPreferenceHelper.setSharedPreferenceString(getApplicationContext(),"places",jsonArray.toString());
                            Log.d("Arun Places:",jsonArray.toString());
                        }
                        else
                        {//show message if already in favourite
                            Toast.makeText(getApplicationContext(),"Place Already Added",Toast.LENGTH_LONG).show();
                            return;
                        }
                    }
                    Toast.makeText(getApplicationContext(),"Added Place Successfully",Toast.LENGTH_LONG).show();


                }
                catch (Exception e)
                {

                }
            }
        });

    //Go back
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
//function to check if city us already added
    private boolean alreadyExists(JSONArray jsonArray) {
        for(int i=0;i<jsonArray.length();i++)
        {
            try {
                JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                if(selectedCity.equals(jsonObject.getString("city")) && selectedState.equals(jsonObject.getString("state")) && selectedCountry.equals(jsonObject.getString("country")))
                {
                    return true;
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
//Fetch Countries from API
    public void fetchCountries()
    {
        String url = "https://api.airvisual.com/v2/countries?key=c7728efa-9e98-425a-94a8-a0d75d7db254";

        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Fetching Countries from API...");
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
                                List<String> countries = new ArrayList<String>();
                                JSONArray jsonArray = response.getJSONArray("data");
                                for(int i=0;i<jsonArray.length();i++)
                                {
                                    JSONObject country = (JSONObject)jsonArray.get(i);
                                    countries.add(country.getString("country"));
                                }
                            //update spinner with values from API
                                populateSpinner(countrySpinner,countries);
                                countrySpinner.setEnabled(true);
                                stateSpinner.setEnabled(false);
                                citySpinner.setEnabled(false);
                                addCity.setVisibility(View.GONE);
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

    //Function to fetch states iof selected city
    public void fetchState(String countrySelected)
    {
        String url = "https://api.airvisual.com/v2/states?country="+countrySelected+"&key=c7728efa-9e98-425a-94a8-a0d75d7db254";

        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Fetching States of "+countrySelected+"...");
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
                                List<String> states = new ArrayList<String>();
                                JSONArray jsonArray = response.getJSONArray("data");
                                for(int i=0;i<jsonArray.length();i++)
                                {
                                    JSONObject country = (JSONObject)jsonArray.get(i);
                                    states.add(country.getString("state"));
                                }

                                //update spinner with values from API
                                populateSpinner(stateSpinner,states);
                                countrySpinner.setEnabled(true);
                                stateSpinner.setEnabled(true);
                                citySpinner.setEnabled(false);
                                addCity.setVisibility(View.GONE);
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

    //Function to fetch city iof selected stat
    public void fetchCity(String countrySelected,String stateSelected)
    {
        String url = "https://api.airvisual.com/v2/cities?state="+stateSelected+"&country="+countrySelected+"&key=c7728efa-9e98-425a-94a8-a0d75d7db254";

        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Fetching Cities of "+stateSelected+"...");
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
                                List<String> cities = new ArrayList<String>();
                                JSONArray jsonArray = response.getJSONArray("data");
                                for(int i=0;i<jsonArray.length();i++)
                                {
                                    JSONObject country = (JSONObject)jsonArray.get(i);
                                    cities.add(country.getString("city"));
                                }

                                //update spinner with values from API
                                populateSpinner(citySpinner,cities);
                                countrySpinner.setEnabled(true);
                                stateSpinner.setEnabled(true);
                                citySpinner.setEnabled(true);
                                addCity.setVisibility(View.VISIBLE);
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

    private void populateSpinner(Spinner spinner, List<String> lables) {
        // Creating adapter for spinner
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, lables);

        // Drop down layout style - list view with radio button
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner.setAdapter(spinnerAdapter);
    }
}

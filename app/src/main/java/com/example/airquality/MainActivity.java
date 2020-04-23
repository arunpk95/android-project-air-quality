package com.example.airquality;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button newLocation;
     List<Place> places;
    ListView listView;
    TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        newLocation = findViewById(R.id.newLocation);

        newLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),AddNewLocation.class);
                startActivity(i);
            }
        });

    //populate listview from stored favourite places
        updatePlaces();
    }

    @Override
    public void onResume(){
        super.onResume();
        updatePlaces();
    }

    //function to populate listview from stored favourite places
    public  void updatePlaces()
    {
        try {
            //get from shared preferences
            final JSONArray jsonArray = new JSONArray(SharedPreferenceHelper.getSharedPreferenceString(getApplicationContext(),"places",""));

            listView=(ListView)findViewById(R.id.place_list);
            textView=(TextView)findViewById(R.id.label);
            String[] listItem = new String[jsonArray.length()];
            for(int i=0;i<jsonArray.length();i++)
            {
                JSONObject jsonObject = new JSONObject(jsonArray.get(i).toString());
                listItem[i] = jsonObject.getString("city")+ " ("+jsonObject.getString("state")+","+jsonObject.getString("country") + ")";
            }
            final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, android.R.id.text1, listItem);
            listView.setAdapter(adapter);

            //opening stats activity when a favourite place is selected
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    // TODO Auto-generated method stub
                    String value=adapter.getItem(position);
                    Toast.makeText(getApplicationContext(),value,Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(),ViewCityStat.class);
                    try {
                        intent.putExtra("place",jsonArray.getJSONObject(position).toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    startActivity(intent);

                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}

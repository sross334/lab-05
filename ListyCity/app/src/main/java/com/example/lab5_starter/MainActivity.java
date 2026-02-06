package com.example.lab5_starter;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements CityDialogFragment.CityDialogListener {

    private Button addCityButton;
    private ListView cityListView;

    private FirebaseFirestore db;

    private CollectionReference citiesRef;
    private ArrayList<City> cityArrayList;
    private ArrayAdapter<City> cityArrayAdapter;
    private EditText delText;

    private Button delButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set views
        addCityButton = findViewById(R.id.buttonAddCity);
        cityListView = findViewById(R.id.listviewCities);
        delButton = findViewById(R.id.delButton);
        delText= findViewById(R.id.delCityE);

        // create city array
        cityArrayList = new ArrayList<>();
        cityArrayAdapter = new CityArrayAdapter(this, cityArrayList);
        cityListView.setAdapter(cityArrayAdapter);

        db = FirebaseFirestore.getInstance();
        citiesRef = db.collection("cities");

        citiesRef.addSnapshotListener((value, error) -> {
            if(error != null){
                Log.e("firestore", error.toString());
            }

            if(value != null && !value.isEmpty()){
                cityArrayList.clear();
                for(QueryDocumentSnapshot snapshot: value){
                    String name = snapshot.getString("name");
                    String province = snapshot.getString("Province");


                    cityArrayList.add(new City(name, province));
                }

                cityArrayAdapter.notifyDataSetChanged();
            }
        });

        // set listeners
        addCityButton.setOnClickListener(view -> {
            CityDialogFragment cityDialogFragment = new CityDialogFragment();
            cityDialogFragment.show(getSupportFragmentManager(),"Add City");
        });

        cityListView.setOnItemClickListener((adapterView, view, i, l) -> {
            City city = cityArrayAdapter.getItem(i);
            CityDialogFragment cityDialogFragment = CityDialogFragment.newInstance(city);
            cityDialogFragment.show(getSupportFragmentManager(),"City Details");
        });

        delButton.setOnClickListener(view -> {
            String name = delText.getText().toString().trim();
            if(name.isEmpty()){
                Toast.makeText(this, "Enter A Value", Toast.LENGTH_SHORT).show();
                return;
            }
            deleteCity(name);
        });

    }

    @Override
    public void updateCity(City city, String title, String year) {
        city.setName(title);
        city.setProvince(year);
        cityArrayAdapter.notifyDataSetChanged();

        // Updating the database using delete + addition
    }

    @Override
    public void addCity(City city){
        cityArrayList.add(city);
        cityArrayAdapter.notifyDataSetChanged();

        DocumentReference docRef = citiesRef.document(city.getName());
        docRef.set(city);

    }

    @Override
    public void deleteCity(String name){
        for(int i = 0; i < cityArrayList.size(); i++){

            if(name.equals(cityArrayList.get(i).getName())){
                cityArrayList.remove(i);
                cityArrayAdapter.notifyDataSetChanged();

                DocumentReference docRef = citiesRef.document(name);
                docRef.delete();

                return;

            }

        }
        Toast.makeText(this, "Enter a valid city", Toast.LENGTH_SHORT).show();
        return;
    }

    public void addDummyData(){
        City m1 = new City("Edmonton", "AB");
        City m2 = new City("Vancouver", "BC");
        addCity(m1);
        addCity(m2);
        cityArrayAdapter.notifyDataSetChanged();
    }
}
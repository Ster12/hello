package esiee.runningdetector;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.io.IOException;

public class Activity_horse extends AppCompatActivity {




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_horse);

        final ListView list_horse = (ListView) findViewById(R.id.list_view_horse);

        {
            try {
                final String[] allFilesInPackage = getAssets().list("text_files_horse");
                for(int i =0; i<allFilesInPackage.length;i++){
                    Log.d("Zbleh2",allFilesInPackage[i]);
                }
                final ArrayAdapter<String> adapter = new ArrayAdapter<String>(Activity_horse.this, android.R.layout.simple_list_item_1, allFilesInPackage);
                list_horse.setAdapter(adapter);
                list_horse.setClickable(true);
                list_horse.setOnItemClickListener(new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3)
                    {

                        Object o = list_horse.getItemAtPosition(position);


                        Intent intent = new Intent(getApplicationContext(), Activity_Horse_Detail.class);
                        intent.putExtra("Nom_Fichier", allFilesInPackage[position]);
                        startActivity(intent);

                        Log.d("Lecture Ligne",""+allFilesInPackage[position]);

                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("Zbleh2","ERREUR");
            }
        }
    }
}


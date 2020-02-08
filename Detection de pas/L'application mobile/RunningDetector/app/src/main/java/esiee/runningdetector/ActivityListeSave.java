package esiee.runningdetector;


import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;

public class ActivityListeSave extends AppCompatActivity {
    FileInputStream inputtStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liste_save);

        final ListView list_save = (ListView) findViewById(R.id.list_view_save);

        File dir = getFilesDir();
        if (dir.isDirectory()) {

            File[] files = dir.listFiles();
            final String[] files_name = new String[files.length];


            for (int i=0; i < files.length; i++) {
                files_name[i] = files[i].getName();


            }
            final ArrayAdapter<String> adapter = new ArrayAdapter<String>(ActivityListeSave.this, android.R.layout.simple_list_item_1, files_name);
            //final ArrayAdapter<Button> adapter = new ArrayAdapter<Button>(ActivityListeSave.this, android.R.layout.simple_list_item_1, Button_List);

            list_save.setAdapter(adapter);
            list_save.setClickable(true);
            list_save.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3)
                {

                    Object o = list_save.getItemAtPosition(position);


                    Intent intent = new Intent(getApplicationContext(), Activity_Detail_Fichier.class);
                    intent.putExtra("Nom_Fichier", files_name[position]);
                    startActivity(intent);

                    Log.d("Lecture Ligne",""+files_name[position]);

                }
            });
        }

    }
}

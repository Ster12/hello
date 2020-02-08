package esiee.runningdetector;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Activity_Horse_Detail extends AppCompatActivity {

    String Nom_Fichier = "Bleh";
    private float[] tableau_data = new float[40*4];
    private TensorFlowInferenceInterface tensorFlowInferenceInterface;
    private static final String MODEL_NAME = "file:///android_asset/optimized_horse_model.pb";
    private static final String INPUT_NAME = "input";
    private static final String OUTPUT_NAME = "final_result";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__horse__detail);

        tensorFlowInferenceInterface = new TensorFlowInferenceInterface(getAssets(), MODEL_NAME);
        final TextView text_view_horse= findViewById(R.id.text_horse);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            Nom_Fichier = extras.getString("Nom_Fichier");
        }

        Log.d("Debug",Nom_Fichier);

        try  {
            BufferedReader br = new BufferedReader(new InputStreamReader(getAssets().open("text_files_horse/"+Nom_Fichier)));
            // do reading, usually loop until end of file reading
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            int compteur40 = 0;
            int compteurindice = 0;

            int compteur_pas_A = 0;
            int compteur_pas_B = 0;
            int compteur_pas_C = 0;

            while (line != null) {
                sb.append(line); // process line

                compteur40++;
                String[] parts = line.split(" ");
                String part1 = parts[0];
                String part2 = parts[1];
                String part3 = parts[2];
                String part4 = parts[3];
                Log.d("SPLIT",part1 +" "+part2+" "+part3+" "+part4);

                /**
                tableau_data[compteurindice]=Float.parseFloat(part1);
                compteurindice++;
                tableau_data[compteurindice]=Float.parseFloat(part2);
                compteurindice++;
                tableau_data[compteurindice]=Float.parseFloat(part3);
                compteurindice++;
                tableau_data[compteurindice]=Float.parseFloat(part4);
                compteurindice++;
                 **/
                tableau_data[compteurindice]=Float.parseFloat(part1);
                tableau_data[40+compteurindice]=Float.parseFloat(part2);
                tableau_data[80+compteurindice]=Float.parseFloat(part3);
                tableau_data[120+compteurindice]=Float.parseFloat(part4);
                compteurindice++;


                if (compteur40 == 40)
                {
                    compteur40 = 0;
                    compteurindice = 0;
                    float[] res = {0, 0, 0, 0, 0 ,0 };

                    float[] drop_value = {1};
                    tensorFlowInferenceInterface.feed(INPUT_NAME,tableau_data,1,40,4,1);
                    tensorFlowInferenceInterface.feed("drop_out",drop_value,1);
                    tensorFlowInferenceInterface.run(new String[]{OUTPUT_NAME});
                    tensorFlowInferenceInterface.fetch(OUTPUT_NAME, res);

                    float max = res[0];
                    int indice_max = 0;

                    for (int i = 1; i < res.length; i++) {
                        if (res[i] > max) {
                            max = res[i];
                            indice_max = i;
                        }
                    }

                    float maxres = max;

                    if (indice_max == 0 || indice_max == 1)
                    {
                        compteur_pas_A++;
                        //Log.d("SPLIT","Marche");
                    }
                    if (indice_max == 2 || indice_max == 3)
                    {
                        compteur_pas_B++;
                        //Log.d("SPLIT","Course");
                    }
                    if (indice_max == 4 || indice_max == 5)
                    {
                        compteur_pas_C++;
                    }
                }
                line = br.readLine();
            }
            text_view_horse.setText("Type A: "+compteur_pas_A+" Type B: "+compteur_pas_B+ " Type C: "+ compteur_pas_C);
            br.close();
        }catch (IOException e) {
            e.printStackTrace();
            Log.d("Zbleh2","ERREUR");
        }
    }
}

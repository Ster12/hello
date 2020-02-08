package esiee.runningdetector;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;

public class Activity_Detail_Fichier extends AppCompatActivity {
    String Nom_Fichier = "Bleh";
    private float[] tableau_data = new float[40*3];
    private TensorFlowInferenceInterface tensorFlowInferenceInterface;
    private static final String MODEL_NAME = "file:///android_asset/1.12 40x3optimized_running_model_and_stop.pb";
    private static final String INPUT_NAME = "input";
    private static final String OUTPUT_NAME = "final_result";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__detail__fichier);
        tensorFlowInferenceInterface = new TensorFlowInferenceInterface(getAssets(), MODEL_NAME);
        final TextView text_view_Pas_Arriere = findViewById(R.id.Pas_Arriere);
        final TextView text_view_Pas_Avant= findViewById(R.id.Pas_Avant);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            Nom_Fichier = extras.getString("Nom_Fichier");
        }
        Log.d("Debug",Nom_Fichier);

        try (BufferedReader br = new BufferedReader(new FileReader(getFilesDir().getPath()+"/"+Nom_Fichier))) {
            String line;
            int compteur40 = 0;
            int compteurindice = 0;

            int compteur_pas_pied_gauche = 0;
            int compteur_pas_pied_droit = 0;
            int compteur_pas_marche = 0;
            int compteur_pas_course = 0;
            int compteur_pas_aret = 0;
            int compteur_diff_gauche = 0;
            int compteur_diff_droit = 0;
            float valeur_diff = 0;

            while ((line = br.readLine()) != null) {
                //Log.d("Lecture Ligne",line);
                compteur40++;
                String[] parts = line.split(" ");
                String part1 = parts[0];
                String part2 = parts[1];
                String part3 = parts[2];
                //Log.d("SPLIT",part1 +" "+part2+" "+part3);

                tableau_data[compteurindice]=Float.parseFloat(part1);
                compteurindice++;
                tableau_data[compteurindice]=Float.parseFloat(part2);
                compteurindice++;
                tableau_data[compteurindice]=Float.parseFloat(part3);
                compteurindice++;


                if (Float.parseFloat(part3) > 0)
                {
                    compteur_diff_gauche++;
                }
                if (Float.parseFloat(part3) < 0)
                {
                    compteur_diff_droit++;
                }

                if (compteur40 == 40)
                {
                    compteur40 = 0;
                    compteurindice = 0;
                    float[] res = {0, 0, 0};

                    tensorFlowInferenceInterface.feed(INPUT_NAME,tableau_data,1,40,3,1);
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

                    if (indice_max == 0)
                    {
                        compteur_pas_marche++;
                        //Log.d("SPLIT","Marche");
                    }
                    if (indice_max == 1)
                    {
                        compteur_pas_course++;
                        //Log.d("SPLIT","Course");
                    }
                    if (indice_max == 2)
                    {
                        compteur_pas_aret++;
                    }

                    Log.d("SPLIT",""+compteur_diff_gauche+"   "+compteur_diff_droit);
                    if (compteur_diff_gauche >= compteur_diff_droit)
                    {
                        compteur_pas_pied_gauche++;
                    }
                    else if (compteur_diff_gauche < compteur_diff_droit)
                    {
                        compteur_pas_pied_droit++;
                    }
                    compteur_diff_gauche = 0;
                    compteur_diff_droit = 0;
                }
            }
            text_view_Pas_Avant.setText("Marche: "+compteur_pas_marche+" Course: "+compteur_pas_course+ " Arrière: "+ compteur_pas_aret+"\n" +
                    "Gauche: "+compteur_pas_pied_gauche+" Droit: "+compteur_pas_pied_droit);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}

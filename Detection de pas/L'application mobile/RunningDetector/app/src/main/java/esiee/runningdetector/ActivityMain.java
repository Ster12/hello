package esiee.runningdetector;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

public class ActivityMain extends AppCompatActivity {
    boolean enregistre = false;
    String filename;
    FileOutputStream outputStream;
    Double Totaltime = 0.00;
    int compteur = 0;


    private TensorFlowInferenceInterface tensorFlowInferenceInterface;
    private static final String MODEL_NAME = "file:///android_asset/1.12 40x3sample optimized_running_model_and_stop.pb";
    private int taille_graph = 50;
    private static final String INPUT_NAME = "input";
    private static final String OUTPUT_NAME = "final_result";
    private static final int[] INPUT_SIZE = {1, 4};
    private static final String[] FLOWER_TYPE = {"Iris Setosa ","Iris Versicolor","Iris Virginica"};
    float tableauData[] = new float[40*3];
    float ListePiedGauche[] = new float[taille_graph];
    float ListePiedDroit[] = new float[taille_graph];
    float ListeDiff[] = new float[taille_graph];
    int y = 0;
    float offset_gauche = 0;
    float offset_droit = 0;
    boolean calibration = false;
    List<Float> Liste = new ArrayList<Float>();
    int compteur_pas = 0;
    int compteur_début = 0;
    float dif_val_dernier_pas;
    int compteur_dernier_pas = 0;
    boolean seuil_dépassé = false;
    Float Liste_pas_gauche[] = new Float[40];
    Float Liste_pas_droit[] = new Float[40];
    Float Liste_pas_gauche_affiche[] = new Float[40];
    Float Liste_pas_droit_affiche[] = new Float[40];
    Float Liste_diff_val_lisse[] = new Float[40];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView text_view_pied_droit = findViewById(R.id.Pied_Droit);
        final TextView text_view_pied_gauche = findViewById(R.id.Pied_Gauche);
        final Button buttonenregistre = findViewById(R.id.Startrecord);
        final Button buttonstopenregistre = findViewById(R.id.Stoprecord);
        final Button buttoncalibrer = findViewById(R.id.bouton_calibrer);
        final TextView text_view_Timer = findViewById(R.id.Timeview);
        final TextView text_view_Pourcentage = findViewById(R.id.textViewPourcentage);
        final Handler handler = new Handler();
        final Timer timer = new Timer();
        final Button button_liste_enregistrement = findViewById(R.id.button_liste_enregistrement);
        final GraphView graph = findViewById(R.id.graph);
        final Context context = this;
        final TextView text_view_Pas = findViewById(R.id.textViewPas);

        final TextView text_view_TypeMarche = findViewById(R.id.textViewTypeMarche);
        tensorFlowInferenceInterface = new TensorFlowInferenceInterface(getAssets(), MODEL_NAME);
        final TextView text_view_Compteur = findViewById(R.id.textViewCompteur);

        final EditText edit_Text_name_Save = new EditText(this);
        final ListView list_result = new ListView(this);

        Liste_pas_gauche = remplis_de_zeros(Liste_pas_gauche);
        Liste_pas_droit = remplis_de_zeros(Liste_pas_droit);
        Liste_pas_gauche_affiche = remplis_de_zeros(Liste_pas_gauche_affiche);
        Liste_pas_droit_affiche = remplis_de_zeros(Liste_pas_droit_affiche);
        Liste_diff_val_lisse = remplis_de_zeros(Liste_diff_val_lisse);

        ClientListen.runcontrol.setrun(true);
        buttonstopenregistre.setEnabled(false);

        /** TOURNE EN BOUCLE **/
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {

                handler.post(new Runnable() {
                    public void run() {
                        try {
                            //Log.d("TEST AFFI",ActivityConnection.RECEIVE_UDP.gettext());
                            if (ActivityConnection.RECEIVE_UDP.gettext() != null) {
                                /** RECUPERE LES VALEURS DES CAPTEURS **/
                                String[] separated = (ActivityConnection.RECEIVE_UDP.gettext()).split("x");
                                if (separated[0] == "") {
                                    separated[0] = "error";
                                }
                                if (separated[1] == "") {
                                    separated[1] = "error";
                                }

                                separated[0]= Float.toString(Float.parseFloat(separated[0]) + offset_gauche);
                                separated[1]= Float.toString(Float.parseFloat(separated[1])+offset_droit);


                                /** CALCUL PAS **/
                                //Calculer la différence
                                float dif_val = Float.parseFloat(separated[0]) - Float.parseFloat(separated[1]);
                                Liste.add(dif_val);

                                float diff_val_lisse = 0;
                                //Log.d("TEST DIFF",""+Liste.size());
                                if (Liste.size() == 15)
                                {
                                    Liste.remove(0);
                                    //Lissage
                                    diff_val_lisse = lissage3(Liste);
                                }
                                // Détection du seuil
                                if ( -10 > diff_val_lisse || diff_val_lisse > 10)
                                {
                                    if (!seuil_dépassé)
                                    {
                                        Liste_pas_gauche = remplis_de_zeros(Liste_pas_gauche);
                                        Liste_pas_droit = remplis_de_zeros(Liste_pas_droit);
                                        Liste_diff_val_lisse = remplis_de_zeros(Liste_diff_val_lisse);
                                        compteur_dernier_pas=compteur_début;
                                    }
                                    seuil_dépassé = true;
                                }

                                if (seuil_dépassé)
                                    {
                                    //Log.d("INDICE OU L'ON PLACE","" + (compteur_début - compteur_dernier_pas) + " ");
                                    if (compteur_début - compteur_dernier_pas >= 40)
                                    {
                                        Liste_pas_gauche = remplis_de_zeros(Liste_pas_gauche);
                                        Liste_pas_droit = remplis_de_zeros(Liste_pas_droit);
                                        Liste_diff_val_lisse = remplis_de_zeros(Liste_diff_val_lisse);
                                        compteur_dernier_pas=compteur_début;

                                    }
                                    //Log.d("INDICE OU L'ON PLACE","" + (compteur_début - compteur_dernier_pas));
                                    Liste_pas_gauche[compteur_début - compteur_dernier_pas] = Float.parseFloat(separated[0]);
                                    Liste_pas_droit[compteur_début - compteur_dernier_pas] = Float.parseFloat(separated[1]);
                                    Liste_diff_val_lisse[compteur_début - compteur_dernier_pas] = diff_val_lisse;
                                }

                                if ((dif_val_dernier_pas <= 0 && diff_val_lisse > 0 )||(dif_val_dernier_pas >= 0 && diff_val_lisse < 0 ) )
                                {
                                    if (compteur_début - compteur_dernier_pas >10 && seuil_dépassé)
                                    {
                                        Log.d("PAS","J'INCREMENTE LE PAS");
                                        compteur_pas++;
                                        text_view_Pas.setText(""+compteur_pas);
                                        compteur_dernier_pas = compteur_début;
                                        seuil_dépassé = false;

                                        /** ENREGISTRE ET ECRIT LES VALEURS DANS UN FICHIER TEXTE**/
                                        if (enregistre) {
                                            try {
                                                for (int x=0; x<Liste_pas_gauche.length; x++)
                                                {
                                                    outputStream = openFileOutput(filename, Context.MODE_APPEND);
                                                    outputStream.write((Liste_pas_gauche[x] + " " + Liste_pas_droit[x] + " " + Liste_diff_val_lisse[x] +"\n").getBytes());

                                                    DecimalFormat REAL_FORMATTER = new DecimalFormat("0.00");
                                                    text_view_Timer.setText(String.valueOf(REAL_FORMATTER.format(Totaltime)));
                                                }

                                                Log.d("TEST Enregistre", "Ecrit:" + separated[1] + " " + separated[0]);

                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        int y =0;
                                        /** RESEAU DE NEURONE**/
                                        for (int x = 0; x<Liste_pas_gauche.length;x++) {
                                            tableauData[y] = Liste_pas_gauche[x];
                                            y++;
                                            tableauData[y] = Liste_pas_droit[x];
                                            y++;
                                            tableauData[y] = Liste_diff_val_lisse[x];
                                            y++;
                                        }

                                        float[] res = {0, 0, 0};

                                        tensorFlowInferenceInterface.feed(INPUT_NAME, tableauData, 1, 40, 3, 1);
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

                                        DecimalFormat df = new DecimalFormat(" ########.##");
                                        if (indice_max == 0)
                                        {
                                            String newText2 = "Marche";
                                            text_view_TypeMarche.setText(newText2);
                                            newText2 = "Prediction à " + String.valueOf(df.format( maxres * 100)) + "%.";
                                            text_view_Pourcentage.setText(newText2 );

                                        }
                                        if (indice_max == 1)
                                        {
                                            String newText2 = "Course";
                                            text_view_TypeMarche.setText(newText2);
                                            newText2 = String.valueOf(df.format(maxres * 100)) + "%.";
                                            text_view_Pourcentage.setText(newText2 );

                                        }
                                        if (indice_max == 2)
                                        {
                                            String newText2 = "Arrière";
                                            text_view_TypeMarche.setText(newText2);
                                            newText2 = String.valueOf(df.format(maxres * 100)) + "%.";
                                            text_view_Pourcentage.setText(newText2);

                                        }


                                    }
                                }

                                dif_val_dernier_pas = diff_val_lisse;

                                /** AFFICHE LES VALEURS DANS LE GRAPH ET LES TEXT VIEW **/
                                // Remplis les tableaux avant qu'ils ne soit plein
                                if (y < ListePiedGauche.length) {
                                    ListePiedGauche[y] = Float.parseFloat(separated[0]);
                                    ListePiedDroit[y] = Float.parseFloat(separated[1]);
                                    ListeDiff[y] = diff_val_lisse;
                                    y++;
                                }
                                // Décale tout le tableau de -1 et ajoute la dernière valeur recu au bout du tableau
                                //Log.d("DEBUG","1");
                                if (y>=ListePiedGauche.length)
                                {
                                    for (int x = 1; x<ListePiedGauche.length;x++)
                                    {
                                        ListePiedGauche[x-1] = ListePiedGauche[x];
                                        ListePiedDroit[x-1] = ListePiedDroit[x];
                                        ListeDiff[x-1] = ListeDiff[x];
                                    }
                                    ListePiedGauche[ListePiedGauche.length-1] = Float.parseFloat(separated[0]);
                                    ListePiedDroit[ListePiedDroit.length-1] = Float.parseFloat(separated[1]);
                                    ListeDiff[ListeDiff.length-1] = diff_val_lisse;
                                }

                                //Log.d("DEBUG","2");
                                // Crée les datapoint pour le graphique en parcourant les deux tableaux
                                LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>();
                                LineGraphSeries<DataPoint> series2 = new LineGraphSeries<DataPoint>();
                                LineGraphSeries<DataPoint> series3 = new LineGraphSeries<DataPoint>();
                                /**for (int i = 0; i<ListePiedGauche.length; i++)
                                {
                                    series.appendData(new DataPoint(i,ListePiedGauche[i]),true,500);
                                    series2.appendData(new DataPoint(i,ListePiedDroit[i]),true,500);
                                    series3.appendData(new DataPoint(i,ListeDiff[i]),true,500);
                                }**/
                                for (int i = 0; i<Liste_pas_gauche_affiche.length; i++)
                                {
                                    series.appendData(new DataPoint(i,Liste_pas_gauche_affiche[i]),true,500);
                                    series2.appendData(new DataPoint(i,Liste_pas_droit_affiche[i]),true,500);
                                    series3.appendData(new DataPoint(i,ListeDiff[i]),true,500);
                                }
                                // Options des séries
                                series.setTitle("Pied gauche");
                                series2.setTitle("Pied droit");
                                series.setColor(Color.GRAY);
                                series2.setColor(Color.BLUE);
                                series3.setColor(Color.RED);

                                // Options des séries du graph
                                graph.getViewport().setXAxisBoundsManual(true);
                                graph.getViewport().setMinX(0);
                                graph.getViewport().setMaxX(taille_graph);
                                graph.getViewport().setYAxisBoundsManual(true);
                                graph.getViewport().setMinY(-60);
                                graph.getViewport().setMaxY(60);
                                graph.setTitle("Valeur accéléromètre");

                                //if (compteur == taille_graph-1) {
                                graph.removeAllSeries();

                                    // Efface l'ancien graphe et affiche les datapoints actuels

                                graph.addSeries(series);
                                graph.addSeries(series2);
                                graph.addSeries(series3);


                                text_view_pied_droit.setText(separated[1]);
                                text_view_pied_gauche.setText(separated[0]);

                                /** PASSER LES VALEURS DANS LE RESEAU DE NEURONE **/

                                if (compteur == taille_graph-1)
                                {
                                    compteur=0;

                                    if (y>=ListePiedGauche.length) {

                                    }
                                }



                                compteur = compteur+1;
                                text_view_Compteur.setText(String.valueOf(compteur));


                                /** CALIBRATION **/
                                if (calibration)
                                {
                                    separated[0]= Float.toString(Float.parseFloat(separated[0]) - offset_gauche);
                                    separated[1]= Float.toString(Float.parseFloat(separated[1]) - offset_droit);
                                    offset_gauche = 0 - Float.parseFloat(separated[0]);
                                    offset_droit = 0 - Float.parseFloat(separated[1]);
                                    calibration = false;

                                }



                            }
                            else if (!enregistre)
                            {
                                if (outputStream!=null) outputStream.close();
                                Totaltime=0.00;
                            }
                            compteur_début++;
                        } catch (Exception e) {
                            Log.e("Error",e.toString());
                        }
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, 25);

        buttonenregistre.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                filename = new Date().toString().substring(11, 19);
                edit_Text_name_Save.setText(filename);
                AlertDialog dialog = new AlertDialog.Builder(context)
                        .setTitle("Enregistrer?")
                        .setMessage("Entrez le nom du fichier?")
                        .setNegativeButton("Annuler", null)
                        .setPositiveButton("Lancer Enregistrement", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Totaltime = 0.0;
                                enregistre = true;
                                buttonenregistre.setEnabled(false);
                                buttonstopenregistre.setEnabled(true);
                                filename = edit_Text_name_Save.getText().toString()+".txt";
                                button_liste_enregistrement.setEnabled(false);
                            }
                        })
                        .create();
                if(edit_Text_name_Save.getParent()!=null)
                    ((ViewGroup)edit_Text_name_Save.getParent()).removeView(edit_Text_name_Save); // <- fix
                dialog.setView(edit_Text_name_Save);

                dialog.show();
            }
        });

        buttonstopenregistre.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                enregistre = false;
                buttonstopenregistre.setEnabled(false);
                buttonenregistre.setEnabled(true);
                button_liste_enregistrement.setEnabled(true);
                String[] Liste = new String[10];

                AlertDialog dialog = new AlertDialog.Builder(context)
                        .setTitle("Liste Résultat")
                        .setNegativeButton("Quitter", null)
                        .create();
                if(list_result.getParent()!=null) {
                    for (int i = 0; i < 3; i++) {
                        Liste[i] = String.valueOf(i);
                    }

                    ((ViewGroup) list_result.getParent()).removeView(list_result); // <- fix
                    final ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, Liste);

                    list_result.setAdapter(adapter);
                    dialog.setView(list_result);
                }
                dialog.show();
            }
        });

        button_liste_enregistrement.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                Intent intent = new Intent(getApplicationContext(), ActivityListeSave.class);
                startActivity(intent);
            }
        });

        buttoncalibrer.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                calibration = true;
                compteur_pas = 0;
                text_view_Pas.setText(""+compteur_pas);
            }
        });





///////////////////////////////////////////////////////////
        Handler myHandler = new Handler();
        final Handler finalMyHandler = myHandler;
        Runnable myRunnable = new Runnable() {
            public void run() {
                //Log.d("TEST TIMER", "time ok");
                Totaltime += 0.01;
                //else{Log.d("TEST TIMER", "time Pas ok");}
                finalMyHandler.postDelayed(this, 10);

            }
        };
        myHandler.postDelayed(myRunnable,100);
/////////////////////////////////////////////////////////////

    }
    protected Float lissage2(List<Float> Liste)
    {

        return Liste.get(Liste.size()/2);
    }

    protected Float lissage3(List<Float> Liste)
    {
        //Log.d("TEST LISSAGE", ""+Liste.size());
        float somme = 0;
        for (int i=0; i < Liste.size() ; i++ )
        {
            somme = somme + Liste.get(i);
        }
        float val = somme/Liste.size();
        return val;
    }

    protected Float[] remplis_de_zeros(Float[] tab)
    {
        for (int i=0; i<tab.length;i ++)
        {
            tab[i]=0f;
        }
        return tab;
    }
}



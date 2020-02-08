package esiee.runningdetector;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class ActivityConnection extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);

////////////////////////////////////////////////////////////
        /**String filename = new Date().toString().substring(11, 19)+".txt";
        String fileContents = "Pg Pd";
        FileOutputStream outputStream;

        try {
            outputStream = openFileOutput(filename, Context.MODE_APPEND);
            outputStream.write(fileContents.getBytes());
            Log.e("TEST Enregistre","Fichier créé:");
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }**/
/////////////////////////////////////////////////////////////

        SEND_UDP.SEND("Hello ESP!");
        final Button button8 = (Button) findViewById(R.id.button);
        new Thread(new ClientListen()).start();
        ClientListen.runcontrol.setrun(true);
        final Button button_test = (Button) findViewById(R.id.Boutton_Test);
        final Button button_horse = (Button) findViewById(R.id.Button_horse);

        button8.setOnClickListener(new View.OnClickListener() {
            //Listener Pour Temps réel
            @Override
            public void onClick(View v) {
                ClientListen.runcontrol.setrun(true);
                Log.d("TEST CO","Etat text:" + RECEIVE_UDP.gettext());

                // Affiche le dialog de connexion
                final ProgressDialog dialog = new ProgressDialog(ActivityConnection.this);
                dialog.setMessage("Test en cours");
                dialog.setCancelable(false);
                dialog.setInverseBackgroundForced(false);
                dialog.show();

                //Envoi Le message UDP
                SEND_UDP.SEND("Hello ESP!");
                //sendMessage("Hello ESP!");

                //Attend la réponse
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        String text = (RECEIVE_UDP.gettext());
                        if (text != null) {
                            dialog.hide();
                            ClientListen.runcontrol.setrun(false);
                            Log.d("TEST CO","Connexion réussis:" + text);
                            RECEIVE_UDP.settext(null);
                            Log.d("TEST CO","Etat text:" + RECEIVE_UDP.gettext());
                            Log.d("TEST CO","========================================================");
                            Intent intent = new Intent(getApplicationContext(), ActivityMain.class);
                            startActivity(intent);
                        } else {
                            dialog.hide();
                            ClientListen.runcontrol.setrun(false);
                            RECEIVE_UDP.settext(null);
                            Log.d("TEST CO","Connection échouée");
                            Log.d("TEST CO","Etat text:" + RECEIVE_UDP.gettext());
                            Log.d("TEST CO","========================================================");
                        }
                    }
                }, 3000);

            }
        });

        button_test.setOnClickListener(new View.OnClickListener() {
            //Listener Pour Temps réel
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), ActivityListeSave.class);
                startActivity(intent);
            }
        });
        button_horse.setOnClickListener(new View.OnClickListener() {
            //Listener Pour Temps réel
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), Activity_horse.class);
                startActivity(intent);
            }
        });
    }

    public static class SEND_UDP {

        public static void SEND(final String messageStr) {

            final Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {

                    ClientListen.runcontrol.setrun(false);
                        /*String messageStr="Hello ESP!";*/
                    int server_port = 2380;
                    DatagramSocket s = null;
                    try {
                        s = new DatagramSocket();
                    } catch (SocketException e) {
                        e.printStackTrace();
                    }
                    InetAddress local = null;
                    try {
                        local = InetAddress.getByName("11.11.11.11");
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                    int msg_length = messageStr.length();
                    byte[] message = messageStr.getBytes();
                    DatagramPacket p = new DatagramPacket(message, msg_length, local, server_port);
                    try {
                        if (s != null)
                            s.send(p);


                    } catch (IOException e) {
                        e.printStackTrace();

                    } finally {
                        if (s != null) {

                            Log.d("UDP send", "closed");
                            s.close();
                            ClientListen.runcontrol.setrun(true);
                        }
                    }
                    ClientListen.runcontrol.setrun(true);
                }
            });
            thread.start();
            thread.interrupt();
        }
    }

    public static class RECEIVE_UDP {
        private static String test;

        public static String gettext() {
            return test;
        }

        public static void settext(String received) {
            test = received;

        }
    }
}
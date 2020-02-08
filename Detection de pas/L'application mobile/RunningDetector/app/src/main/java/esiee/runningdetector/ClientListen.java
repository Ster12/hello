package esiee.runningdetector;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ClientListen implements Runnable
{
    @Override

    public void run() {
        Log.d("Test Co", "je start");
        while (true)
        {
            while (runcontrol.getrun() == false) {
                //Log.d("Test Co", "Je ne fait rien");
                ActivityConnection.RECEIVE_UDP.settext(null);
            }

            while (runcontrol.getrun() == true) {
                try {
                    String received = null;
                    //Log.d("Test Co", "im listening");
                    DatagramSocket udpSocket = new DatagramSocket(12345);
                    //Log.e("Test Co", "1");
                    byte[] message = new byte[8000];
                    DatagramPacket packet = new DatagramPacket(message, message.length);
                    //udpSocket.setSoTimeout(3000);
                    //Log.e("Test Co", "2");
                    //Log.e("Test Co", "Attente message");
                    udpSocket.receive(packet);
                    //Log.d("Test Co", "3");
                    udpSocket.close();
                    //Log.d("Test Co", "4");
                    received = new String(message, 0, packet.getLength());
                    //Log.d("Test Co", received);
                    ActivityConnection.RECEIVE_UDP.settext(received);
                }
                catch (Exception e) {
                    Log.e("UDP client IOException", "error: ", e);
                    {
                        Log.d("Test Co", "im not listening anymore");
                        ClientListen.runcontrol.setrun(false);
                    }
                }

            }
        }
    }


    public static class runcontrol {

        public static Boolean run = false;
        public static Boolean getrun(){return run;}
        public static void setrun(Boolean newrun)
        {
            run=newrun;

        }
    }
}
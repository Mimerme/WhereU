package io.github.mimerme.whereu.utility;

import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.telephony.SmsManager;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;

public class Utility {
    public static void runSuperUserCommand(String command){
        try {
            //Start the superuser binary
            Process su = Runtime.getRuntime().exec("su");
            DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());

            outputStream.writeBytes(command);
            outputStream.flush();

            //Terminate the process
            outputStream.writeBytes("exit\n");
            outputStream.flush();
            su.waitFor();
        } catch (IOException e){
            e.printStackTrace();
        } catch (InterruptedException e){
            e.printStackTrace();
        } catch (SecurityException e){
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}

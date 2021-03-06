package io.github.mimerme.whereu.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.github.mimerme.whereu.ui.MainActivity;
import io.github.mimerme.whereu.utility.AndroidStorage;
import io.github.mimerme.whereu.utility.Utility;

import static android.content.Context.LOCATION_SERVICE;

public class SmsReceiver extends BroadcastReceiver{
    private static final String CLASS_TAG = "SmsReceiver";


    private class SmsLocationReturn implements LocationListener {
        private LocationManager locationManager;
        private String recipient;
        private Context context;

        public SmsLocationReturn(LocationManager locationManager, String recipient, Context context){
            this.locationManager = locationManager;
            this.recipient = recipient;
            this.context = context;
        }

        @Override
        public void onLocationChanged(Location location) {
            try {
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                List<Address> address = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                Log.i(CLASS_TAG, "Location change received");

                //Reply back with the location
                MainActivity.sendSms(recipient, address.get(0).getAddressLine(0));

                Log.i(CLASS_TAG, "Sent location ${address.get(0).getAddressLine(0)}");
                Utility.runSuperUserCommand("settings put secure location_providers_allowed -gps,network\n");
                unregister();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        private void unregister(){
            locationManager.removeUpdates(this);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent){
        //Load in all the required resources first
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Boolean whitelistEnabled = prefs.getBoolean("whitelist_enable", false);
        Log.i(CLASS_TAG, "Whitelist : " + whitelistEnabled);

        Log.i(CLASS_TAG, "Received SMS");
        LocationManager locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);

        Bundle bundle = intent.getExtras();

        if (bundle != null) {
        Object[] pdus= (Object[]) bundle.get("pdus");

        for (int i = 0; i < pdus.length; i++) {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);

            SmsMessage message = SmsMessage.createFromPdu((byte[]) pdus[i]);
            String formatedNumber = Utility.formatNumber(message.getOriginatingAddress(),
                    telephonyManager.getSimCountryIso());


            if (message != null) {
                Log.i(CLASS_TAG, formatedNumber);

            if (message.getMessageBody().equals("!")) {
                if(whitelistEnabled) {
                    AndroidStorage whitelistStorage = new AndroidStorage(context, "whitelist");
                    Log.i(CLASS_TAG, context.getFilesDir().toString());
                    WhitelistLoader whitelist = new WhitelistLoader(whitelistStorage);
                    if(!whitelist.valid(formatedNumber)) {
                        MainActivity.sendSms(formatedNumber, "You aren't on the whitelist.");
                        return;
                    }
                }


                    MainActivity.sendSms(formatedNumber, "Command Received. Waiting for location...");

                    //Wait for a location update and the send it
                    try {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1L,
                                0f, new SmsLocationReturn(locationManager, message.getDisplayOriginatingAddress(), context));
                        Log.i(CLASS_TAG, "Starting to request location updates");

                        //Enable location services
                        Log.i(CLASS_TAG, "Turning on location services");
                        Utility.runSuperUserCommand("settings put secure location_providers_allowed +gps,network\n");
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
}

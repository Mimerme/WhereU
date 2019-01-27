package io.github.mimerme.whereu.ui;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.preference.PreferenceFragment;
import android.support.v7.app.AlertDialog;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.Manifest;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;

import io.github.mimerme.whereu.R;
import io.github.mimerme.whereu.utility.AndroidStorage;
import io.github.mimerme.whereu.utility.Storage;
import io.github.mimerme.whereu.utility.Utility;
import io.github.mimerme.whereu.utility.crashhandler.CrashHandler;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static class PrefsFragment extends PreferenceFragment{
        @Override
        public void onCreate(Bundle savedInstanceState){
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings_screen);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View v = super.onCreateView(inflater, container, savedInstanceState);
            if(v != null) {
                ListView lv = (ListView) v.findViewById(android.R.id.list);
                lv.setPadding(0, 0, 0, 0);
            }
            return v;
        }
    }


    public static Storage WHITELIST_STORAGE = null;
    //Settings storage is used for configurations saved for the SmsReceiver
    public static Storage SETTINGS_STORAGE = null;

    private static SmsManager smsManager = SmsManager.getDefault();
    public static TelephonyManager telManager;

    private FloatingActionButton mFab;
    private final int CONTACT_PICK_CODE = 69;

    private WhitelistFragment fWhitelist;
    private PreferenceFragment fPrefs;

    private static TelephonyManager getDefaultTelephonyManager(Context c){
        return (TelephonyManager)c.getSystemService(Context.TELEPHONY_SERVICE);
    }

    public static void sendSms(String target, String message){
        smsManager.sendTextMessage(target, null,
                message, null, null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(
                "/mnt/sdcard/"));

        //Initialize the telephony manager to be static
        telManager = getDefaultTelephonyManager(this);

        //Initialize the storages
        MainActivity.WHITELIST_STORAGE = new AndroidStorage(this, "whitelist");

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Initialize the fragments
        fWhitelist = new WhitelistFragment();
        fPrefs = new PrefsFragment();

        //Have to add fragments programatically since static fragments are perminenet
        //This declares the first shown fragment
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fWhitelist)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();

        //Configure the floating action button
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Choose a number to add to the whitelist", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                startActivityForResult(intent, CONTACT_PICK_CODE);
            }
        });

    }

    @Override
    public void onActivityResult (int requestCode,
                                     int resultCode,
                                     Intent data){
        switch(requestCode){
            case CONTACT_PICK_CODE:
                String phoneNo = null;
                String displayName = null;

                //If the data is null that means nothing was selected
                if(data == null)
                    return;

                Uri uri = data.getData();
                Cursor cursor = getContentResolver().query(uri, null, null, null, null);

                if (cursor.moveToFirst()) {
                    int phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                    int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                    phoneNo = cursor.getString(phoneIndex);
                    phoneNo = Utility.formatNumber(phoneNo, telManager.getSimCountryIso());
                    //For consistancy's sake always include a country code if the number doesn't have it
                    String defaultCountrNum = Utility.getCountryDialCode(this, telManager.getSimCountryIso());
                    if(!phoneNo.startsWith(defaultCountrNum)){
                        //If it doesn't start with a country code append it in
                        phoneNo = defaultCountrNum + phoneNo;
                    }

                    displayName = cursor.getString(nameIndex);
                }
                cursor.close();

                if(!fWhitelist.getWhitelistAdapater().checkDuplicate(phoneNo))
                    fWhitelist.getWhitelistAdapater().add(new String[]{displayName, phoneNo});
                else
                    Toast.makeText(getApplicationContext(), "That number is already on the whitelist", Toast.LENGTH_LONG).show();
                break;
            default:
                break;
        }
    }

    @Override
    public void onStart(){
        super.onStart();

        Log.i("Main", "Started main activity");

        //Check for the proper permissions
        askForPermissions(getString(R.string.why_location),
                "Location",
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION});

        askForPermissions(getString(R.string.why_sms),
                "SMS",
                new String[]{Manifest.permission.READ_SMS,
                        Manifest.permission.SEND_SMS,
                        Manifest.permission.RECEIVE_SMS});

        askForPermissions(getString(R.string.why_phone),
                "Phone",
                new String[]{Manifest.permission.READ_PHONE_STATE});
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        FragmentManager manager = getSupportFragmentManager();
        switch(id){
            case R.id.nav_whitelist:
                manager.beginTransaction().replace(R.id.fragment_container, fWhitelist)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit();
                mFab.show();
                break;
            case R.id.nav_debug:
                manager.beginTransaction().replace(R.id.fragment_container, new LogFragment())
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit();
                mFab.hide();
                break;
            case R.id.nav_help:
                manager.beginTransaction().replace(R.id.fragment_container, new HelpFragment())
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit();
                mFab.hide();
                break;
            case R.id.nav_settings:
                manager.beginTransaction().replace(R.id.fragment_container, fPrefs)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit();
                mFab.hide();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void askForPermissions(String permissionMessage, String permTitles, final String[] permissions){
        //Since permissions can have overlap when asking for perms check multiple but only ask for the general permission
        //ie. COURSE & FINE location can be grouped as just LOCATION
        for (String p : permissions) {
            if(ContextCompat.checkSelfPermission(this, p)
                    != PackageManager.PERMISSION_GRANTED){
                Log.i("MainActivity", "No " + p);
                new AlertDialog.Builder(this)
                        .setTitle("Why does the app need " + permTitles + " permissions?")
                        .setMessage(permissionMessage)
                        .setCancelable(false)
                        .setPositiveButton("Got Tt", (dialog, which) -> {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    permissions,
                                    0);
                        })
                        .show();
                break;
            }
        }
    }
}

package io.github.mimerme.whereu.ui;

import android.app.FragmentTransaction;
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
import android.support.v7.app.AlertDialog;
import android.telephony.SmsManager;
import android.util.Log;
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
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.IOException;

import io.github.mimerme.whereu.R;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static SmsManager smsManager = SmsManager.getDefault();
    private FloatingActionButton mFab;
    private final int CONTACT_PICK_CODE = 69;

    public static void sendSms(String target, String message){
        smsManager.sendTextMessage(target, null,
                message, null, null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        //Have to add fragments programatically since static fragments are perminenet
        //This declares the first shown fragment
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new WhitelistFragment())
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

                if(data == null)
                    return;

                Uri uri = data.getData();
                Cursor cursor = getContentResolver().query(uri, null, null, null, null);

                if (cursor.moveToFirst()) {
                    int phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                    phoneNo = cursor.getString(phoneIndex);
                }

                cursor.close();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        FragmentManager manager = getSupportFragmentManager();
        switch(id){
            case R.id.nav_whitelist:
                manager.beginTransaction().replace(R.id.fragment_container, new WhitelistFragment())
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

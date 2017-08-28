package com.iboalali.sysnotifsnooze;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.concurrent.CopyOnWriteArrayList;

public class MainActivity extends AppCompatActivity {

    private Context context;
    private NotificationReceiver notificationReceiver;
    private final int MY_PERMISSIONS_REQUEST_BIND_NOTIFICATION_LISTENER_SERVICE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        context = this;

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        notificationReceiver =  new NotificationReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.iboalali.sysnotifsnooze.NOTIFICATION_LISTENER_SERVICE");
        registerReceiver(notificationReceiver, intentFilter);

        //if (ContextCompat.checkSelfPermission(getApplicationContext(),
        //        Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE)
        //        != PackageManager.PERMISSION_GRANTED) {
        //    request_BIND_NOTIFICATION_LISTENER_SERVICE_permission();
        //}else{
        //    Snackbar.make(findViewById(R.id.mainLayout), "Permission Granted", Snackbar.LENGTH_SHORT).show();
        //}

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE)
                != PackageManager.PERMISSION_GRANTED) {
            request_BIND_NOTIFICATION_LISTENER_SERVICE_permission();
        }else{
            Snackbar.make(findViewById(R.id.mainLayout), "Permission Granted", Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(notificationReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    public static void gotoNotifyservice(Context context) {
        try {
            Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            context.startActivity(intent);
        } catch (ActivityNotFoundException anfe) {
            try {
                Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
                context.startActivity(intent);
                Toast.makeText(context, "Go to \\'Accessibility\\' > \\'NotificationListener", Toast.LENGTH_LONG).show();
            } catch (ActivityNotFoundException anfe2) {
                Toast.makeText(context, anfe2.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }


    private void request_BIND_NOTIFICATION_LISTENER_SERVICE_permission(){
        Log.d("MainActivity:", "requesting notification permission");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Requesting Permission");
        builder.setMessage("Click \"OK\" to go to the Notification Permission Screen and toggle the switch for this app and then press back to come back here");
        builder.setCancelable(true);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                gotoNotifyservice(context);
            }
        });
        builder.setNegativeButton("Close APP", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ((Activity)context).finish();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();




/*
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_CONTACTS)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                Snackbar.make(findViewById(R.id.mainLayout) , "This permission is needed to check what notification appears and hiding the correct one",
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE},
                                        MY_PERMISSIONS_REQUEST_BIND_NOTIFICATION_LISTENER_SERVICE);
                            }
                        })
                        .show();

                //new showMessageAsync(getApplicationContext()).execute();

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE},
                        MY_PERMISSIONS_REQUEST_BIND_NOTIFICATION_LISTENER_SERVICE);

                // MY_PERMISSIONS_REQUEST_BIND_NOTIFICATION_LISTENER_SERVICE is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
*/
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case MY_PERMISSIONS_REQUEST_BIND_NOTIFICATION_LISTENER_SERVICE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // NotificationListener-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
        }

    }

    class NotificationReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("MainActivity","was here");
            String temp = intent.getStringExtra("notification_event");
            Log.d("MainActivity (OnRe)", temp);
            //for(StatusBarNotification sbn : NotificationListener.this.getActiveNotifications()){
            //    Log.d("My Notif (onReceive):", sbn.getPackageName().toString());
            //    //sbn.getPackageName()
            //}
        }
    }

    public class showMessageAsync extends AsyncTask<Void, Void, Void> {
        AlertDialog.Builder alertDialogBuilder;
        private Context context;

        public showMessageAsync(Context context){
            this.context = context;
        }

        protected void onPreExecute() {
            alertDialogBuilder = new AlertDialog.Builder(context);

        }

        protected Void doInBackground(Void... unused) {
            return null;
        }

        protected void onPostExecute(Void unused) {
            alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    request_BIND_NOTIFICATION_LISTENER_SERVICE_permission();
                }
            });
            alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            alertDialogBuilder.setCancelable(true);
            alertDialogBuilder.setMessage("This permission is needed to check what notification appears and hiding the correct one");
            alertDialogBuilder.setTitle("Explanation");

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

        }
    }


}

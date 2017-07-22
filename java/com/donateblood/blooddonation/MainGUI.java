package com.donateblood.blooddonation;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MainGUI extends AppCompatActivity  {

    public static  ArrayList<DonorPerson> Donors= new ArrayList<DonorPerson>();
    public static SeekBar seek_bar;
    public static TextView seek_Text; public int PeopleAvilable;
    @InjectView(R.id.findppl) Button _findButton;
    GPSTracker gps;
    String bloodgroup=null;
    private double latitude;
    private double longitude;DB db; public int distance;
    DBCursor cursor;
    DBCollection collection;
    Database dataobj = new Database();
    public ArrayAdapter<String> Spinneradapter;
    public AlertDialog alertDialog;
    public Spinner mySpinner;
    @InjectView(R.id.image) ImageView _USERImage;
    @InjectView(R.id.Name) TextView _USERName;

    @Override
    public void onBackPressed() {


        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                       /* if(LoginActivity.fbLogin){
                        LoginManager.getInstance().logOut();
                        LoginActivity.fbLogin=false;
                        } */
                        finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maingui);
        ButterKnife.inject(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        alertDialog = new AlertDialog.Builder(MainGUI.this).create();
        alertDialog.setTitle("Donors Available");
        Spinner spinner =(Spinner) findViewById(R.id.spinner);
        String[] list = getResources().getStringArray(R.array.blood_type);
        Spinneradapter = new ArrayAdapter<String>(this,R.layout.spinner_layout,R.id.txt,list);

        //Show profile picture
        byte[] decodedString = Base64.decode(LoginActivity.ImageofLoggedINUser, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        Bitmap decodedFinalImage = getRoundedShape(decodedByte);
        //Bitmap RoundedImage = getRoundedShape(ImageBitmap);
        _USERImage.setImageBitmap(decodedFinalImage);
        _USERName.setText(LoginActivity.NameofLoggedINUser);

        spinner.setAdapter(Spinneradapter);
        OperateSeekbar();

        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        // Show the users available ====================================
        _findButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCurrentLatLong();
                dbAsync thrd = new dbAsync();
                thrd.execute();
            }
            //distance=Distance(lablat, lablong, curlat, curlong);
        });
    }

    public static Bitmap getRoundedShape(Bitmap scaleBitmapImage) {
        int targetWidth = 200;
        int targetHeight = 200;
        Bitmap targetBitmap = Bitmap.createBitmap(targetWidth,
                targetHeight, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(targetBitmap);
        Path path = new Path();
        path.addCircle(((float) targetWidth - 1) / 2,
                ((float) targetHeight - 1) / 2,
                (Math.min(((float) targetWidth),
                        ((float) targetHeight)) / 2),
                Path.Direction.CCW);

        canvas.clipPath(path);
        Bitmap sourceBitmap = scaleBitmapImage;
        canvas.drawBitmap(sourceBitmap,
                new Rect(0, 0, sourceBitmap.getWidth(),
                        sourceBitmap.getHeight()),
                new Rect(0, 0, targetWidth, targetHeight), null);
        return targetBitmap;
    }

    // Seek bar distance calculator
    public void OperateSeekbar() {


        seek_bar = (SeekBar) findViewById(R.id.seekBar);
        seek_Text = (TextView) findViewById(R.id.DistanceText);
        seek_bar.setProgress(1);
        seek_bar.setMax(99);
        seek_Text.setText(seek_bar.getProgress()+" km");
        seek_bar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    int Progress_value;
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        Progress_value =i;
                        seek_Text.setText(i+" km");
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        seek_Text.setText(Progress_value+" km");
                        getCurrentLatLong();
                        SeekBarAsync seeek = new SeekBarAsync();
                        seeek.execute();
                    }
                }
        );
    }

    // Get the current location of the user ====================================
    public void getCurrentLatLong(){
        gps = new GPSTracker(MainGUI.this);
        if (gps.canGetLocation()) {
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    //SEEK BAR ASYNC CLASS
    public class SeekBarAsync extends AsyncTask<Void,Void,Void>{

        private ProgressDialog pDialog;
        @Override
        protected Void doInBackground(Void... voids) {
            getOtherLatLong();
           PeopleAvilable = Donors.size();
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainGUI.this);
            pDialog.setMessage("Searching available donors...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            super.onPostExecute(aVoid);
            alertDialog.setMessage("Number of available donors are "+PeopleAvilable );
            pDialog.dismiss();
            //seek_bar.setProgress(0);
            alertDialog.show();

        }
    }

    // SEARCH BUTTON ASYNC CLASS
    public class dbAsync extends AsyncTask<Void,Void,Void>{
        private ProgressDialog pDialog;
        @Override
        protected Void doInBackground(Void... voids) {

            getOtherLatLong();
            return null;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainGUI.this);
            pDialog.setMessage("Searching people nearby...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }
        @Override
        protected void onPostExecute(Void aVoid) {

            super.onPostExecute(aVoid);
            pDialog.dismiss();
            Intent intent = new Intent(getApplicationContext(), PeopleList.class);
           // intent.putExtra("list",listWithoutDuplicates);
            startActivity(intent);
        }
    }

    // Get other donors location and add them to donors list
    public void getOtherLatLong() {
        ArrayList allPPLlat = new ArrayList();
        ArrayList allPPLlong = new ArrayList();
        ArrayList allPPLAge = new ArrayList();
        ArrayList allPPLNumbers = new ArrayList();
        ArrayList allPPLNames = new ArrayList();
        ArrayList allPPLImages = new ArrayList();
        ArrayList allPPLEmails = new ArrayList();
        ArrayList allPPLIDs = new ArrayList();
        Donors.clear();
        db = dataobj.getconnection();
        collection = db.getCollection("UserDetails");
        mySpinner=(Spinner) findViewById(R.id.spinner);
       bloodgroup = mySpinner.getSelectedItem().toString();
       BasicDBObject whereQuery = new BasicDBObject();
       whereQuery.put("bloodgroup",bloodgroup);
        cursor = collection.find(whereQuery);

        while (cursor.hasNext()) {
            DBObject doc = cursor.next();
            // Lats longs used in the next for Loop for calculation distances
            allPPLlat.add(doc.get("lat"));
            allPPLlong.add(doc.get("long"));
            // All these other arraylists are used to store object of a donor person
            allPPLNumbers.add(doc.get("number").toString());
            allPPLIDs.add(doc.get("ID").toString());
            allPPLAge.add(doc.get("age").toString());
            allPPLNames.add(doc.get("Name").toString());
            allPPLImages.add(doc.get("image").toString());
            allPPLEmails.add(doc.get("email").toString());

        }
        distance = Integer.parseInt(seek_Text.getText().toString().substring(0, seek_Text.length() - 3));


        for(int i =0;i<allPPLlat.size();i++){

           double Dist= Distance(Double.parseDouble(allPPLlat.get(i).toString()),Double.parseDouble(allPPLlong.get(i).toString()),latitude,longitude);
            Dist=Dist/1000;
            if(Dist<distance){

                Donors.add(new DonorPerson(""+allPPLNames.get(i), ""+allPPLEmails.get(i) ,""+allPPLNumbers.get(i) ,""+allPPLImages.get(i),""+allPPLAge.get(i),""+allPPLIDs.get(i)));
                //Log.e("test","in if "+i);
            }
            //Log.e("test","in for "+i);
        }
    }

    // calculate distance between current user and other users donors

    public double Distance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371.0; // km
        double dLat = (lat2 - lat1) * Math.PI / 180.0;
        double dLon = (lon2 - lon1) * Math.PI / 180.0;
        lat1 = lat1 * Math.PI / 180.0;
        lat2 = lat2 * Math.PI / 180.0;

        double a = Math.sin(dLat / 2.0) * Math.sin(dLat / 2.0) +
                Math.sin(dLon / 2.0) * Math.sin(dLon / 2.0) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = R * c;
        return d * 1000; // return distance in m
    }

}

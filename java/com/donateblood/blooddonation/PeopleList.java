package com.donateblood.blooddonation;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import java.io.Serializable;
import java.util.ArrayList;

public class PeopleList extends AppCompatActivity {

    public static ArrayAdapter<DonorPerson> adapter;
  // ArrayList<DonorPerson> Donors;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.peoplelistview);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

      // Donors = (ArrayList<DonorPerson>) getIntent().getSerializableExtra("DonorsList");
        adapter = new MyListAdapter();
        populateListView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        finish();
        super.onBackPressed();
    }

    public void populateListView() {
        ListView list = (ListView) findViewById(R.id.DonorsListView);
        list.setAdapter(adapter);
    }


    public class MyListAdapter extends ArrayAdapter<DonorPerson> {

        ArrayList allPPLNumbers = new ArrayList();
        public AlertDialog alertDialog;
        ArrayList allPPLIDs = new ArrayList();
        ArrayList allPPLNames = new ArrayList();
        ArrayList allPPLImages = new ArrayList();
        EditText userInput;

        String ID ="";

        public MyListAdapter() {
            super(PeopleList.this, R.layout.singlelistitemview,MainGUI.Donors);
        }

        @Override
        public View getView(final int position, final View convertView, ViewGroup parent) {

            View itemView = null;
            if (convertView == null) {

                itemView = getLayoutInflater().inflate(R.layout.singlelistitemview, parent, false);
            }
            else {
                itemView = convertView;
            }


            ViewHolder viewHolder = new ViewHolder();
            // Find the Donor to work with.
            DonorPerson currentPerson =MainGUI.Donors.get(position);
            //allPPLNumbers.add(currentPerson.getNumber());
            //allPPLIDs.add(currentPerson.getID());

            //Set the Image of the Current Donor
            viewHolder.DonorImage = (ImageView) itemView.findViewById(R.id.image);
            viewHolder.Name = (TextView) itemView.findViewById(R.id.Name);
            viewHolder.Email = (TextView) itemView.findViewById(R.id.Email);
            viewHolder.Age = (TextView) itemView.findViewById(R.id.Age);
            //currentPerson.getImage();
            byte[] decodedString = Base64.decode(currentPerson.getImage(), Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            Bitmap decodedFinalImage = getRoundedShape(decodedByte);
            //Bitmap RoundedImage = getRoundedShape(ImageBitmap);
            viewHolder.DonorImage.setImageBitmap(decodedFinalImage);

            viewHolder.Name.setText("Name: "+currentPerson.getName());
            viewHolder.Email.setText("Email: "+currentPerson.getEmail());
            viewHolder.Age.setText("Age: "+currentPerson.getAge());

            viewHolder.CallBtn = (Button) itemView.findViewById(R.id.buttonCall);
            viewHolder.CallBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DonorPerson currentPerson =MainGUI.Donors.get(position);
                    String number =currentPerson.getNumber();
                    //String number = (String) allPPLNumbers.get(position);
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.setData(Uri.parse("tel:" + number));
                    if (ActivityCompat.checkSelfPermission(PeopleList.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    startActivity(intent);
                }
            });

            viewHolder.MsgBtn = (Button) itemView.findViewById(R.id.buttonMsg);
            viewHolder.MsgBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DonorPerson currentPerson =MainGUI.Donors.get(position);
                    String number =currentPerson.getNumber();
                    //String number = (String) allPPLNumbers.get(position);
                    Uri uri = Uri.parse("smsto:" + number);
                    Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
                    intent.putExtra("sms_body", "");
                    startActivity(intent);
                }
            });

           viewHolder.NotifyBtn = (Button) itemView.findViewById(R.id.buttonNotify);
            viewHolder.NotifyBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String [] requesterDetails = new String[2];
                    //GCM user ID to noftify donors
                    DonorPerson currentPerson =MainGUI.Donors.get(position);
                    String IDofDonor = currentPerson.getID();
                    //String IDofDonor = (String) allPPLIDs.get(position);

                    // GCM ID of requester used to notify back
                    String IDofRequester = LoginActivity.IDofLoggedINUser;
                    // Name and Image are used to be shown on Contact back activity
                    String NameofRequester= LoginActivity.NameofLoggedINUser;
                    //String ImageofRequester = LoginActivity.ImageofLoggedINUser;
                    // Number is used to contact the requester
                    String NumberofRequester = LoginActivity.NumberofLoggedINUser;

                    requesterDetails[0] = IDofRequester;
                    requesterDetails[1] = NameofRequester;
                    //requesterDetails[2] = ImageofRequester;
                    String APIkey = "AIzaSyCmUazxt0V94gp8EJjBJpwBAPPuPsrflls";
                    GCM gcm = new GCM();
                    gcm.sendMessage(APIkey, IDofDonor, NumberofRequester, requesterDetails);
                    Toast.makeText(PeopleList.this, "User notified successfully", Toast.LENGTH_SHORT).show();
                }
            });
            return itemView;

        }


        public Bitmap getRoundedShape(Bitmap scaleBitmapImage) {
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
    }

    public class ViewHolder {

        ImageView DonorImage;
        Button CallBtn;
        Button MsgBtn;
        TextView Name;
        TextView Email;
        TextView Age;
        Button NotifyBtn;
    }
}



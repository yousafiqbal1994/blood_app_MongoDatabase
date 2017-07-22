package com.donateblood.blooddonation;

import android.*;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ContactBack extends AppCompatActivity {

    Button MSGBTN;
    Button CallBTN;
    Button UnFit;
    Button NotAvailable;
    TextView RequesterName; ImageView RequesterImage;
    public static boolean DonorUnfit,DonorNotAvailable;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.inject(this);
        setContentView(R.layout.contactback);
        RequesterImage = (ImageView) findViewById(R.id.requesterimage);
        RequesterName = (TextView) findViewById(R.id.Name);
        NotAvailable= (Button) findViewById(R.id.notavailable);
        CallBTN = (Button) findViewById(R.id.buttonCall);
        MSGBTN = (Button) findViewById(R.id.buttonMsg);
        UnFit = (Button) findViewById(R.id.unfit);
        // Show requester Name
        RequesterName.setText(MessageReceive.RequesterName+" need blood");
        RequesterImage.setImageResource(R.drawable.sad);

        MSGBTN.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String number = MessageReceive.number;
                Uri uri = Uri.parse("smsto:" + number);
                Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
                intent.putExtra("sms_body", "");
                startActivity(intent);
            }
        });

        CallBTN.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String number = MessageReceive.number;
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + number));
                if (ActivityCompat.checkSelfPermission(ContactBack.this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                startActivity(intent);
            }
        });
        NotAvailable.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Write code here
                // GCM ID to notify Back
                DonorNotAvailable = true;
                String APIkey = "AIzaSyCmUazxt0V94gp8EJjBJpwBAPPuPsrflls";
                GCM gcm = new GCM();
                String ID = MessageReceive.RequesterID;
                gcm.sendMessageBack(APIkey,ID);
                Toast.makeText(getBaseContext(), MessageReceive.RequesterName+" Notified", Toast.LENGTH_SHORT).show();
            }

        });

        UnFit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Write code here
                // GCM ID to notify Back
                DonorUnfit =true;
                String APIkey = "AIzaSyCmUazxt0V94gp8EJjBJpwBAPPuPsrflls";
                GCM gcm = new GCM();
                String ID = MessageReceive.RequesterID;
                gcm.sendMessageBack(APIkey,ID);
                Toast.makeText(getBaseContext(), MessageReceive.RequesterName+" Notified", Toast.LENGTH_SHORT).show();
            }

        });

    }

}

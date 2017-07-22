package com.donateblood.blooddonation;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Config;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.sinch.verification.CodeInterceptionException;
import com.sinch.verification.InvalidInputException;
import com.sinch.verification.PhoneNumberUtils;
import com.sinch.verification.ServiceErrorException;
import com.sinch.verification.SinchVerification;
import com.sinch.verification.Verification;
import com.sinch.verification.VerificationListener;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import butterknife.ButterKnife;
import butterknife.InjectView;
public class SignupActivity extends AppCompatActivity {
    public Spinner mySpinner;
    public static String bloodgroup, name, password, number, email, age,NumberCorrect;
    public static String Code;
    @InjectView(R.id.input_name) EditText _nameText;
    @InjectView(R.id.input_email) EditText _emailText;
    @InjectView(R.id.input_password) EditText _passwordText;
    @InjectView(R.id.btn_signup) Button btn_signup;
    @InjectView(R.id.btn_MobileVerify) Button btn_VerifyMobile;
    @InjectView(R.id.link_login) TextView _loginLink;
    @InjectView(R.id.input_number) TextView _numText;
    @InjectView(R.id.input_age) TextView _age;
    DB db; public  Boolean check = false; ProgressBar progressBar;
    DBCursor cursor;
    DBCollection collection;
    Database dataobj = new Database();
    public boolean failed = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        String[] list = getResources().getStringArray(R.array.blood_type);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_layout, R.id.txt, list);
        spinner.setAdapter(adapter);
        ButterKnife.inject(this);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setClickable(false);
        // getCurrentLatLong();
        btn_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                signup();
                if(failed==true){
                    return;
                }
                else {
                    // check all email of database not to match the entered email
                    CheckEmailAsync checkEmail = new CheckEmailAsync();
                    checkEmail.execute();
                }
            }
        });
        _loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                finish();
            }
        });

        btn_VerifyMobile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(_numText.getText().length()!=0){
                    showProgressDialog();
                    startVerification(_numText.getText().toString());
                }
                else {
                    Toast.makeText(SignupActivity.this,"Enter a phone number",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void startVerification(String phoneNumber) {
        String Appkey = "0a556fc7-d6b4-42ba-8840-b84ecc5e1453";
        com.sinch.verification.Config config = SinchVerification.config().applicationKey(Appkey).context(getApplicationContext()).build();
        VerificationListener listener = new MyVerificationListener();
        String defaultRegion = PhoneNumberUtils.getDefaultCountryIso(SignupActivity.this);
        String phoneNumberInE164 = PhoneNumberUtils.formatNumberToE164(phoneNumber, defaultRegion);
        Verification verification = SinchVerification.createFlashCallVerification(config, phoneNumberInE164, listener);
        verification.initiate();
    }

    // Inner Class for mobile verification
    private class MyVerificationListener implements VerificationListener {
        @Override
        public void onInitiated() {

        }

        @Override
        public void onInitiationFailed(Exception e) {
            hideProgressDialog();
            if (e instanceof InvalidInputException) {
                Toast.makeText(SignupActivity.this,"Incorrect number provided",Toast.LENGTH_LONG).show();
                Log.e("wrong","Incorrect number provided");
            } else if (e instanceof ServiceErrorException) {
                Toast.makeText(SignupActivity.this,"Sinch service error",Toast.LENGTH_LONG).show();
                Log.e("wrong","Sinch service error");
            } else {
                Toast.makeText(SignupActivity.this,"Other system error, check your network state", Toast.LENGTH_LONG).show();
                Log.e("wrong","Other system error, check your network state");
            }

        }

        @Override
        public void onVerified() {
            hideProgressDialog();
            new AlertDialog.Builder(SignupActivity.this)
                    .setMessage("Verification Successful!")
                    .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.cancel();
                        }
                    })
                    .show();
        }

        @Override
        public void onVerificationFailed(Exception e) {

            hideProgressDialog();
            if (e instanceof CodeInterceptionException) {
                 Toast.makeText(SignupActivity.this,"Intercepting the verification call automatically failed",Toast.LENGTH_LONG).show();
            } else if (e instanceof ServiceErrorException) {
                 Toast.makeText(SignupActivity.this, "Sinch service error",Toast.LENGTH_LONG).show();
            } else {
                 Toast.makeText(SignupActivity.this,"Other system error, check your network state", Toast.LENGTH_LONG).show();
            }

        }
    }

    private void showProgressDialog() {
        progressBar.setVisibility(ProgressBar.VISIBLE);
    }

    private void hideProgressDialog() {
        progressBar.setVisibility(ProgressBar.INVISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void signup() {

        if (validate() == false) {
            onSignupFailed();
            return;
        }

    }

    public void onSignupSuccess() {

        btn_signup.setEnabled(true);
        setResult(RESULT_OK, null);
        Intent intent = new Intent(getApplicationContext(), UploadImage.class);
        startActivity(intent);
        finish();
    }

    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), "Registration failed", Toast.LENGTH_LONG).show();
        btn_signup.setEnabled(true);
        failed = true;
    }

    public boolean validate() {
        boolean valid = true;
        GetUserDetails();
        if (name.isEmpty() || name.length() < 4 || name.length() >20) {
            _nameText.setError("Enter between 4 to 20 characters");
            valid = false;
        } else {
            _nameText.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("Enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 16) {
            _passwordText.setError("between 4 and 16  characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }
        ;
        if (age.isEmpty() || age.length() > 2 || Integer.parseInt(_age.getText().toString()) < 0) {
            _age.setError("Enter correct age");
            valid = false;
        } else {
            _age.setError(null);
        }

        if (number.isEmpty()) {
            _numText.setError("Enter valid number");
            valid = false;
        } else {
            _numText.setError(null);
        }

        return valid;
    }

    public void GetUserDetails() {
        mySpinner = (Spinner) findViewById(R.id.spinner);
        bloodgroup = mySpinner.getSelectedItem().toString();
        name = _nameText.getText().toString();
        email = _emailText.getText().toString();
        password = _passwordText.getText().toString();
        age = _age.getText().toString();
        number = _numText.getText().toString();
    }



    public class SendEmailAsynTask extends AsyncTask<Void, Void, Void> {

        private ProgressDialog pDialog;
        JSONObject json = null;
        @Override
        protected Void doInBackground(Void... voids) {

            char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
            StringBuilder sb = new StringBuilder();
            Random random = new Random();
            for (int i = 0; i < 5; i++) {
                char c = chars[random.nextInt(chars.length)];
                sb.append(c);
            }

            Code = sb.toString();
            String email = SignupActivity.email;

            final HashMap<String ,String> EmailDetails = new HashMap<>();
            EmailDetails.put("email",email);
            EmailDetails.put("verification",Code);
            json = new HttpCall().postForJSON("http://usafiqbalbloodapp.netne.net/blood_app/donationsignup.php",EmailDetails);

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(SignupActivity.this);
            pDialog.setMessage("Sending verification code to email...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            pDialog.dismiss();
            Toast.makeText(getBaseContext(), "Verification code sent successfully", Toast.LENGTH_LONG).show();
            onSignupSuccess();

        }
    }

    public class CheckEmailAsync extends AsyncTask<Void,Void,Void> {
        private ProgressDialog pDialog;
        @Override
        protected Void doInBackground(Void... voids) {
            check = EmailExisted();
            return null;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
                pDialog = new ProgressDialog(SignupActivity.this);
                pDialog.setMessage("Checking email...");
                pDialog.setIndeterminate(false);
                pDialog.setCancelable(false);
                pDialog.show();
            }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            pDialog.dismiss();
            if(check==true){
                Toast.makeText(getBaseContext(), "User with this email already existed", Toast.LENGTH_SHORT).show();
            }
            else {
                SendEmailAsynTask sendEmail = new SendEmailAsynTask();
                sendEmail.execute();
            }
            }
        }



    public boolean EmailExisted(){
        check =false;
        String email = _emailText.getText().toString();
        email =email.trim().toLowerCase();
        db = dataobj.getconnection();
        collection = db.getCollection("UserDetails");
        BasicDBObject query = new BasicDBObject();
        query.put("email", email);
        cursor = collection.find(query);
        if (cursor.hasNext()) {
            check=true;
        }
        return check;
    }

}


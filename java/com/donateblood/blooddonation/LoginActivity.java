package com.donateblood.blooddonation;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;
    DB db;
    DBCursor cursor;
    GPSTracker gps ;
    DBCollection collection;
    Database dataobj = new Database();
    public static String email;
    public static String password,ImageofLoggedINUser,NameofLoggedINUser,IDofLoggedINUser,NumberofLoggedINUser;
    //public static boolean fbLogin =false;
    //private CallbackManager mcallbackManager;

   /* private FacebookCallback<LoginResult>  mcallbackk = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
           // AccessToken accss = loginResult.getAccessToken();
           // Profile prof = Profile.getCurrentProfile();
            fbLogin=true;
            new GPSTracker(LoginActivity.this);
            Log.e("Login","Login facebook started");
            Intent intent = new Intent(getApplicationContext(), MainGUI.class);
            startActivity(intent);
            finish();
        }

        @Override
        public void onCancel() {
        }
        @Override
        public void onError(FacebookException error) {

        }
    }; */
    @InjectView(R.id.input_email) EditText _emailText;
    @InjectView(R.id.input_password) EditText _passwordText;
    @InjectView(R.id.btn_login) Button _loginButton;
    @InjectView(R.id.link_signup) TextView _signupLink;
    //GPSTracker gps;

    //public static Button myloc;
  // public static TextView myTxt;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //FacebookSdk.sdkInitialize(LoginActivity.this);
        setContentView(R.layout.activity_login);
        ButterKnife.inject(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
       /* mcallbackManager = CallbackManager.Factory.create();
        LoginButton fbLogin = (LoginButton) findViewById(R.id.login_button);
        fbLogin.registerCallback(mcallbackManager,mcallbackk); */

      // myloc = (Button) findViewById(R.id.loc);
       // myTxt= (TextView) findViewById(R.id.text);

     /*   myloc.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
               // Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                gps=new GPSTracker(LoginActivity.this);
            }
        }); */
        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(isNetworkAvailable() && _passwordText.getText().length()!=0 && _emailText.getText().length()!=0){
                AsyncLogin LoginThread = new AsyncLogin();
                LoginThread.execute();
                }
                else {
                    if(!isNetworkAvailable()){
                    Toast.makeText(getBaseContext(), "No network available", Toast.LENGTH_SHORT).show();}
                    else {
                        validate();
                    }
                }
            }
        });
        /*fbLogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

            }
        }); */
        _signupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if(activeNetworkInfo != null && activeNetworkInfo.isConnected()){
            return true;
        }else {
            return false;
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public class AsyncLogin extends AsyncTask<Void,Void,Void> {
        public boolean flag =false;
        private ProgressDialog pDialog;
        @Override
        protected Void doInBackground(Void... voids) {
            login();
            return null;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!validate()) {
                onLoginFailed();
                return;
            }
            else {
            _loginButton.setEnabled(false);
            pDialog = new ProgressDialog(LoginActivity.this);
            pDialog.setMessage("Authenticating...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            pDialog.dismiss();

            if(flag==true){
                Toast.makeText(getBaseContext(), "Login Successfully", Toast.LENGTH_LONG).show();
            onLoginSuccess();
            }
            if(flag==false){
                onLoginFailed();
            }
        }
        public void login() {
            if(_emailText.getText().length()==0 || _passwordText.getText().length()==0){
                return;
            }
            else {
                email = _emailText.getText().toString().trim().toLowerCase();
                password = _passwordText.getText().toString();
                db = dataobj.getconnection();
                collection = db.getCollection("UserDetails");
                BasicDBObject query = new BasicDBObject();
                query.put("email", email);
                query.put("password", password);
                cursor = collection.find(query);
                if (cursor.hasNext()) {
                    DBObject doc = cursor.next();
                    ImageofLoggedINUser = doc.get("image").toString();
                    NameofLoggedINUser = doc.get("Name").toString();
                    NumberofLoggedINUser= doc.get("number").toString();
                    IDofLoggedINUser = doc.get("ID").toString();

                    flag = true;
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);

       // mcallbackManager.onActivityResult(requestCode,resultCode,data);


        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {

                // TODO: Implement successful signup logic here
                // By default we just finish the Activity and log them in automatically
                this.finish();
            }
        }
    }

   /* @Override
    public void onBackPressed() {
        // disable going back to the MainActivity
        moveTaskToBack(true);
    }*/

    public void onLoginSuccess() {
        _loginButton.setEnabled(true);
        CheckConnectivity.NetAvailable=true;
        new GPSTracker(LoginActivity.this);
        Log.e("Login","Login activity started");
        Intent intent = new Intent(getApplicationContext(), MainGUI.class);
        startActivity(intent);
        finish();
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Login failed. Incorrect credentials", Toast.LENGTH_LONG).show();
        _loginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("Enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty()) {
            _passwordText.setError("Enter correct password");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }
}

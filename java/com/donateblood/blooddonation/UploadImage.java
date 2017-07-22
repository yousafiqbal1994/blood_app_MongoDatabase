package com.donateblood.blooddonation;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.mongodb.DB;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSInputFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class UploadImage extends AppCompatActivity {

    public static final int RESULT_LOAD = 1;
    @InjectView(R.id.imageView) ImageView ImageUpload;
    @InjectView(R.id.upload) Button Btn_Upload;
    @InjectView(R.id.proceed) Button Btn_Proceed;
    EditText code;
    public String bloodgroup,name,password,number,email,picturePath,age,ID;
    public String encodedPhotoString=null;
    Database dbobj = new Database();
    GPSTracker gps; private DB db;
    public double latitude=0;
    public double longitude=0; Bitmap myPhoto;  Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.uploadimage);
        code = (EditText) findViewById(R.id.code);
        ButterKnife.inject(this);
        getCurrentLatLong();

        // Upload image ====================================
        Btn_Upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                Intent upload = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(upload,RESULT_LOAD);
            } catch (ActivityNotFoundException E) {

                    String errorMessage = "Your device doesn't support the crop action!";
                    Toast toast = Toast.makeText(UploadImage.this, errorMessage, Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });
        Btn_Proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(code.length()==0){
                    Toast.makeText(getBaseContext(), "Enter verification code", Toast.LENGTH_LONG).show();
                }
                else {
                    Prcoess();
                }
            }
        });
    }
    // When image is selected from Gallery
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==RESULT_LOAD && resultCode==RESULT_OK && data!=null){


            Uri SelectedImageURI = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), SelectedImageURI);
            } catch (IOException e) {
                e.printStackTrace();
            }
            myPhoto=  getResizedBitmap(bitmap,200,200);
            //ImageUpload.setImageURI(SelectedImageURI);
            ImageUpload.setImageBitmap(myPhoto);

            // retrieve image path
            String[] projection = {MediaStore.Images.Media.DATA};
            try {
                Cursor cursor = getContentResolver().query(SelectedImageURI, projection, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(projection[0]);
                 picturePath = cursor.getString(columnIndex);
                cursor.close();

                String fileNameSegments[] = picturePath.split("/");
                String fileName = fileNameSegments[fileNameSegments.length - 1];
                try {
                    File f = new File(picturePath);
                    Bitmap myImg = decodeFile(f,720);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    myImg.compress(Bitmap.CompressFormat.JPEG, 50, stream);
                    byte[] byte_arr = stream.toByteArray();
                    // Encode Image to String
                    encodedPhotoString = Base64.encodeToString(byte_arr, 0);
                   // ImageUpload.setImageBitmap(myImg);

                }catch (Exception e){
                    Toast.makeText(UploadImage.this,"Unable to Set Image Try Again",Toast.LENGTH_SHORT).show();
                }
            }
            catch(Exception e) {
               // Log.e("Path Error", e.toString());
                Toast.makeText(getBaseContext(), "Error finding path", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
            //
        }
    }

    // Processing and adding user to database from here ====================================
    public void Prcoess(){
        String userentered=code.getText().toString();
        String sentcode = SignupActivity.Code;
        if(userentered.equals(sentcode) && encodedPhotoString!=null ){
        dbAsync signupThread = new dbAsync();
         signupThread.execute();
        }
        else {
            Toast.makeText(getBaseContext(), "Wrong code or No image uploaded", Toast.LENGTH_LONG).show();
        }
    }

    public class dbAsync extends AsyncTask<Void,Void,Void> {

        private ProgressDialog pDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(UploadImage.this);
            pDialog.setMessage("Creating Account...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }
        @Override
        protected Void doInBackground(Void... voids) {

            GetUserDetails();
            GenerateGCMID();
            email= email.trim().toLowerCase();
            dbobj.insertUser(ID,name,email,password,number,bloodgroup,latitude,longitude,encodedPhotoString,age);
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            pDialog.dismiss();
            Toast.makeText(getBaseContext(), "Created Successfully", Toast.LENGTH_LONG).show();
            onSignupSuccess();
        }
    }

    public void GenerateGCMID(){
        GCMClientManager pushClientManager = new GCMClientManager(this, "921544902369");
        pushClientManager.registerIfNeeded(new GCMClientManager.RegistrationCompletedHandler() {
            @Override
            public void onSuccess(String registrationId, boolean isNewRegistration) {

                Log.d("Registration id", registrationId);
                ID = registrationId;
                Log.e("reg",ID);

            }

            @Override
            public void onFailure(String ex) {
                super.onFailure(ex);
            }
        });
    }

    // Go to another activity on success ====================================
    public void onSignupSuccess() {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
        finish();
    }

    // fetch user details ====================================
    public void GetUserDetails(){

        bloodgroup = SignupActivity.bloodgroup.toString();
        name = SignupActivity.name.toString();
        email = SignupActivity.email.toString();
        password = SignupActivity.password.toString();
        number = SignupActivity.number.toString();
        age = SignupActivity.age.toString();
    }

    // get Current location ====================================
    public void getCurrentLatLong(){
        gps = new GPSTracker(UploadImage.this);
        if (gps.canGetLocation()) {
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();
        }
    }

    // Decode the image
   public Bitmap decodeFile(File f,int IMAGE_MAX_SIZE){
       Bitmap b = null;

       //Decode image size
       BitmapFactory.Options o = new BitmapFactory.Options();
       o.inJustDecodeBounds = true;

       FileInputStream fis = null;
       try {
           fis = new FileInputStream(f);
       } catch (FileNotFoundException e) {
           e.printStackTrace();
       }
       BitmapFactory.decodeStream(fis, null, o);
       try {
           fis.close();
           int scale = 1;
           if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
               scale = (int) Math.pow(2, (int) Math.ceil(Math.log(IMAGE_MAX_SIZE /
                       (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
           }
           //Decode with inSampleSize
           BitmapFactory.Options o2 = new BitmapFactory.Options();
           o2.inSampleSize = scale;
           fis = new FileInputStream(f);
           b = BitmapFactory.decodeStream(fis, null, o2);
           fis.close();

       }catch (IOException e) {
           e.printStackTrace();
       }

       return b;
   }

    // Resize the image ====================================
    public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth)
    {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // create a matrix for the manipulation
        Matrix matrix = new Matrix();
        // resize the bit map
        matrix.postScale(scaleWidth, scaleHeight);
        // recreate the new Bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }

}

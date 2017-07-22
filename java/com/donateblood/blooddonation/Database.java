package com.donateblood.blooddonation;
import android.util.Log;
import android.widget.Spinner;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

public class Database {
    private DB db;
    private DBCollection collection;

    // Get database connection
    public DB getconnection (){
        MongoClientURI uri = new MongoClientURI("mongodb://yousaf:UC1941994@ds015730.mlab.com:15730/donateblood");
        MongoClient client = null;
        client = new MongoClient(uri);
        db = client.getDB(uri.getDatabase());
        return db;
    }

    // insert user details ====================================
    public void insertUser(String ID,String name, String email, String password, String number,String bloodgroup,double latitude,double longitude,String encodedPhotoString,String age){
        db = getconnection();
        collection = db.getCollection("UserDetails");
        BasicDBObject document = new BasicDBObject();
        document.put("ID",ID);
        document.put("Name", name);
        document.put("email", email);
        document.put("password", password);
        document.put("age",age);
        document.put("number", number);
        document.put("bloodgroup", bloodgroup);
        document.put("lat",latitude);
        document.put("long",longitude);
        document.put("image", encodedPhotoString);

        collection.insert(document);
    }

    // update user details ====================================
    public void UpdateUser(DB db, String email,String Curlat, String Curlong ){

        collection = db.getCollection("UserDetails");
        BasicDBObject whereQuery = new BasicDBObject();
        whereQuery.put("email",email);
        BasicDBObject updateFields = new BasicDBObject();
        updateFields.append("lat", Curlat);
        updateFields.append("long", Curlong);
        BasicDBObject setQuery = new BasicDBObject();
        setQuery.append("$set", updateFields);
        collection.update(whereQuery, setQuery);
        Log.e("test","User updated");

    }
}

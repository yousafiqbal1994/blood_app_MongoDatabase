
package com.donateblood.blooddonation;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class HttpCall {

    public JSONObject postForJSON(String requestURL,HashMap<String, String> postDataParams){

        String result= performPostCall(requestURL,postDataParams);
        if(!result.isEmpty()){
            try {
                Log.e("RESULT FROM POST CALL", result);
                return new JSONObject(result);
            } catch (JSONException e) {
                Log.e("JSON Parser Error", e.toString());
                Log.e("RESULT FROM POST CALL", result);
//                e.printStackTrace();
                return null;
            }
        }
        Log.e("RESULT FROM POST CALL", "EMPTY RESULT");
        return null;
    }
    public JSONObject getJSON(String URL){
        String result= sendGetRequest(URL);
        if(!result.isEmpty()){
            try {
                Log.e("RESULT FROM POST CALL", result);
                return new JSONObject(result);
            } catch (JSONException e) {
                Log.e("JSON Parser Error", e.toString());
                Log.e("RESULT FROM POST CALL", result);
//                e.printStackTrace();
                return null;
            }
        }
        Log.e("RESULT FROM POST CALL", "EMPTY RESULT");
        return null;
    }
    public String performPostCall(String requestURL,
                                  HashMap<String, String> postDataParams) {

        URL url;
        String response = "";
        try {
            url = new URL(requestURL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);


            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(getPostDataString(postDataParams));

            writer.flush();
            writer.close();
            os.close();
            int responseCode=conn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                String line;
                BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line=br.readLine()) != null) {
                    response+=line;
                }
            }
            else {
                response="";

            }
        } catch (Exception e) {
//            e.printStackTrace();
            Log.e("Exception", e.toString());
            return response;

        }

        return response;
    }
    public String sendGetRequest(String uri) {
        String response = "";
        try {
            URL url = new URL(uri);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));



            StringBuilder sb = new StringBuilder();

            while((response = bufferedReader.readLine())!=null){
                sb.append(response);
            }

            return sb.toString();
        } catch (Exception e) {
            Log.e("Exception", e.toString());
            return response;
        }
    }
    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }
}

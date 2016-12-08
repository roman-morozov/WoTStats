package org.rmorozov.wot_stats;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class JSONParser {
    static JSONArray jarray;
    static JSONObject jarrayObj;

    static {
        jarray = null;
        jarrayObj = null;
    }

    private String getJSON(String requestUrl) {
        StringBuilder builder = new StringBuilder();
        try {
            URL url = new URL(requestUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(10000);
            connection.setConnectTimeout(10000);
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()), 8);
                while (true) {
                    String line = reader.readLine();
                    if (line != null) {
                        builder.append(line).append("\n");
                    } else {
                        connection.disconnect();
                        return builder.toString();
                    }
                }
            }
            Log.e("Error....", "Failed to download file");
            return null;
        } catch (IOException e3) {
            e3.printStackTrace();
            return null;
        }
    }

    public JSONArray getJSONArrayFromUrl(String requestUrl) {
        try {
            jarrayObj = new JSONObject(getJSON(requestUrl));
            jarray = jarrayObj.getJSONArray("data");
            return jarray;
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
            return null;
        }
    }

    public JSONObject getJSONObjFromUrl(String requestUrl) {
        try {
            jarrayObj = new JSONObject(getJSON(requestUrl));
            return jarrayObj;
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
            return null;
        }
    }
}

package com.annabenson.stockwatch;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;


/**
 * Created by Anna on 3/3/2018.
 */

public class AsyncStockLoader extends AsyncTask<String,Integer,String> {

    private MainActivity mainActivity;
    private int count;

    private final String dataURLStem = "https://query1.finance.yahoo.com/v1/finance/search?lang=en-US&region=US&quotesCount=10&newsCount=0&q=";
    private static final String TAG = "AsyncStockLoader";

    public AsyncStockLoader(MainActivity ma){ mainActivity = ma;}

    // Shows a toast while the search request is in flight
    @Override
    protected void onPreExecute(){
        Toast.makeText(mainActivity, "Loading Stock Data...", Toast.LENGTH_SHORT).show();
    }

    // Parses the search results and either shows the selection dialog or a not-found dialog
    @Override
    protected void onPostExecute(String s) {
        ArrayList<Stock> stocksList = parseJSON(s);
        if(stocksList == null || stocksList.size() == 0){
            mainActivity.notFoundDialog();
        }
        else {
            mainActivity.stockSelect(stocksList);
        }
    }

    // Fetches stock search results from the Yahoo Finance search API for the given symbol query
    @Override
    protected String doInBackground(String... params) {

        String dataURL = dataURLStem + params[0].toUpperCase();
        Log.d(TAG, "doInBackground: URL is " + dataURL);
        Uri dataUri = Uri.parse(dataURL);
        String urlToUse = dataUri.toString();

        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(urlToUse);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(is)));

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }

            Log.d(TAG, "doInBackground: " + sb.toString());

        } catch (Exception e) {
            Log.e(TAG, "doInBackground: ", e);
            return null;
        }

        return sb.toString();
    }


    // Parses the Yahoo Finance search response and returns a list of matching EQUITY stocks
    private ArrayList<Stock> parseJSON(String s) {

        Log.d(TAG, "parseJSON: started JSON");

        ArrayList<Stock> stocksList = new ArrayList<>();
        try {
            JSONObject jObjMain = new JSONObject(s);
            JSONArray quotes = jObjMain.getJSONArray("quotes");
            count = quotes.length();

            for (int i = 0; i < quotes.length(); i++) {
                JSONObject jStock = (JSONObject) quotes.get(i);
                String type = jStock.optString("quoteType", "");
                if (type.equals("EQUITY")) {
                    String symbol = jStock.getString("symbol");
                    String name = jStock.optString("shortname", jStock.optString("longname", symbol));

                    int idx = symbol.indexOf('.');
                    if (idx >= 0) {
                        Log.d(TAG, "parseJSON: ignored " + name + ", " + symbol);
                    } else {
                        Log.d(TAG, "parseJSON: loaded " + name + ", " + symbol);
                        stocksList.add(new Stock(name, symbol));
                    }
                }
            }
            return stocksList;
        } catch (Exception e) {
            Log.d(TAG, "parseJSON: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }


}

package com.annabenson.stockwatch;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Anna on 3/5/2018.
 */

public class AsyncFinancialDataLoader extends AsyncTask<String,Integer,String> {

    private MainActivity mainActivity;
    private int count;

    private final String dataURLPrefix = "https://api.iextrading.com/1.0/stock/";
    private final String getDataURLSuffix = "/quote";
    private static final String TAG = "AsyncFinDataLoader";

    public AsyncFinancialDataLoader(MainActivity ma){ mainActivity = ma;}

    @Override
    protected void onPreExecute(){
        Toast.makeText(mainActivity, "Loading Financial Data...", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPostExecute(String s) {
        ArrayList<Stock> stocksList = parseJSON(s);
        //Stock x = stocksList.get(0);
        //String n = x.getName();
        //Log.d(TAG, "onPostExecute: loaded" + n);
        //Toast.makeText(mainActivity, "Loaded " + stocksList.size() + " stocks.", Toast.LENGTH_SHORT).show();
        //mainActivity.stockSelect(stocksList);
        //mainActivity.updateData(stocksList);

        mainActivity.addNewStock(stocksList);
        // call something to add new data to existing data
    }

    @Override
    protected String doInBackground(String... params) {

        String dataURL = dataURLPrefix + params[0] + getDataURLSuffix;
        Log.d(TAG, "doInBackground: URL is " + dataURL);
        Uri dataUri = Uri.parse(dataURL);
        String urlToUse = dataUri.toString();
        Log.d(TAG, "doInBackground: " + urlToUse);

        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(urlToUse);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
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

        Log.d(TAG, "doInBackground: " + sb.toString());
        Log.d(TAG, "doInBackground: returning");
        return sb.toString();
    }

    private ArrayList<Stock> parseJSON(String s) {

        Log.d(TAG, "parseJSON: started JSON");

        ArrayList<Stock> stocksList = new ArrayList<>();
        try {
            JSONObject jStock = new JSONObject(s); // {}
            //JSONObject jObjMain2 = jObjMain3.getJSONObject("ResultSet");
            //JSONArray jObjMain = jObjMain2.getJSONArray("Result");
            //count = jObjMain.length();

            //String symbol = jStock.getString("symbol");
            //if(

            String symbol = jStock.getString("symbol");
            double price = Double.parseDouble (jStock.getString("latestPrice"));
            double change = Double.parseDouble (jStock.getString("change"));
            double percent = Double.parseDouble (jStock.getString("changePercent"));
            stocksList.add(new Stock("", symbol, price, change, percent));

            /*

            for (int i = 0; i < jObjMain.length(); i++) {
                JSONObject jStock = (JSONObject) jObjMain.get(i);
                String type = jStock.getString("type");
                if( type.equals("S")  ){
                    // so only get Stocks
                    String symbol = jStock.getString("symbol");
                    String name = jStock.getString("name");
                    Log.d(TAG, "parseJSON: loaded " + name + ", " + symbol);
                    stocksList.add(new Stock(name, symbol));
                }

                //String exch = jStock.getString("exch");
                //String type = jStock.getString("type");
                //String exchDisp = jStock.getString("exchDisp");
                //String typeDisp = jStock.getString("typeDisp");


            }
            */
            return stocksList;
        } catch (Exception e) {
            Log.d(TAG, "parseJSON: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}

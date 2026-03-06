package com.annabenson.stockwatch;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

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

    private final String dataURLPrefix = "https://query1.finance.yahoo.com/v8/finance/chart/";
    private static final String TAG = "AsyncFinDataLoader";

    public AsyncFinancialDataLoader(MainActivity ma){ mainActivity = ma;}

    // No setup needed before the background fetch
    @Override
    protected void onPreExecute(){ }

    // Passes the parsed stock back to MainActivity to add to the displayed list
    @Override
    protected void onPostExecute(String s) {
        Stock stock = parseJSON(s);
        mainActivity.addNewStock(stock);
    }

    // Fetches price data from the Yahoo Finance chart API for the given stock symbol
    @Override
    protected String doInBackground(String... params) {

        String dataURL = dataURLPrefix + params[0].toUpperCase();
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

    // Parses the chart API response and returns a Stock with price and change data;
    // calculates change from previous close if the API omits the change fields
    private Stock parseJSON(String s) {

        Log.d(TAG, "parseJSON: started JSON");

        try {
            JSONObject root = new JSONObject(s);
            JSONObject chart = root.getJSONObject("chart");
            JSONArray results = chart.getJSONArray("result");
            JSONObject meta = results.getJSONObject(0).getJSONObject("meta");

            String symbol = meta.getString("symbol");
            double price = meta.getDouble("regularMarketPrice");
            double change = meta.optDouble("regularMarketChange", Double.NaN);
            double percent = meta.optDouble("regularMarketChangePercent", Double.NaN);

            if (Double.isNaN(change)) {
                double prevClose = meta.optDouble("chartPreviousClose", meta.optDouble("previousClose", price));
                change = price - prevClose;
                percent = prevClose != 0 ? (change / prevClose) * 100 : 0;
                Log.d(TAG, "parseJSON: calculated change=" + change + " percent=" + percent);
            }

            return new Stock("", symbol, price, change, percent);

        } catch (Exception e) {
            Log.d(TAG, "parseJSON: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}

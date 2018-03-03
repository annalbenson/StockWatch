package com.annabenson.stockwatch;

import android.os.AsyncTask;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Anna on 3/3/2018.
 */

public class AsyncStockLoader /*extends AsyncTask<String,Integer,String>*/ {

    private MainActivity mainActivity;
    private int count;

    //private final String dataURL = ""
    private static final String TAG = "AysncStockLoader";

    //@Override
    protected void onPreExecute(){
        Toast.makeText(mainActivity, "Loading Stock Data...", Toast.LENGTH_SHORT).show();
    }

    //@Override
    protected void onPostExecute(String s) {
        //ArrayList<Stock> countryList = parseJSON(s);
        //Toast.makeText(mainActivity, "Loaded " + countryList.size() + " countries.", Toast.LENGTH_SHORT).show();
        //mainActivity.updateData(countryList);
    }

}

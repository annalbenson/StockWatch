package com.annabenson.stockwatch;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.EventLogTags;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS;
import static android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, View.OnLongClickListener {

    // TEST TEST 1234 3/19/18

    // 3/21: Checked pdf vis a vi grading and saw that should have excluded stocks with periods
        //on brief inspection stocks with periods crash the app so that's a problem

    //private TextView textView;
    private SwipeRefreshLayout swiper;
    private static final String TAG = "MainActivity";
    private RecyclerView rV;
    private List<Stock> stocksList = new ArrayList<>();
    private RecyclerViewAdapter rVAdapter;
    private DatabaseHandler databaseHandler;
    private MainActivity mainActivity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



            // recycler view
            rV = findViewById(R.id.recycler);
            rVAdapter = new RecyclerViewAdapter(stocksList, this);
            rV.setAdapter(rVAdapter);
            rV.setLayoutManager(new LinearLayoutManager(this));
            databaseHandler = new DatabaseHandler(this);
            swiper = findViewById(R.id.swiper);


            swiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    if (connected()) {


                        ArrayList<Stock> list = databaseHandler.loadStocks();
                        stocksList.clear();
                        for (int i = 0; i < list.size(); i++) {
                            String symbol = list.get(i).getSymbol();
                            String name = list.get(i).getName();
                            new AsyncFinancialDataLoader(mainActivity).execute(symbol);
                        }
                        Collections.sort(stocksList);
                        rVAdapter.notifyDataSetChanged();
                        swiper.setRefreshing(false);
                    } else {
                        noNetDialogRefresh();
                        swiper.setRefreshing(false);
                    }
                }
            });

    }// end onCreate

    @Override
    protected void onResume(){

        if(connected()) {


            databaseHandler.dumpDbToLog();
            ArrayList<Stock> list = databaseHandler.loadStocks();

            // download stock data and build stock object and add to stocksList

            stocksList.clear();

            //new AsyncFinancialDataLoader(mainActivity).execute("AMZN");

            for (int i = 0; i < list.size(); i++) {

                String symbol = list.get(i).getSymbol();
                String name = list.get(i).getName();
                new AsyncFinancialDataLoader(mainActivity).execute(symbol);
                //stocksList.get(i).setName(name);

            }


            //stocksList.addAll(list);
            //Log.d(TAG, "onResume: " + list);

            // after for loop
            Collections.sort(stocksList);
            rVAdapter.notifyDataSetChanged();
        }
        else{
            noNetDialogRefresh();
        }
        // Call super last
        super.onResume();
    }

    protected void addNewStock(Stock stock){
        // called by async fin data load

        Log.d(TAG, "addNewStock: " + stock.getSymbol());
        // get name from dummy list in onResume maybe?
        //stock.setName();
        ArrayList<Stock> list = databaseHandler.loadStocks();

        for(int i = 0; i < list.size(); i ++){
            if(list.get(i).getSymbol().equals( stock.getSymbol())){
                stock.setName( list.get(i).getName());
            }
        }

        stocksList.add(stock);
        Collections.sort(stocksList);
        rVAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        databaseHandler.shutDown();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.action_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.addStock:
                //String s = "";
                if( connected() ){
                    addStockDialog();
                }
                else{
                    noNetDialogAdd();
                }
                //Toast.makeText(this, "You want to add a Stock", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        //Toast.makeText(v.getContext(), "SHORT" , Toast.LENGTH_SHORT).show();

        // open the browser to the stock's Market Watch site

        int pos = rV.getChildLayoutPosition(v);
        Stock s = stocksList.get(pos);

        String symbol = s.getSymbol();
        String url = "http://www.marketwatch.com/investing/stock/" + symbol;

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);

        // update code passed
        //return;
    }

    @Override
    public boolean onLongClick(View v){
        //Toast.makeText(v.getContext(), "LONG" , Toast.LENGTH_SHORT).show();
        int pos = rV.getChildLayoutPosition(v);
        Stock s = stocksList.get(pos);

        //Dialog to check if want to delete
        deleteDialog(pos, s);

        return false;
    }

    // network checkers

    private boolean connected(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
    public void noNetDialogAdd(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Stocks Cannot Be Added Without A Network Connection");
        builder.setTitle("No Network Connection");
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void noNetDialogRefresh(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Stocks Cannot Be Updated Without A Network Connection");
        builder.setTitle("No Network Connection");
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void addStockDialog(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        Log.d("addStockDialog", "addStockDialog called:");
        final EditText et = new EditText(this);
        et.setInputType(InputType.TYPE_CLASS_TEXT | TYPE_TEXT_FLAG_CAP_CHARACTERS );

        et.setGravity(Gravity.CENTER_HORIZONTAL);

        builder.setView(et);


        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.d("addStockDialog", "positive clicked");
                String s = et.getText().toString();

                boolean in = false;
                for(int j = 0; j < stocksList.size() ;j++){
                    String symb = stocksList.get(j).getSymbol();
                    if(symb.equals(s)){in = true;}
                }
                if(in){
                    // Duplicate
                    mainActivity.duplicateDialog(s);
                }
                else {
                    // Not duplicate
                    String[] sArr = {s};
                    new AsyncStockLoader(mainActivity).execute(sArr);
                }

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // do nothing
                Log.d("addStockDialog", "negative clicked");
                //return;
            }
        });
        builder.setMessage("Please enter a Stock Symbol");
        builder.setTitle("Stock Selection");

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void duplicateDialog(String symb){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Stock Symbol " + symb + " is already displayed");
        builder.setTitle("Duplicate Stock");
        // NEED: exclamation icon
        //builder.setIcon();

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void notFoundDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Data for stock symbol");
        builder.setTitle("Stock Symbol Not Found");


        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void stockSelect(ArrayList<Stock> s){
        // list select dialog, user chooses stock and it's sent to updateData

        final ArrayList<Stock> sList = s;

        final CharSequence[] sArray = new CharSequence[sList.size()];
        for(int i = 0; i < sList.size(); i++){
            sArray[i] = sList.get(i).getSymbol() + " - " + sList.get(i).getName();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Make a selection");

        builder.setItems(sArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                ArrayList<Stock> selected = new ArrayList<>();
                selected.add(sList.get(which));
                //mainActivity.getFinancialData(selected);
                updateData(selected);
            }
        });
        builder.setNegativeButton("Nevermind", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // do nothing, return
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    public void deleteDialog(int pos, Stock s){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final int pos2 = pos;
        //builder.setIcon(R.drawable.deleteIcon) or something

        builder.setIcon(R.drawable.delete_icon);

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // delete from DB, delete from list, notify Adapter
                databaseHandler.deleteStock(stocksList.get(pos2).getSymbol());
                stocksList.remove(pos2);
                rVAdapter.notifyDataSetChanged();
                //return;
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // do nothing
                //return;
            }
        });
        builder.setMessage("Delete Stock Symbol " + s.getSymbol() + "?"); // NEED ACTUAL SYMBOL HERE
        builder.setTitle("Delete Stock");

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void updateData(ArrayList<Stock> sList) {
        if(sList != null){
            // add new stock to the database
            databaseHandler.addAll(sList);
            // refresh recyclerview
            ArrayList<Stock> list = databaseHandler.loadStocks();
            stocksList.clear();

            if(list.size() != 0) {

                for (int i = 0; i < list.size(); i++) {

                    String symbol = list.get(i).getSymbol();


                    new AsyncFinancialDataLoader(mainActivity).execute(symbol);
                    //String name = list.get(i).getName();
                    //stocksList.get(i).setName(name);
                }
            }
            else{
                String symbol = list.get(0).getSymbol();


                new AsyncFinancialDataLoader(mainActivity).execute(symbol);
            }


            //stocksList.addAll(list);
            //stocksList.addAll(sList);



            Collections.sort(stocksList);
            rVAdapter.notifyDataSetChanged();

        }

    }

}

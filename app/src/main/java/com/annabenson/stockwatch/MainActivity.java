package com.annabenson.stockwatch;

import android.content.DialogInterface;
import android.content.Intent;
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
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, View.OnLongClickListener {

    //private TextView textView;
    private SwipeRefreshLayout swiper;
    private boolean on;

    private static final String TAG = "MainActivity";
    private RecyclerView rV;
    private List<Stock> stocksList = new ArrayList<>();
    private RecyclerViewAdapter rVAdapter;

    private static final int ADD_CODE = 1;
    private static final int UPDATE_CODE = 2;
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

        // swipe to refresh
        //textView = (TextView) findViewById(R.id.textView);
        //textView.setText("Is this thing on? ");
        swiper = findViewById(R.id.swiper);
        //on = false;


        swiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //textView.setText("Is this thing on? " + ! on );
                on = ! on;
                swiper.setRefreshing(false);
            }
        });


        //databaseHandler.shutDown();

        //new AsyncStockLoader(this).execute();

        /*
        databaseHandler.addStock( new Stock("Apple Inc.", "AAPL", 135.72,0.38,0.28));
        databaseHandler.addStock( new Stock("Amazon.com Inc.", "AMZN", 845.09,0.93,0.11));

        // 1e) Dummy data
        for(int i = 2; i < 10; i ++){
            databaseHandler.addStock(new Stock("Company" + i, "SYMB" + i, i*10,i*100, i / 100.0 ));
        }
        rVAdapter.notifyDataSetChanged();
        */
        //new AsyncStockLoader(this).execute();

    }

    @Override
    protected void onResume(){

        databaseHandler.dumpDbToLog();
        ArrayList<Stock> list = databaseHandler.loadStocks();
        stocksList.clear();
        stocksList.addAll(list);
        Log.d(TAG, "onResume: " + list);
        rVAdapter.notifyDataSetChanged();

        // Call super last
        super.onResume();
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
                addStockDialog();
                //Toast.makeText(this, "You want to add a Stock", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void addStockDialog(){
        // 06/18
        // Symbol Entry Dialog

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        Log.d("addStockDialog", "addStockDialog called:");
        final EditText et = new EditText(this);
        et.setInputType(InputType.TYPE_CLASS_TEXT);
        et.setGravity(Gravity.CENTER_HORIZONTAL);

        builder.setView(et);
        builder.setIcon(R.drawable.ic_launcher_background);// change later

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


                // Pop up list dialog

                // user selects


                // add code
                //return;
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
            stocksList.addAll(list);
            //stocksList.addAll(sList);
            rVAdapter.notifyDataSetChanged();
        }

    }

}

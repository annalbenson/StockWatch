package com.annabenson.stockwatch;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS;
import static android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener {

    private SwipeRefreshLayout swiper;
    private static final String TAG = "MainActivity";
    private RecyclerView rV;
    private List<Stock> stocksList = new ArrayList<>();
    private RecyclerViewAdapter rVAdapter;
    private DatabaseHandler databaseHandler;
    private MainActivity mainActivity = this;

    // Sets up the RecyclerView, adapter, database handler, and swipe-to-refresh listener
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rV = findViewById(R.id.recycler);
        rVAdapter = new RecyclerViewAdapter(stocksList, this);
        rV.setAdapter(rVAdapter);
        rV.setLayoutManager(new LinearLayoutManager(this));
        databaseHandler = new DatabaseHandler(this);
        swiper = findViewById(R.id.swiper);

        // Swipe left on a row to delete that stock
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = viewHolder.getAdapterPosition();
                databaseHandler.deleteStock(stocksList.get(pos).getSymbol());
                stocksList.remove(pos);
                rVAdapter.notifyItemRemoved(pos);
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY, int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;
                Paint paint = new Paint();
                paint.setColor(Color.RED);
                c.drawRect(itemView.getRight() + dX, itemView.getTop(),
                        itemView.getRight(), itemView.getBottom(), paint);
                Drawable icon = ContextCompat.getDrawable(MainActivity.this, R.drawable.delete_icon);
                if (icon != null) {
                    int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                    int iconTop = itemView.getTop() + iconMargin;
                    int iconLeft = itemView.getRight() - iconMargin - icon.getIntrinsicWidth();
                    icon.setBounds(iconLeft, iconTop, iconLeft + icon.getIntrinsicWidth(), iconTop + icon.getIntrinsicHeight());
                    icon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                    icon.draw(c);
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        }).attachToRecyclerView(rV);

        // Reload all stock data from the API when the user pulls down to refresh
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

    }

    // Loads saved stocks from the database and fetches fresh financial data for each on resume
    @Override
    protected void onResume() {
        if (connected()) {
            databaseHandler.dumpDbToLog();
            ArrayList<Stock> list = databaseHandler.loadStocks();
            stocksList.clear();

            for (int i = 0; i < list.size(); i++) {
                String symbol = list.get(i).getSymbol();
                String name = list.get(i).getName();
                new AsyncFinancialDataLoader(mainActivity).execute(symbol);
            }

            Collections.sort(stocksList);
            rVAdapter.notifyDataSetChanged();
        } else {
            noNetDialogRefresh();
        }
        super.onResume();
    }

    // Called by AsyncFinancialDataLoader when stock price data has been fetched;
    // looks up the company name from the database and adds the stock to the displayed list
    protected void addNewStock(Stock stock) {
        if (stock == null) {
            Log.e(TAG, "addNewStock: stock is null, API call failed");
            return;
        }
        Log.d(TAG, "addNewStock: " + stock.getSymbol());
        ArrayList<Stock> list = databaseHandler.loadStocks();

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getSymbol().equals(stock.getSymbol())) {
                stock.setName(list.get(i).getName());
            }
        }

        stocksList.add(stock);
        Collections.sort(stocksList);
        rVAdapter.notifyDataSetChanged();
    }

    // Closes the database connection when the activity is destroyed
    @Override
    protected void onDestroy() {
        databaseHandler.shutDown();
        super.onDestroy();
    }

    // Inflates the action bar menu with add and test connection buttons
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_menu, menu);
        return true;
    }

    // Handles action bar menu item taps: add stock or test API connection
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.addStock) {
            if (connected()) {
                addStockDialog();
            } else {
                noNetDialogAdd();
            }
            return true;
        } else if (id == R.id.testConnection) {
            // Pings the Yahoo Finance API in the background and toasts the result
            new AsyncTask<Void, Void, Boolean>() {
                @Override
                protected Boolean doInBackground(Void... voids) {
                    try {
                        java.net.HttpURLConnection conn = (java.net.HttpURLConnection)
                                new java.net.URL("https://query1.finance.yahoo.com/v1/finance/search?q=AAPL&quotesCount=1&newsCount=0")
                                        .openConnection();
                        conn.setRequestMethod("GET");
                        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                        conn.setConnectTimeout(8000);
                        conn.setReadTimeout(8000);
                        int code = conn.getResponseCode();
                        return code == 200;
                    } catch (Exception e) {
                        return false;
                    }
                }

                @Override
                protected void onPostExecute(Boolean success) {
                    Toast.makeText(MainActivity.this,
                            success ? "API connection OK" : "API connection FAILED",
                            Toast.LENGTH_LONG).show();
                }
            }.execute();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    // Opens the MarketWatch page for the tapped stock in the device browser
    @Override
    public void onClick(View v) {
        int pos = rV.getChildLayoutPosition(v);
        Stock s = stocksList.get(pos);

        String symbol = s.getSymbol();
        String url = "http://www.marketwatch.com/investing/stock/" + symbol;

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    // Returns true if the device currently has an active network connection
    private boolean connected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    // Shows an alert that stocks cannot be added without a network connection
    public void noNetDialogAdd() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Stocks Cannot Be Added Without A Network Connection");
        builder.setTitle("No Network Connection");
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Shows an alert that stocks cannot be refreshed without a network connection
    public void noNetDialogRefresh() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Stocks Cannot Be Updated Without A Network Connection");
        builder.setTitle("No Network Connection");
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Shows a dialog prompting the user to enter a stock symbol to search for
    public void addStockDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        Log.d("addStockDialog", "addStockDialog called:");
        final EditText et = new EditText(this);
        et.setInputType(InputType.TYPE_CLASS_TEXT | TYPE_TEXT_FLAG_CAP_CHARACTERS);
        et.setGravity(Gravity.CENTER_HORIZONTAL);
        builder.setView(et);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            // Checks for duplicates then launches AsyncStockLoader to search for the symbol
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.d("addStockDialog", "positive clicked");
                String s = et.getText().toString();

                boolean in = false;
                for (int j = 0; j < stocksList.size(); j++) {
                    String symb = stocksList.get(j).getSymbol();
                    if (symb.equalsIgnoreCase(s)) { in = true; }
                }
                if (in) {
                    mainActivity.duplicateDialog(s);
                } else {
                    String[] sArr = {s};
                    new AsyncStockLoader(mainActivity).execute(sArr);
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.d("addStockDialog", "negative clicked");
            }
        });
        builder.setMessage("Please enter a Stock Symbol");
        builder.setTitle("Stock Selection");

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Shows an alert that the entered stock symbol is already in the list
    public void duplicateDialog(String symb) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Stock Symbol " + symb + " is already displayed");
        builder.setTitle("Duplicate Stock");
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Shows an alert that no matching stock was found for the entered symbol
    public void notFoundDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Data for stock symbol");
        builder.setTitle("Stock Symbol Not Found");
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Presents a list of matching stocks for the user to choose from after a search
    public void stockSelect(ArrayList<Stock> s) {
        final ArrayList<Stock> sList = s;

        final CharSequence[] sArray = new CharSequence[sList.size()];
        for (int i = 0; i < sList.size(); i++) {
            sArray[i] = sList.get(i).getSymbol() + " - " + sList.get(i).getName();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Make a selection");

        builder.setItems(sArray, new DialogInterface.OnClickListener() {
            // Saves the selected stock and loads its financial data
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

    // Saves newly selected stocks to the database and fetches financial data for all saved stocks
    public void updateData(ArrayList<Stock> sList) {
        if (sList != null) {
            databaseHandler.addAll(sList);
            ArrayList<Stock> list = databaseHandler.loadStocks();
            stocksList.clear();

            if (list.size() != 0) {
                for (int i = 0; i < list.size(); i++) {
                    String symbol = list.get(i).getSymbol();
                    new AsyncFinancialDataLoader(mainActivity).execute(symbol);
                }
            } else {
                String symbol = list.get(0).getSymbol();
                new AsyncFinancialDataLoader(mainActivity).execute(symbol);
            }

            Collections.sort(stocksList);
            rVAdapter.notifyDataSetChanged();
        }
    }

}

package com.annabenson.stockwatch;

import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by Anna on 2/20/2018.
 */

public class RecyclerViewAdapter extends RecyclerView.Adapter<MyViewHolder>{

    public static final String TAG = "RecViewAdapt";
    private List<Stock> stocksList;
    private MainActivity mainAct;

    public RecyclerViewAdapter(List<Stock> stckList, MainActivity ma){
        this.stocksList = stckList;
        mainAct = ma;
    }

    @Override
    public MyViewHolder onCreateViewHolder(final ViewGroup parent, int viewType){
        Log.d(TAG, "onCreateViewHolder: MAKING NEW");
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.stock_list_row, parent, false);

        itemView.setOnClickListener(mainAct);
        itemView.setOnLongClickListener(mainAct);

        return new MyViewHolder(itemView);

    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position){
        Stock stock = stocksList.get(position);
        holder.name.setText(stock.getName());
        holder.symbol.setText(stock.getSymbol());
        holder.price.setText(String.format("%s",stock.getPrice()));
        holder.change.setText(String.format("^ %.2f (%.2f)", stock.getChange(), stock.getPercent()));
    }

    @Override
    public int getItemCount(){ return stocksList.size();}


}

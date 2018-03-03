package com.annabenson.stockwatch;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;



/**
 * Created by Anna on 2/20/2018.
 */

public class MyViewHolder extends RecyclerView.ViewHolder {

    public TextView name;
    public TextView symbol;
    public TextView price;
    public TextView change;

    public MyViewHolder(View view){
        super(view);
        name = view.findViewById(R.id.nameID);
        symbol = view.findViewById(R.id.symbolID);
        price = view.findViewById(R.id.priceID);
        change = view.findViewById(R.id.changeID);
    }

}

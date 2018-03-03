package com.annabenson.stockwatch;

/**
 * Created by Anna on 2/20/2018.
 */

public class Stock {

    // 4/10
    private String name;
    private String symbol;
    private double price;
    private double change;
    private double percent;

    public Stock(String name, String symbol, double price, double change, double percent) {
        setName( name);
        setSymbol( symbol );
        setPrice( price );
        setChange( change );
        setPercent( percent );
    }

    public Stock(String name, String symbol) {
        setName( name);
        setSymbol( symbol );
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getChange() {
        return change;
    }

    public void setChange(double change) {
        this.change = change;
    }

    public double getPercent() {
        return percent;
    }

    public void setPercent(double percent) {
        this.percent = percent;
    }

    @Override
    public String toString() {
        return "Stock{" +
                "name='" + name + '\'' +
                ", symbol='" + symbol + '\'' +
                ", price=" + price +
                ", change=" + change +
                ", percent=" + percent +
                '}';
    }
}

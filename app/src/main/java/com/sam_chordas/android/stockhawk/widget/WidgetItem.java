package com.sam_chordas.android.stockhawk.widget;

/**
 * Created by bryce on 7/11/16.
 */
public class WidgetItem {
    public String symbol;
    public String change;
    public String percent_change;
    public String price;

    public WidgetItem(String symbol, String change, String percent_change, String price) {
        this.symbol = symbol;
        this.change = change;
        this.percent_change = percent_change;
        this.price = price;
    }
}

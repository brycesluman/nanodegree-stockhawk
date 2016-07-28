package com.sam_chordas.android.stockhawk.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

import java.io.Console;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by bryce on 7/11/16.
 */
public class StockRemoteViewsFactory implements
        RemoteViewsService.RemoteViewsFactory {
    private static int mCount = 0;
    private List<WidgetItem> mWidgetItems = new ArrayList<WidgetItem>();
    private Context mContext;
    private int mAppWidgetId;

    public StockRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    public void onCreate() {

    }

    public void onDestroy() {
        mWidgetItems.clear();
    }

    public int getCount() {
        return mCount;
    }

    public RemoteViews getViewAt(int position) {
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.appwidget_item);
        rv.setTextViewText(R.id.widget_symbol, mWidgetItems.get(position).symbol.toUpperCase());
        String stringFormat = mContext.getResources().getString(R.string.amt_change);
        String amtChange = String.format(stringFormat, mWidgetItems.get(position).change, mWidgetItems.get(position).percent_change);
        rv.setTextViewText(R.id.widget_change, amtChange);
        if (Float.valueOf(mWidgetItems.get(position).change) > 0) {
            rv.setTextColor(R.id.widget_change, mContext.getResources().getColor(R.color.material_green_700));
        } else if (Float.valueOf(mWidgetItems.get(position).change) < 0) {
            rv.setTextColor(R.id.widget_change, mContext.getResources().getColor(R.color.material_red_700));
        }
        rv.setTextViewText(R.id.widget_price, mWidgetItems.get(position).price);
        final Intent fillInIntent = new Intent();
        fillInIntent.putExtra(StockAppWidgetProvider.EXTRA_SYMBOL, mWidgetItems.get(position).symbol);

        rv.setOnClickFillInIntent(R.id.widget_item, fillInIntent);

        return rv;
    }

    public RemoteViews getLoadingView() {
        return null;
    }

    public int getViewTypeCount() {
        return 1;
    }

    public long getItemId(int position) {
        return position;
    }

    public boolean hasStableIds() {
        return true;
    }

    public void onDataSetChanged() {
        Cursor c = mContext.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                new String[]{
                        QuoteColumns.SYMBOL,
                        QuoteColumns.CHANGE,
                        QuoteColumns.PERCENT_CHANGE,
                        QuoteColumns.BIDPRICE
                },
                QuoteColumns.ISCURRENT + " = ?",
                new String[]{"1"}, null);
        if (c != null) {
            mCount = c.getCount();
            while (c.moveToNext()) {
                mWidgetItems.add(new WidgetItem(c.getString(0), c.getString(1), c.getString(2), c.getString(3)));
            }
        }
    }
}

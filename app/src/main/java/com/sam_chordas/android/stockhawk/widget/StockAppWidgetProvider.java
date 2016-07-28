package com.sam_chordas.android.stockhawk.widget;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.ui.DetailActivity;

/**
 * Created by bryce on 7/10/16.
 */
public class StockAppWidgetProvider extends AppWidgetProvider {
    public static final String EXTRA_SYMBOL = "com.sam_chordas.android.stockhawk.EXTRA_SYMBOL";
    public static final String ACTION_OPEN_DETAIL = "com.sam_chordas.android.stockhawk.ACTION_OPEN_DETAIL";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION_OPEN_DETAIL)) {
            String item = intent.getExtras().getString(EXTRA_SYMBOL);
            Uri uri = QuoteProvider.History.withSymbol(item);
            Intent activityIntent = new Intent(context, DetailActivity.class);
            activityIntent.setData(uri);
            context.startActivity(activityIntent);
        }
        super.onReceive(context, intent);
    }

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int N = appWidgetIds.length;

        for (int i = 0; i < appWidgetIds.length; ++i) {

            Intent intent = new Intent(context, StockRemoteViewsService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.appwidget);

            rv.setRemoteAdapter(appWidgetIds[i], R.id.stack_view, intent);

            rv.setEmptyView(R.id.stack_view, R.id.empty_view);

            Intent onItemClick = new Intent(context, StockAppWidgetProvider.class);
            onItemClick.setAction(ACTION_OPEN_DETAIL);
            onItemClick.setData(Uri.parse(onItemClick
                    .toUri(Intent.URI_INTENT_SCHEME)));
            PendingIntent onClickPendingIntent = PendingIntent
                    .getBroadcast(context, 0, onItemClick,
                            PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setPendingIntentTemplate(R.id.stack_view,
                    onClickPendingIntent);

            appWidgetManager.updateAppWidget(appWidgetIds[i], rv);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

}

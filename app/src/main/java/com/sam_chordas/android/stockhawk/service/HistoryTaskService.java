package com.sam_chordas.android.stockhawk.service;

import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.sam_chordas.android.stockhawk.data.HistoricalDataColumns;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.ui.DetailActivity;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by bryce on 6/10/16.
 */
public class HistoryTaskService extends GcmTaskService{
    private String LOG_TAG = HistoryTaskService.class.getSimpleName();

    private OkHttpClient client = new OkHttpClient();
    private Context mContext;
    private StringBuilder mStoredSymbols = new StringBuilder();
    private boolean isUpdate;

    public HistoryTaskService(){}

    public HistoryTaskService(Context context){
        mContext = context;
    }
    String fetchData(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }


    @Override
    public int onRunTask(TaskParams params){
        Cursor initQueryCursor;
        if (mContext == null){
            mContext = this;
        }
        Bundle extrasBundle = params.getExtras();
        StringBuilder urlStringBuilder = new StringBuilder();
        try{
            // Base URL for the Yahoo query
            urlStringBuilder.append("http://query.yahooapis.com/v1/public/yql?q=");
            urlStringBuilder.append(URLEncoder.encode("select * from yahoo.finance.historicaldata where symbol = \"", "UTF-8"));
            urlStringBuilder.append(URLEncoder.encode(extrasBundle.getString(DetailActivity.SYMBOL), "UTF-8"));
            urlStringBuilder.append(URLEncoder.encode("\" AND startDate = \"" +
                    extrasBundle.getString(DetailActivity.START_DATE) +
                    "\" AND endDate = \"" +
                    URLEncoder.encode(extrasBundle.getString(DetailActivity.END_DATE), "UTF-8")));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // finalize the URL for the API query.
        //urlStringBuilder.append("\"%20and%20startDate%20%3D%20%222015-06-06%22%20and%20endDate%20%3D%20%222016-01-01%22&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=");
        urlStringBuilder.append("\"&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=");

        String urlString;
        String getResponse;
        int result = GcmNetworkManager.RESULT_FAILURE;

        if (urlStringBuilder != null){
            urlString = urlStringBuilder.toString();
            try{
                Log.d("HistoryTaskService", urlString);
                getResponse = fetchData(urlString);
                result = GcmNetworkManager.RESULT_SUCCESS;
                try {
                    ContentValues contentValues = new ContentValues();
                    // update ISCURRENT to 0 (false) so new data is current
                    if (isUpdate){
                        contentValues.put(QuoteColumns.ISCURRENT, 0);
                        mContext.getContentResolver().update(QuoteProvider.History.CONTENT_URI, contentValues,
                                null, null);
                    }
                    mContext.getContentResolver().delete(QuoteProvider.History.CONTENT_URI,
                            HistoricalDataColumns.SYMBOL + " = ?",
                            new String[]{extrasBundle.getString(DetailActivity.SYMBOL)}
                            );
                    mContext.getContentResolver().applyBatch(QuoteProvider.AUTHORITY,
                            Utils.historyJsonToContentVals(getResponse));
                } catch (RemoteException | OperationApplicationException e){
                    Log.e(LOG_TAG, "Error applying batch insert", e);
                }
            } catch (IOException e){
                e.printStackTrace();
            }
        }

        return result;
    }
}

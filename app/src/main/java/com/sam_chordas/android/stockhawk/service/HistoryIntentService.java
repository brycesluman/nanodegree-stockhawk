package com.sam_chordas.android.stockhawk.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.TaskParams;
import com.sam_chordas.android.stockhawk.ui.DetailActivity;

/**
 * Created by sam_chordas on 10/1/15.
 */
public class HistoryIntentService extends IntentService {

    public HistoryIntentService() {
        super(HistoryIntentService.class.getName());
    }

    public HistoryIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(HistoryIntentService.class.getSimpleName(), "History Intent Service");
        HistoryTaskService historyTaskService = new HistoryTaskService(this);
        Bundle args = new Bundle();
        args.putString(DetailActivity.SYMBOL, intent.getStringExtra(DetailActivity.SYMBOL));
        args.putString(DetailActivity.START_DATE, intent.getStringExtra(DetailActivity.START_DATE));
        args.putString(DetailActivity.END_DATE, intent.getStringExtra(DetailActivity.END_DATE));
        // We can call OnRunTask from the intent service to force it to run immediately instead of
        // scheduling a task.
        historyTaskService.onRunTask(new TaskParams(intent.getStringExtra("tag"), args));
    }
}

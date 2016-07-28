package com.sam_chordas.android.stockhawk.ui;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.HistoricalDataColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.service.HistoryIntentService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private Intent mServiceIntent;
    public static final String ARG_URI = "uri";
    private static final int CURSOR_LOADER_ID = 1001;
    private Uri mUri;
    private LineChart mChart;
    private View mProgress;
    private static final long ONE_DAY = 1000 * 60 * 60 * 24;
    private static final long ONE_YEAR = ONE_DAY * 365;

    public DetailActivityFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (getArguments().containsKey(ARG_URI)) {
            mUri = getArguments().getParcelable(ARG_URI);

            if (mUri != null) {
                Log.d("DetailActivityFragment", mUri.toString());
            }
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            mServiceIntent = new Intent(getActivity(), HistoryIntentService.class);
            mServiceIntent.putExtra(DetailActivity.SYMBOL, QuoteProvider.History.getSymbolFromUri(mUri));
            mServiceIntent.putExtra(DetailActivity.START_DATE, String.valueOf(dateFormat.format(Calendar.getInstance().getTimeInMillis() - (ONE_YEAR))));
            mServiceIntent.putExtra(DetailActivity.END_DATE, String.valueOf(dateFormat.format(Calendar.getInstance().getTimeInMillis())));
        }
        if (savedInstanceState == null) {
            if (Utils.isConnected(getActivity())) {
                getActivity().startService(mServiceIntent);
            } else {
                networkToast();
            }

        }

        View view = inflater.inflate(R.layout.fragment_detail, container, false);
        mChart = (LineChart) view.findViewById(R.id.chart);

        mChart.setVisibility(View.GONE);
        mProgress = (View) view.findViewById(R.id.progress_indicator);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getActivity().getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getContext(), mUri,
                new String[]{HistoricalDataColumns._ID,
                        HistoricalDataColumns.SYMBOL,
                        HistoricalDataColumns.DATE_STRING,
                        HistoricalDataColumns.CLOSE
                },
                null,
                null,
                HistoricalDataColumns.DATE_STRING + " ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d("DetailActivityFragment", "onLoadFinished: " + data.getCount());
        if (loader.getId() == CURSOR_LOADER_ID && data.getCount() > 0) {
            mChart.invalidate();
            mChart.notifyDataSetChanged();
            mChart.setVisibility(View.VISIBLE);
            mProgress.setVisibility(View.GONE);

            Log.d("DetailActivityFragment", "COUNT: " + data.getCount());
            ArrayList<String> xNewData = new ArrayList<String>();
            ArrayList<Entry> yNewData = new ArrayList<Entry>();
            for (data.moveToFirst(); !data.isAfterLast(); data.moveToNext()) {
                xNewData.add(data.getString(data.getColumnIndex(HistoricalDataColumns.DATE_STRING)));
                yNewData.add(new Entry(data.getFloat(data.getColumnIndex(HistoricalDataColumns.CLOSE)), data.getPosition()));
            }

            LineDataSet dataSet = new LineDataSet(yNewData, getActivity().getString(R.string.closing_values));
            dataSet.setColor(getResources().getColor(R.color.white));
            dataSet.setDrawCircles(false);

            LineData lineData = new LineData(xNewData, dataSet);
            mChart.getAxisLeft().setTextColor(getResources().getColor(R.color.white));
            mChart.getAxisRight().setTextColor(getResources().getColor(R.color.white));
            mChart.getXAxis().setTextColor(getResources().getColor(R.color.white));
            mChart.getLegend().setTextColor(getResources().getColor(R.color.white));
            lineData.setDrawValues(false);
            mChart.setData(lineData);
            mChart.setDescription("");

            data.close();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(DetailActivityFragment.ARG_URI, mUri);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    public void networkToast() {
        Toast.makeText(getActivity(), getString(R.string.network_toast), Toast.LENGTH_SHORT).show();
    }
}

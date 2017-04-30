package com.udacity.stockhawk.ui;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {


    @BindView(R.id.chart1)
    LineChart mChart;

    private static final int ID_STOCK_DETAIL_LOADER = 1;
    private static final int CHART_ANIMATE_TIME = 1000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        getSupportLoaderManager().initLoader(ID_STOCK_DETAIL_LOADER,null,this);
        ButterKnife.bind(this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                getIntent().getData(),
                Contract.Quote.QUOTE_COLUMNS,
                null,
                null,
                Contract.Quote.COLUMN_SYMBOL);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //check if data is not null and construct the LineChart
        if (data!=null && data.moveToFirst()){
            String symbol = data.getString(Contract.Quote.POSITION_SYMBOL);
            String history = data.getString(Contract.Quote.POSITION_HISTORY);
            setTitle(symbol);
            Description desc = new Description();
            desc.setText(getString(R.string.chart_description));
            desc.setTextColor(Color.WHITE);
            mChart.setDescription(desc);
            mChart.setDrawGridBackground(true);
            mChart.setGridBackgroundColor(getResources().getColor(R.color.chart_background));

            String[] historicQuoytes= history.split("\\n");
            drawChart(historicQuoytes);

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public void drawChart(String[] historicData){
        //reverse order for incremental historic
        Collections.reverse(Arrays.asList(historicData));
        final String[] xValues = new String[historicData.length];
        ArrayList<Entry> values = new ArrayList<Entry>();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yy-MM-dd", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();

        for(int i=0;i<historicData.length;i++){
            String[] historyValue = historicData[i].split(",");
            values.add(new Entry((float)i, Float.valueOf(historyValue[1])));
            calendar.setTimeInMillis(Long.valueOf(historyValue[0]));
            xValues[i] = dateFormat.format(calendar.getTime());
        }

        LineDataSet dataSet = new LineDataSet(values, getString(R.string.chart_label));
        dataSet.setFormLineWidth(1f);
        dataSet.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
        dataSet.setFormSize(15.f);
        LineData lineData = new LineData(dataSet);
        mChart.setData(lineData);
        mChart.invalidate(); // refresh

        //Show date values en X axis
        IAxisValueFormatter formatter = new IAxisValueFormatter() {

            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return xValues[(int) value];
            }
        };

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setValueFormatter(formatter);

        YAxis left = mChart.getAxisLeft();
        left.setEnabled(true);
        left.setLabelCount(10, true);
        left.setTextColor(Color.WHITE);
        mChart.getAxisRight().setEnabled(false);
        mChart.animateX(CHART_ANIMATE_TIME);
    }
}

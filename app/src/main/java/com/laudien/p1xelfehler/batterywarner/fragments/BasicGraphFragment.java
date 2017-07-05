package com.laudien.p1xelfehler.batterywarner.fragments;

import android.content.ContentResolver;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.ColorUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LabelFormatter;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.Series;
import com.laudien.p1xelfehler.batterywarner.R;
import com.laudien.p1xelfehler.batterywarner.data.GraphContract;
import com.laudien.p1xelfehler.batterywarner.helper.ToastHelper;

import java.util.Locale;

import static android.widget.Toast.LENGTH_SHORT;

/**
 * Super class of all Fragments that are using the charging curve.
 */
public abstract class BasicGraphFragment extends Fragment {
    private static final int MAX_DATA_POINTS = 300;
    /**
     * An instance of the GraphInfo holding information about the charging curve.
     */
    GraphInfo graphInfo;
    /**
     * The GraphView where the graphs are shown
     */
    GraphView graphView;
    /**
     * Checkbox which turns the percentage graph on and off.
     */
    Switch switch_percentage;
    /**
     * Checkbox which turns the temperature graph on and off.
     */
    Switch switch_temp;
    /**
     * TextView that contains the title over the GraphView.
     */
    TextView textView_title;
    /**
     * TextView that contains the charging time.
     */
    TextView textView_chargingTime;
    /**
     * The percentage graph that is displayed in the GraphView.
     */
    LineGraphSeries<DataPoint> graph_percentage;
    /**
     * The temperature graph that is displayed in the GraphView.
     */
    LineGraphSeries<DataPoint> graph_temperature;
    private final CompoundButton.OnCheckedChangeListener onSwitchChangedListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
            Series s = null;
            if (compoundButton == switch_percentage) {
                s = graph_percentage;
            } else if (compoundButton == switch_temp) {
                s = graph_temperature;
            }
            if (s != null) {
                if (checked) {
                    graphView.addSeries(s);
                } else {
                    graphView.removeSeries(s);
                }
            }
        }
    };
    private long startTime, endTime;
    private int color_percentage;
    private int color_percentageBackground;
    private int color_temperature;
    private byte labelCounter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        loadGraphColors();
        View view = inflater.inflate(R.layout.fragment_graph, container, false);
        graphView = view.findViewById(R.id.graphView);
        switch_percentage = view.findViewById(R.id.switch_percentage);
        switch_percentage.setOnCheckedChangeListener(onSwitchChangedListener);
        switch_temp = view.findViewById(R.id.switch_temp);
        switch_temp.setOnCheckedChangeListener(onSwitchChangedListener);
        textView_title = view.findViewById(R.id.textView_title);
        textView_chargingTime = view.findViewById(R.id.textView_chargingTime);
        initGraphView();
        graphView.getGridLabelRenderer().setLabelFormatter(getLabelFormatter());
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        graphView.removeAllSeries();
        loadGraphs();
    }

    /**
     * Method that provides an array of the graphs that should be displayed.
     *
     * @return Returns an array of graphs.
     */
    protected abstract Uri getUri();

    /**
     * Method that loads the graph from the database into the GraphView
     * and sets the text of the TextView that shows the time.
     * You can override it to only do it under some conditions
     * (for example only allow it for the pro version).
     */
    void loadGraphs() {
        ContentResolver contentResolver = getContext().getContentResolver();
        Cursor mCursor = contentResolver.query(
                getUri(),
                null,
                null,
                null,
                GraphContract.GraphEntry.COLUMN_TIME
        );
        // load graphs from Cursor
        if (mCursor != null && mCursor.getCount() > 0) {
            mCursor.moveToFirst();
            graph_percentage = new LineGraphSeries<>();
            graph_temperature = new LineGraphSeries<>();
            double time, temperature;
            int percentage;
            long firstTime = mCursor.getLong(0);
            startTime = mCursor.getLong(mCursor.getColumnIndex(GraphContract.GraphEntry.COLUMN_TIME));
            do {
                endTime = mCursor.getLong(0);
                time = (double) (endTime - firstTime) / 60000;
                percentage = mCursor.getInt(1);
                temperature = (double) mCursor.getInt(2) / 10;
                graph_percentage.appendData(
                        new DataPoint(time, percentage),
                        true,
                        MAX_DATA_POINTS
                );
                graph_temperature.appendData(
                        new DataPoint(time, temperature),
                        true,
                        MAX_DATA_POINTS
                );
            } while (mCursor.moveToNext());
            mCursor.close();
            // set graph colors
            graph_percentage.setDrawBackground(true);
            graph_percentage.setColor(color_percentage);
            graph_percentage.setBackgroundColor(color_percentageBackground);
            graph_temperature.setColor(color_temperature);
            // add graphs to GraphView
            if (switch_percentage.isChecked()) {
                graphView.addSeries(graph_percentage);
            }
            if (switch_temp.isChecked()) {
                graphView.addSeries(graph_temperature);
            }
            createOrUpdateInfoObject();
            double highestValue = graph_percentage.getHighestValueX();
            if (highestValue > 0) {
                graphView.getViewport().setMaxX(highestValue);
            } else {
                graphView.getViewport().setMaxX(1);
            }
        } else {
            graph_temperature = null;
            graph_percentage = null;
            graphView.getViewport().setMaxX(1);
        }
        setTimeText();
    }

    /**
     * Creates a new or updates the existing instance of the GraphInfo that is used to store
     * information about the graphs.
     */
    private void createOrUpdateInfoObject() {
        if (graphInfo == null) {
            graphInfo = new GraphInfo(
                    startTime,
                    endTime,
                    graph_percentage.getHighestValueX(),
                    graph_temperature.getHighestValueY(),
                    graph_temperature.getLowestValueY(),
                    graph_percentage.getHighestValueY() - graph_percentage.getLowestValueY()
            );
        } else {
            graphInfo.updateValues(
                    startTime,
                    endTime,
                    graph_percentage.getHighestValueX(),
                    graph_temperature.getHighestValueY(),
                    graph_temperature.getLowestValueY(),
                    graph_percentage.getHighestValueY() - graph_percentage.getLowestValueY()
            );
        }
    }

    /**
     * Sets the text of the textView_chargingTime TextView to the charging time.
     */
    void setTimeText() {
        if (graphInfo != null) {
            textView_chargingTime.setText(String.format(
                    Locale.getDefault(),
                    "%s: %s",
                    getString(R.string.info_charging_time),
                    graphInfo.getTimeString(getContext())
            ));
        }
    }

    /**
     * Reloads the graphs from the database.
     */
    void reload() {
        graphView.removeAllSeries();
        loadGraphs();
    }

    private void loadGraphColors() {
        boolean darkThemeEnabled = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(getString(R.string.pref_dark_theme_enabled), getResources().getBoolean(R.bool.pref_dark_theme_enabled_default));
        // percentage
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getContext().getTheme();
        theme.resolveAttribute(R.attr.colorAccent, typedValue, true);
        color_percentage = typedValue.data;
        color_percentageBackground = ColorUtils.setAlphaComponent(color_percentage, 64);
        // temperature
        if (darkThemeEnabled) { // dark theme
            color_temperature = Color.GREEN;
        } else { // default theme
            theme.resolveAttribute(R.attr.colorPrimary, typedValue, true);
            color_temperature = typedValue.data;
        }
    }

    /**
     * Provides the format of the text of the x and y axis of the graph.
     *
     * @return Returns a LabelFormatter that is used in the GraphView.
     */
    private LabelFormatter getLabelFormatter() {
        return new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) { // X-axis (time)
                    if (value == 0) {
                        labelCounter = 1;
                        return "0 min";
                    }
                    if (value < 0.1) {
                        return "";
                    }
                    if (labelCounter++ % 3 == 0)
                        return super.formatLabel(value, true) + " min";
                    return "";
                } else if (switch_percentage.isChecked() ^ switch_temp.isChecked()) { // Y-axis (percent)
                    if (switch_percentage.isChecked())
                        return super.formatLabel(value, false) + "%";
                    if (switch_temp.isChecked())
                        return super.formatLabel(value, false) + "°C";
                }
                return super.formatLabel(value, false);
            }
        };
    }

    /**
     * Initializes the ViewPort of the GraphView. Sets the part of the x and y axis that is shown.
     */
    private void initGraphView() {
        Viewport viewport = graphView.getViewport();
        viewport.setXAxisBoundsManual(true);
        viewport.setYAxisBoundsManual(true);
        viewport.setMinX(0);
        viewport.setMaxY(100);
        viewport.setMinY(0);
    }

    /**
     * Shows the info dialog defined in the GraphInfo. Shows a toast if there are no graphs or
     * if there is no GraphInfo.
     */
    public void showInfo() {
        if (graph_temperature != null && graph_percentage != null && graphInfo != null) {
            graphInfo.showDialog(getContext());
        } else {
            ToastHelper.sendToast(getContext(), R.string.toast_no_data, LENGTH_SHORT);
        }
    }
}

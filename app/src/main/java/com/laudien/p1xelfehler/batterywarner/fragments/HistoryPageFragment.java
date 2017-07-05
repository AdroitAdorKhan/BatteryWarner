package com.laudien.p1xelfehler.batterywarner.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.laudien.p1xelfehler.batterywarner.data.GraphContract;

import java.io.File;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * Fragment that loads a charging curve from a file path given in the arguments.
 * Provides some functionality to change or remove the file.
 */
public class HistoryPageFragment extends BasicGraphFragment {
    /**
     * The key for the file path in the argument bundle.
     */
    public static final String EXTRA_FILE_PATH = "filePath";
    private File file;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.containsKey(EXTRA_FILE_PATH)) {
                String filePath = bundle.getString(EXTRA_FILE_PATH);
                if (filePath != null) {
                    file = new File(filePath);
                }
            }
        }
        View view = super.onCreateView(inflater, container, savedInstanceState);
        textView_title.setVisibility(View.GONE);
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(EXTRA_FILE_PATH)) {
                String filePath = savedInstanceState.getString(EXTRA_FILE_PATH);
                if (filePath != null && !filePath.equals("")) {
                    file = new File(filePath);
                }
            }
        }
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(EXTRA_FILE_PATH, file.getPath());
    }

    @Override
    protected Uri getUri() {
        return GraphContract.GraphEntry.URI_GRAPH_HISTORY.buildUpon().appendPath(file.getName()).build();
    }

    @Override
    void loadGraphs() {
        if (ContextCompat.checkSelfPermission(getContext(), WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED) {
            super.loadGraphs();
        }
    }
}

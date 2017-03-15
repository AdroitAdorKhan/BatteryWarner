package com.laudien.p1xelfehler.batterywarner.Activities.HistoryActivity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.io.File;
import java.util.ArrayList;

import static com.laudien.p1xelfehler.batterywarner.Activities.HistoryActivity.HistoryPageFragment.EXTRA_FILE_PATH;

/**
 * A FragmentStatePagerAdapter that is used by the HistoryFragment to load HistoryPageFragments
 * into a ViewPager.
 */
class HistoryPagerAdapter extends FragmentStatePagerAdapter {
    private ArrayList<File> files;

    HistoryPagerAdapter(FragmentManager fm, ArrayList<File> files) {
        super(fm);
        this.files = files;
    }

    @Override
    public Fragment getItem(int position) {
        HistoryPageFragment fragment = new HistoryPageFragment();
        Bundle bundle = new Bundle(1);
        bundle.putString(EXTRA_FILE_PATH, files.get(position).getPath());
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int getCount() {
        return files.size();
    }

    @Override
    public int getItemPosition(Object object) {
        /*HistoryPageFragment fragment = (HistoryPageFragment) object;
        String filePath = fragment.getFile().getPath();
        int position = files.indexOf(new File(filePath));
        if (position >= 0) {
            return position;
        } else {
            return POSITION_NONE;
        }*/
        return POSITION_NONE;
    }

    boolean removeItem(int position) {
        File file = files.get(position);
        if (file.delete()) {
            files.remove(file);
            notifyDataSetChanged();
            return true;
        } else {
            return false;
        }
    }

    File getFile(int position) {
        return files.get(position);
    }
}

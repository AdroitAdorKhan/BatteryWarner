package com.laudien.p1xelfehler.batterywarner.Activities.MainActivity;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.laudien.p1xelfehler.batterywarner.HelperClasses.ImageHelper;
import com.laudien.p1xelfehler.batterywarner.R;

import static com.laudien.p1xelfehler.batterywarner.Activities.MainActivity.BatteryInfoFragment.COLOR_HIGH;
import static com.laudien.p1xelfehler.batterywarner.Activities.MainActivity.BatteryInfoFragment.COLOR_LOW;
import static com.laudien.p1xelfehler.batterywarner.Activities.MainActivity.BatteryInfoFragment.COLOR_OK;

/**
 * Fragment that shows some information about the current battery status. Refreshes automatically.
 * Contains a button for toggling all warnings or logging of the app.
 */
public class MainPageFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_page, container, false);
        final ImageView img_battery = (ImageView) view.findViewById(R.id.img_battery);
        BatteryInfoFragment infoFragment = (BatteryInfoFragment) getChildFragmentManager().findFragmentById(R.id.fragment_battery_info);
        infoFragment.setOnBatteryColorChangedListener(new BatteryInfoFragment.OnBatteryColorChangedListener() {
            @Override
            public void onColorChanged(byte colorID) {
                switch (colorID){
                    case COLOR_LOW:
                        ImageHelper.setImageColor(getContext().getResources().getColor(R.color.colorBatteryLow), img_battery);
                        break;
                    case COLOR_OK:
                        ImageHelper.setImageColor(getContext().getResources().getColor(R.color.colorBatteryOk), img_battery);
                        break;
                    case COLOR_HIGH:
                        ImageHelper.setImageColor(getContext().getResources().getColor(R.color.colorBatteryHigh), img_battery);
                        break;
                }
            }
        });
        return view;
    }
}

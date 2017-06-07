package com.laudien.p1xelfehler.batterywarner.services;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.laudien.p1xelfehler.batterywarner.AppInfoHelper;

public class GoToProService extends IntentService {
    public GoToProService() {
        super(null);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + AppInfoHelper.PACKAGE_NAME_PRO)));
        } catch (android.content.ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + AppInfoHelper.PACKAGE_NAME_PRO)));
        }
    }
}

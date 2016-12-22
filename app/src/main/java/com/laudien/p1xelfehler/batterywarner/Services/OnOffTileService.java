package com.laudien.p1xelfehler.batterywarner.Services;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import com.laudien.p1xelfehler.batterywarner.Contract;
import com.laudien.p1xelfehler.batterywarner.Fragments.OnOffFragment;
import com.laudien.p1xelfehler.batterywarner.R;

@RequiresApi(api = Build.VERSION_CODES.N)
public class OnOffTileService extends TileService {

    private static final String TAG = "OnOffTileService";
    private Tile tile;

    @Override
    public void onTileAdded() {
        super.onTileAdded();
        tile = getQsTile();
        if (!Contract.IS_PRO) {
            Toast.makeText(getApplicationContext(), getString(R.string.not_pro), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        tile = getQsTile();
        // load state from shared preferences
        if (Contract.IS_PRO) {
            SharedPreferences sharedPreferences = getSharedPreferences(Contract.SHARED_PREFS, MODE_PRIVATE);
            boolean isEnabled = sharedPreferences.getBoolean(Contract.PREF_IS_ENABLED, true);
            if (isEnabled)
                tile.setState(Tile.STATE_ACTIVE);
            else
                tile.setState(Tile.STATE_INACTIVE);

        } else {
            tile.setState(Tile.STATE_INACTIVE);
        }
        tile.updateTile();
    }

    @Override
    public void onClick() {
        super.onClick();
        if (!Contract.IS_PRO) {
            Toast.makeText(getApplicationContext(), getString(R.string.not_pro), Toast.LENGTH_SHORT).show();
            return;
        }
        boolean isActive = tile.getState() == Tile.STATE_ACTIVE;
        SharedPreferences sharedPreferences = getSharedPreferences(Contract.SHARED_PREFS, MODE_PRIVATE);
        if (isActive) { // disable battery warnings
            tile.setState(Tile.STATE_INACTIVE);
            Toast.makeText(getApplicationContext(), getString(R.string.disabled_info), Toast.LENGTH_SHORT).show();
            Log.i(TAG, "Tile deactivated!");
        } else { // enable battery warnings
            tile.setState(Tile.STATE_ACTIVE);
            Toast.makeText(getApplicationContext(), getString(R.string.enabled_info), Toast.LENGTH_SHORT).show();
            Log.i(TAG, "Tile activated!");
        }
        sharedPreferences.edit().putBoolean(Contract.PREF_IS_ENABLED, !isActive).apply();
        tile.updateTile();

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(OnOffFragment.BROADCAST_ACTION);
        sendBroadcast(broadcastIntent);
    }
}

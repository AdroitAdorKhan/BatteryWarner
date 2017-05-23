package com.laudien.p1xelfehler.batterywarner

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat

import com.laudien.p1xelfehler.batterywarner.fragments.HistoryFragment.HistoryFragment

/**
 * Activity that shows all the charging curves that were saved.
 * It is the frame for the HistoryFragment and only loads the fragment if the external storage
 * permission is given. If not, it asks for the permission and loads the fragment if the
 * user allows it.
 */
class HistoryActivity : BaseActivity() {
    private val PERMISSION_REQUEST_CODE = 60

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.frame_layout)
        setToolbarTitle(getString(R.string.title_history))
        // check for permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_CODE
            )
        }
        loadFragment()
    }

    private fun loadFragment() {
        supportFragmentManager.beginTransaction().replace(R.id.container_layout, HistoryFragment()).commit()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    finish()
                    return
                }
            }
            loadFragment()
        }
    }
}

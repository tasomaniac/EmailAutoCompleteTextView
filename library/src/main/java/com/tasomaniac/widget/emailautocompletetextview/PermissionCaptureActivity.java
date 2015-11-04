package com.tasomaniac.widget.emailautocompletetextview;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;

@TargetApi(Build.VERSION_CODES.M)
public class PermissionCaptureActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final String permission = getIntent().getStringExtra(BackgroundPermissionManager.EXTRA_PERMISSION);
        if (permission == null) {
            throw new IllegalArgumentException("Permission should be provided with " +
                    "com.tasomaniac.widget.PERMISSION key.");
        }

        if (!getIntent().getBooleanExtra(BackgroundPermissionManager.EXTRA_FORCE_PERMISSION, false)
                && ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            sendPermissionRationaleBroadcast(permission, true);
            finish();
        } else {

            ActivityCompat.requestPermissions(this, new String[]{permission},
                    getIntent().getIntExtra(BackgroundPermissionManager.EXTRA_REQUEST_CODE, Integer.MAX_VALUE));
        }

    }

    private void sendPermissionRationaleBroadcast(String permission, boolean value) {
        final Intent intent = new Intent(PermissionCallbacks.ACTION_PERMISSION_SHOW_RATIONALE);
        intent.putExtra(BackgroundPermissionManager.EXTRA_PERMISSION, permission);
        intent.putExtra(BackgroundPermissionManager.EXTRA_SHOW_RATIONALE, value);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            // if permission was denied check if we should ask again in the future (i.e. they
            // did not check 'never ask again')
            String permission = getIntent().getStringExtra(BackgroundPermissionManager.EXTRA_PERMISSION);
            if (shouldShowRequestPermissionRationale(permission)) {
                sendPermissionRationaleBroadcast(permission, true);
            } else {
                sendPermissionRationaleBroadcast(permission, false);
            }
        }

        final Intent intent = new Intent(PermissionCallbacks.ACTION_PERMISSION_RESULT);
        intent.putExtra(BackgroundPermissionManager.EXTRA_REQUEST_CODE, requestCode);
        intent.putExtra(BackgroundPermissionManager.EXTRA_PERMISSIONS, permissions);
        intent.putExtra(BackgroundPermissionManager.EXTRA_GRANT_RESULTS, grantResults);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        finish();
    }
}
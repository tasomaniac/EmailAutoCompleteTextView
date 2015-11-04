package com.tasomaniac.widget.emailautocompletetextview;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

public class BackgroundPermissionManager {

    public static final String EXTRA_REQUEST_CODE = "com.tasomaniac.widget.REQUEST_CODE";
    public static final String EXTRA_PERMISSION = "com.tasomaniac.widget.PERMISSION";
    public static final String EXTRA_PERMISSIONS = "com.tasomaniac.widget.PERMISSIONS";
    public static final String EXTRA_GRANT_RESULTS = "com.tasomaniac.widget.GRANT_RESULTS";
    public static final String EXTRA_SHOW_RATIONALE = "com.tasomaniac.widget.SHOW_RATIONALE";
    public static final String EXTRA_FORCE_PERMISSION = "com.tasomaniac.widget.FORCE_PERMISSION";

    private final PermissionCallbacks callbacks;
    private final Context context;

    private IntentFilter mIntentFilter;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();
            if (PermissionCallbacks.ACTION_PERMISSION_RESULT.equals(action)) {
                callbacks.onRequestPermissionsResult(intent.getIntExtra(BackgroundPermissionManager.EXTRA_REQUEST_CODE, 0),
                        intent.getStringArrayExtra(BackgroundPermissionManager.EXTRA_PERMISSIONS),
                        intent.getIntArrayExtra(BackgroundPermissionManager.EXTRA_GRANT_RESULTS));
            } else if (PermissionCallbacks.ACTION_PERMISSION_SHOW_RATIONALE.equals(action)) {
                callbacks.onShowRequestPermissionRationale(intent.getStringExtra(BackgroundPermissionManager.EXTRA_PERMISSION),
                        intent.getBooleanExtra(BackgroundPermissionManager.EXTRA_SHOW_RATIONALE, false));
            }
        }
    };

    public BackgroundPermissionManager(PermissionCallbacks callbacks, Context context) {
        this.callbacks = callbacks;
        this.context = context;

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(PermissionCallbacks.ACTION_PERMISSION_RESULT);
        mIntentFilter.addAction(PermissionCallbacks.ACTION_PERMISSION_SHOW_RATIONALE);
    }

    public void resume() {
        LocalBroadcastManager.getInstance(context)
                .registerReceiver(mReceiver, mIntentFilter);
    }

    public void pause() {
        LocalBroadcastManager.getInstance(context)
                .unregisterReceiver(mReceiver);
    }

    public static void requestPermission(final @NonNull Context context,
                                         final @NonNull String permission, final int requestCode) {
        requestPermission(context, permission, requestCode, false);
    }

    public static void requestPermission(final @NonNull Context context,
                                         final @NonNull String permission, final int requestCode, boolean force) {

        Intent intent = new Intent(context, PermissionCaptureActivity.class);
        intent.putExtra(EXTRA_PERMISSION, permission);
        intent.putExtra(EXTRA_REQUEST_CODE, requestCode);
        intent.putExtra(EXTRA_FORCE_PERMISSION, force);
        context.startActivity(intent);
    }


}

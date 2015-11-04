package com.tasomaniac.widget.emailautocompletetextview;

import android.support.annotation.NonNull;

public interface PermissionCallbacks {

    String ACTION_PERMISSION_RESULT = "com.tasomaniac.widget.ACTION_PERMISSION_RESULT";
    String ACTION_PERMISSION_SHOW_RATIONALE = "com.tasomaniac.widget.ACTION_PERMISSION_SHOW_RATIONALE";

    void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults);

    void onShowRequestPermissionRationale(@NonNull String permission, boolean showRationale);
}

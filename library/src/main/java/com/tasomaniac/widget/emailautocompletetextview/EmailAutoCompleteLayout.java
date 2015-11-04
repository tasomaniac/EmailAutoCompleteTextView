package com.tasomaniac.widget.emailautocompletetextview;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.HashSet;

public class EmailAutoCompleteLayout extends LinearLayout
        implements View.OnFocusChangeListener, PermissionCallbacks {

    private static final int PERMISSIONS_REQUEST_GET_ACCOUNTS = 0;

    private ArrayAdapter<String> adapter;
    protected HashSet<String> accounts = new HashSet<>();

    private AutoCompleteTextView autoCompleteTextView;
    private CheckBox permissionPrimer;

    private BackgroundPermissionManager backgroundPermissionManager;

    public EmailAutoCompleteLayout(Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    public EmailAutoCompleteLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public EmailAutoCompleteLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public EmailAutoCompleteLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        backgroundPermissionManager.resume();
        setupAccountAutocomplete();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        backgroundPermissionManager.pause();
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, final int defStyleRes) {
        backgroundPermissionManager = new BackgroundPermissionManager(this, context);

        inflate(context, R.layout.layout_email_autocomplete, this);
        setOrientation(LinearLayout.VERTICAL);
        autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.autocomplete);
        permissionPrimer = (CheckBox) findViewById(R.id.permission_primer);

        autoCompleteTextView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        autoCompleteTextView.setOnFocusChangeListener(this);
    }

    private void setupAccountAutocomplete() {

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.GET_ACCOUNTS) ==
                PackageManager.PERMISSION_GRANTED) {
            permissionPrimer.setVisibility(View.GONE);

            adapter = createEmailAddressAdapter();
            autoCompleteTextView.setAdapter(adapter);
        } else {
            setupPermissionPrimer();
        }
    }

    private void setupPermissionPrimer() {
        permissionPrimer.setChecked(false);
        permissionPrimer.setVisibility(View.VISIBLE);
        permissionPrimer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    BackgroundPermissionManager.requestPermission(getContext(),
                            Manifest.permission.GET_ACCOUNTS,
                            PERMISSIONS_REQUEST_GET_ACCOUNTS, true);
                }
            }
        });
    }

    private ArrayAdapter<String> createEmailAddressAdapter() {
        Account[] deviceAccounts = AccountManager.get(getContext()).getAccounts();
        for (Account account : deviceAccounts) {
            if (isEmailAddress(account.name)) {
                accounts.add(account.name);
            }
        }

        return new ArrayAdapter<>(getContext(),
                android.R.layout.select_dialog_item,
                new ArrayList<>(accounts));
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus && ViewCompat.isAttachedToWindow(this)) {
            autoCompleteTextView.showDropDown();
        }
    }

    private static boolean isEmailAddress(String possibleEmail) {
        return Patterns.EMAIL_ADDRESS.matcher(possibleEmail).matches();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == PERMISSIONS_REQUEST_GET_ACCOUNTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupAccountAutocomplete();
                autoCompleteTextView.requestFocus();
                autoCompleteTextView.showDropDown();
                permissionPrimer.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onShowRequestPermissionRationale(@NonNull String permission, boolean showRationale) {
        permissionPrimer.setChecked(false);
        permissionPrimer.setVisibility(showRationale ? View.VISIBLE : View.GONE);
    }
}
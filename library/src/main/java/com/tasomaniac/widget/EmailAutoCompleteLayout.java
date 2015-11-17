package com.tasomaniac.widget;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.HashSet;


public class EmailAutoCompleteLayout extends BaseEmailAutoCompleteLayout
        implements View.OnFocusChangeListener, PermissionCallbacks {

    private static final int PERMISSIONS_REQUEST_GET_ACCOUNTS = 0;

    private ArrayAdapter<String> adapter;
    protected HashSet<String> accounts = new HashSet<>();

    private AutoCompleteTextView autoCompleteTextView;
    private final CheckBox permissionPrimer;

    private final BackgroundPermissionManager backgroundPermissionManager;

    public EmailAutoCompleteLayout(Context context) {
        this(context, null, 0, 0);
    }

    public EmailAutoCompleteLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0, 0);
    }

    public EmailAutoCompleteLayout(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs, defStyle, 0);
    }

    public EmailAutoCompleteLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        // Can't call through to super(Context, AttributeSet, int) since it doesn't exist on API 10
        super(context, attrs);

        backgroundPermissionManager = new BackgroundPermissionManager(this, context);

//        inflate(context, R.layout.layout_email_autocomplete, this);
        setOrientation(LinearLayout.VERTICAL);
        setAddStatesFromChildren(true);

        permissionPrimer = new CheckBox(context);
        permissionPrimer.setTextColor(0x8a000000);
        permissionPrimer.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        permissionPrimer.setText(context.getString(R.string.message_get_accounts_permission));
        addView(permissionPrimer);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (child instanceof AutoCompleteTextView) {
            setAutoCompleteTextView((AutoCompleteTextView) child);
            super.addView(child, 0, params);
        } else {
            // Carry on adding the View...
            super.addView(child, index, params);
        }
    }

    private void setAutoCompleteTextView(AutoCompleteTextView child) {
        // If we already have an AutoCompleteTextView, throw an exception
        if (autoCompleteTextView != null) {
            throw new IllegalArgumentException("We already have an AutoCompleteTextView, can only have one");
        }
        autoCompleteTextView = child;

        autoCompleteTextView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        autoCompleteTextView.setOnFocusChangeListener(this);

        if (!isInEditMode()) {
            setupAccountAutocomplete();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        backgroundPermissionManager.resume();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        backgroundPermissionManager.pause();
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

    /**
     * Returns the {@link android.widget.AutoCompleteTextView} used for text input.
     */
    @Nullable
    public AutoCompleteTextView getAutoCompleteTextView() {
        return autoCompleteTextView;
    }
}
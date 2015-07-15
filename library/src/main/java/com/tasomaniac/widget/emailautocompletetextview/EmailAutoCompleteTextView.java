package com.tasomaniac.widget.emailautocompletetextview;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filterable;
import android.widget.ListAdapter;

import java.util.ArrayList;
import java.util.HashSet;

public class EmailAutoCompleteTextView extends AutoCompleteTextView {

    private static final String PREFS_CHOOSEN_ACCOUNTS = "choosen_accounts";
    private static final String KEY_ACCOUNTS = "accounts";

    private SharedPreferences preferences;
    private ArrayAdapter<String> mAdapter;
    protected HashSet<String> accounts = new HashSet<>();

    private boolean mAutoRememberTypedEmail;

    public EmailAutoCompleteTextView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public EmailAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public EmailAutoCompleteTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        preferences = getContext()
                .getSharedPreferences(PREFS_CHOOSEN_ACCOUNTS, Context.MODE_PRIVATE);
        if (attrs != null) {

            final TypedArray a = context.obtainStyledAttributes(
                    attrs,
                    R.styleable.EmailAutoCompleteTextView,
                    defStyle,
                    0);

            mAutoRememberTypedEmail = a.getBoolean(
                    R.styleable.EmailAutoCompleteTextView_autoRememberTypedEmail,
                    true);

            a.recycle();
        }

        setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        mAdapter = getEmailAddressAdapter();
        super.setAdapter(mAdapter);
    }

    @Override
    public boolean enoughToFilter() {
    	return true;
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);

        if (mAutoRememberTypedEmail && !focused) {
            String typedEmail = getText().toString();
            if (isEmailAddress(typedEmail)) {
                addNewEmail(typedEmail);
            }
        }
    }

    /**
     * Add the email to the old emails list stored in {@code SharedPreferences}.
     *
     * @param email New Email address.
     * @return true if it is successfully added to the list.
     *         It means that the email address is in valid format
     *         and it is a new email that is not in the list.
     */
    public boolean addNewEmail(@NonNull String email) {

        if (isEmailAddress(email) && accounts.add(email)) {
            SharedPreferences.Editor editor = preferences.edit()
                    .putString(KEY_ACCOUNTS, TextUtils.join(",", accounts));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                editor.apply();
            } else {
                editor.commit();
            }

            mAdapter.add(email);

            return true;
        }
        return false;
    }

    /**
     * @deprecated
     * Please don't use this function
     * since the adapter is automatically generated with the accounts on the device.
     *
     * @param adapter the adapter holding the auto completion data
     */
    @Override
    public <T extends ListAdapter & Filterable> void setAdapter(@NonNull T adapter) {
    }

    /**
     * Returns an ArrayAdapter containing the emails on the device.
     *
     * @return {code ArrayAdapter<String>} containing emails.
     */
    private ArrayAdapter<String> getEmailAddressAdapter() {
        //TODO: check for runtime permissions.
        Account[] deviceAccounts = AccountManager.get(getContext()).getAccounts();
        for (Account account : deviceAccounts) {
            if (isEmailAddress(account.name)) {
                accounts.add(account.name);
            }
        }
        String[] old_emails = preferences.getString(KEY_ACCOUNTS, "").split("[,]");
        for (String email : old_emails) {
            if (isEmailAddress(email)) {
                accounts.add(email);
            }
        }

        return new ArrayAdapter<>(getContext(),
                android.R.layout.select_dialog_item,
                new ArrayList<>(accounts));
    }

    /**
     * Utility function to check if the provided String is an email or not.
     *
     * @param possibleEmail Given String.
     * @return true if the given String is an email address.
     */
    private static boolean isEmailAddress(String possibleEmail) {
        return Patterns.EMAIL_ADDRESS.matcher(possibleEmail).matches();
    }
}
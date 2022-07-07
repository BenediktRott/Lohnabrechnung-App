package com.example.turnen.ui.SettingsFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.PreferenceDataStore;

import com.example.turnen.R;

import java.util.Set;
import java.util.concurrent.Executors;

public class SharedPreferenceDataStore extends PreferenceDataStore{

    private final SharedPreferences mSharedPreferences;
    private final MyListener listener;
    private FragmentActivity fragmentActivity;
    private Context context;

    private final String edtTxtSetIBAN = SettingsFragment.edtTxtSetIBAN;
    private final String edtTxtShowIBAN = SettingsFragment.edtTxtShowIBAN;

    public SharedPreferenceDataStore(@NonNull SharedPreferences sharedPreferences, MyListener listener, FragmentActivity fragmentActivity, Context context) {
        mSharedPreferences = sharedPreferences;
        this.listener = listener;
        this.fragmentActivity = fragmentActivity;
        this.context = context;
    }

    //creates BiometricPrompt for confirming IBAN change
    private BiometricPrompt createBiometricPrompt(String key, String value){
        java.util.concurrent.ExecutorService executor = Executors.newSingleThreadExecutor();
        BiometricPrompt biometricPrompt = new BiometricPrompt(fragmentActivity, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                mSharedPreferences.edit().putString(key, value).apply();
                fragmentActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        notifyListener(key);
                    }
                });
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
            }
        });
        return biometricPrompt;
    }

    private void notifyListener(String key){
        if(listener != null){
            listener.callback(mSharedPreferences, key);
        }
    }

    @NonNull
    public SharedPreferences getSharedPreferences() {
        return mSharedPreferences;
    }

    @Override
    public void putString(String key, @Nullable String value) {
        Log.d("putString", "In put String");

        //Check if change was concerning the IBAN, if so prompt with Biometric prompt
        if(key.equals(edtTxtSetIBAN)){
            //check if Biometric Prompt is possible
            if(BiometricManager.from(context).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS){
                BiometricPrompt biometricPrompt = createBiometricPrompt(key, value);
                BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder().setTitle(context.getString(R.string.authenticateSetIBAN))
                        .setNegativeButtonText(context.getString(R.string.cancel))
                        .build();
                biometricPrompt.authenticate(promptInfo);
            }else {
                mSharedPreferences.edit().putString(key, value).apply();
                notifyListener(key);
            }

        }else {
            mSharedPreferences.edit().putString(key, value).apply();
            notifyListener(key);
        }
    }

    @Override
    public void putStringSet(String key, @Nullable Set<String> values) {
        mSharedPreferences.edit().putStringSet(key, values).apply();
        notifyListener(key);
    }

    @Override
    public void putInt(String key, int value) {
        mSharedPreferences.edit().putInt(key, value).apply();
        notifyListener(key);
    }

    @Override
    public void putLong(String key, long value) {
        mSharedPreferences.edit().putLong(key, value).apply();
        notifyListener(key);
    }

    @Override
    public void putFloat(String key, float value) {
        mSharedPreferences.edit().putFloat(key, value).apply();
        notifyListener(key);
    }

    @Override
    public void putBoolean(String key, boolean value) {
        mSharedPreferences.edit().putBoolean(key, value).apply();
        notifyListener(key);
    }

    @Nullable
    @Override
    public String getString(String key, @Nullable String defValue) {
        return mSharedPreferences.getString(key, defValue);
    }

    @Nullable
    @Override
    public Set<String> getStringSet(String key, @Nullable Set<String> defValues) {
        return mSharedPreferences.getStringSet(key, defValues);
    }

    @Override
    public int getInt(String key, int defValue) {
        return mSharedPreferences.getInt(key, defValue);
    }

    @Override
    public long getLong(String key, long defValue) {
        return mSharedPreferences.getLong(key, defValue);
    }

    @Override
    public float getFloat(String key, float defValue) {
        return mSharedPreferences.getFloat(key, defValue);
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        return mSharedPreferences.getBoolean(key, defValue);
    }
}

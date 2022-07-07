package com.example.turnen.ui.SettingsFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.example.turnen.MainActivity;
import com.example.turnen.R;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Objects;
import java.util.concurrent.Executors;

public class SettingsFragment extends PreferenceFragmentCompat{

    public static final String fileName = "shared_prefsEnc";
    private static SharedPreferences encPref;
    //listener set and defined in Main Activity to be able to change the sidebar menu
    private static MyListener listener;

    /**
     * EditTextPreference Keys
     */
    public final static String edtTxtSetIBAN = "edtTxtSetIBAN";
    public final static String edtTxtShowIBAN = "edtTxtShowIBAN";
    public final static String edtTxtSetName = "edtTxtSetName";
    public final static String edtTxtSetRole = "edtTxtSetRole";
    public final static String edtTxtSetSalary = "edtTxtSetSalary";
    public final static String switchPrefSetSignature = "switchPrefSetSignature";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        encPref = getOrCreateEncryptedSharedPreferences(getContext());
        getPreferenceManager().setPreferenceDataStore(new SharedPreferenceDataStore(encPref, listener, getActivity(), getContext()));
        setPreferencesFromResource(R.xml.preferences, rootKey);

        EditTextPreference editTextPreference = findPreference(edtTxtSetIBAN);
        SwitchPreference switchPreference = findPreference(edtTxtShowIBAN);

        //Launch edtTxtIBAN Dialog with NO text, if there exists one
        if (editTextPreference != null) {
            setEditTxtIBAN(editTextPreference);
            editTextPreference.setOnBindEditTextListener(editText -> editText.setText(""));
        }

        //switchPreference that makes the whole IBAN visible, always defaults back to off
        //Will create a biometric prompt (if possible) when trying to change
        if (switchPreference != null) {
            switchPreference.setChecked(false);
            switchPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if(switchPreference.isChecked()){
                        if(BiometricManager.from(requireContext()).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
                                == BiometricManager.BIOMETRIC_SUCCESS){
                            BiometricPrompt biometricPrompt = createBiometricPrompt(editTextPreference, switchPreference);
                            BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                                    .setTitle(getString(R.string.authenticateShowIBAN))
                                    .setNegativeButtonText(getString(R.string.cancel))
                                    .build();
                            biometricPrompt.authenticate(promptInfo);
                        }else {
                            Objects.requireNonNull(editTextPreference).
                                    setSummary(requireEncryptedSharedPreferences(getContext())
                                            .getString(edtTxtSetIBAN, getString(R.string.notSet)));
                        }
                    }else{
                        setEditTxtIBAN(Objects.requireNonNull(editTextPreference));
                    }
                    return true;
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //make sure switchPreference to show IBAN will always get reset to false
        SwitchPreference switchPreference = findPreference(edtTxtShowIBAN);
        if (switchPreference != null) {
            switchPreference.setChecked(false);
        }
    }


    //Create BiometricPrompt for switchPreference to show IBAN and set the edtTxtIBAN accordingly
    private BiometricPrompt createBiometricPrompt(EditTextPreference editTextPreference, SwitchPreference switchPreference){
        java.util.concurrent.ExecutorService executor = Executors.newSingleThreadExecutor();
        BiometricPrompt biometricPrompt = new BiometricPrompt(requireActivity(), executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        switchPreference.setChecked(false);
                    }
                });
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        editTextPreference.setSummary(requireEncryptedSharedPreferences(getContext())
                                .getString(edtTxtSetIBAN, getString(R.string.notSet)));
                    }
                });

            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        switchPreference.setChecked(false);
                    }
                });
            }
        });
        return biometricPrompt;
    }

    public static void setListener(MyListener listener){
        SettingsFragment.listener = listener;
    }

    /**
     * Gets or creates EncryptedSharedPreferences, depending on whether they have already been created or not
     * @param context Context
     * @return Returns the correct SharedPreferences
     */
    @Nullable
    public static SharedPreferences getOrCreateEncryptedSharedPreferences(Context context){

        if(encPref!=null){
            return encPref;
        }

        String masterKeyAlias = null;
        try {
            long startTime = System.nanoTime();
            masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);


            encPref = EncryptedSharedPreferences.create(fileName, masterKeyAlias,
                    context.getApplicationContext(),
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
            long endTime = System.nanoTime();
            Log.d("TimeSettings", "Time was: " + (endTime - startTime)/1000000);
            return encPref;
        } catch (GeneralSecurityException e) {
            Toast.makeText(context, "Security Failed", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (IOException e) {
            Toast.makeText(context, "Security Failed", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets or creates EncryptedSharedPreferences, depending on whether they have already been created or not
     * @param context Context
     * @return Returns the correct SharedPreferences
     */
    @NonNull
    public static SharedPreferences requireEncryptedSharedPreferences(Context context){

        if(encPref!=null){
            return encPref;
        }

        String masterKeyAlias = null;
        try {
            masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            encPref = EncryptedSharedPreferences.create(fileName, masterKeyAlias,
                    context.getApplicationContext(),
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
            return encPref;
        } catch (GeneralSecurityException e) {
            Toast.makeText(context, "Security Failed", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (IOException e) {
            Toast.makeText(context, "Security Failed", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        //throw Runtime Exception if for some reason encPref could neither be fetched nor created
        if(encPref == null){
            throw new RuntimeException("SharedPreferences couldn't be created or fetched");
        }
        return encPref;

    }

    /**
     * Sets the text of editTextPreference to the correctly formatted IBAN from SharedPreferences
     * @param editTextPreference editText to be set
     */
    private void setEditTxtIBAN(@NonNull EditTextPreference editTextPreference){
        String IBAN = requireEncryptedSharedPreferences(getContext()).getString("edtTxtSetIBAN", "");
        if(IBAN.equals("") || IBAN.equals(getString(R.string.notSet))){
            editTextPreference.setSummary(getString(R.string.notSet));
            return;
        }
        String summary = MainActivity.formatIBAN(IBAN);
        editTextPreference.setSummary(summary);
    }

}

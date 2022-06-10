package com.example.divyansh.googleapivoice;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceFragmentCompat;


public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener{
    SharedPreferences sharedPreferences;
    public static final String KEY_THEME = "Theme options";
    public static final String KEY_IMG = "Show images under word suggestions";
    public static final String KEY_FONT ="Font";
    public static final String KEY_FONTSIZE ="Font size";
    public static final String KEY_NOSUGG ="Number of word suggestions";

    private TextView heading;
    //private TextView heading = (TextView) getView().findViewById(R.id.settingshead);

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        //set theme - comments change font of breadcrumb but not the rest of the pop up :(
//        SharedPreferences sharedprefs = getPreferenceScreen().getSharedPreferences();
//        String store_font = sharedprefs.getString(KEY_FONT, "Montserrat");
        int theme = R.style.PreferenceBoxThemeMont;
//        if (store_font.equals("Calibri")){
//            theme = R.style.PreferenceBoxThemeCali;
//        }
//        else if (store_font.equals("Arial")){
//            theme = theme = R.style.PreferenceBoxThemeArial;
//        }
//        else if (store_font.equals("Helvetica")){
//            theme = R.style.PreferenceBoxThemeHelv;
//        }
        container.getContext().setTheme(theme);
        return view;
    }

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        addPreferencesFromResource(R.xml.preferences);
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case KEY_IMG:
                //preference 1: Show images or not
                boolean changedimg = sharedPreferences.getBoolean(key, true);
                Log.d("Change IMG SharedPref", key + " = " + Boolean.toString(changedimg));
                break;
            case KEY_THEME:
                //preference 2: Theme: Dark or light
                String changedtheme = sharedPreferences.getString(key, "Light");
                Log.d("Change THEME SharedPref", key + " = " + changedtheme);
                //change theme colour of settings
                if (changedtheme.equals("Light")) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                } else if (changedtheme.equals("Dark")) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                }
                break;
            case KEY_FONT:
                String changedfont = sharedPreferences.getString(key, "Montserrat");
                //Changing font for heading of settings page
//            heading = getActivity().findViewById(R.id.settingshead);
//            Typeface typeface = ResourcesCompat.getFont(getActivity().getApplicationContext(), R.font.montserratmed);
//
//            if (changedfont.equals("Montserrat")) {
//                typeface = ResourcesCompat.getFont(getActivity().getApplicationContext(), R.font.montserratmed);
//            }
//            else if (changedfont.equals("Calibri")){
//                typeface = ResourcesCompat.getFont(getActivity().getApplicationContext(), R.font.calibri);
//            }
//            else if (changedfont.equals("Arial")){
//                typeface = ResourcesCompat.getFont(getActivity().getApplicationContext(), R.font.arial);
//            }
//            else if (changedfont.equals("Helvetica")){
//                typeface = ResourcesCompat.getFont(getActivity().getApplicationContext(), R.font.helvetica);
//            }
//            heading.setTypeface(typeface);
                Log.d("Change FONT SharedPref", key + " = " + changedfont);
                break;
            case KEY_FONTSIZE:
                String changedsize = sharedPreferences.getString(key, "Medium");
                Log.d("Change FONT SharedPref", key + " = " + changedsize);
                break;
            case "Number of word suggestions":
                int new_nosugg = sharedPreferences.getInt(key, 6);
                Log.d("Change nosug SharedPref", key + " = " + new_nosugg);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //unregister the preference change listener
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        //register the preferenceChange listener
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }
}

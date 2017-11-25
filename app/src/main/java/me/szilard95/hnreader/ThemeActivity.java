package me.szilard95.hnreader;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;


public abstract class ThemeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        boolean dark = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("use_dark_theme", false);
        if (dark) setTheme(R.style.DarkTheme);
        super.onCreate(savedInstanceState);
    }
}

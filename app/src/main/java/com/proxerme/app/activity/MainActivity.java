package com.proxerme.app.activity;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.proxerme.app.R;
import com.proxerme.app.customtabs.CustomTabActivityHelper;
import com.proxerme.app.customtabs.WebviewFallback;
import com.proxerme.library.connection.ProxerConnection;

/**
 * This Activity does some work, all Activities have in common and all Activities should
 * inherit from this one.
 *
 * @author Ruben Gees
 */
@SuppressLint("Registered")
public class MainActivity extends AppCompatActivity {

    private CustomTabActivityHelper customTabActivityHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ProxerConnection.init();
        customTabActivityHelper = new CustomTabActivityHelper();
    }

    @Override
    protected void onStart() {
        super.onStart();

        customTabActivityHelper.bindCustomTabsService(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        customTabActivityHelper.unbindCustomTabsService(this);
        ProxerConnection.cleanup();
    }

    public void setLikelyUrl(@NonNull String url) {
        customTabActivityHelper.mayLaunchUrl(Uri.parse(url), null, null);
    }

    public void showPage(@NonNull String url) {
        CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder(customTabActivityHelper
                .getSession()).setToolbarColor(ContextCompat.getColor(this, R.color.primary))
                .build();

        CustomTabActivityHelper.openCustomTab(
                this, customTabsIntent, Uri.parse(url), new WebviewFallback());
    }
}
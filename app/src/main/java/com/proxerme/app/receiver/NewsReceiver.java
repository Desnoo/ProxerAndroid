package com.proxerme.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.proxerme.app.service.NotificationService;

/**
 * Receiver for the {@link NotificationService}.
 *
 * @author Ruben Gees
 */
public class NewsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationService.startActionLoadNews(context);
    }
}

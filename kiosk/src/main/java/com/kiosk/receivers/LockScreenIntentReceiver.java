package com.kiosk.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class LockScreenIntentReceiver extends BroadcastReceiver {

    private Class clazz;
    private final String TAG = LockScreenIntentReceiver.class.getSimpleName();

    private static final LockScreenIntentReceiver ourInstance = new LockScreenIntentReceiver();

    public static LockScreenIntentReceiver getInstance(Context context) {
        return ourInstance;
    }

    public LockScreenIntentReceiver() {
    }

    public LockScreenIntentReceiver(Class clazz) {
        this.clazz = clazz;
    }

    // Handle actions and display LockScreen
    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent != null) {
            if (intent.getAction() != null) {
                if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
                    startLockScreen(context);
                    Log.i(TAG, " : Receiver is Working");
                }
            }
        }
    }


    // Display lock screen
    private void startLockScreen(Context context) {
        Intent mIntent = new Intent(context, clazz);
        mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(mIntent);
    }
}
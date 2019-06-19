package com.abe.sample;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

import com.kiosk.receivers.LockScreenIntentReceiver;
import com.kiosk.utils.LockScreenUtils;
import com.kiosk.widgets.CustomViewGroup;

import java.util.ArrayList;
import java.util.List;

import static android.content.Intent.ACTION_MAIN;
import static android.content.Intent.CATEGORY_HOME;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DEFAULT;
import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
import static android.content.pm.PackageManager.DONT_KILL_APP;
import static com.abe.sample.AppConstants.REQUEST_CODE;
import static com.abe.sample.UriConstants.getApplicationPackage;

public class MainActivity extends AppCompatActivity implements LockScreenUtils.OnLockStatusChangedListener {

    LockScreenIntentReceiver lockScreenIntentReceiver;
    private LockScreenUtils mLockScreenUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        kioskMode();
        init();
    }

    @Override
    protected void onPause() {
//        BusProvider.getInstance().unregister(this);
        ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null)
            activityManager.moveTaskToFront(getTaskId(), 0);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        unRegisterBroadCaseReceiver();
        super.onDestroy();
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!hasFocus) {
//            Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
//            sendBroadcast(closeDialog);
        }
    }

    // Handle button clicks
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == 0) {
            onBackPressed();
            return true;
        } else
            return (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) ||
                    (keyCode == KeyEvent.KEYCODE_POWER) ||
                    (keyCode == KeyEvent.KEYCODE_VOLUME_UP) ||
                    (keyCode == KeyEvent.KEYCODE_CAMERA) ||
                    keyCode == KeyEvent.KEYCODE_HOME ||
                    (keyCode == KeyEvent.KEYCODE_MENU);
    }

    @Override
    public void onBackPressed() {
    }

    private void init() {
        mLockScreenUtils = new LockScreenUtils();
    }

    private void kioskMode() {
        lockStatusBar();
        controlTelephonyService();
        registerBroadCaseReceiver();
        if (!isMyAppLauncherDefault())
            resetPreferredLauncherAndOpenChooser(this);
    }

    void lockStatusBar() {
        try {
            WindowManager manager = ((WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE));
            WindowManager.LayoutParams localLayoutParams = new WindowManager.LayoutParams();
            localLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
            localLayoutParams.gravity = Gravity.TOP;
            localLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;

            localLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            localLayoutParams.height = (int) (50 * getResources().getDisplayMetrics().scaledDensity);
            localLayoutParams.format = PixelFormat.TRANSPARENT;
            CustomViewGroup view = new CustomViewGroup(this);
            if (manager != null) {
                if (Build.VERSION.SDK_INT < 21)
                    manager.addView(view, localLayoutParams);
                else
                    checkDrawOverlayPermission();
            }
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            Window window = getWindow();
            if (window != null) {
                window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
                window.setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
            }
        } catch (Exception ignored) {
        }
    }

    public void checkDrawOverlayPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, getApplicationPackage());
                startActivityForResult(intent, REQUEST_CODE);
            }
        }
    }

    private void controlTelephonyService() {
        Intent intent = getIntent();
        String BUNDLE_KILL = "kill";
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            boolean hasKill = intent.hasExtra(BUNDLE_KILL);
            if (bundle != null && hasKill) {
                int kill = bundle.getInt(BUNDLE_KILL);
                if (kill == 1) {
                    enableKeyguard();
                    unlockHomeButton();
                } else {
                    disableKeyguard();
                    StateListener phoneStateListener = new StateListener();
                    TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                    if (telephonyManager != null)
                        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
                }
            }
        }
    }

    private class StateListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {

            super.onCallStateChanged(state, incomingNumber);
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    unlockHomeButton();
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    break;
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void enableKeyguard() {
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        if (keyguardManager != null) {
            KeyguardManager.KeyguardLock mKL = keyguardManager.newKeyguardLock("IN");
            mKL.reenableKeyguard();
        }
    }

    @SuppressWarnings("deprecation")
    private void disableKeyguard() {
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        if (keyguardManager != null) {
            KeyguardManager.KeyguardLock mKL = keyguardManager.newKeyguardLock("IN");
            mKL.disableKeyguard();
        }
    }

    private void registerBroadCaseReceiver() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BOOT_COMPLETED);
        lockScreenIntentReceiver = new LockScreenIntentReceiver(MainActivity.class);
        registerReceiver(lockScreenIntentReceiver, filter);
    }

    public static void resetPreferredLauncherAndOpenChooser(Context context) {
        PackageManager packageManager = context.getPackageManager();
        ComponentName componentName = new ComponentName(context, MainActivity.class);
        packageManager.setComponentEnabledSetting(componentName, COMPONENT_ENABLED_STATE_ENABLED, DONT_KILL_APP);

        Intent selector = new Intent(ACTION_MAIN);
        selector.addCategory(CATEGORY_HOME);
        selector.setFlags(FLAG_ACTIVITY_NEW_TASK);
        selector.setFlags(FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(selector);
        packageManager.setComponentEnabledSetting(componentName, COMPONENT_ENABLED_STATE_DEFAULT, DONT_KILL_APP);
    }

    // Unlock home button and wait for its callback
    public void unlockHomeButton() {
        mLockScreenUtils.unlock();
    }

    public void unRegisterBroadCaseReceiver() {
        unregisterReceiver(lockScreenIntentReceiver);
    }

    @Override
    public void onLockStatusChanged(boolean isLocked) {
        if (!isLocked)
            unlockDevice();
    }

    private void unlockDevice() {
        finish();
    }

    private boolean isMyAppLauncherDefault() {
        final IntentFilter filter = new IntentFilter(Intent.ACTION_MAIN);
        filter.addCategory(Intent.CATEGORY_HOME);

        List<IntentFilter> filters = new ArrayList<>();
        filters.add(filter);

        final String myPackageName = getPackageName();
        List<ComponentName> activities = new ArrayList<>();
        final PackageManager packageManager = getPackageManager();

        packageManager.getPreferredActivities(filters, activities, null);

        for (ComponentName activity : activities) {
            if (myPackageName.equals(activity.getPackageName()))
                return true;
        }
        return false;
    }
}

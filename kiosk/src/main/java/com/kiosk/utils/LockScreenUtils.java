package com.kiosk.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;

import com.kiosk.R;


public class LockScreenUtils {

    // Member variables
    private OverlayDialog mOverlayDialog;
    private OnLockStatusChangedListener mLockStatusChangedListener;


    // Interface to communicate with owner activity
    public interface OnLockStatusChangedListener {
        void onLockStatusChanged(boolean isLocked);
    }

    // Reset the variables
    public LockScreenUtils() {
        reset();
    }

    // Display overlay dialog with a view to prevent home button click
    public void lock(Activity activity) {
        if (mOverlayDialog == null) {
            mOverlayDialog = new OverlayDialog(activity);
            mOverlayDialog.show();
            mLockStatusChangedListener = (OnLockStatusChangedListener) activity;
        }
    }

    // Reset variables
    private void reset() {
        if (mOverlayDialog != null) {
            mOverlayDialog.dismiss();
            mOverlayDialog = null;
        }
    }

    // Unlock the home button and give callback to unlock the screen
    public void unlock() {
        if (mOverlayDialog != null) {
            mOverlayDialog.dismiss();
            mOverlayDialog = null;
            if (mLockStatusChangedListener != null) {
                mLockStatusChangedListener.onLockStatusChanged(false);
            }
        }
    }

    // Create overlay dialog for locked screen to disable hardware buttons
    private static class OverlayDialog extends AlertDialog {

        OverlayDialog(Activity activity) {
            super(activity, R.style.OverlayDialog);
            int LAYOUT_FLAG;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            else
                LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
            Window window = getWindow();
            if(window != null) {
                WindowManager.LayoutParams params = window.getAttributes();
                params.type = LAYOUT_FLAG;
                params.dimAmount = 0.0F;
                params.width = 0;
                params.height = 0;
                params.gravity = Gravity.BOTTOM;
                window.setAttributes(params);
                window.setFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                        Color.WHITE);
            }
            setOwnerActivity(activity);
            setCancelable(false);
        }

        // Consume touch events
        public final boolean dispatchTouchEvent(@NonNull MotionEvent motionevent) {
            return true;
        }

    }
}
package com.curbngo.restartapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;

public class RestartApp extends CordovaPlugin {

    public static final String TAG = "RestartApp";
    protected Context applicationContext;
    protected CallbackContext currentContext;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        Log.d(TAG, "initialize()");
        this.applicationContext = cordova.getActivity().getApplicationContext();
        super.initialize(cordova, webView);
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        this.currentContext = callbackContext;

        if ("restartApp".equals(action)) {
            try {
                coldRestart();
                return true;
            } catch (Exception e) {
                handleError("Restart failed: " + e.getMessage());
                return false;
            }
        }

        return false;
    }

    /**
     * Performs a full cold app restart to clear memory and plugin leaks.
     * Uses AlarmManager to relaunch the app in ~100ms and then fully kills the process.
     * This guarantees static memory (like plugin fields) is cleared.
     */
    private void coldRestart() throws Exception {
        final String baseError = "Unable to cold restart application: ";
        try {
            logInfo("Cold restarting application (with killProcess)");

            if (applicationContext == null) {
                handleError(baseError + "Context is null");
                return;
            }

            PackageManager pm = applicationContext.getPackageManager();
            if (pm == null) {
                handleError(baseError + "PackageManager is null");
                return;
            }

            Intent mStartActivity = pm.getLaunchIntentForPackage(applicationContext.getPackageName());
            if (mStartActivity == null) {
                handleError(baseError + "StartActivity is null");
                return;
            }

            mStartActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

            PendingIntent mPendingIntent = PendingIntent.getActivity(
                applicationContext,
                223344,
                mStartActivity,
                PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            AlarmManager mgr = (AlarmManager) applicationContext.getSystemService(Context.ALARM_SERVICE);
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);

            Log.i(TAG, "Killing app to perform cold restart");
            System.exit(0);

        } catch (Exception ex) {
            handleError(baseError + ex.getMessage());
        }
    }

    private void handleError(String message) {
        logError(message);
        if (currentContext != null) {
            currentContext.error(message);
        }
    }

    private void logInfo(String msg) {
        Log.i(TAG, msg);
    }

    private void logError(String msg) {
        Log.e(TAG, msg);
    }
}

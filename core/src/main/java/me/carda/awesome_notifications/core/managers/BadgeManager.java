package me.carda.awesome_notifications.core.managers;

import static java.lang.Math.max;

import android.app.Notification;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;

import me.carda.awesome_notifications.core.AwesomeNotifications;
import me.carda.awesome_notifications.core.Definitions;
import me.carda.awesome_notifications.core.logs.Logger;
import me.leolin.shortcutbadger.ShortcutBadgeException;
import me.leolin.shortcutbadger.ShortcutBadger;

public class BadgeManager {
    private static final String TAG = "BadgeManager";

    // ************** SINGLETON PATTERN ***********************

    protected static BadgeManager instance;
    private final Object badgeSupportLock = new Object();
    private volatile Boolean isBadgeCounterSupported;

    protected BadgeManager(){}

    public static BadgeManager getInstance() {
        if (instance == null)
            instance = new BadgeManager();
        return instance;
    }

    // ********************************************************

    public int getGlobalBadgeCounter(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        // Read previous value. If not found, use 0 as default value.
        return max(prefs.getInt(Definitions.BADGE_COUNT, 0),0);
    }

    public void setGlobalBadgeCounter(Context context, int count) {
        count = max(count, 0);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putInt(Definitions.BADGE_COUNT, count);
        boolean applied = applyShortcutBadgeCount(context, count);
        logDebug("Global badge count set to " + count + ". ShortcutBadger applied: " + applied);

        editor.apply();
    }

    public void applyNotificationBadge(Context context, Notification notification, int count) {
        count = max(count, 0);
        try {
            ShortcutBadger.applyNotification(context, notification, count);
            logDebug("Applied notification badge hook with count " + count);
        } catch (Exception exception) {
            logDebug("Notification badge hook failed: " + exception.getMessage());
        }
    }

    public void resetGlobalBadgeCounter(Context context) {
        setGlobalBadgeCounter(context, 0);
    }

    public int incrementGlobalBadgeCounter(Context context) {
        int totalAmount = getGlobalBadgeCounter(context);
        totalAmount++;
        setGlobalBadgeCounter(context, totalAmount);
        return totalAmount;
    }

    public int decrementGlobalBadgeCounter(Context context) {
        int totalAmount = max(getGlobalBadgeCounter(context) - 1, 0);
        setGlobalBadgeCounter(context, totalAmount);
        return totalAmount;
    }

    boolean isBadgeDeviceGloballyAllowed(Context context){
        try {
            return Settings.Secure.getInt(context.getContentResolver(), "notification_badging") == PermissionManager.ON;
        } catch (Settings.SettingNotFoundException ignored) {
            return true;
        }
    }

    boolean isBadgeNumberingAllowed(Context context){
        return isBadgeCounterSupported(context);
    }

    boolean isBadgeAppGloballyAllowed(Context context){
        // TODO missing global badge checking for the current application scope
        //Settings.Secure.getInt(context.getContentResolver(), "notification_badging").contains(context.getPackageName());
        return true;
    }

    public boolean isBadgeGloballyAllowed(Context context){
        return (
                    Build.VERSION.SDK_INT < Build.VERSION_CODES.N /*Android 7*/ ||
                    isBadgeDeviceGloballyAllowed(context)
               ) &&
               isBadgeAppGloballyAllowed(context) &&
               isBadgeNumberingAllowed(context);
    }

    private boolean applyShortcutBadgeCount(Context context, int count) {
        try {
            if (count == 0) {
                return ShortcutBadger.removeCount(context);
            }
            return ShortcutBadger.applyCount(context, count);
        } catch (Exception exception) {
            logDebug("ShortcutBadger apply failed: " + exception.getMessage());
            return false;
        }
    }

    private boolean isBadgeCounterSupported(Context context) {
        Boolean cachedResult = isBadgeCounterSupported;
        if (cachedResult != null) {
            logDebug("Using cached badge counter support: " + cachedResult);
            return cachedResult;
        }

        synchronized (badgeSupportLock) {
            if (isBadgeCounterSupported != null) {
                logDebug("Using cached badge counter support: " + isBadgeCounterSupported);
                return isBadgeCounterSupported;
            }

            int currentBadgeCount = getGlobalBadgeCounter(context);
            boolean supported = false;
            try {
                supported = ShortcutBadger.isBadgeCounterSupported(context);
                if (supported) {
                    reapplyBadgeCountOrThrow(context, currentBadgeCount);
                }
            } catch (Exception exception) {
                logDebug("Badge counter support check failed: " + exception.getMessage());
                supported = false;
            }

            isBadgeCounterSupported = supported;
            logDebug("Badge counter support checked: " + supported);
            return supported;
        }
    }

    private void reapplyBadgeCountOrThrow(Context context, int count) throws ShortcutBadgeException {
        count = max(count, 0);
        if (count == 0) {
            ShortcutBadger.removeCountOrThrow(context);
        } else {
            ShortcutBadger.applyCountOrThrow(context, count);
        }
        logDebug("Reapplied badge count after support check: " + count);
    }

    private void logDebug(String message) {
        if (AwesomeNotifications.debug) {
            Logger.d(TAG, message);
        }
    }
}

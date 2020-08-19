package co.vaango.attendance.multibiometric.utils;

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ProgressBar;

import java.util.Set;

public class AndroidUtils {

    private static ProgressDialog progress;

    public static abstract class LoadingTask extends AsyncTask<Object, Void, Void> {

        private Context context;
        private Object[] params;

        public LoadingTask(Context context) {
            this.context = context;
        }

        public Object[] getParams() {
            return params;
        }

        public Context getContext() {
            return context;
        }

        @Override
        protected void onPreExecute() {
            showLoadingDialog(context);
        }

        @Deprecated
        @Override
        protected Void doInBackground(Object... params) {
            this.params = params;
            doInBackground();
            return null;
        }

        protected abstract void doInBackground();

        @Override
        protected void onPostExecute(Void aVoid) {
            dismissLoadingDialog();
        }

    }

    public static boolean isApiLevelGreaterThan(int apiLevel) {
        return Build.VERSION.SDK_INT > apiLevel;
    }

    public static boolean isApiLevelFrom(int apiLevel) {
        return Build.VERSION.SDK_INT >= apiLevel;
    }

    public static boolean isApiLevelBetween(int apiLevelFrom, int apiLevelTo) {
        return Build.VERSION.SDK_INT >= apiLevelFrom && Build.VERSION.SDK_INT <= apiLevelTo;
    }

    public static boolean isApiLevelLessThan(int apiLevel) {
        return Build.VERSION.SDK_INT < apiLevel;
    }

    public static boolean isServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public static int getColor(Resources resources, int resourceId) {
        if (AndroidUtils.isApiLevelFrom(23))
            return resources.getColor(resourceId, resources.newTheme());
        return resources.getColor(resourceId);
    }

    public static Drawable getDrawable(Resources resources, int resourceId) {
        if (AndroidUtils.isApiLevelFrom(21))
            return resources.getDrawable(resourceId, resources.newTheme());
        return resources.getDrawable(resourceId);
    }

    @SuppressWarnings("unchecked")
    public static void writePreference(Context context, String key, Object value) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (value instanceof String)
            editor.putString(key, (String) value);
        else if (value instanceof Integer)
            editor.putInt(key, (Integer) value);
        else if (value instanceof Boolean)
            editor.putBoolean(key, (Boolean) value);
        else if (value instanceof Float)
            editor.putFloat(key, (Float) value);
        else if (value instanceof Long)
            editor.putLong(key, (Long) value);
        else if (value instanceof Set<?>)
            editor.putStringSet(key, (Set<String>) value);
        editor.apply();
    }

    @SuppressWarnings("unchecked")
    public static <T> T readPreference(Context context, String key, T defaultValue) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (defaultValue instanceof String)
            return (T) sharedPreferences.getString(key, (String) defaultValue);
        else if (defaultValue instanceof Integer)
            return (T) Integer.valueOf(sharedPreferences.getInt(key, (Integer) defaultValue));
        else if (defaultValue instanceof Boolean)
            return (T) Boolean.valueOf(sharedPreferences.getBoolean(key, (Boolean) defaultValue));
        else if (defaultValue instanceof Float)
            return (T) Float.valueOf(sharedPreferences.getFloat(key, (Float) defaultValue));
        else if (defaultValue instanceof Long)
            return (T) Long.valueOf(sharedPreferences.getLong(key, (Long) defaultValue));
        else if (defaultValue instanceof Set<?>)
            return (T) sharedPreferences.getStringSet(key, (Set<String>) defaultValue);
        else
            return defaultValue;
    }

    public static void removePreference(Context context, String key) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().remove(key).apply();
    }

    public static void showLoadingDialog(Context context) {
        try {
            if (progress != null)
                progress = null;
            progress = new ProgressDialog(context);
            try {
                progress.show();
            } catch (WindowManager.BadTokenException e) {
                Log.d("facecheck",""+e);
            }
            progress.setCancelable(false);
            progress.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            ProgressBar progressBar = new ProgressBar(context);
            progressBar.setIndeterminate(true);
            progress.setContentView(progressBar);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void dismissLoadingDialog() {
        try {
            if (progress != null)
                progress.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

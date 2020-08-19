package co.vaango.attendance.multibiometric.utils;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import co.vaango.attendance.multibiometric.BiometricApplication;
import co.vaango.attendance.R;
import co.vaango.attendance.util.ExceptionUtils;
import co.vaango.attendance.util.ToastManager;
import co.vaango.attendance.view.ErrorDialogFragment;
import co.vaango.attendance.view.InfoDialogFragment;

public class BaseActivity extends FragmentActivity {

    private BiometricApplication application;
    private static boolean triedToCloseApp = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        application = (BiometricApplication) this.getApplicationContext();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        application.setTopActivity(this);
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if (this.equals(application.getTopActivity()))
            application.setTopActivity(null);
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        if (this.equals(application.getTopActivity()))
            application.setTopActivity(null);
    }

    @Override
    public void onBackPressed() {
        if (isTaskRoot() && !triedToCloseApp) {
            triedToCloseApp = true;
            Toast.makeText(this, R.string.APP_CLOSE_CONFIRMATION, Toast.LENGTH_SHORT).show();
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    triedToCloseApp = false;
                }
            }, AppConstants.APP_CLOSE_CONFIRMATION_WAIT_TIME);
        } else
            super.onBackPressed();
    }

    private ProgressDialog mProgressDialog;
    protected void showProgress(int messageId) {
        showProgress(getString(messageId));
    }

    protected void showProgress(final String message) {
        hideProgress();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressDialog = ProgressDialog.show(BaseActivity.this, "", message);
            }
        });
    }

    protected void hideProgress() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
            }
        });
    }

    protected void showToast(int messageId) {
        showToast(getString(messageId));
    }

    protected void showToast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastManager.show(BaseActivity.this, message);
            }
        });
    }

    protected void showError(String message, boolean close) {
        ErrorDialogFragment.newInstance(message, close).show(getFragmentManager(), "error");
    }

    protected void showError(int messageId) {
        showError(getString(messageId));
    }

    protected void showError(String message) {
        showError(message, false);
    }

    protected void showError(Throwable th) {
        Log.e(getClass().getSimpleName(), "Exception", th);
        showError(ExceptionUtils.getMessage(th), false);
    }

    protected void showInfo(int messageId) {
        showInfo(getString(messageId));
    }

    protected void showInfo(String message) {
        InfoDialogFragment.newInstance(message).show(getFragmentManager(), "info");
    }

    @Override
    protected void onStop() {
        super.onStop();
        hideProgress();
    }
}

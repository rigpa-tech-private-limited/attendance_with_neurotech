package co.vaango.attendance.multibiometric.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import co.vaango.attendance.R;

public class PopUpHelper {
    public static void ok(Context context, String message, boolean cancelable) {
        IAlertDialogCreate(context, null, message, context.getString(R.string.OK), null, null, -1, cancelable);
    }

    private static void IAlertDialogCreate(Context context, String title, String message, String positiveButtonText,
                                           String negativeButtonText, DialogInterface.OnClickListener onClickListener, int layoutResId,
                                           boolean cancelable) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.transparent_dialog_borderless);
        if (layoutResId != -1)
            builder.setView(LayoutInflater.from(context).inflate(layoutResId, null));
        else {
            //if (title != null) {
//            View view=LayoutInflater.from(context).inflate(R.layout.popup_title, null);
//            builder.setCustomTitle(view);
            //}
            if (message != null)
                builder.setMessage(Html.fromHtml("<font color='#000000'>"+message+"</font>"));
            builder.setPositiveButton(positiveButtonText, onClickListener);
            if (negativeButtonText != null)
                builder.setNegativeButton(negativeButtonText, onClickListener);
        }
        try {
            builder.setCancelable(cancelable);
            builder.create();
            AlertDialog alertDialog = builder.show();
            alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(AndroidUtils.getColor(context.getResources(), R.color.colorPrimary));
            alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(AndroidUtils.getColor(context.getResources(), R.color.colorAccent));
            alertDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        } catch (WindowManager.BadTokenException e) {
            e.printStackTrace();
        }
    }

    public static void okTextCenter(Context context, String message, boolean cancelable) {
        IAlertDialogCreateokTextCenter(context, null, message, context.getString(R.string.OK), null, null, -1, cancelable);
    }

    private static void IAlertDialogCreateokTextCenter(Context context, String title, String message, String positiveButtonText,
                                           String negativeButtonText, DialogInterface.OnClickListener onClickListener, int layoutResId,
                                           boolean cancelable) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.transparent_dialog_borderless);
        if (layoutResId != -1)
            builder.setView(LayoutInflater.from(context).inflate(layoutResId, null));
        else {
            //if (title != null) {
//            View view=LayoutInflater.from(context).inflate(R.layout.popup_title, null);
//            builder.setCustomTitle(view);
            //}
            if (message != null){
                TextView myMsg = new TextView(context);
                myMsg.setText(message);
                myMsg.setGravity(Gravity.CENTER_HORIZONTAL);
                builder.setView(myMsg);
            }

//            if (message != null)
//                builder.setMessage(Html.fromHtml("<font color='#000000'>"+message+"</font>"));
            builder.setPositiveButton(positiveButtonText, onClickListener);
            if (negativeButtonText != null)
                builder.setNegativeButton(negativeButtonText, onClickListener);
        }
        try {
            builder.setCancelable(cancelable);
            builder.create();
            AlertDialog alertDialog = builder.show();
            final Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            LinearLayout.LayoutParams positiveButtonLL = (LinearLayout.LayoutParams) positiveButton.getLayoutParams();
            positiveButtonLL.gravity = Gravity.CENTER;
            positiveButton.setLayoutParams(positiveButtonLL);
            //alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(AndroidUtils.getColor(context.getResources(), R.color.colorPrimary));
            alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(AndroidUtils.getColor(context.getResources(), R.color.colorAccent));
            alertDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        } catch (WindowManager.BadTokenException e) {
            e.printStackTrace();
        }
    }
}

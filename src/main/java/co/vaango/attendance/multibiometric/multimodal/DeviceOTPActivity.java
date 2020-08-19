package co.vaango.attendance.multibiometric.multimodal;

import co.vaango.attendance.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import co.vaango.attendance.multibiometric.utils.BaseActivity;
import co.vaango.attendance.multibiometric.utils.AndroidUtils;
import co.vaango.attendance.multibiometric.utils.AppConstants;
import co.vaango.attendance.multibiometric.utils.HttpRequester;
import co.vaango.attendance.multibiometric.utils.PopUpHelper;

public class DeviceOTPActivity extends BaseActivity implements View.OnClickListener {

    private EditText otp_val;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_otp);
        otp_val = (EditText) findViewById(R.id.otp_txt);
        Button otp = (Button) findViewById(R.id.otp_btn);
        otp.setOnClickListener(this);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(otp_val, InputMethodManager.SHOW_IMPLICIT);

        otp_val.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if(s.length() == 6){
                    hideKeyboard(DeviceOTPActivity.this);
                    Log.d("facecheck", "Btn Click");
                    AndroidUtils.showLoadingDialog(DeviceOTPActivity.this);
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("facecheck", "Start API Call - OTP value = "+String.valueOf(otp_val.getText()));
                            try {

                                IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                                Intent batteryStatus = getApplicationContext().registerReceiver(null, ifilter);

                                int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                                int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                                float batteryPct = (level * 100) / (float)scale;
                                Log.d("facecheck", "BatteryPct = "+batteryPct);
                                Map<String, String> parameters = new HashMap<String, String>();
                                parameters.put("otp", String.valueOf(otp_val.getText()));
                                parameters.put("app_version", AppConstants.APP_VERSION);
                                parameters.put("os_type", "android");
                                parameters.put("os_version", Build.VERSION.RELEASE);
                                parameters.put("uuid", Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
                                parameters.put("device_brand", getDeviceName());
                                parameters.put("device_type", "mobile");
                                parameters.put("battery_status", String.valueOf(batteryPct));
                                final JSONObject result = HttpRequester.IHttpPostRequest(DeviceOTPActivity.this, AppConstants.DEVICE_OTP, parameters, null, true, null);
                                Log.d("facecheck", String.valueOf(result));
                                AndroidUtils.dismissLoadingDialog();
                                if (result.getString("status").equals("error")) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                showToast(result.getString("message"));
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                } else if (result.getString("status").equals("success")) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Log.d("facecheck", String.valueOf(result));
                                            try {
                                                AndroidUtils.writePreference(DeviceOTPActivity.this, AppConstants.token, result.getString("token"));
                                                Intent intent = new Intent(DeviceOTPActivity.this, MultiModalActivity.class);
                                                intent.putExtra("syncing",true);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(intent);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    thread.start();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.otp_btn:

                break;

        }
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    /** Returns the consumer friendly device name */
    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        }
        return capitalize(manufacturer) + " " + model;
    }

    private static String capitalize(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        char[] arr = str.toCharArray();
        boolean capitalizeNext = true;

        StringBuilder phrase = new StringBuilder();
        for (char c : arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase.append(Character.toUpperCase(c));
                capitalizeNext = false;
                continue;
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            }
            phrase.append(c);
        }

        return phrase.toString();
    }


}

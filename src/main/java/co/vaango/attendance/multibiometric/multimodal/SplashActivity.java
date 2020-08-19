package co.vaango.attendance.multibiometric.multimodal;

import co.vaango.attendance.R;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.WindowManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import co.vaango.attendance.multibiometric.Database.DatabaseQueryClass;
import co.vaango.attendance.multibiometric.Features.ShowStudentList.StudentListActivity;
import co.vaango.attendance.multibiometric.modals.DeviceLog;
import co.vaango.attendance.multibiometric.utils.BaseActivity;
import co.vaango.attendance.multibiometric.utils.AndroidUtils;
import co.vaango.attendance.multibiometric.utils.AppConstants;
import co.vaango.attendance.multibiometric.utils.Config;
import co.vaango.attendance.multibiometric.utils.HttpRequester;

import static com.neurotec.lang.NCore.getContext;

public class SplashActivity extends BaseActivity {

    private boolean killed;
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    private String datetimeStamp = df.format(new Date());
    private DatabaseQueryClass databaseQueryClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        if (AndroidUtils.isApiLevelFrom(21)) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(AndroidUtils.getColor(getResources(), R.color.app_bg));
        }
        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!killed) {
                    Log.d("facecheck", "splashscreen");
                    final String token = AndroidUtils.readPreference(SplashActivity.this, AppConstants.token, "");
                    Log.d("facecheck", "token = " + token);
                    if (token.isEmpty()) {
                        Intent intent = new Intent(SplashActivity.this, DeviceOTPActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        Log.d("facecheck", "Start API Call SYNC CHECK ");
                        int status = 0;
                        if (checkNetworkConnection(getApplicationContext())) {
                            try {
                                Map<String, String> parameters = new HashMap<String, String>();
                                parameters.put("token", token);
                                parameters.put("status", Config.LOG_SUCCESS);
                                parameters.put("title", Config.OPEN_APP_TITLE);
                                parameters.put("desc", Config.OPEN_APP_DESC);
                                parameters.put("time", datetimeStamp);
                                Log.d("facecheck", String.valueOf(parameters));
                                final JSONObject result = HttpRequester.IHttpPostRequest(getApplicationContext(), AppConstants.DEVICE_LOG_SYNC, parameters, null, true, null);
                                Log.d("facecheck", String.valueOf(result));
                                if (result.getString("status").equals("success")) {
                                    status = 1;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        databaseQueryClass = new DatabaseQueryClass(getContext());
                        DeviceLog log = new DeviceLog(-1, Config.LOG_SUCCESS, Config.OPEN_APP_TITLE, Config.OPEN_APP_DESC, datetimeStamp, status);
                        Log.d("facecheck", "log Insert Data" + log.toString());
                        long id = databaseQueryClass.insertLog(log);
                        if (id > 0) {
                            log.setId(id);
                            Log.d("facecheck", "log Details inserted");
                        }
                        Intent intent = new Intent(SplashActivity.this, MultiModalActivity.class);
                        intent.putExtra("syncing", true);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                }
            }
        }.start();
    }


    public boolean checkNetworkConnection(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        killed = true;
    }

}

package co.vaango.attendance.multibiometric.multimodal;

import android.Manifest;
import android.app.AlertDialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.neurotec.biometrics.client.NBiometricClient;
import com.neurotec.lang.NCore;

import org.json.JSONException;
import org.json.JSONObject;

import co.vaango.attendance.multibiometric.Database.DatabaseQueryClass;
import co.vaango.attendance.multibiometric.modals.DeviceLog;
import co.vaango.attendance.multibiometric.utils.AndroidUtils;
import co.vaango.attendance.multibiometric.utils.AppConstants;
import co.vaango.attendance.multibiometric.utils.BaseActivity;
import co.vaango.attendance.licensing.LicensingManager;
import co.vaango.attendance.R;
import co.vaango.attendance.multibiometric.Model;
import co.vaango.attendance.multibiometric.utils.Config;
import co.vaango.attendance.multibiometric.utils.HttpRequester;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.neurotec.lang.NCore.getContext;

public final class MultiModalActivity extends BaseActivity implements ActivityCompat.OnRequestPermissionsResultCallback, View.OnClickListener {

    private static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    private static final String WARNING_PROCEED_WITH_NOT_GRANTED_PERMISSIONS = "Do you wish to proceed without granting all permissions?";
    private static final String WARNING_NOT_ALL_GRANTED = "Some permissions are not granted.";
    private static final String MESSAGE_ALL_PERMISSIONS_GRANTED = "All permissions granted";

    private static String TAG = "facecheck";

    private final int MODALITY_CODE_FACE = 1;

    private Map<String, Integer> mPermissions = new HashMap<String, Integer>();

    private static List<String> getMandatoryComponentsInternal() {
        List<String> components = new ArrayList<String>();
        for (String component : FaceActivity.mandatoryComponents()) {
            if (!components.contains(component)) {
                components.add(component);
            }
        }
        return components;
    }

    private static List<String> getAdditionalComponentsInternal() {
        List<String> components = new ArrayList<String>();
        for (String component : FaceActivity.additionalComponents()) {
            if (!components.contains(component)) {
                components.add(component);
            }
        }
        return components;
    }

    public static List<String> getAllComponentsInternal() {
        List<String> combinedComponents = getMandatoryComponentsInternal();
        combinedComponents.addAll(getAdditionalComponentsInternal());
        return combinedComponents;
    }


    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }

    private String[] getNotGrantedPermissions() {
        List<String> neededPermissions = new ArrayList<String>();

        int storagePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int phonePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        int cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int microphonePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);

        if (phonePermission != PackageManager.PERMISSION_GRANTED) {
            neededPermissions.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (storagePermission != PackageManager.PERMISSION_GRANTED) {
            neededPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            neededPermissions.add(Manifest.permission.CAMERA);
        }
        if (microphonePermission != PackageManager.PERMISSION_GRANTED) {
            neededPermissions.add(Manifest.permission.RECORD_AUDIO);
        }

        return neededPermissions.toArray(new String[neededPermissions.size()]);
    }

    private void requestPermissions(String[] permissions) {
        ActivityCompat.requestPermissions(this, permissions, REQUEST_ID_MULTIPLE_PERMISSIONS);
    }

    private TextView welcome_text;
    private ImageView welcome_image;
    private LinearLayout welcome_screen_ll;
    private ImageView company_logo;
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    private String datetimeStamp = df.format(new Date());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NCore.setContext(this);
        setContentView(R.layout.multi_modal_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        ImageView imageFace = (ImageView) findViewById(R.id.face);
        imageFace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent faceActivity = new Intent(MultiModalActivity.this, FaceActivity.class);
                startActivityForResult(faceActivity, MODALITY_CODE_FACE);
            }
        });

        TextView continueBtn = (TextView) findViewById(R.id.multimodal_button_continue);
        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent faceActivity = new Intent(MultiModalActivity.this, FaceActivity.class);
                startActivityForResult(faceActivity, MODALITY_CODE_FACE);
            }
        });

        welcome_text = (TextView) findViewById(R.id.welcome_text);
        welcome_image = (ImageView) findViewById(R.id.welcome_image);
        welcome_screen_ll = (LinearLayout) findViewById(R.id.welcome_screen_ll);
        company_logo = (ImageView) findViewById(R.id.company_logo);
        welcome_screen_ll.setOnClickListener(MultiModalActivity.this);
        if (checkNetworkConnection(getApplicationContext())) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "Start API Call Device Info ");
                    try {
                        Map<String, String> parameters = new HashMap<String, String>();
                        String token = AndroidUtils.readPreference(MultiModalActivity.this, AppConstants.token, "");
                        Log.d(TAG, "token = " + token);
                        parameters.put("token", token);
                        final JSONObject result = HttpRequester.IHttpPostRequest(MultiModalActivity.this, AppConstants.DEVICE_INFO, parameters, null, true, null);
                        Log.d(TAG, String.valueOf(result));
                        if (result.getString("status").equals("success")) {
                            try {
                                JSONObject company_settings = result.getJSONObject("company");
                                Log.d(TAG, "welcome_text_plain = " + company_settings.getString("welcome_text_plain"));
                                Log.d(TAG, "welcome_screen = " + company_settings.getString("welcome_screen"));
                                Log.d(TAG, "logo = " + company_settings.getString("logo"));

                                welcome_text.setText(company_settings.getString("welcome_text_plain"));
                                welcome_screen_ll.setBackgroundColor(Color.parseColor(company_settings.getString("welcome_color")));
//                                    new MultiModalActivity.ImageLoadTask(company_settings.getString("welcome_screen"), welcome_image).execute();
                                new MultiModalActivity.ImageLoadTask(company_settings.getString("logo"), company_logo).execute();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                AlertDialog.Builder builder = new AlertDialog.Builder(MultiModalActivity.this);
                                builder.setMessage(result.getString("message"))
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                AndroidUtils.writePreference(MultiModalActivity.this, AppConstants.token, "");
                                                Intent intent = new Intent(MultiModalActivity.this, DeviceOTPActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(intent);
                                            }
                                        });
                                AlertDialog alert = builder.create();
                                alert.show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
        } else {
            Toast.makeText(getApplicationContext(), "No network connection", Toast.LENGTH_SHORT).show();
        }
        String[] neededPermissions = getNotGrantedPermissions();
        if (neededPermissions.length == 0) {
            new InitializationTask().execute();
        } else {
            requestPermissions(neededPermissions);
        }
    }

    public boolean checkNetworkConnection(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.welcome_screen_ll) {
            Log.d(TAG, "welcome_screen_ll Click");
            Intent faceActivity = new Intent(MultiModalActivity.this, FaceActivity.class);
            startActivityForResult(faceActivity, MODALITY_CODE_FACE);
        }
    }

    public class ImageLoadTask extends AsyncTask<Void, Void, Bitmap> {

        private String url;
        private ImageView imageView;

        ImageLoadTask(String url, ImageView imageView) {
            this.url = url;
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            try {
                URL urlConnection = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) urlConnection
                        .openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                return BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            imageView.setImageBitmap(result);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult code: " + resultCode);
        if (resultCode == RESULT_OK) {
            if (requestCode == MODALITY_CODE_FACE) {
                if (data != null) {
                    Log.d(TAG, String.valueOf(data));
                }
            } else {
                throw new AssertionError("Unrecognised request code");
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public static String toLowerCase(String string) {
        StringBuilder sb = new StringBuilder();
        sb.append(string.substring(0, 1).toUpperCase());
        sb.append(string.substring(1).toLowerCase());
        return sb.toString().replaceAll("_", " ");
    }

    public void onRequestPermissionsResult(int requestCode, final String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS: {
                // Initialize the map with permissions
                mPermissions.clear();
                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++) {
                        mPermissions.put(permissions[i], grantResults[i]);
                    }
                    // Check if at least one is not granted
                    if (mPermissions.get(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                            || mPermissions.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                            || mPermissions.get(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                            || mPermissions.get(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                        showDialogOK(WARNING_PROCEED_WITH_NOT_GRANTED_PERMISSIONS,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which) {
                                            case DialogInterface.BUTTON_POSITIVE:
                                                Log.w(TAG, WARNING_NOT_ALL_GRANTED);
                                                for (Map.Entry<String, Integer> entry : mPermissions.entrySet()) {
                                                    if (entry.getValue() != PackageManager.PERMISSION_GRANTED) {
                                                        Log.w(TAG, entry.getKey() + ": PERMISSION_DENIED");
                                                    }
                                                }
                                                new InitializationTask().execute();
                                                break;
                                            case DialogInterface.BUTTON_NEGATIVE:
                                                requestPermissions(permissions);
                                                break;
                                            default:
                                                throw new AssertionError("Unrecognised permission dialog parameter value");
                                        }
                                    }
                                });
                    } else {
                        Log.i(TAG, MESSAGE_ALL_PERMISSIONS_GRANTED);
                        Log.d(TAG, "isExternalStorageAvailable "+isExternalStorageAvailable());
                        Log.d(TAG, "isExternalStorageReadable "+isExternalStorageReadable());
                        if (isExternalStorageAvailable() || isExternalStorageReadable()) {
                            writeData(getStorageDir(fileName));
                            readData(getStorageDir(fileName));
                            new InitializationTask().execute();
                        }
                    }
                }
            }
        }
    }

    final class InitializationTask extends AsyncTask<Object, Boolean, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgress(R.string.msg_initializing);
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        protected Boolean doInBackground(Object... params) {

            Log.d(TAG, "LicensingManager.isFaceExtractionActivated() "+LicensingManager.isFaceExtractionActivated());
            Log.d(TAG, "LicensingManager.isFaceMatchingActivated() "+LicensingManager.isFaceMatchingActivated());
            Log.d(TAG, "LicensingManager.isFaceStandardsActivated() "+LicensingManager.isFaceStandardsActivated());
            String license_status = Config.LOG_FAILED;
            String license_log_title = Config.LICENSE_TITLE;
            String license_log_desc = "";
            if (!LicensingManager.isFaceExtractionActivated() && !LicensingManager.isFaceMatchingActivated() && !LicensingManager.isFaceStandardsActivated()) {
                showProgress(R.string.msg_obtaining_licenses);
                try {
                    license_status = Config.LOG_SUCCESS;
                    Log.d(TAG, "License process called");
                    if (LicensingManager.getInstance().obtain(MultiModalActivity.this, getMandatoryComponentsInternal())) {
                        showToast(R.string.msg_licenses_obtained);
                        license_log_desc = Config.LICENSE_OBTAINED;
                    } else {
                        showToast(R.string.msg_licenses_partially_obtained);
                        license_log_desc = Config.LICENSE_PARTIALLY_OBTAINED;
                    }
                } catch (Exception e) {
                    showError(e.getMessage(), false);
                    license_status = Config.LOG_FAILED;
                    license_log_desc = Config.LICENSE_NOT_OBTAINED;
                }
            } else {
                Log.d(TAG, "License process not called");
                license_status = Config.LOG_SUCCESS;
                license_log_desc = Config.LICENSE_ALREADY_OBTAINED;
            }

            Log.d(TAG, "Start API Call SYNC CHECK ");
            int status = 0;
            if (checkNetworkConnection(getApplicationContext())) {
                try {
                    String token = AndroidUtils.readPreference(getApplicationContext(), AppConstants.token, "");
                    Map<String, String> parameters = new HashMap<String, String>();
                    parameters.put("token", token);
                    parameters.put("status", license_status);
                    parameters.put("title", license_log_title);
                    parameters.put("desc", license_log_desc);
                    parameters.put("time", datetimeStamp);
                    Log.d(TAG, String.valueOf(parameters));
                    final JSONObject result = HttpRequester.IHttpPostRequest(getApplicationContext(), AppConstants.DEVICE_LOG_SYNC, parameters, null, true, null);
                    Log.d(TAG, String.valueOf(result));
                    if (result.getString("status").equals("success")) {
                        status = 1;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            DatabaseQueryClass databaseQueryClass = new DatabaseQueryClass(getContext());
            DeviceLog log = new DeviceLog(-1, license_status, license_log_title, license_log_desc, datetimeStamp, status);
            Log.d(TAG, "log Insert Data" + log.toString());
            long id = databaseQueryClass.insertLog(log);
            if (id > 0) {
                log.setId(id);
                Log.d(TAG, "log Details inserted");
            }

            try {
                NBiometricClient client = Model.getInstance().getClient();
                manageSyncJob();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            hideProgress();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void manageSyncJob() {
        JobScheduler mJobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        JobInfo.Builder builder = new JobInfo.Builder(1, new ComponentName(getPackageName(), OfflineDataSyncService.class.getName()));
        builder.setPeriodic(900000);
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        if (mJobScheduler.schedule(builder.build()) <= 0) {
            Log.e("facecheck", "Job Scheduler failed");
        } else {
            Log.d(TAG, "Job Scheduler initiated");
        }
    }


    String fileName = "internet_license.lic";
    String lic_data ="# \n\nFace Client, Face Matcher internet license.\n\n:00000005670E497349E7A17D09931603F96B690DA06471D28979BECD2EB455E2E87146\n7BBE1BC13AFB3E1EE5FA3EC68C708AAF5309F71429CA4CD534B39C9BFC23869E9C7338E\nC4E986DB13D4DA468BCDFC6DA98DA3241602D77E1D39C48E3471C11F63685FDF4CA10E9\n665CFFF100135C6C30374081AB6C43AF23E76FA62AAFEDC3327DEB82BA89DBBE1114538\n4426D31B95B59BE90C0DB7959727D1B43FC7BF2D1DAE656B6BE19FA2B5E6C39210E3955\nDBEA4BA6C38D7C825274D2D94BF23F378BA2B5C10C8CA27DD73955D064A73E1C694332D\nB\n";

    //write data to file
    public void writeData(String filePath) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(filePath);
            fileOutputStream.write(lic_data.getBytes());
            fileOutputStream.close();
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    //read data from the file
    public void readData(String filePath) {

        StringBuilder stringBuilder = new StringBuilder();
        try {
            FileInputStream fileInputStream = new FileInputStream(filePath);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader reader = new BufferedReader(inputStreamReader);

            String temp;
            while ((temp = reader.readLine()) != null) {
                stringBuilder.append(temp);
            }
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("facecheck", "out read"+stringBuilder.toString());
    }

    //checks if external storage is available for read and write
    public boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    //checks if external storage is available for read
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public String getStorageDir(String fileName) {
        //create folder
        File file = new File(Environment.getExternalStorageDirectory(), "Neurotechnology/Licenses");
        if (!file.mkdirs()) {
            file.mkdirs();
        }
        String filePath = file.getAbsolutePath() + File.separator + fileName;
        return filePath;
    }
}

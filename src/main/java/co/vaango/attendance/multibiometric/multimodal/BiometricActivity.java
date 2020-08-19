package co.vaango.attendance.multibiometric.multimodal;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.neurotec.biometrics.NBiometric;
import com.neurotec.biometrics.NBiometricOperation;
import com.neurotec.biometrics.NBiometricStatus;
import com.neurotec.biometrics.NBiometricTask;
import com.neurotec.biometrics.NFace;
import com.neurotec.biometrics.NLRecord;
import com.neurotec.biometrics.NLTemplate;
import com.neurotec.biometrics.NMatchingResult;
import com.neurotec.biometrics.NSubject;
import com.neurotec.biometrics.NTemplate;
import com.neurotec.biometrics.client.NBiometricClient;
import com.neurotec.biometrics.client.NClusterBiometricConnection;
import com.neurotec.images.NImage;
import com.neurotec.io.NBuffer;
import com.neurotec.lang.NCore;
import com.neurotec.licensing.gui.ActivationActivity;

import co.vaango.attendance.app.BaseActivity;
import co.vaango.attendance.app.DirectoryViewer;
import co.vaango.attendance.app.InfoActivity;
import co.vaango.attendance.licensing.LicensingManager;
import co.vaango.attendance.licensing.LicensingManager.LicensingStateCallback;
import co.vaango.attendance.licensing.LicensingState;
import co.vaango.attendance.multibiometric.Database.DatabaseQueryClass;
import co.vaango.attendance.multibiometric.Features.CreateStudent.Student;
import co.vaango.attendance.multibiometric.Model;
import co.vaango.attendance.R;
import co.vaango.attendance.multibiometric.modals.User;
import co.vaango.attendance.multibiometric.modals.Visit;
import co.vaango.attendance.multibiometric.preferences.MultimodalPreferences;
import co.vaango.attendance.multibiometric.utils.AndroidUtils;
import co.vaango.attendance.multibiometric.utils.AppConstants;
import co.vaango.attendance.multibiometric.utils.Config;
import co.vaango.attendance.multibiometric.utils.HttpRequester;
import co.vaango.attendance.multibiometric.utils.PopUpHelper;
import co.vaango.attendance.multibiometric.view.EnrollmentDialogFragment;
import co.vaango.attendance.multibiometric.view.SubjectListFragment;

import com.neurotec.util.concurrent.CompletionHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.neurotec.lang.NCore.getContext;

public abstract class BiometricActivity extends BaseActivity implements EnrollmentDialogFragment.EnrollmentDialogListener, SubjectListFragment.SubjectSelectionListener, LicensingStateCallback {

    // ===========================================================
    // Private static fields
    // ===========================================================

    private static final int REQUEST_CODE_GET_FILE = 1;


    private static final String EXTRA_REQUEST_CODE = "request_code";
    private static final int VERIFICATION_REQUEST_CODE = 1;
    private static final int DATABASE_REQUEST_CODE = 2;

    protected static final String RECORD_REQUEST_FACE = "face";
    protected static final String RECORD_REQUEST_FINGER = "finger";
    protected static final String RECORD_REQUEST_IRIS = "iris";
    protected static final String RECORD_REQUEST_VOICE = "voice";

    private static final String TAG = "facecheck";

    private boolean checkout;

    private String timeStr = "";

    private String userName = "";

    private String userID = "";
    
    private int sync_status = 0;

    private String datetimeStamp = "";

    protected boolean isDetectStarted = false;

    // ===========================================================
    // Private fields
    // ===========================================================

    private CompletionHandler<NSubject[], ? super NBiometricOperation> subjectListHandler = new CompletionHandler<NSubject[], NBiometricOperation>() {

        @Override
        public void completed(NSubject[] result, NBiometricOperation attachment) {
            Model.getInstance().setSubjects(result);
        }

        @Override
        public void failed(Throwable exc, NBiometricOperation attachment) {
            Log.e(TAG, exc.toString(), exc);
        }

    };

    private CompletionHandler<NBiometricTask, NBiometricOperation> completionHandler = new CompletionHandler<NBiometricTask, NBiometricOperation>() {

        @Override
        public void completed(NBiometricTask task, NBiometricOperation operation) {
            String message = null;
            NBiometricStatus status = task.getStatus();
            Log.i(TAG, String.format("Operation: %s, Status: %s", operation, status));

            onOperationCompleted(operation, task);
            if (status == NBiometricStatus.CANCELED) return;
            isDetectStarted = false;
            if (task.getError() != null) {
                Log.i(TAG, String.format("task.getError: %s", task.getError()));
                showError(task.getError());
                onBack();
            } else {
                subject = task.getSubjects().get(0);
                switch (operation) {
                    case CAPTURE:
                    case CREATE_TEMPLATE: {
                        if (status == NBiometricStatus.OK) {
                            NSubject subject1 = task.getSubjects().get(0);
//                            Log.d(TAG, "Base64 Face..." + Base64.encodeToString(subject1.getFaces().get(0).getImage().save().toByteArray(), Base64.NO_WRAP));
                            AndroidUtils.writePreference(BiometricActivity.this, "person_image", Base64.encodeToString(subject1.getFaces().get(0).getImage().save().toByteArray(), Base64.NO_WRAP));
                            Log.d(TAG, "CREATE_TEMPLATE");
                            NBiometricTask task1 = Model.getInstance().getClient().createTask(EnumSet.of(NBiometricOperation.IDENTIFY), subject1);
                            Model.getInstance().getClient().performTask(task1, NBiometricOperation.IDENTIFY, completionHandler);
                            Log.d(TAG, "identify task performed");
                        } else if (task.getSubjects().size() > 0 && task.getSubjects().get(0).getFaces().size() > 0 && task.getStatus() == NBiometricStatus.TIMEOUT) {
                            //message = getString(R.string.msg_extraction_failed, getString(R.string.msg_liveness_check_failed));
                            Log.i(TAG, String.format("msg_liveness_check_failed Operation: %s, Status: %s", operation, status));
                            onBack();
                        } else {
                            //message = getString(R.string.msg_extraction_failed, status.toString());
                            Log.i(TAG, String.format("msg_extraction_failed Operation: %s, Status: %s", operation, status));
                            onBack();
                        }
                    }
                    break;
                    case ENROLL:
                    case ENROLL_WITH_DUPLICATE_CHECK: {
                        if (status == NBiometricStatus.OK) {
                            message = getString(R.string.msg_enrollment_succeeded);
                        } else {
                            message = getString(R.string.msg_enrollment_failed, status.toString());
                        }
                        client.list(NBiometricOperation.LIST, subjectListHandler);
                        showMsg(message);
                    }
                    break;
                    case VERIFY: {
                        if (status == NBiometricStatus.OK) {
                            message = getString(R.string.msg_verification_succeeded);
                        } else {
                            message = getString(R.string.msg_verification_failed, status.toString());
                        }
                        showMsg(message);
                    }
                    break;
                    case IDENTIFY: {
                        if (status == NBiometricStatus.OK) {
                            NSubject subject = task.getSubjects().get(0);
                            int face_id = -1;
                            for (NMatchingResult result : subject.getMatchingResults()) {
//                                Log.d(TAG, "NMatchingResult Face ID => " + result.getId());
                                face_id = Integer.parseInt(result.getId());
                            }
                            DatabaseQueryClass databaseQueryClass = new DatabaseQueryClass(getContext());
                            User user = databaseQueryClass.getUserByFaceId(face_id);
//                            Log.d(TAG, "User Data for Face ID = " + face_id + "=>" + user.toString());
                            Visit visitData = databaseQueryClass.getVisitByFaceId(face_id);
                            userName = user.getUser_name();
                            userID = String.valueOf(user.getFace_id());
                            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
                            datetimeStamp = df.format(new Date());
                            Log.d(TAG,"Today datetimeStamp : " + datetimeStamp);
                            if (visitData!=null && visitData.getId() > 0 && visitData.getOut_time().equals("")) {
                                Log.d(TAG, "visit Data for Face ID = " + face_id + "=>" + visitData.toString());
                                Date dt_1 = null;
                                try {
                                    dt_1 = df.parse(datetimeStamp);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                Date dt_2 = null;
                                try {
                                    Date d = df.parse(visitData.getIn_time());
                                    Calendar cal = Calendar.getInstance();
                                    cal.setTime(d);
                                    cal.add(Calendar.MINUTE, Config.IN_TIME_BUFFER);
                                    String newTime = df.format(cal.getTime());
                                    dt_2 = df.parse(newTime);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                                Date dt_in = null;
                                try {
                                    dt_in = df.parse(visitData.getIn_time());
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                                Log.d(TAG,"Current Time : " + df.format(dt_1));
                                Log.d(TAG,"In Time : " + df.format(dt_in));
                                Log.d(TAG,"In Time Buffer : " + df.format(dt_2));

                                if (dt_1.compareTo(dt_2) > 0) {
                                    Log.d(TAG,"Current-Time occurs after In-Time Buffer");
                                    timeStr = "CHECKED OUT";
                                    checkout = true;
                                    if (checkNetworkConnection(getApplicationContext())) {
                                        try {
                                            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                                            Intent batteryStatus = getApplicationContext().registerReceiver(null, ifilter);
                                            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                                            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                                            float batteryPct = (level * 100) / (float) scale;
                                            Map<String, String> parameters = new HashMap<String, String>();
                                            String token = AndroidUtils.readPreference(BiometricActivity.this, AppConstants.token, "");
                                            Log.d(TAG, "token = " + token);
                                            parameters.put("token", token);
                                            parameters.put("id", userID);
                                            parameters.put("time", datetimeStamp);
                                            parameters.put("battery_status", String.valueOf(batteryPct));
                                            final JSONObject result = HttpRequester.IHttpPostRequest(BiometricActivity.this, AppConstants.ATTENDANCE, parameters, null, true, null);
                                            Log.d(TAG, String.valueOf(result));
                                            if (result.getString("status").equals("success")) {
                                                Log.d(TAG, result.getString("status"));
                                                sync_status = 1;
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    Visit visit = new Visit(-1, user.getUser_name(), user.getFace_id(), visitData.getIn_time(), datetimeStamp, sync_status);
                                    long id = databaseQueryClass.updateVisitInfo(visit);
                                    if (id > 0) {
                                        Log.d(TAG, "user visit Details updated" + " visit id " + id + " ... " + user.getUser_name() + " - " + user.getFace_id() + " - " + datetimeStamp);
                                    }

                                    Intent i = new Intent(BiometricActivity.this, AttendanceViewActivity.class);
                                    i.putExtra("user_name", userName);
                                    i.putExtra("footer_text", timeStr);
                                    i.putExtra("check_out", checkout);
                                    i.putExtra("user_exist", true);
                                    startActivity(i);
                                } else {
                                    Log.d(TAG,"Current-Time occurs before In-Time Buffer");
                                    timeStr = "ALREADY CHECKED IN";
                                    checkout = true;
                                    Intent i = new Intent(BiometricActivity.this, AttendanceViewActivity.class);
                                    i.putExtra("user_name", userName);
                                    i.putExtra("footer_text", timeStr);
                                    i.putExtra("check_out", checkout);
                                    i.putExtra("user_exist", true);
                                    startActivity(i);
                                }
                            } else {
                                timeStr = "CHECKED IN";
                                checkout = false;
                                if (checkNetworkConnection(getApplicationContext())) {

                                    try {
                                        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                                        Intent batteryStatus = getApplicationContext().registerReceiver(null, ifilter);
                                        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                                        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                                        float batteryPct = (level * 100) / (float) scale;
                                        Map<String, String> parameters = new HashMap<String, String>();
                                        String token = AndroidUtils.readPreference(BiometricActivity.this, AppConstants.token, "");
                                        Log.d(TAG, "token = " + token);
                                        parameters.put("token", token);
                                        parameters.put("id", userID);
                                        parameters.put("time", datetimeStamp);
                                        parameters.put("battery_status", String.valueOf(batteryPct));
                                        final JSONObject result = HttpRequester.IHttpPostRequest(BiometricActivity.this, AppConstants.ATTENDANCE, parameters, null, true, null);
                                        Log.d(TAG, String.valueOf(result));
                                        if (result.getString("status").equals("success")) {
                                            sync_status = 1;
                                        }

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                Visit visit = new Visit(-1, user.getUser_name(), user.getFace_id(), datetimeStamp, "", sync_status);
                                Log.d(TAG, "visit Insert Data" + visit.toString());
                                long id = databaseQueryClass.insertVisit(visit);
                                if (id > 0) {
                                    visit.setId(id);
                                    Log.d(TAG, "user visit Details inserted" + " visit id " + id + " ... " + user.getUser_name() + " - " + user.getFace_id() + " - " + datetimeStamp);
                                }
                                Intent i = new Intent(BiometricActivity.this, AttendanceViewActivity.class);
                                i.putExtra("user_name", userName);
                                i.putExtra("footer_text", timeStr);
                                i.putExtra("check_out", checkout);
                                i.putExtra("user_exist", true);
                                startActivity(i);
                            }
                        } else {
                            Intent i = new Intent(BiometricActivity.this, AttendanceViewActivity.class);
                            i.putExtra("user_name", userName);
                            i.putExtra("footer_text", timeStr);
                            i.putExtra("check_out", checkout);
                            i.putExtra("user_exist", false);
                            startActivity(i);
                        }

                    }
                    break;
                    default: {
                        throw new AssertionError("Invalid NBiometricOperation");
                    }
                }

            }
        }

        public boolean checkNetworkConnection(Context context) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return (networkInfo != null && networkInfo.isConnected());
        }

        public void showMsg(String message) {
            if (message != null) {

                showToast(message);
                Log.i(TAG, "show Message=>" + message);
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                onBack();
                            }
                        }, 1000);
            }
        }

        @Override
        public void failed(Throwable th, NBiometricOperation operation) {
            onOperationCompleted(operation, null);
            Log.i(TAG, "NBiometricOperation faild=>" + th);
        }
    };

    private LinearLayout captureControls;
    private LinearLayout stopControls;
    private LinearLayout successControls;
    private LinearLayout actionControls;

    protected boolean mAppClosing = false;
    protected boolean mAppIsGoingToBackground = false;

    // ===========================================================
    // Protected fields
    // ===========================================================

    protected NBiometricClient client = null;
    protected NClusterBiometricConnection mConnection = null;
    protected NSubject subject = null;
    protected final PropertyChangeListener biometricPropertyChanged = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if ("Status".equals(evt.getPropertyName())) {
                onStatusChanged(((NBiometric) evt.getSource()).getStatus());
            }
        }
    };

    // ===========================================================
    // Protected abstract methods
    // ===========================================================

    protected abstract Class<?> getPreferences();

    protected abstract void updatePreferences(NBiometricClient client);

    protected abstract boolean isCheckForDuplicates();

    protected abstract List<String> getAdditionalComponents();

    protected abstract List<String> getMandatoryComponents();

    protected abstract String getModalityAssetDirectory();

    // ===========================================================
    // Protected methods
    // ===========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "BiometricActivity onCreate");
        super.onCreate(savedInstanceState);
        NCore.setContext(this);
        try {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            setContentView(R.layout.multimodal_main_biometric);
            captureControls = (LinearLayout) findViewById(R.id.multimodal_capture_controls);
            successControls = (LinearLayout) findViewById(R.id.multimodal_success_controls);
            stopControls = (LinearLayout) findViewById(R.id.multimodal_stop_controls);
            actionControls = (LinearLayout) findViewById(R.id.multimodal_action_controls);
            new InitializationTask().execute(savedInstanceState == null);
        } catch (Exception e) {
            showError(e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_GET_FILE) {
            if (resultCode == RESULT_OK) {
                try {
                    onFileSelected(data.getData());
                } catch (Throwable th) {
                    showError(th);
                }
            }
        }
    }

    protected void onStartCapturing() {
    }

    protected void onStopCapturing() {
        cancel();
    }

    protected void onOperationStarted(NBiometricOperation operation) {
        if (operation == NBiometricOperation.CAPTURE) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isStopSupported()) {
                        captureControls.setVisibility(View.GONE);
                        stopControls.setVisibility(View.VISIBLE);
                        successControls.setVisibility(View.GONE);
                        actionControls.setVisibility(View.GONE);
                    }
                }
            });
        } else {
            if (isActive()) {
                showProgress(R.string.msg_processing);
            }
        }
    }

    protected void onOperationCompleted(final NBiometricOperation operation, final NBiometricTask task) {
        hideProgress();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (task != null && (task.getStatus() == NBiometricStatus.OK || task.getOperations().contains(NBiometricOperation.IDENTIFY) || task.getOperations().contains(NBiometricOperation.VERIFY)
                        || task.getOperations().contains(NBiometricOperation.ENROLL_WITH_DUPLICATE_CHECK)
                        || task.getOperations().contains(NBiometricOperation.ENROLL))) {
                    captureControls.setVisibility(View.GONE);
                    stopControls.setVisibility(View.GONE);
                    successControls.setVisibility(View.GONE);
                    actionControls.setVisibility(View.VISIBLE);
                } else {
                    stopControls.setVisibility(View.GONE);
                    successControls.setVisibility(View.GONE);
                    successControls.setVisibility(View.GONE);
                    actionControls.setVisibility(View.GONE);
                }
            }
        });
    }

    protected void onLicensesObtained() {
        Log.d(TAG, "LicensingManager.isFaceExtractionActivated() "+ LicensingManager.isFaceExtractionActivated());
        Log.d(TAG, "LicensingManager.isFaceMatchingActivated() "+LicensingManager.isFaceMatchingActivated());
        Log.d(TAG, "LicensingManager.isFaceStandardsActivated() "+LicensingManager.isFaceStandardsActivated());
    }

    protected void onFileSelected(Uri uri) throws Exception {
    }
    
    protected final boolean isActive() {
        return client.getCurrentBiometric() != null || client.getCurrentSubject() != null;
    }

    protected boolean isStopSupported() {
        return true;
    }

    protected void stop() {
        client.force();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAppIsGoingToBackground = false;
    }

    protected void cancel() {
        if (client != null) {
            client.cancel();
        }
    }


    protected void onBack() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                stopControls.setVisibility(View.GONE);
                successControls.setVisibility(View.GONE);
                successControls.setVisibility(View.GONE);
                actionControls.setVisibility(View.GONE);
            }
        });
    }


    protected void onStatusChanged(final NBiometricStatus status) {
    }

    // ===========================================================
    // Public methods
    // ===========================================================

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mAppClosing = true;
    }

    @Override
    protected void onStop() {
        mAppIsGoingToBackground = true;
        cancel();
        if (mAppClosing) {
        }
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_preferences: {
                startActivity(new Intent(this, getPreferences()));
                break;
            }
            case R.id.action_database: {
                Bundle bundle = new Bundle();
                bundle.putInt(EXTRA_REQUEST_CODE, DATABASE_REQUEST_CODE);
                SubjectListFragment.newInstance(Model.getInstance().getSubjects(), false, bundle).show(getFragmentManager(), "database");
                break;
            }
            case R.id.action_activation: {
                Intent activation = new Intent(this, ActivationActivity.class);
                Bundle params = new Bundle();
                params.putStringArrayList(ActivationActivity.LICENSES, new ArrayList<String>(MultiModalActivity.getAllComponentsInternal()));
                activation.putExtras(params);
                startActivity(activation);
                break;
            }
            case R.id.action_about: {
                startActivity(new Intent(this, InfoActivity.class));
                break;
            }
        }
        return true;
    }

    @Override
    public void onEnrollmentIDProvided(String id) {
        subject.setId(id);
        updatePreferences(client);
        NBiometricOperation operation = isCheckForDuplicates() ? NBiometricOperation.ENROLL_WITH_DUPLICATE_CHECK : NBiometricOperation.ENROLL;
        NBiometricTask task = client.createTask(EnumSet.of(operation), subject);
        client.performTask(task, NBiometricOperation.ENROLL, completionHandler);
        onOperationStarted(NBiometricOperation.ENROLL);
    }

    @Override
    public void onSubjectSelected(NSubject otherSubject, Bundle bundle) {
        if (bundle.getInt(EXTRA_REQUEST_CODE) == VERIFICATION_REQUEST_CODE) {
            subject.setId(otherSubject.getId());
            updatePreferences(client);
            NBiometricTask task = client.createTask(EnumSet.of(NBiometricOperation.VERIFY), subject);
            client.performTask(task, NBiometricOperation.VERIFY, completionHandler);
            onOperationStarted(NBiometricOperation.VERIFY);
        }
    }

    @Override
    public void onLicensingStateChanged(LicensingState state) {
        switch (state) {
            case OBTAINING:
                showProgress(R.string.msg_obtaining_licenses);
                break;
            case OBTAINED:
                hideProgress();
                showToast(R.string.msg_licenses_obtained);
                break;
            case NOT_OBTAINED:
                hideProgress();
                showToast(R.string.msg_licenses_not_obtained);
                break;
        }
    }

    public void capture(NSubject subject, EnumSet<NBiometricOperation> additionalOperations) {
        if (subject == null) throw new NullPointerException("subject");
        this.subject = subject;
        updatePreferences(client);

        EnumSet<NBiometricOperation> operations = EnumSet.of(NBiometricOperation.CREATE_TEMPLATE);
        if (additionalOperations != null) {
            operations.addAll(additionalOperations);
        }
        NBiometricTask task = client.createTask(operations, subject);
        isDetectStarted = true;
        client.performTask(task, NBiometricOperation.CREATE_TEMPLATE, completionHandler);
        onOperationStarted(NBiometricOperation.CAPTURE);
    }

    public void extract(NSubject subject) {
        if (subject == null) throw new NullPointerException("subject");
        this.subject = subject;
        updatePreferences(client);
        NBiometricTask task = client.createTask(EnumSet.of(NBiometricOperation.CREATE_TEMPLATE), subject);
        client.performTask(task, NBiometricOperation.CREATE_TEMPLATE, completionHandler);
        onOperationStarted(NBiometricOperation.CREATE_TEMPLATE);
    }

    final class InitializationTask extends AsyncTask<Object, Boolean, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Object... params) {
            if (params.length < 1) {
                throw new IllegalArgumentException("Missing parameter if to obtain license");
            }
            try {
                client = Model.getInstance().getClient();
                subject = Model.getInstance().getSubject();
                mAppClosing = false;
                client.list(NBiometricOperation.LIST, subjectListHandler);
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
            onLicensesObtained();
        }
    }
}

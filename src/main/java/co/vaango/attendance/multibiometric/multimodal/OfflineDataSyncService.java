package co.vaango.attendance.multibiometric.multimodal;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.neurotec.biometrics.NBiometricOperation;
import com.neurotec.biometrics.NBiometricStatus;
import com.neurotec.biometrics.NBiometricTask;
import com.neurotec.biometrics.NFace;
import com.neurotec.biometrics.NLAttributes;
import com.neurotec.biometrics.NLRecord;
import com.neurotec.biometrics.NLTemplate;
import com.neurotec.biometrics.NSubject;
import com.neurotec.biometrics.NTemplate;
import com.neurotec.biometrics.client.NBiometricClient;
import com.neurotec.images.NImage;
import com.neurotec.io.NBuffer;
import com.orhanobut.logger.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.vaango.attendance.multibiometric.Database.DatabaseHelper;
import co.vaango.attendance.multibiometric.Database.DatabaseQueryClass;
import co.vaango.attendance.multibiometric.Model;
import co.vaango.attendance.multibiometric.modals.DeviceLog;
import co.vaango.attendance.multibiometric.modals.User;
import co.vaango.attendance.multibiometric.modals.Visit;
import co.vaango.attendance.multibiometric.utils.AndroidUtils;
import co.vaango.attendance.multibiometric.utils.AppConstants;
import co.vaango.attendance.multibiometric.utils.Config;
import co.vaango.attendance.multibiometric.utils.HttpRequester;

import static com.neurotec.lang.NCore.getContext;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class OfflineDataSyncService extends JobService {
    private static String TAG = "facecheck";
//    private static final String TAG = OfflineDataSyncService.class.getSimpleName();
    private Visit mVisit;
    private DatabaseHelper databaseHelper = DatabaseHelper.getInstance(this);
    private SQLiteDatabase sqLiteDatabase;
    private NBiometricClient client;

    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    private String datetimeStamp = df.format(new Date());
    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.d(TAG, "onStartJob called");
        if (databaseHelper != null) {
            sqLiteDatabase = databaseHelper.getWritableDatabase();
            if (sqLiteDatabase != null) {
                Log.d(TAG, "sqLiteDatabase called");
                if (checkNetworkConnection(getApplicationContext())) {
                    Log.d(TAG, "checkNetworkConnection sync called");
                    DeviceLog log = new DeviceLog(-1, Config.LOG_SUCCESS, Config.OFFLINE_SYNC_TITLE, Config.OFFLINE_SYNC_JOB_DESC, datetimeStamp, 0);
                    Log.d(TAG, "log Insert Data" + log.toString());
                    long id = insertLog(log);
                    if (id > 0) {
                        log.setId(id);
                        Log.d(TAG, "log Details inserted");
                    }
                    syncDeviceInfo();
                    syncVisits();
                    syncUsers();
                    syncDeviceLogs();
                } else {
                    Log.d(TAG, "checkNetworkConnection sync not called");
                }
            } else {
                Log.d(TAG, "sqLiteDatabase not called");
            }
        }
        return false;
    }

    public boolean checkNetworkConnection(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.d(TAG, "onStopJob called");
        return false;
    }

    public void syncDeviceInfo() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "API Call => DEVICE INFO SYNC ");
                try {
                    Map<String, String> parameters = new HashMap<String, String>();
                    String token = AndroidUtils.readPreference(getApplicationContext(), AppConstants.token, "");
                    WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    // Level of current connection
                    int rssi = wifiManager.getConnectionInfo().getRssi();
                    int level = WifiManager.calculateSignalLevel(rssi, 5);
                    Log.d(TAG, "Signal Strength Level is " + level + " out of 5");

                    IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                    Intent batteryStatus = getApplicationContext().registerReceiver(null, ifilter);

                    int battery_level = 0;
                    if (batteryStatus != null) {
                        battery_level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                    }
                    int scale = 0;
                    if (batteryStatus != null) {
                        scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                    }
                    float batteryPct = (battery_level * 100) / (float) scale;
                    Log.d(TAG, "BatteryPct = " + batteryPct);
                    parameters.put("token", token);
                    parameters.put("signal_strength", String.valueOf(level));
                    parameters.put("battery", String.valueOf(batteryPct));
                    final JSONObject result = HttpRequester.IHttpPostRequest(getApplicationContext(), AppConstants.DEVICE_INFO_SYNC, parameters, null, true, null);
                    Log.d(TAG, String.valueOf(result));
                    if (result.getString("status").equals("success")) {
                        DeviceLog log = new DeviceLog(-1, Config.LOG_SUCCESS, Config.OFFLINE_SYNC_TITLE, Config.OFFLINE_SYNC_DEVICE_INFO_DESC, datetimeStamp, 0);
                        Log.d(TAG, "log Insert Data" + log.toString());
                        long id = insertLog(log);
                        if (id > 0) {
                            log.setId(id);
                            Log.d(TAG, "log Details inserted");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public void syncDeviceLogs(){
        final String logsSyncJsonArray = getAllSyncLogs();
        Log.d(TAG, "logsSyncJsonArray: " + logsSyncJsonArray);
        if (!logsSyncJsonArray.equals("")) {
            //Toast.makeText(getApplicationContext(), "syncVisits process called", Toast.LENGTH_SHORT).show();
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "Start API Call SYNC CHECK ");
                    try {
                        Map<String, String> parameters = new HashMap<String, String>();
                        String token = AndroidUtils.readPreference(getApplicationContext(), AppConstants.token, "");
                        parameters.put("token", token);
                        parameters.put("logs", logsSyncJsonArray);
                        Log.d(TAG, String.valueOf(parameters));
                        final JSONObject result = HttpRequester.IHttpPostRequest(getApplicationContext(), AppConstants.DEVICE_LOGS_SYNC, parameters, null, true, null);
                        Log.d(TAG, String.valueOf(result));
                        if (result.getString("status").equals("success")) {
                            JSONArray ids = result.getJSONArray("ids");
                            Log.d(TAG, String.valueOf(ids));
                            for (int i = 0; i < ids.length(); i++) {
                                String v_id = ids.getString(i);
                                Log.d(TAG, String.valueOf(v_id));
                                DeviceLog log = getDeviceLogById(v_id);
                                log.setStatus(log.getStatus());
                                log.setTitle(log.getTitle());
                                log.setDescription(log.getDescription());
                                log.setTime(log.getTime());
                                log.setSync_status(1);
                                updateLogsInfo(log);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
        } else {
            // Toast.makeText(getApplicationContext(), "SQLite and Remote MySQL DBs are in sync", Toast.LENGTH_SHORT).show();
        }
    }

    public String getAllSyncLogs() {

        Cursor cursor = null;
        try {

            String SELECT_QUERY = String.format("SELECT %s, %s, %s, %s, %s, %s FROM %s WHERE sync_status = '%s'", Config.COLUMN_LOG_ID, Config.COLUMN_LOG_STATUS, Config.COLUMN_LOG_TITLE, Config.COLUMN_LOG_DESC, Config.COLUMN_LOG_TIME, Config.COLUMN_LOG_SYNC_STATUS, Config.TABLE_LOGS, "0");
            cursor = sqLiteDatabase.rawQuery(SELECT_QUERY, null);

            if (cursor != null)
                if (cursor.moveToFirst()) {
                    ArrayList<HashMap<String, String>> logsListJson;
                    logsListJson = new ArrayList<HashMap<String, String>>();
                    List<DeviceLog> logsList = new ArrayList<DeviceLog>();
                    do {
                        int id = cursor.getInt(cursor.getColumnIndex(Config.COLUMN_LOG_ID));
                        String status = cursor.getString(cursor.getColumnIndex(Config.COLUMN_LOG_STATUS));
                        String title = cursor.getString(cursor.getColumnIndex(Config.COLUMN_LOG_TITLE));
                        String description = cursor.getString(cursor.getColumnIndex(Config.COLUMN_LOG_DESC));
                        String time = cursor.getString(cursor.getColumnIndex(Config.COLUMN_LOG_TIME));
                        int sync_status = cursor.getInt(cursor.getColumnIndex(Config.COLUMN_LOG_SYNC_STATUS));

                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put("id", String.valueOf(id));
                        map.put("status", String.valueOf(status));
                        map.put("title", String.valueOf(title));
                        map.put("desc", String.valueOf(description));
                        map.put("time", String.valueOf(time));
                        logsListJson.add(map);
                        logsList.add(new DeviceLog(id, status, title, description, time, sync_status));
                    } while (cursor.moveToNext());
                    Log.d(TAG, "getAllSyncVisits: " + logsList);
                    Gson gson = new GsonBuilder().create();
                    return gson.toJson(logsListJson);
                }
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.getMessage());
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return "";
    }

    public long updateLogsInfo(DeviceLog log) {

        long rowCount = 0;

        ContentValues contentValues = new ContentValues();
        contentValues.put(Config.COLUMN_LOG_STATUS, log.getStatus());
        contentValues.put(Config.COLUMN_LOG_TITLE, log.getTitle());
        contentValues.put(Config.COLUMN_LOG_DESC, log.getDescription());
        contentValues.put(Config.COLUMN_LOG_TIME, log.getTime());
        contentValues.put(Config.COLUMN_LOG_SYNC_STATUS, log.getSync_status());

        try {
            rowCount = sqLiteDatabase.update(Config.TABLE_LOGS, contentValues,
                    Config.COLUMN_LOG_ID + " = ? ",
                    new String[]{String.valueOf(log.getId())});
        } catch (SQLiteException e) {
            Log.d(TAG, "Exception: " + e.getMessage());
        } finally {
            Log.d(TAG, "updateLogsInfo: " + rowCount);
        }
        return rowCount;
    }


    public DeviceLog getDeviceLogById(String v_id) {

        Cursor cursor = null;
        DeviceLog log = null;
        try {

            cursor = sqLiteDatabase.query(Config.TABLE_LOGS, null,
                    Config.COLUMN_LOG_ID + " = ? ", new String[]{v_id},
                    null, null, null);

            if (cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndex(Config.COLUMN_LOG_ID));
                String status = cursor.getString(cursor.getColumnIndex(Config.COLUMN_LOG_STATUS));
                String title = cursor.getString(cursor.getColumnIndex(Config.COLUMN_LOG_TITLE));
                String description = cursor.getString(cursor.getColumnIndex(Config.COLUMN_LOG_DESC));
                String time = cursor.getString(cursor.getColumnIndex(Config.COLUMN_LOG_TIME));
                int sync_status = cursor.getInt(cursor.getColumnIndex(Config.COLUMN_LOG_SYNC_STATUS));
                log = new DeviceLog(id, status, title, description, time, sync_status);
            }
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.getMessage());
        } finally {
            if (cursor != null)
                cursor.close();
            Log.d(TAG, "getDeviceLogById: " + log);
        }
        return log;
    }


    public void syncVisits() {
        final String visitsSyncJsonArray = getAllSyncVisits();
        Log.d(TAG, "visitsSyncJsonArray: " + visitsSyncJsonArray);
        if (!visitsSyncJsonArray.equals("")) {
            //Toast.makeText(getApplicationContext(), "syncVisits process called", Toast.LENGTH_SHORT).show();
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "Start API Call SYNC CHECK ");
                    try {
                        Map<String, String> parameters = new HashMap<String, String>();
                        String token = AndroidUtils.readPreference(getApplicationContext(), AppConstants.token, "");
                        parameters.put("token", token);
                        parameters.put("visits", visitsSyncJsonArray);
                        Log.d(TAG, String.valueOf(parameters));
                        final JSONObject result = HttpRequester.IHttpPostRequest(getApplicationContext(), AppConstants.VISITS_SYNC, parameters, null, true, null);
                        Log.d(TAG, String.valueOf(result));
                        if (result.getString("status").equals("success")) {
                            JSONArray ids = result.getJSONArray("ids");
                            Log.d(TAG, String.valueOf(ids));
                            for (int i = 0; i < ids.length(); i++) {
                                String v_id = ids.getString(i);
                                Log.d(TAG, String.valueOf(v_id));
                                mVisit = getVisitById(v_id);
                                mVisit.setUser_name(mVisit.getUser_name());
                                mVisit.setFace_id(mVisit.getFace_id());
                                mVisit.setIn_time(mVisit.getIn_time());
                                mVisit.setOut_time(mVisit.getOut_time());
                                mVisit.setSync_status(1);
                                updateVisitInfo(mVisit);
                            }
                            DeviceLog log = new DeviceLog(-1, Config.LOG_SUCCESS, Config.OFFLINE_SYNC_TITLE, Config.OFFLINE_SYNC_VISITS_DESC, datetimeStamp, 0);
                            Log.d(TAG, "log Insert Data" + log.toString());
                            long id = insertLog(log);
                            if (id > 0) {
                                log.setId(id);
                                Log.d(TAG, "log Details inserted");
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
        } else {
            // Toast.makeText(getApplicationContext(), "SQLite and Remote MySQL DBs are in sync", Toast.LENGTH_SHORT).show();
        }
    }

    public void syncUsers() {
        try {
            client = Model.getInstance().getClient();

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "API Call => ALL VISITORS ");
                    try {
                        Map<String, String> parameters = new HashMap<String, String>();
                        String token = AndroidUtils.readPreference(getApplicationContext(), AppConstants.token, "");
                        Log.d(TAG, "token = " + token);
                        parameters.put("token", token);
                        final JSONObject result = HttpRequester.IHttpPostRequest(getApplicationContext(), AppConstants.ALL_VISITORS, parameters, null, true, null);
                        Log.d(TAG, String.valueOf(result));
                        if (result.getString("status").equals("success")) {

//                            String template = "{\"status\": \"success\", " +
//                                    "\"members\": [ " +
//                                    "{\"id\": \"1\", \"name\": \"kannan\", \"image\": \"https://scheck.vaango.co/1.jpeg\"}, " +
//                                    "{\"id\": \"2\", \"name\": \"Vinoth\", \"image\": \"https://scheck.vaango.co/6.jpeg\"}, " +
//                                    "{\"id\": \"3\", \"name\": \"Raj\", \"image\": \"https://scheck.vaango.co/3.jpeg\"}," +
//                                    "{\"id\": \"4\", \"name\": \"Bharad\", \"image\": \"https://scheck.vaango.co/4.jpeg\"}," +
//                                    "{\"id\": \"5\", \"name\": \"Vivek\", \"image\": \"https://scheck.vaango.co/5.jpeg\"}" +
//                                    " ]}";
//
//                            JSONObject jsonWithArrayInIt = new JSONObject(template); //JSONObject created for the template.
//                            JSONArray visits = jsonWithArrayInIt.getJSONArray("members"); //JSONArray of Items got from the JSONObject.

                            JSONArray visits = result.getJSONArray("members");
                            if (visits.length() > 0) {
                                Log.d(TAG, String.valueOf(visits));
                                Model.getInstance().getClient().clear();

                                Boolean userDeleted = deleteAllUsers();
                                Log.d(TAG, "users Table Deleted " + (userDeleted));
                                long usersDataCount = getNumberOfUsers();
//                                Boolean visitsDeleted = deleteAllVisits();
//                                DeviceLog.d(TAG, "visits Table Deleted " + (visitsDeleted));
                                long visitsDataCount = getNumberOfVisits();
                                Log.d(TAG, "user Table Count " + (usersDataCount));
                                Log.d(TAG, "visit Table Count " + (visitsDataCount));
                                for (int i = 0; i < visits.length(); i++) {
                                    JSONObject objects = visits.getJSONObject(i);
                                    String base64str = getByteArrayFromImageURL(objects.getString("image"));
                                    extractTemplate(base64str, objects.getString("id"), objects.getString("name"));
                                    if (i == (visits.length() - 1)) {
                                        Log.d(TAG, Arrays.toString(Model.getInstance().getClient().listIds()));
                                        Log.d(TAG, "-----ENROLLMENT COMPLETED------");
//                                        Model.getInstance().getClient().delete("2");
//                                        Log.d(TAG, Arrays.toString(Model.getInstance().getClient().listIds()));
                                        DeviceLog log = new DeviceLog(-1, Config.LOG_SUCCESS, Config.OFFLINE_SYNC_TITLE, Config.OFFLINE_SYNC_USERS_DESC+Arrays.toString(Model.getInstance().getClient().listIds()), datetimeStamp, 0);
                                        Log.d(TAG, "log Insert Data" + log.toString());
                                        long id = insertLog(log);
                                        if (id > 0) {
                                            log.setId(id);
                                            Log.d(TAG, "log Details inserted");
                                        }
                                    }
                                }
                                Log.d(TAG, "Database elements (" + Model.getInstance().getClient().listIds().length + ")");
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    private String getByteArrayFromImageURL(String url) {

        try {
            URL imageUrl = new URL(url);
            URLConnection ucon = imageUrl.openConnection();
            InputStream is = ucon.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int read = 0;
            while ((read = is.read(buffer, 0, buffer.length)) != -1) {
                baos.write(buffer, 0, read);
            }
            baos.flush();
            return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
        } catch (Exception e) {
            Log.d("Error", e.toString());
        }
        return null;
    }


    private void extractTemplate(String base64Str, String userId, String userName) throws IOException {

        NSubject subject = null;
        NFace face = null;
        NImage image = null;
        NBiometricTask task = null;
        NBiometricStatus status = null;

        try {
            subject = new NSubject();
            face = new NFace();
            byte[] data = Base64.decode(base64Str, Base64.DEFAULT);
            if (data != null) {
                ByteBuffer buffer = ByteBuffer.wrap(data);
                image = NImage.fromMemory(buffer);

                face.setImage(image);
                // Add face image to NSubject
                subject.getFaces().add(face);
                // Create task
                task = client.createTask(EnumSet.of(NBiometricOperation.CREATE_TEMPLATE), subject);
                // Perform task
                client.performTask(task);
                status = task.getStatus();
                if (task.getError() != null) {
                    Log.d(TAG, String.valueOf(task.getError()));
//                    showError(task.getError());
                    return;
                }

                if (subject.getFaces().size() > 1)
                    Log.d(TAG, String.format("Found %d faces\n", subject.getFaces().size() - 1));

                // List attributes for all located faces
                for (NFace nface : subject.getFaces()) {
                    for (NLAttributes attribute : nface.getObjects()) {
                        Rect rect = attribute.getBoundingRect();
                        Log.d(TAG, ("msg_face_found"));
                        Log.d(TAG, ("format_face_rect " + " left - " + rect.left + " top - " + rect.top + " right - " + rect.right + " bottom - " + rect.bottom));

                        if ((attribute.getRightEyeCenter().confidence > 0) || (attribute.getLeftEyeCenter().confidence > 0)) {
                            Log.d(TAG, ("msg_eyes_found"));
                            if (attribute.getRightEyeCenter().confidence > 0) {
                                Log.d(TAG, ("format_first_eye_location_confidence " + " x - " + attribute.getRightEyeCenter().x + " y - " + attribute.getRightEyeCenter().y + " confidence - " + attribute.getRightEyeCenter().confidence));
                            }
                            if (attribute.getLeftEyeCenter().confidence > 0) {
                                Log.d(TAG, ("format_second_eye_location_confidence " + " x - " + attribute.getLeftEyeCenter().x + " y - " + attribute.getLeftEyeCenter().y + " confidence - " + attribute.getLeftEyeCenter().confidence));
                            }
                        }
                    }
                }

                if (status == NBiometricStatus.OK) {
                    Log.d(TAG, String.format("msg_extraction_success Operation: %s, Status: %s", "template creation", status));
                    enroll(task.getSubjects().get(0), userId, userName);
                } else {
                    Log.d(TAG, String.format("msg_extraction_failed Operation: %s, Status: %s", "template creation", status));
                    DatabaseQueryClass databaseQueryClass = new DatabaseQueryClass(getContext());
                    int faceId = Integer.parseInt(userId);

                    User user = new User(-1, userName, 1, faceId, "0", 0);

                    long id = databaseQueryClass.insertUser(user);

                    if (id > 0) {
                        user.setId(id);
                        Log.d(TAG, String.format("Inserted User Name: %s, ID: %s", userName, id));
                    }
                }
            }
        } finally {
            if (subject != null) subject.dispose();
            if (face != null) face.dispose();
        }
    }

    private void enroll(NSubject subject1, String userId, String userName) throws IOException {

        NSubject subject = null;
        NFace face = null;
        NImage image = null;
        NBiometricTask task = null;
        NBiometricStatus status = null;

        try {
            subject = new NSubject();
            byte[] template1 = subject1.getTemplate().getFaces().save().toByteArray();
            Log.d(TAG, "template1 " + template1);
            NLTemplate nLTemplate = new NLTemplate(new NBuffer(template1));
            Log.d(TAG, "nLTemplate " + nLTemplate);
            Log.d(TAG, "nLTemplate not null " + nLTemplate);
            NTemplate template = new NTemplate();
            NLTemplate faceTemplate = new NLTemplate();
            for (NLRecord rec : nLTemplate.getRecords()) {
                Log.d(TAG, "mFaces add rec " + rec);
                faceTemplate.getRecords().add(rec);
            }
            template.setFaces(faceTemplate);
            subject.setTemplate(template);
            int lower = 1;
            int higher = 100;

            int random = (int) (Math.random() * (higher - lower)) + lower;
            subject.setId(userId);
            Log.d(TAG, "UserID-" + userId);

            // Create task
            task = client.createTask(EnumSet.of(NBiometricOperation.ENROLL_WITH_DUPLICATE_CHECK), subject);

            // Perform task
            client.performTask(task);
            status = task.getStatus();
            if (task.getError() != null) {
                Log.d(TAG, String.valueOf(task.getError()));
                return;
            }
            String enroll_status = "0";
            if (status == NBiometricStatus.OK) {
                enroll_status = "1";
                Log.d(TAG, String.format("enrollment_success Operation: %s, Status: %s", "enrollment", status));
            } else {
                enroll_status = "0";
                Log.d(TAG, String.format("enrollment_failed Operation: %s, Status: %s", "enrollment", status));
            }
            DatabaseQueryClass databaseQueryClass = new DatabaseQueryClass(getContext());
            int faceId = Integer.parseInt(userId);

            User user = new User(-1, userName, 1, faceId, enroll_status, 0);

            long id = databaseQueryClass.insertUser(user);

            if (id > 0) {
                user.setId(id);
                Log.d(TAG, String.format("Inserted User Name: %s, ID: %s", userName, id));
            }

        } finally {
            if (subject != null) subject.dispose();
            if (face != null) face.dispose();
        }
    }


    public String getAllSyncVisits() {

        Cursor cursor = null;
        try {

            String SELECT_QUERY = String.format("SELECT %s, %s, %s, %s, %s, %s FROM %s WHERE sync_status = '%s'", Config.COLUMN_VISIT_ID, Config.COLUMN_VISITOR_NAME, Config.COLUMN_VISIT_FACE_ID, Config.COLUMN_VISIT_TIME_IN, Config.COLUMN_VISIT_TIME_OUT, Config.COLUMN_VISIT_SYNC_STATUS, Config.TABLE_VISITS, "0");
            cursor = sqLiteDatabase.rawQuery(SELECT_QUERY, null);

            if (cursor != null)
                if (cursor.moveToFirst()) {
                    ArrayList<HashMap<String, String>> visitsListJson;
                    visitsListJson = new ArrayList<HashMap<String, String>>();
                    List<Visit> visitsList = new ArrayList<Visit>();
                    do {
                        int id = cursor.getInt(cursor.getColumnIndex(Config.COLUMN_VISIT_ID));
                        String name = cursor.getString(cursor.getColumnIndex(Config.COLUMN_VISITOR_NAME));
                        long face_id = cursor.getLong(cursor.getColumnIndex(Config.COLUMN_VISIT_FACE_ID));
                        String time_in = cursor.getString(cursor.getColumnIndex(Config.COLUMN_VISIT_TIME_IN));
                        String time_out = cursor.getString(cursor.getColumnIndex(Config.COLUMN_VISIT_TIME_OUT));
                        int sync_status = cursor.getInt(cursor.getColumnIndex(Config.COLUMN_VISIT_SYNC_STATUS));

                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put("id", String.valueOf(id));
                        map.put("vid", String.valueOf(face_id));
                        map.put("in", String.valueOf(time_in));
                        map.put("out", String.valueOf(time_out));
                        visitsListJson.add(map);
                        visitsList.add(new Visit(id, name, face_id, time_in, time_out, sync_status));
                    } while (cursor.moveToNext());
                    Log.d(TAG, "getAllSyncVisits: " + visitsList);
                    Gson gson = new GsonBuilder().create();
                    return gson.toJson(visitsListJson);
//                    return visitsList;
                }
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.getMessage());
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return "";
    }

    public long updateVisitInfo(Visit visit) {

        long rowCount = 0;

        ContentValues contentValues = new ContentValues();
        contentValues.put(Config.COLUMN_VISITOR_NAME, visit.getUser_name());
        contentValues.put(Config.COLUMN_VISIT_FACE_ID, visit.getFace_id());
        contentValues.put(Config.COLUMN_VISIT_TIME_IN, visit.getIn_time());
        contentValues.put(Config.COLUMN_VISIT_TIME_OUT, visit.getOut_time());
        contentValues.put(Config.COLUMN_VISIT_SYNC_STATUS, visit.getSync_status());

        try {
            rowCount = sqLiteDatabase.update(Config.TABLE_VISITS, contentValues,
                    Config.COLUMN_VISIT_ID + " = ? ",
                    new String[]{String.valueOf(visit.getId())});
        } catch (SQLiteException e) {
            Log.d(TAG, "Exception: " + e.getMessage());
        } finally {
            Log.d(TAG, "updateVisitInfo: " + rowCount);
        }
        return rowCount;
    }


    public Visit getVisitById(String v_id) {

        Cursor cursor = null;
        Visit visit = null;
        try {

            cursor = sqLiteDatabase.query(Config.TABLE_VISITS, null,
                    Config.COLUMN_VISIT_ID + " = ? ", new String[]{v_id},
                    null, null, null);

            if (cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndex(Config.COLUMN_VISIT_ID));
                String name = cursor.getString(cursor.getColumnIndex(Config.COLUMN_VISITOR_NAME));
                long face_id = cursor.getLong(cursor.getColumnIndex(Config.COLUMN_VISIT_FACE_ID));
                String time_in = cursor.getString(cursor.getColumnIndex(Config.COLUMN_VISIT_TIME_IN));
                String time_out = cursor.getString(cursor.getColumnIndex(Config.COLUMN_VISIT_TIME_OUT));
                int sync_status = cursor.getInt(cursor.getColumnIndex(Config.COLUMN_VISIT_SYNC_STATUS));
                visit = new Visit(id, name, face_id, time_in, time_out, sync_status);
            }
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.getMessage());
        } finally {
            if (cursor != null)
                cursor.close();
            Log.d(TAG, "getVisitById: " + visit);
        }
        return visit;
    }

    public boolean deleteAllUsers() {
        boolean deleteStatus = false;

        try {
            //for "1" delete() method returns number of deleted rows
            //if you don't want row count just use delete(TABLE_NAME, null, null)
            sqLiteDatabase.delete(Config.TABLE_USERS, null, null);

            long count = DatabaseUtils.queryNumEntries(sqLiteDatabase, Config.TABLE_USERS);

            if (count == 0)
                deleteStatus = true;

        } catch (SQLiteException e) {
            Log.d(TAG, "Exception: " + e.getMessage());
        } finally {
            Log.d(TAG, "deleteAllUsers: " + deleteStatus);
        }

        return deleteStatus;
    }

    public long getNumberOfUsers() {
        long count = -1;

        try {
            count = DatabaseUtils.queryNumEntries(sqLiteDatabase, Config.TABLE_USERS);
        } catch (SQLiteException e) {
            Log.d(TAG, "Exception: " + e.getMessage());
        } finally {
            Log.d(TAG, "getNumberOfUsers: " + count);
        }

        return count;
    }

    public long getNumberOfVisits() {
        long count = -1;

        try {
            count = DatabaseUtils.queryNumEntries(sqLiteDatabase, Config.TABLE_VISITS);
        } catch (SQLiteException e) {
            Log.d(TAG, "Exception: " + e.getMessage());
        } finally {
            Log.d(TAG, "getNumberOfVisits: " + count);
        }

        return count;
    }

    public boolean deleteAllVisits() {
        boolean deleteStatus = false;
        try {
            //for "1" delete() method returns number of deleted rows
            //if you don't want row count just use delete(TABLE_NAME, null, null)
            sqLiteDatabase.delete(Config.TABLE_VISITS, null, null);

            long count = DatabaseUtils.queryNumEntries(sqLiteDatabase, Config.TABLE_VISITS);

            if (count == 0)
                deleteStatus = true;

        } catch (SQLiteException e) {
            Log.d(TAG, "Exception: " + e.getMessage());
        } finally {
            Log.d(TAG, "deleteAllVisits: " + deleteStatus);
        }

        return deleteStatus;
    }

    public long insertLog(DeviceLog log){

        long id = -1;

        ContentValues contentValues = new ContentValues();
        contentValues.put(Config.COLUMN_LOG_STATUS, log.getStatus());
        contentValues.put(Config.COLUMN_LOG_TITLE, log.getTitle());
        contentValues.put(Config.COLUMN_LOG_DESC, log.getDescription());
        contentValues.put(Config.COLUMN_LOG_TIME, log.getTime());
        contentValues.put(Config.COLUMN_LOG_SYNC_STATUS, log.getSync_status());

        try {
            id = sqLiteDatabase.insertOrThrow(Config.TABLE_LOGS, null, contentValues);
        } catch (SQLiteException e){
            Log.d(TAG, "Exception: " + e.getMessage());
        } finally {
            Log.d(TAG, "insertLog" + id);
        }

        return id;
    }

}

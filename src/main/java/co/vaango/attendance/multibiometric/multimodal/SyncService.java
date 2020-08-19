package co.vaango.attendance.multibiometric.multimodal;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.vaango.attendance.R;
import co.vaango.attendance.multibiometric.Database.DatabaseHelper;
import co.vaango.attendance.multibiometric.Database.DatabaseQueryClass;
import co.vaango.attendance.multibiometric.Features.CreateStudent.Student;
import co.vaango.attendance.multibiometric.Model;
import co.vaango.attendance.multibiometric.modals.User;
import co.vaango.attendance.multibiometric.utils.AndroidUtils;
import co.vaango.attendance.multibiometric.utils.AppConstants;
import co.vaango.attendance.multibiometric.utils.Config;
import co.vaango.attendance.multibiometric.utils.HttpRequester;

import static com.neurotec.lang.NCore.getContext;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class SyncService extends JobService {
    private static final String TAG = SyncService.class.getSimpleName();
    private Student mStudent;
    private DatabaseHelper databaseHelper = DatabaseHelper.getInstance(this);
    private SQLiteDatabase sqLiteDatabase;
    private NBiometricClient client;

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.d("facecheck", "onStartJob called");
        if (databaseHelper != null) {
            sqLiteDatabase = databaseHelper.getWritableDatabase();
            if (sqLiteDatabase != null) {
                Log.d("facecheck", "sqLiteDatabase called");
                if (checkNetworkConnection(getApplicationContext())) {
                    Log.d("facecheck", "checkNetworkConnection syncStudents called");
//                    syncStudents();
                    Log.d("facecheck", "checkNetworkConnection syncUsers called");
                    syncUsers();
                } else {
                    Log.d("facecheck", "checkNetworkConnection syncStudents not called");
                }
            } else {
                Log.d("facecheck", "sqLiteDatabase not called");
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
        Log.d("facecheck", "onStopJob called");
        return false;
    }

    public void syncUsers() {
        try {
            Toast.makeText(getApplicationContext(), "syncUsers called", Toast.LENGTH_LONG).show();
            client = Model.getInstance().getClient();

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.d("facecheck", "Start API Call Device Info ");
                    try {
                        Map<String, String> parameters = new HashMap<String, String>();
                        String token = AndroidUtils.readPreference(getApplicationContext(), AppConstants.token, "");
                        Log.d("facecheck", "token = " + token);
                        parameters.put("token", token);
                        final JSONObject result = HttpRequester.IHttpPostRequest(getApplicationContext(), AppConstants.ALL_VISITORS, parameters, null, true, null);
                        Log.d("facecheck", String.valueOf(result));
                        if (result.getString("status").equals("success")) {

                            String template = "{\"status\": \"success\", " +
                                    "\"members\": [ " +
                                    "{\"id\": \"1\", \"name\": \"kannan\", \"image\": \"https://scheck.vaango.co/1.jpeg\"}, " +
                                    "{\"id\": \"2\", \"name\": \"Vinoth\", \"image\": \"https://scheck.vaango.co/6.jpeg\"}, " +
                                    "{\"id\": \"3\", \"name\": \"Raj\", \"image\": \"https://scheck.vaango.co/3.jpeg\"}," +
                                    "{\"id\": \"4\", \"name\": \"Bharad\", \"image\": \"https://scheck.vaango.co/4.jpeg\"}," +
                                    "{\"id\": \"5\", \"name\": \"Vivek\", \"image\": \"https://scheck.vaango.co/5.jpeg\"}" +
                                    " ]}";

                            JSONObject jsonWithArrayInIt = new JSONObject(template); //JSONObject created for the template.
//                            JSONArray visits = jsonWithArrayInIt.getJSONArray("members"); //JSONArray of Items got from the JSONObject.

                            JSONArray visits = result.getJSONArray("members");
                            if(visits.length()>0) {
                                Log.d("facecheck", String.valueOf(visits));
                                Model.getInstance().getClient().clear();

                                Boolean userDeleted = deleteAllUsers();
                                Log.d("facecheck", "users Table Deleted " + (userDeleted));
                                long usersDataCount = getNumberOfUsers();
                                Boolean visitsDeleted = deleteAllVisits();
                                Log.d("facecheck", "visits Table Deleted " + (visitsDeleted));
                                long visitsDataCount = getNumberOfVisits();
                                Log.d("facecheck", "user Table Count " + (usersDataCount));
                                Log.d("facecheck", "visit Table Count " + (visitsDataCount));
                                for (int i = 0; i < visits.length(); i++) {
                                    JSONObject objects = visits.getJSONObject(i);
                                    String base64str = getByteArrayFromImageURL(objects.getString("image"));
                                    extractTemplate(base64str, objects.getString("id"), objects.getString("name"));
                                    if (i == (visits.length() - 1)) {
                                        Log.d("facecheck", Arrays.toString(Model.getInstance().getClient().listIds()));
                                        Log.d("facecheck", "-----ENROLLMENT COMPLETED------");
//                                        Model.getInstance().getClient().delete("2");
//                                        DeviceLog.d("facecheck", Arrays.toString(Model.getInstance().getClient().listIds()));
                                    }
                                }
                                Log.d("facecheck", "Database elements (" + Model.getInstance().getClient().listIds().length + ")");
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
                    Log.d("facecheck", String.valueOf(task.getError()));
//                    showError(task.getError());
                    return;
                }

                if (subject.getFaces().size() > 1)
                    Log.d("facecheck", String.format("Found %d faces\n", subject.getFaces().size() - 1));

                // List attributes for all located faces
                for (NFace nface : subject.getFaces()) {
                    for (NLAttributes attribute : nface.getObjects()) {
                        Rect rect = attribute.getBoundingRect();
                        Log.d("facecheck", ("msg_face_found"));
                        Log.d("facecheck", ("format_face_rect " + " left - " + rect.left + " top - " + rect.top + " right - " + rect.right + " bottom - " + rect.bottom));

                        if ((attribute.getRightEyeCenter().confidence > 0) || (attribute.getLeftEyeCenter().confidence > 0)) {
                            Log.d("facecheck", ("msg_eyes_found"));
                            if (attribute.getRightEyeCenter().confidence > 0) {
                                Log.d("facecheck", ("format_first_eye_location_confidence " + " x - " + attribute.getRightEyeCenter().x + " y - " + attribute.getRightEyeCenter().y + " confidence - " + attribute.getRightEyeCenter().confidence));
                            }
                            if (attribute.getLeftEyeCenter().confidence > 0) {
                                Log.d("facecheck", ("format_second_eye_location_confidence " + " x - " + attribute.getLeftEyeCenter().x + " y - " + attribute.getLeftEyeCenter().y + " confidence - " + attribute.getLeftEyeCenter().confidence));
                            }
                        }
                    }
                }

                if (status == NBiometricStatus.OK) {
                    Log.d("facecheck", String.format("msg_extraction_success Operation: %s, Status: %s", "template creation", status));
                    enroll(task.getSubjects().get(0), userId, userName);
                } else {
                    Log.d("facecheck", String.format("msg_extraction_failed Operation: %s, Status: %s", "template creation", status));
                    DatabaseQueryClass databaseQueryClass = new DatabaseQueryClass(getContext());
                    int faceId = Integer.parseInt(userId);

                    User user = new User(-1, userName, 1, faceId, "0", 0);

                    long id = databaseQueryClass.insertUser(user);

                    if (id > 0) {
                        user.setId(id);
                        Log.d("facecheck", String.format("Inserted User Name: %s, ID: %s", userName, id));
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
            Log.d("facecheck", "template1 " + template1);
            NLTemplate nLTemplate = new NLTemplate(new NBuffer(template1));
            Log.d("facecheck", "nLTemplate " + nLTemplate);
            Log.d("facecheck", "nLTemplate not null " + nLTemplate);
            NTemplate template = new NTemplate();
            NLTemplate faceTemplate = new NLTemplate();
            for (NLRecord rec : nLTemplate.getRecords()) {
                Log.d("facecheck", "mFaces add rec " + rec);
                faceTemplate.getRecords().add(rec);
            }
            template.setFaces(faceTemplate);

            subject.setTemplate(template);
            int lower = 1;
            int higher = 100;

            int random = (int) (Math.random() * (higher - lower)) + lower;
            subject.setId(userId);
            Log.d("facecheck", "UserID-" + userId);

            // Create task
            task = client.createTask(EnumSet.of(NBiometricOperation.ENROLL_WITH_DUPLICATE_CHECK), subject);

            // Perform task
            client.performTask(task);
            status = task.getStatus();
            if (task.getError() != null) {
                Log.d("facecheck", String.valueOf(task.getError()));
                return;
            }
            String enroll_status = "0";
            if (status == NBiometricStatus.OK) {
                enroll_status = "1";
                Log.d("facecheck", String.format("enrollment_success Operation: %s, Status: %s", "enrollment", status));
            } else {
                enroll_status = "0";
                Log.d("facecheck", String.format("enrollment_failed Operation: %s, Status: %s", "enrollment", status));
            }
            DatabaseQueryClass databaseQueryClass = new DatabaseQueryClass(getContext());
            int faceId = Integer.parseInt(userId);

            User user = new User(-1, userName, 1, faceId, enroll_status, 0);

            long id = databaseQueryClass.insertUser(user);

            if (id > 0) {
                user.setId(id);
                Log.d("facecheck", String.format("Inserted User Name: %s, ID: %s", userName, id));
            }

        } finally {
            if (subject != null) subject.dispose();
            if (face != null) face.dispose();
        }
    }

    public void syncStudents() {
        List<Student> studentSyncList = getAllSyncStudent();
        if (studentSyncList.size() > 0) {
            Toast.makeText(getApplicationContext(), "Sync process called", Toast.LENGTH_LONG).show();
            for (final Student student : studentSyncList) {
                Log.d("facecheck", "syncStatus" + student.getSync_status());
                int sync_status = student.getSync_status();
                if (sync_status == 0) {
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("facecheck", "Start API Call SYNC CHECK ");
                            try {
                                Map<String, String> parameters = new HashMap<String, String>();
                                parameters.put("service_name", "addStudent");
                                parameters.put("name", student.getName());
                                parameters.put("registration_no", String.valueOf(student.getRegistrationNumber()));
                                parameters.put("phone", student.getPhoneNumber());
                                parameters.put("email", student.getEmail());
                                Log.d("facecheck", String.valueOf(parameters));
                                final JSONObject result = HttpRequester.IHttpPostRequest(getApplicationContext(), AppConstants.SYNC_CHECK, parameters, null, false, null);
                                Log.d("facecheck", String.valueOf(result));
                                if (result.getString("status").equals("success")) {
                                    mStudent = getStudentByRegNum(student.getRegistrationNumber());
                                    mStudent.setName(student.getName());
                                    mStudent.setRegistrationNumber(student.getRegistrationNumber());
                                    mStudent.setPhoneNumber(student.getPhoneNumber());
                                    mStudent.setEmail(student.getEmail());
                                    mStudent.setSync_status(1);
                                    updateStudentInfo(mStudent);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    thread.start();
                }
            }
        } else {
            Toast.makeText(getApplicationContext(), "SQLite and Remote MySQL DBs are in sync", Toast.LENGTH_LONG).show();
        }
    }


    public List<Student> getAllSyncStudent() {

        Cursor cursor = null;
        try {

            String SELECT_QUERY = String.format("SELECT %s, %s, %s, %s, %s, %s FROM %s WHERE sync_status = '%s'", Config.COLUMN_STUDENT_ID, Config.COLUMN_STUDENT_NAME, Config.COLUMN_STUDENT_REGISTRATION, Config.COLUMN_STUDENT_EMAIL, Config.COLUMN_STUDENT_PHONE, Config.COLUMN_SYNC_STATUS, Config.TABLE_STUDENT, "0");
            cursor = sqLiteDatabase.rawQuery(SELECT_QUERY, null);

            if (cursor != null)
                if (cursor.moveToFirst()) {
                    List<Student> studentList = new ArrayList<Student>();
                    do {
                        int id = cursor.getInt(cursor.getColumnIndex(Config.COLUMN_STUDENT_ID));
                        String name = cursor.getString(cursor.getColumnIndex(Config.COLUMN_STUDENT_NAME));
                        long registrationNumber = cursor.getLong(cursor.getColumnIndex(Config.COLUMN_STUDENT_REGISTRATION));
                        String email = cursor.getString(cursor.getColumnIndex(Config.COLUMN_STUDENT_EMAIL));
                        String phone = cursor.getString(cursor.getColumnIndex(Config.COLUMN_STUDENT_PHONE));
                        int sync_status = cursor.getInt(cursor.getColumnIndex(Config.COLUMN_SYNC_STATUS));

                        studentList.add(new Student(id, name, registrationNumber, email, phone, sync_status));
                    } while (cursor.moveToNext());
                    Logger.d("getAllSyncStudent: " + studentList);
                    return studentList;
                }
        } catch (Exception e) {
            Logger.d("Exception: " + e.getMessage());
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return Collections.emptyList();
    }

    public long updateStudentInfo(Student student) {

        long rowCount = 0;

        ContentValues contentValues = new ContentValues();
        contentValues.put(Config.COLUMN_STUDENT_NAME, student.getName());
        contentValues.put(Config.COLUMN_STUDENT_REGISTRATION, student.getRegistrationNumber());
        contentValues.put(Config.COLUMN_STUDENT_PHONE, student.getPhoneNumber());
        contentValues.put(Config.COLUMN_STUDENT_EMAIL, student.getEmail());
        contentValues.put(Config.COLUMN_SYNC_STATUS, student.getSync_status());

        try {
            rowCount = sqLiteDatabase.update(Config.TABLE_STUDENT, contentValues,
                    Config.COLUMN_STUDENT_ID + " = ? ",
                    new String[]{String.valueOf(student.getId())});
        } catch (SQLiteException e) {
            Logger.d("Exception: " + e.getMessage());
        } finally {
            Logger.d("updateStudentInfo: " + rowCount);
        }

        return rowCount;
    }


    public Student getStudentByRegNum(long registrationNum) {

        Cursor cursor = null;
        Student student = null;
        try {

            cursor = sqLiteDatabase.query(Config.TABLE_STUDENT, null,
                    Config.COLUMN_STUDENT_REGISTRATION + " = ? ", new String[]{String.valueOf(registrationNum)},
                    null, null, null);

            if (cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndex(Config.COLUMN_STUDENT_ID));
                String name = cursor.getString(cursor.getColumnIndex(Config.COLUMN_STUDENT_NAME));
                long registrationNumber = cursor.getLong(cursor.getColumnIndex(Config.COLUMN_STUDENT_REGISTRATION));
                String phone = cursor.getString(cursor.getColumnIndex(Config.COLUMN_STUDENT_PHONE));
                String email = cursor.getString(cursor.getColumnIndex(Config.COLUMN_STUDENT_EMAIL));
                int sync_status = cursor.getInt(cursor.getColumnIndex(Config.COLUMN_SYNC_STATUS));
                student = new Student(id, name, registrationNumber, phone, email, sync_status);
            }
        } catch (Exception e) {
            Logger.d("Exception: " + e.getMessage());
        } finally {
            if (cursor != null)
                cursor.close();
            Logger.d("getStudentByRegNum: " + student);
        }

        return student;
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
            Logger.d("Exception: " + e.getMessage());
        } finally {
            Logger.d("deleteAllUsers: " + deleteStatus);
        }

        return deleteStatus;
    }

    public long getNumberOfUsers() {
        long count = -1;

        try {
            count = DatabaseUtils.queryNumEntries(sqLiteDatabase, Config.TABLE_USERS);
        } catch (SQLiteException e) {
            Logger.d("Exception: " + e.getMessage());
        } finally {
            Logger.d("getNumberOfUsers: " + count);
        }

        return count;
    }

    public long getNumberOfVisits() {
        long count = -1;

        try {
            count = DatabaseUtils.queryNumEntries(sqLiteDatabase, Config.TABLE_VISITS);
        } catch (SQLiteException e) {
            Logger.d("Exception: " + e.getMessage());
        } finally {
            Logger.d("getNumberOfVisits: " + count);
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
            Logger.d("Exception: " + e.getMessage());
        } finally {
            Logger.d("deleteAllVisits: " + deleteStatus);
        }

        return deleteStatus;
    }

}

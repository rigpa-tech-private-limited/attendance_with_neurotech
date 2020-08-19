package co.vaango.attendance.multibiometric.multimodal;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.vaango.attendance.multibiometric.Database.DatabaseQueryClass;
import co.vaango.attendance.multibiometric.Features.CreateStudent.Student;
import co.vaango.attendance.multibiometric.Features.UpdateStudentInfo.StudentUpdateListener;
import co.vaango.attendance.multibiometric.utils.AppConstants;
import co.vaango.attendance.multibiometric.utils.Config;
import co.vaango.attendance.multibiometric.utils.HttpRequester;

public class NetworkMonitor extends BroadcastReceiver {
    private static StudentUpdateListener studentUpdateListener;
    private static int studentItemPosition;

    private Student mStudent;
    private DatabaseQueryClass databaseQueryClass;

    @Override
    public void onReceive(final Context context, Intent intent) {

        if(checkNetworkConnection(context)){
            databaseQueryClass = new DatabaseQueryClass(context);
            final List<Student> studentList = databaseQueryClass.getAllStudent();

            for(final Student student : studentList) {
                int sync_status = student.getSync_status();
                if(sync_status == 0){
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
                                final JSONObject result = HttpRequester.IHttpPostRequest(context, AppConstants.SYNC_CHECK, parameters, null, false, null);
                                Log.d("facecheck", String.valueOf(result));
                                if (!result.getString("status").equals("success")) {
                                    mStudent = databaseQueryClass.getStudentByRegNum(student.getRegistrationNumber());
                                    Toast.makeText(context, "SYNC_CHECK - success", Toast.LENGTH_SHORT).show();
                                    mStudent.setName(student.getName());
                                    mStudent.setRegistrationNumber(student.getRegistrationNumber());
                                    mStudent.setPhoneNumber(student.getPhoneNumber());
                                    mStudent.setEmail(student.getEmail());
                                    mStudent.setSync_status(0);

                                    long id = databaseQueryClass.updateStudentInfo(mStudent);

                                    if(id>0){
                                        studentUpdateListener.onStudentInfoUpdated(mStudent, studentItemPosition);
                                        context.sendBroadcast(new Intent(Config.UI_UPDATE_BROADCAST));
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    thread.start();

                }
            }

        }

    }

    public boolean checkNetworkConnection(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }
}

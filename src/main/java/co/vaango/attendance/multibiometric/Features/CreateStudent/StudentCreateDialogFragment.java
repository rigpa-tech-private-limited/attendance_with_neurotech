package co.vaango.attendance.multibiometric.Features.CreateStudent;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import co.vaango.attendance.multibiometric.Database.DatabaseQueryClass;
import co.vaango.attendance.R;
import co.vaango.attendance.multibiometric.multimodal.DbHelper;
import co.vaango.attendance.multibiometric.multimodal.DbVisit;
import co.vaango.attendance.multibiometric.multimodal.DeviceOTPActivity;
import co.vaango.attendance.multibiometric.multimodal.MultiModalActivity;
import co.vaango.attendance.multibiometric.multimodal.Visit;
import co.vaango.attendance.multibiometric.utils.AndroidUtils;
import co.vaango.attendance.multibiometric.utils.AppConstants;
import co.vaango.attendance.multibiometric.utils.Config;
import co.vaango.attendance.multibiometric.utils.HttpRequester;


public class StudentCreateDialogFragment extends DialogFragment {

    private static StudentCreateListener studentCreateListener;

    private EditText nameEditText;
    private EditText registrationEditText;
    private EditText phoneEditText;
    private EditText emailEditText;
    private Button createButton;
    private Button cancelButton;

    private String nameString = "";
    private long registrationNumber = -1;
    private String phoneString = "";
    private String emailString = "";

    public StudentCreateDialogFragment() {
        // Required empty public constructor
    }

    public static StudentCreateDialogFragment newInstance(String title, StudentCreateListener listener){
        studentCreateListener = listener;
        StudentCreateDialogFragment studentCreateDialogFragment = new StudentCreateDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        studentCreateDialogFragment.setArguments(args);

        studentCreateDialogFragment.setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialog);

        return studentCreateDialogFragment;
    }


    public boolean checkNetworkConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_student_create_dialog, container, false);

        nameEditText = view.findViewById(R.id.studentNameEditText);
        registrationEditText = view.findViewById(R.id.registrationEditText);
        phoneEditText = view.findViewById(R.id.phoneEditText);
        emailEditText = view.findViewById(R.id.emailEditText);
        createButton = view.findViewById(R.id.createButton);
        cancelButton = view.findViewById(R.id.cancelButton);

        String title = getArguments().getString(Config.TITLE);
        getDialog().setTitle(title);

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nameString = nameEditText.getText().toString();
                registrationNumber = Integer.parseInt(registrationEditText.getText().toString());
                phoneString = phoneEditText.getText().toString();
                emailString = emailEditText.getText().toString();

                if(checkNetworkConnection()){
                    Toast.makeText(getContext(), "checkNetworkConnection - true", Toast.LENGTH_SHORT).show();
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("facecheck", "Start API Call SYNC CHECK ");
                            try {
                                Map<String, String> parameters = new HashMap<String, String>();
                                parameters.put("service_name", "addStudent");
                                parameters.put("name", nameString);
                                parameters.put("registration_no", String.valueOf(registrationNumber));
                                parameters.put("phone", phoneString);
                                parameters.put("email", emailString);
                                final JSONObject result = HttpRequester.IHttpPostRequest(getContext(), AppConstants.SYNC_CHECK, parameters, null, false, null);
                                Log.d("facecheck", String.valueOf(result));
                                if (result.getString("status").equals("success")) {
                                    Log.d("facecheck", "SYNC_CHECK - success");
                                    Student student = new Student(-1, nameString, registrationNumber, phoneString, emailString, 1);

                                    DatabaseQueryClass databaseQueryClass = new DatabaseQueryClass(getContext());

                                    long id = databaseQueryClass.insertStudent(student);

                                    if (id > 0) {
                                        student.setId(id);
                                        studentCreateListener.onStudentCreated(student);
                                        getDialog().dismiss();
                                    }
                                } else {
                                    Log.d("facecheck", "SYNC_CHECK - failed");
                                    Student student = new Student(-1, nameString, registrationNumber, phoneString, emailString, 0);
                                    DatabaseQueryClass databaseQueryClass = new DatabaseQueryClass(getContext());

                                    long id = databaseQueryClass.insertStudent(student);

                                    if (id > 0) {
                                        student.setId(id);
                                        studentCreateListener.onStudentCreated(student);
                                        getDialog().dismiss();
                                    }
                                }
                            } catch (Exception e) {
                                Log.d("facecheck", "SYNC_CHECK - failed"+e);
                                Student student = new Student(-1, nameString, registrationNumber, phoneString, emailString, 0);
                                DatabaseQueryClass databaseQueryClass = new DatabaseQueryClass(getContext());

                                long id = databaseQueryClass.insertStudent(student);

                                if (id > 0) {
                                    student.setId(id);
                                    studentCreateListener.onStudentCreated(student);
                                    getDialog().dismiss();
                                }
                            }
                        }
                    });
                    thread.start();


                } else {
                    Toast.makeText(getContext(), "checkNetworkConnection - false", Toast.LENGTH_SHORT).show();
                    Student student = new Student(-1, nameString, registrationNumber, phoneString, emailString, 0);

                    DatabaseQueryClass databaseQueryClass = new DatabaseQueryClass(getContext());

                    long id = databaseQueryClass.insertStudent(student);

                    if (id > 0) {
                        student.setId(id);
                        studentCreateListener.onStudentCreated(student);
                        getDialog().dismiss();
                    }
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            //noinspection ConstantConditions
            dialog.getWindow().setLayout(width, height);
        }
    }

}

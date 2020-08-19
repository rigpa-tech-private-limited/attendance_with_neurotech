package co.vaango.attendance.multibiometric.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import co.vaango.attendance.multibiometric.Features.CreateStudent.Student;
import co.vaango.attendance.multibiometric.modals.DeviceLog;
import co.vaango.attendance.multibiometric.modals.User;
import co.vaango.attendance.multibiometric.modals.Visit;
import co.vaango.attendance.multibiometric.utils.Config;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DatabaseQueryClass {

    private Context context;

    public DatabaseQueryClass(Context context){
        this.context = context;
        Logger.addLogAdapter(new AndroidLogAdapter());
    }

    public long insertStudent(Student student){

        long id = -1;
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(context);
        SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(Config.COLUMN_STUDENT_NAME, student.getName());
        contentValues.put(Config.COLUMN_STUDENT_REGISTRATION, student.getRegistrationNumber());
        contentValues.put(Config.COLUMN_STUDENT_PHONE, student.getPhoneNumber());
        contentValues.put(Config.COLUMN_STUDENT_EMAIL, student.getEmail());
        contentValues.put(Config.COLUMN_SYNC_STATUS, student.getSync_status());

        try {
            id = sqLiteDatabase.insertOrThrow(Config.TABLE_STUDENT, null, contentValues);
        } catch (SQLiteException e){
            Logger.d("Exception: " + e.getMessage());
        } finally {
            sqLiteDatabase.close();
        }

        return id;
    }

    public List<Student> getAllStudent(){

        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(context);
        SQLiteDatabase sqLiteDatabase = databaseHelper.getReadableDatabase();

        Cursor cursor = null;
        try {

            cursor = sqLiteDatabase.query(Config.TABLE_STUDENT, null, null, null, null, null, null, null);

            /**
             // If you want to execute raw query then uncomment below 2 lines. And comment out above line.

             String SELECT_QUERY = String.format("SELECT %s, %s, %s, %s, %s FROM %s", Config.COLUMN_STUDENT_ID, Config.COLUMN_STUDENT_NAME, Config.COLUMN_STUDENT_REGISTRATION, Config.COLUMN_STUDENT_EMAIL, Config.COLUMN_STUDENT_PHONE, Config.TABLE_STUDENT);
             cursor = sqLiteDatabase.rawQuery(SELECT_QUERY, null);
             */

            if(cursor!=null)
                if(cursor.moveToFirst()){
                    List<Student> studentList = new ArrayList<Student>();
                    do {
                        int id = cursor.getInt(cursor.getColumnIndex(Config.COLUMN_STUDENT_ID));
                        String name = cursor.getString(cursor.getColumnIndex(Config.COLUMN_STUDENT_NAME));
                        long registrationNumber = cursor.getLong(cursor.getColumnIndex(Config.COLUMN_STUDENT_REGISTRATION));
                        String email = cursor.getString(cursor.getColumnIndex(Config.COLUMN_STUDENT_EMAIL));
                        String phone = cursor.getString(cursor.getColumnIndex(Config.COLUMN_STUDENT_PHONE));
                        int sync_status = cursor.getInt(cursor.getColumnIndex(Config.COLUMN_SYNC_STATUS));

                        studentList.add(new Student(id, name, registrationNumber, email, phone,sync_status));
                    }   while (cursor.moveToNext());

                    return studentList;
                }
        } catch (Exception e){
            Logger.d("Exception: " + e.getMessage());
        } finally {
            if(cursor!=null)
                cursor.close();
            sqLiteDatabase.close();
        }

        return Collections.emptyList();
    }

    public List<Student> getAllSyncStudent(){

        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(context);
        SQLiteDatabase sqLiteDatabase = databaseHelper.getReadableDatabase();

        Cursor cursor = null;
        try {

//            cursor = sqLiteDatabase.query(Config.TABLE_STUDENT, null, null, null, null, null, null, null);


             // If you want to execute raw query then uncomment below 2 lines. And comment out above line.

             String SELECT_QUERY = String.format("SELECT %s, %s, %s, %s, %s, %s FROM %s WHERE sync_status = '%s'", Config.COLUMN_STUDENT_ID, Config.COLUMN_STUDENT_NAME, Config.COLUMN_STUDENT_REGISTRATION, Config.COLUMN_STUDENT_EMAIL, Config.COLUMN_STUDENT_PHONE, Config.COLUMN_SYNC_STATUS, Config.TABLE_STUDENT,"0");
             cursor = sqLiteDatabase.rawQuery(SELECT_QUERY, null);


            if(cursor!=null)
                if(cursor.moveToFirst()){
                    List<Student> studentList = new ArrayList<Student>();
                    do {
                        int id = cursor.getInt(cursor.getColumnIndex(Config.COLUMN_STUDENT_ID));
                        String name = cursor.getString(cursor.getColumnIndex(Config.COLUMN_STUDENT_NAME));
                        long registrationNumber = cursor.getLong(cursor.getColumnIndex(Config.COLUMN_STUDENT_REGISTRATION));
                        String email = cursor.getString(cursor.getColumnIndex(Config.COLUMN_STUDENT_EMAIL));
                        String phone = cursor.getString(cursor.getColumnIndex(Config.COLUMN_STUDENT_PHONE));
                        int sync_status = cursor.getInt(cursor.getColumnIndex(Config.COLUMN_SYNC_STATUS));

                        studentList.add(new Student(id, name, registrationNumber, email, phone,sync_status));
                    }   while (cursor.moveToNext());

                    return studentList;
                }
        } catch (Exception e){
            Logger.d("Exception: " + e.getMessage());
        } finally {
            Logger.d("finally: called");
            if(cursor!=null)
                cursor.close();
            sqLiteDatabase.close();
            Logger.d("finally: sqLiteDatabase.close()");
        }

        return Collections.emptyList();
    }

    public Student getStudentByRegNum(long registrationNum){

        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(context);
        SQLiteDatabase sqLiteDatabase = databaseHelper.getReadableDatabase();

        Cursor cursor = null;
        Student student = null;
        try {

            cursor = sqLiteDatabase.query(Config.TABLE_STUDENT, null,
                    Config.COLUMN_STUDENT_REGISTRATION + " = ? ", new String[]{String.valueOf(registrationNum)},
                    null, null, null);

            /**
             // If you want to execute raw query then uncomment below 2 lines. And comment out above sqLiteDatabase.query() method.

             String SELECT_QUERY = String.format("SELECT * FROM %s WHERE %s = %s", Config.TABLE_STUDENT, Config.COLUMN_STUDENT_REGISTRATION, String.valueOf(registrationNum));
             cursor = sqLiteDatabase.rawQuery(SELECT_QUERY, null);
             */

            if(cursor.moveToFirst()){
                int id = cursor.getInt(cursor.getColumnIndex(Config.COLUMN_STUDENT_ID));
                String name = cursor.getString(cursor.getColumnIndex(Config.COLUMN_STUDENT_NAME));
                long registrationNumber = cursor.getLong(cursor.getColumnIndex(Config.COLUMN_STUDENT_REGISTRATION));
                String phone = cursor.getString(cursor.getColumnIndex(Config.COLUMN_STUDENT_PHONE));
                String email = cursor.getString(cursor.getColumnIndex(Config.COLUMN_STUDENT_EMAIL));
                int sync_status = cursor.getInt(cursor.getColumnIndex(Config.COLUMN_SYNC_STATUS));

                student = new Student(id, name, registrationNumber, phone, email, sync_status);
            }
        } catch (Exception e){
            Logger.d("Exception: " + e.getMessage());
        } finally {
            if(cursor!=null)
                cursor.close();
            sqLiteDatabase.close();
        }

        return student;
    }

    public long updateStudentInfo(Student student){

        long rowCount = 0;
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(context);
        SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(Config.COLUMN_STUDENT_NAME, student.getName());
        contentValues.put(Config.COLUMN_STUDENT_REGISTRATION, student.getRegistrationNumber());
        contentValues.put(Config.COLUMN_STUDENT_PHONE, student.getPhoneNumber());
        contentValues.put(Config.COLUMN_STUDENT_EMAIL, student.getEmail());
        contentValues.put(Config.COLUMN_SYNC_STATUS, student.getSync_status());

        try {
            rowCount = sqLiteDatabase.update(Config.TABLE_STUDENT, contentValues,
                    Config.COLUMN_STUDENT_ID + " = ? ",
                    new String[] {String.valueOf(student.getId())});
        } catch (SQLiteException e){
            Logger.d("Exception: " + e.getMessage());
        } finally {
            sqLiteDatabase.close();
        }

        return rowCount;
    }

    public long deleteStudentByRegNum(long registrationNum) {
        long deletedRowCount = -1;
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(context);
        SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();

        try {
            deletedRowCount = sqLiteDatabase.delete(Config.TABLE_STUDENT,
                    Config.COLUMN_STUDENT_REGISTRATION + " = ? ",
                    new String[]{ String.valueOf(registrationNum)});
        } catch (SQLiteException e){
            Logger.d("Exception: " + e.getMessage());
        } finally {
            sqLiteDatabase.close();
        }

        return deletedRowCount;
    }

    public boolean deleteAllStudents(){
        boolean deleteStatus = false;
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(context);
        SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();

        try {
            //for "1" delete() method returns number of deleted rows
            //if you don't want row count just use delete(TABLE_NAME, null, null)
            sqLiteDatabase.delete(Config.TABLE_STUDENT, null, null);

            long count = DatabaseUtils.queryNumEntries(sqLiteDatabase, Config.TABLE_STUDENT);

            if(count==0)
                deleteStatus = true;

        } catch (SQLiteException e){
            Logger.d("Exception: " + e.getMessage());
        } finally {
            sqLiteDatabase.close();
        }

        return deleteStatus;
    }

    public long insertUser(User user){

        long id = -1;
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(context);
        SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(Config.COLUMN_USER_NAME, user.getUser_name());
        contentValues.put(Config.COLUMN_USER_TYPE, user.getUser_type());
        contentValues.put(Config.COLUMN_FACE_ID, user.getFace_id());
        contentValues.put(Config.COLUMN_ENROLL_STATUS, user.getEnroll_status());
        contentValues.put(Config.COLUMN_SYNC_STATUS, user.getSync_status());

        try {
            id = sqLiteDatabase.insertOrThrow(Config.TABLE_USERS, null, contentValues);
        } catch (SQLiteException e){
            Logger.d("Exception: " + e.getMessage());
        } finally {
            sqLiteDatabase.close();
        }

        return id;
    }

    public List<User> getAllUsers(){

        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(context);
        SQLiteDatabase sqLiteDatabase = databaseHelper.getReadableDatabase();

        Cursor cursor = null;
        try {

            cursor = sqLiteDatabase.query(Config.TABLE_USERS, null, null, null, null, null, null, null);

            /**
             // If you want to execute raw query then uncomment below 2 lines. And comment out above line.

             String SELECT_QUERY = String.format("SELECT %s, %s, %s, %s, %s, %s FROM %s", Config.COLUMN_USER_ID, Config.COLUMN_USER_NAME, Config.COLUMN_USER_TYPE, Config.COLUMN_FACE_ID, Config.COLUMN_ENROLL_STATUS, Config.COLUMN_SYNC_STATUS, Config.TABLE_USERS);
             cursor = sqLiteDatabase.rawQuery(SELECT_QUERY, null);
             */

            if(cursor!=null)
                if(cursor.moveToFirst()){
                    List<User> usersList = new ArrayList<User>();
                    do {
                        int id = cursor.getInt(cursor.getColumnIndex(Config.COLUMN_USER_ID));
                        String name = cursor.getString(cursor.getColumnIndex(Config.COLUMN_USER_NAME));
                        int user_type = cursor.getInt(cursor.getColumnIndex(Config.COLUMN_USER_TYPE));
                        int face_id = cursor.getInt(cursor.getColumnIndex(Config.COLUMN_FACE_ID));
                        String enroll_status = cursor.getString(cursor.getColumnIndex(Config.COLUMN_ENROLL_STATUS));
                        int sync_status = cursor.getInt(cursor.getColumnIndex(Config.COLUMN_SYNC_STATUS));


                        usersList.add(new User(id, name, user_type, face_id, enroll_status, sync_status));
                    }   while (cursor.moveToNext());

                    return usersList;
                }
        } catch (Exception e){
            Logger.d("Exception: " + e.getMessage());
        } finally {
            if(cursor!=null)
                cursor.close();
            sqLiteDatabase.close();
        }

        return Collections.emptyList();
    }


    public User getUserByFaceId(int faceid){

        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(context);
        SQLiteDatabase sqLiteDatabase = databaseHelper.getReadableDatabase();

        Cursor cursor = null;
        User user = null;
        try {

            cursor = sqLiteDatabase.query(Config.TABLE_USERS, null,
                    Config.COLUMN_FACE_ID + " = ? ", new String[]{String.valueOf(faceid)},
                    null, null, null);

            if(cursor.moveToFirst()){
                int id = cursor.getInt(cursor.getColumnIndex(Config.COLUMN_USER_ID));
                String name = cursor.getString(cursor.getColumnIndex(Config.COLUMN_USER_NAME));
                int user_type = cursor.getInt(cursor.getColumnIndex(Config.COLUMN_USER_TYPE));
                int face_id = cursor.getInt(cursor.getColumnIndex(Config.COLUMN_FACE_ID));
                String enroll_status = cursor.getString(cursor.getColumnIndex(Config.COLUMN_ENROLL_STATUS));
                int sync_status = cursor.getInt(cursor.getColumnIndex(Config.COLUMN_SYNC_STATUS));

                user = new User(id, name, user_type, face_id, enroll_status, sync_status);
            }
        } catch (Exception e){
            Logger.d("Exception: " + e.getMessage());
        } finally {
            if(cursor!=null)
                cursor.close();
            sqLiteDatabase.close();
        }

        return user;
    }

    public boolean deleteAllUsers(){
        boolean deleteStatus = false;
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(context);
        SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();

        try {
            //for "1" delete() method returns number of deleted rows
            //if you don't want row count just use delete(TABLE_NAME, null, null)
            sqLiteDatabase.delete(Config.TABLE_USERS, null, null);

            long count = DatabaseUtils.queryNumEntries(sqLiteDatabase, Config.TABLE_USERS);

            if(count==0)
                deleteStatus = true;

        } catch (SQLiteException e){
            Logger.d("Exception: " + e.getMessage());
        } finally {
            sqLiteDatabase.close();
        }

        return deleteStatus;
    }

    public long getNumberOfUsers(){
        long count = -1;
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(context);
        SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();

        try {
            count = DatabaseUtils.queryNumEntries(sqLiteDatabase, Config.TABLE_USERS);
        } catch (SQLiteException e){
            Logger.d("Exception: " + e.getMessage());
        } finally {
            sqLiteDatabase.close();
        }

        return count;
    }


    public long insertVisit(Visit visit){

        long id = -1;
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(context);
        SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(Config.COLUMN_VISITOR_NAME, visit.getUser_name());
        contentValues.put(Config.COLUMN_VISIT_FACE_ID, visit.getFace_id());
        contentValues.put(Config.COLUMN_VISIT_TIME_IN, visit.getIn_time());
        contentValues.put(Config.COLUMN_VISIT_TIME_OUT, visit.getOut_time());
        contentValues.put(Config.COLUMN_VISIT_SYNC_STATUS, visit.getSync_status());

        try {
            id = sqLiteDatabase.insertOrThrow(Config.TABLE_VISITS, null, contentValues);
        } catch (SQLiteException e){
            Logger.d("Exception: " + e.getMessage());
        } finally {
            sqLiteDatabase.close();
        }

        return id;
    }

    public List<Visit> getAllVisits(){

        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(context);
        SQLiteDatabase sqLiteDatabase = databaseHelper.getReadableDatabase();

        Cursor cursor = null;
        try {

            cursor = sqLiteDatabase.query(Config.TABLE_VISITS, null, null, null, null, null, null, null);

            if(cursor!=null)
                if(cursor.moveToFirst()){
                    List<Visit> visitsList = new ArrayList<Visit>();
                    do {
                        int id = cursor.getInt(cursor.getColumnIndex(Config.COLUMN_VISIT_ID));
                        String name = cursor.getString(cursor.getColumnIndex(Config.COLUMN_VISITOR_NAME));
                        int face_id = cursor.getInt(cursor.getColumnIndex(Config.COLUMN_VISIT_FACE_ID));
                        String in_time = cursor.getString(cursor.getColumnIndex(Config.COLUMN_VISIT_TIME_IN));
                        String out_time = cursor.getString(cursor.getColumnIndex(Config.COLUMN_VISIT_TIME_OUT));
                        int sync_status = cursor.getInt(cursor.getColumnIndex(Config.COLUMN_VISIT_SYNC_STATUS));


                        visitsList.add(new Visit(id, name, face_id, in_time, out_time, sync_status));
                    }   while (cursor.moveToNext());

                    return visitsList;
                }
        } catch (Exception e){
            Logger.d("Exception: " + e.getMessage());
        } finally {
            if(cursor!=null)
                cursor.close();
            sqLiteDatabase.close();
        }

        return Collections.emptyList();
    }

    public Visit getVisitByFaceId(int faceid){

        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(context);
        SQLiteDatabase sqLiteDatabase = databaseHelper.getReadableDatabase();

        Cursor cursor = null;
        Visit visit = null;
        try {

//            cursor = sqLiteDatabase.query(Config.TABLE_VISITS, null,
//                    Config.COLUMN_VISIT_FACE_ID + " = ? ", new String[]{String.valueOf(faceid)},
//                    null, null, null);


             // If you want to execute raw query then uncomment below 2 lines. And comment out above sqLiteDatabase.query() method.

             String SELECT_QUERY = String.format("SELECT * FROM %s WHERE %s = %s AND %s = %s", Config.TABLE_VISITS, Config.COLUMN_VISIT_FACE_ID, String.valueOf(faceid), Config.COLUMN_VISIT_TIME_OUT,"''");
            Log.d("facecheck", SELECT_QUERY);
             cursor = sqLiteDatabase.rawQuery(SELECT_QUERY, null);

            if(cursor.moveToFirst()){

                int id = cursor.getInt(cursor.getColumnIndex(Config.COLUMN_VISIT_ID));
                String name = cursor.getString(cursor.getColumnIndex(Config.COLUMN_VISITOR_NAME));
                int face_id = cursor.getInt(cursor.getColumnIndex(Config.COLUMN_VISIT_FACE_ID));
                String in_time = cursor.getString(cursor.getColumnIndex(Config.COLUMN_VISIT_TIME_IN));
                String out_time = cursor.getString(cursor.getColumnIndex(Config.COLUMN_VISIT_TIME_OUT));
                int sync_status = cursor.getInt(cursor.getColumnIndex(Config.COLUMN_VISIT_SYNC_STATUS));

                visit = new Visit(id, name, face_id, in_time, out_time, sync_status);
            }
        } catch (Exception e){
            Logger.d("Exception: " + e.getMessage());
        } finally {
            if(cursor!=null)
                cursor.close();
            sqLiteDatabase.close();
        }

        return visit;
    }


    public long updateVisitInfo(Visit visit){

        long rowCount = 0;
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(context);
        SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(Config.COLUMN_VISITOR_NAME, visit.getUser_name());
        contentValues.put(Config.COLUMN_VISIT_TIME_IN, visit.getIn_time());
        contentValues.put(Config.COLUMN_VISIT_TIME_OUT, visit.getOut_time());
        contentValues.put(Config.COLUMN_VISIT_SYNC_STATUS, visit.getSync_status());

        try {
            rowCount = sqLiteDatabase.update(Config.TABLE_VISITS, contentValues,
                    Config.COLUMN_VISIT_FACE_ID + " = ? AND "+Config.COLUMN_VISIT_TIME_OUT + " = ? ",
                    new String[] {String.valueOf(visit.getFace_id()),""});
        } catch (SQLiteException e){
            Logger.d("Exception: " + e.getMessage());
        } finally {
            sqLiteDatabase.close();
        }

        return rowCount;
    }

    public long getNumberOfVisits(){
        long count = -1;
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(context);
        SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();

        try {
            count = DatabaseUtils.queryNumEntries(sqLiteDatabase, Config.TABLE_VISITS);
        } catch (SQLiteException e){
            Logger.d("Exception: " + e.getMessage());
        } finally {
            sqLiteDatabase.close();
        }

        return count;
    }

    public boolean deleteAllVisits(){
        boolean deleteStatus = false;
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(context);
        SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();

        try {
            //for "1" delete() method returns number of deleted rows
            //if you don't want row count just use delete(TABLE_NAME, null, null)
            sqLiteDatabase.delete(Config.TABLE_VISITS, null, null);

            long count = DatabaseUtils.queryNumEntries(sqLiteDatabase, Config.TABLE_VISITS);

            if(count==0)
                deleteStatus = true;

        } catch (SQLiteException e){
            Logger.d("Exception: " + e.getMessage());
        } finally {
            sqLiteDatabase.close();
        }

        return deleteStatus;
    }



    public long insertLog(DeviceLog log){

        long id = -1;
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(context);
        SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(Config.COLUMN_LOG_STATUS, log.getStatus());
        contentValues.put(Config.COLUMN_LOG_TITLE, log.getTitle());
        contentValues.put(Config.COLUMN_LOG_DESC, log.getDescription());
        contentValues.put(Config.COLUMN_LOG_TIME, log.getTime());
        contentValues.put(Config.COLUMN_LOG_SYNC_STATUS, log.getSync_status());

        try {
            id = sqLiteDatabase.insertOrThrow(Config.TABLE_LOGS, null, contentValues);
        } catch (SQLiteException e){
            Logger.d("Exception: " + e.getMessage());
        } finally {
            sqLiteDatabase.close();
        }

        return id;
    }
}
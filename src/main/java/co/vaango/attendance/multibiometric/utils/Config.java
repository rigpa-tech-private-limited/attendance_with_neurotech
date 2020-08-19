package co.vaango.attendance.multibiometric.utils;

public class Config {

    public static final Integer IN_TIME_BUFFER = 1;
    public static final String DATABASE_NAME = "student-db";

    //column names of student table
    public static final String TABLE_STUDENT = "student";
    public static final String COLUMN_STUDENT_ID = "_id";
    public static final String COLUMN_STUDENT_NAME = "name";
    public static final String COLUMN_STUDENT_REGISTRATION = "registration_no";
    public static final String COLUMN_STUDENT_PHONE = "phone";
    public static final String COLUMN_STUDENT_EMAIL = "email";

    //column names of users table
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "_id";
    public static final String COLUMN_USER_NAME = "name";
    public static final String COLUMN_USER_TYPE = "type";
    public static final String COLUMN_FACE_ID = "face_id";
    public static final String COLUMN_ENROLL_STATUS = "enroll_status";
    public static final String COLUMN_SYNC_STATUS = "sync_status";

    //column names of visits table
    public static final String TABLE_VISITS = "visits";
    public static final String COLUMN_VISIT_ID = "_id";
    public static final String COLUMN_VISITOR_NAME = "user_name";
    public static final String COLUMN_VISIT_FACE_ID = "face_id";
    public static final String COLUMN_VISIT_TIME_IN = "in_time";
    public static final String COLUMN_VISIT_TIME_OUT = "out_time";
    public static final String COLUMN_VISIT_SYNC_STATUS = "sync_status";
    public static final String FACE_ID_CONSTRAINT = "face_id_unique";

    //column names of users table
    public static final String TABLE_LOGS = "logs";
    public static final String COLUMN_LOG_ID = "_id";
    public static final String COLUMN_LOG_STATUS = "status";
    public static final String COLUMN_LOG_TITLE = "title";
    public static final String COLUMN_LOG_DESC = "description";
    public static final String COLUMN_LOG_TIME = "time";
    public static final String COLUMN_LOG_SYNC_STATUS = "sync_status";

    //others for general purpose key-value pair data
    public static final String TITLE = "title";
    public static final String CREATE_STUDENT = "create_student";
    public static final String UPDATE_STUDENT = "update_student";


    public static final String LOG_SUCCESS = "success";
    public static final String LOG_FAILED = "failed";
    public static final String LICENSE_TITLE = "Getting Internet Licenses";
    public static final String LICENSE_OBTAINED = "Licenses were obtained";
    public static final String LICENSE_NOT_OBTAINED = "Licenses were not obtained";
    public static final String LICENSE_PARTIALLY_OBTAINED = "Licenses were partially obtained";
    public static final String LICENSE_ALREADY_OBTAINED = "Licenses were already obtained";

    public static final String OFFLINE_SYNC_TITLE = "Sync with server";
    public static final String OFFLINE_SYNC_JOB_DESC = "Peroidic sync with server started";
    public static final String OFFLINE_SYNC_USERS_DESC = "Check if any user enrolled newly or removed from database. The following Face IDs are synced with device ";
    public static final String OFFLINE_SYNC_VISITS_DESC = "Visits Data synced";
    public static final String OFFLINE_SYNC_DEVICE_INFO_DESC = "Devico info (battery status, signal strength) synced";

    public static final String OPEN_APP_TITLE = "App Launch";
    public static final String OPEN_APP_DESC = "Opening the application";

    public static final String UI_UPDATE_BROADCAST = "co.vaango.attendance.uiupdatebroadcast";
}
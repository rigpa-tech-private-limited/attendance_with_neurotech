package co.vaango.attendance.multibiometric.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AppConstants {

    public static final String SUCCESS = "success";
    public static final String ERROR = "error";
    public static final String STATUS = "status";
    public static final String MESSAGE = "message";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String AUTHORIZATION = "Authorization";
    public static final String BEARER = "Bearer";
    // Connection settings
    public static final int CONNECTION_READ_TIMEOUT = 10000;
    public static final int SERVER_CONNECTION_TIMEOUT = 60000;
//    public static final String API_URL = "https://scheck.vaango.co/api/v0/";
    public static final String APP_VERSION = "1.0.0";
    public static final String API_URL = "http://182.65.121.30:3000/api/v0/";
    public static final Boolean ATTENDANCE_FLAG = true;
    public static final String DEVICE_OTP = API_URL + "settings/otp";
    public static final String DEVICE_INFO = API_URL + "settings/details";
    public static final String FACE_API = API_URL + "visitor/face";
    public static final String NEW_VISITOR = API_URL + "visitor/new";
    public static final String ALL_VISITORS = API_URL + "visitor/all";
    public static final String ATTENDANCE = API_URL + "visitor/attendance";
    public static final String PLATE_COUNT = API_URL + "visitor/plate";
    public static final String VISITS_SYNC = API_URL + "sync/attendance";
    public static final String DEVICE_LOGS_SYNC = API_URL + "sync/device_logs";
    public static final String DEVICE_LOG_SYNC = API_URL + "visitor/device_log";
    public static final String DEVICE_INFO_SYNC = API_URL + "sync/device_info";
    public static final String SYNC_CHECK = "http://172.20.10.2:8888/heeracars-api/index.php";
    public static final int APP_CLOSE_CONFIRMATION_WAIT_TIME = 2 * 1000;
    public static final String token = "token";


    public enum DateFormat {
        DEFAULT("yyyy-MM-dd HH:mm:ss"),
        CHAT_TIMESTAMP("HH:mm a"),
        FILE_NAME("ddMMyyyy_HHmmss"),
        MONTH_DAY_YEAR("MMM dd yyyy"),
        HOUR_MINUTES("HH:mm"),
        MINUTES_SECONDS("mm:ss"),
        ISO("yyyy-MM-dd'T'HH:mm'Z'"),
        HOUR_MINUTES_NO_COLON("HHmm"),
        DATE_ALONE("dd/MM/yyyy"),
        DEFAULT_DATE("yyyy-MM-dd"),
        DAY_MONTH_YEAR("MMM dd yyyy"),
        MONTH_YEAR("MMMM yyyy");

        public SimpleDateFormat value;

        private DateFormat(String value) {
            this.value = new SimpleDateFormat(value, Locale.getDefault());
        }

        public String getFormattedString(Date date) {
            return value.format(date);
        }

        public String getFormattedStringWithQuotes(Date date) {
            return "'" + value.format(date) + "'";
        }

    }

    public enum MimeType {
        PLAIN("text/plain"), HTML("text/html"), MULTIPART_FORM_DATA("multipart/form-data"), JSON(
                "application/json"), IMAGE("image/*");
        public String value;

        private MimeType(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            // TODO Auto-generated method stub
            return value;
        }
    }

    public enum CharacterEncoding {
        UTF_8("UTF-8");

        public String value;

        private CharacterEncoding(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            // TODO Auto-generated method stub
            return value;
        }
    }
}

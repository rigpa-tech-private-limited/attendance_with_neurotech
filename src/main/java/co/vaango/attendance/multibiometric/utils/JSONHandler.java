package co.vaango.attendance.multibiometric.utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;

public class JSONHandler {

    public static <T> T getJSONValue(JSONObject jsonObject, String key, Class<T> returnType) {
        try {
            if (jsonObject.has(key) && !returnType.isAssignableFrom(Date.class)
                    && !(returnType.isAssignableFrom(float.class) || returnType.isAssignableFrom(Float.class)))
                return (T) jsonObject.get(key);
            else {
                if (returnType.isAssignableFrom(String.class))
                    return (T) "";
                if (returnType.isAssignableFrom(Date.class)) {
                    Date date = null;
                    try {
                        date = AppConstants.DateFormat.DEFAULT.value.parse(jsonObject.getString(key));
                    } catch (Exception e) {
                    }
                    return (T) date;
                } else if (returnType.isAssignableFrom(int.class) || returnType.isAssignableFrom(Integer.class))
                    return (T) Integer.valueOf(-1);
                else if (returnType.isAssignableFrom(boolean.class) || returnType.isAssignableFrom(Boolean.class))
                    return (T) Boolean.valueOf(false);
                else if (returnType.isAssignableFrom(long.class) || returnType.isAssignableFrom(Long.class))
                    return (T) Long.valueOf(-1l);
                else if (returnType.isAssignableFrom(float.class) || returnType.isAssignableFrom(Float.class)) {
                    try {
                        return (T) Float.valueOf(jsonObject.get(key).toString());
                    } catch (Exception e) {
                    }
                    return (T) Float.valueOf(-1f);
                } else if (returnType.isAssignableFrom(double.class) || returnType.isAssignableFrom(Double.class))
                    return (T) Double.valueOf(-1d);
                else if (returnType.isAssignableFrom(JSONObject.class))
                    return (T) new JSONObject();
                else if (returnType.isAssignableFrom(JSONArray.class))
                    return (T) new JSONArray();
                else
                    return null;
            }
        } catch (Exception e) {
            return null;
        }
    }
}

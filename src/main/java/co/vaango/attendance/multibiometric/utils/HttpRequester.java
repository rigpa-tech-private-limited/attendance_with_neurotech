package co.vaango.attendance.multibiometric.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ProgressBar;

import co.vaango.attendance.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

public class HttpRequester {

    private static ProgressDialog progress;

    public static class ParameterBuilder {

        public static ParameterBuilder getInstance() {
            return new ParameterBuilder();
        }

        private HashMap<String, String> parameters = new HashMap<String, String>();

        public ParameterBuilder addParameter(String key, String value) {
            parameters.put(key, value);
            return this;
        }

        public HashMap<String, String> build() {
            return parameters;
        }
    }

    public interface HttpRequesterCallback {

        public void onSuccess(JSONObject jsonResponse);

        public void onError(String errorMessage);

    }

    public static JSONObject HttpPostRequest(final Context context, final String url,
                                             final Map<String, String> parameters, final Map<String, String> headers, final boolean includeToken)
            throws Exception {
        return Executors.newSingleThreadExecutor().submit(new Callable<JSONObject>() {

            @Override
            public JSONObject call() throws Exception {
                // TODO Auto-generated method stub
                return IHttpPostRequest(context, url, parameters, headers, includeToken, null);
            }
        }).get();
    }

    public static void HttpPostRequest(final Context context, final String url, final Map<String, String> parameters,
                                       final boolean showLoading, final Map<String, String> headers, final boolean includeToken,
                                       @NonNull final HttpRequesterCallback callback) {
        new AsyncTask<String, Void, JSONObject>() {

            @Override
            protected JSONObject doInBackground(String... params) {
                // TODO Auto-generated method stub
                try {
                    return IHttpPostRequest(context, url, parameters, headers, includeToken, null);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPreExecute() {
                if (showLoading)
                    showLoadingDialog(context);
            }

            ;

            @Override
            protected void onPostExecute(JSONObject result) {
                // TODO Auto-generated method stub
                if (showLoading)
                    dismissLoadingDialog();
                if (result == null)
                    callback.onError(AndroidUtils.isNetworkAvailable(context)
                            ? context.getString(R.string.CONNECTION_ERROR_MESSAGE)
                            : context.getString(R.string.NETWORK_UNAVAILABLE));
                else {
                    try {
                        if (result.getString(AppConstants.STATUS).equals(AppConstants.ERROR))
                            callback.onError(JSONHandler.getJSONValue(result, AppConstants.MESSAGE, String.class));
                        else
                            callback.onSuccess(result);
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        callback.onError(AndroidUtils.isNetworkAvailable(context)
                                ? context.getString(R.string.CONNECTION_ERROR_MESSAGE)
                                : context.getString(R.string.NETWORK_UNAVAILABLE));
                    }
                }
            }

        }.execute("");
    }

    public static JSONObject HttpPostJsonBodyRequest(final Context context, final String url, final String body,
                                                     final boolean includeToken) throws Exception {
        return Executors.newSingleThreadExecutor().submit(new Callable<JSONObject>() {

            @Override
            public JSONObject call() throws Exception {
                return IHttpPostRequest(context, url, null, ParameterBuilder.getInstance().addParameter(AppConstants.CONTENT_TYPE, AppConstants.MimeType.JSON.value).build(), includeToken, body);
            }
        }).get();
    }

    public static void HttpPostJsonBodyRequest(final Context context, final String url, final String body,
                                               final boolean showLoading, final boolean includeToken, final HttpRequesterCallback callback) {
        new AsyncTask<String, Void, JSONObject>() {

            @Override
            protected JSONObject doInBackground(String... params) {
                // TODO Auto-generated method stub
                try {
                    return IHttpPostRequest(context, url, null,
                            ParameterBuilder.getInstance()
                                    .addParameter(AppConstants.CONTENT_TYPE, AppConstants.MimeType.JSON.value).build(),
                            includeToken, null);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPreExecute() {
                if (showLoading)
                    showLoadingDialog(context);
            }

            ;

            @Override
            protected void onPostExecute(JSONObject result) {
                // TODO Auto-generated method stub
                if (showLoading)
                    dismissLoadingDialog();
                if (result == null)
                    callback.onError(AndroidUtils.isNetworkAvailable(context)
                            ? context.getString(R.string.CONNECTION_ERROR_MESSAGE)
                            : context.getString(R.string.NETWORK_UNAVAILABLE));
                else {
                    try {
                        if (result.getString(AppConstants.STATUS).equals(AppConstants.ERROR))
                            callback.onError(JSONHandler.getJSONValue(result, AppConstants.MESSAGE, String.class));
                        else
                            callback.onSuccess(result);
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        callback.onError(AndroidUtils.isNetworkAvailable(context)
                                ? context.getString(R.string.CONNECTION_ERROR_MESSAGE)
                                : context.getString(R.string.NETWORK_UNAVAILABLE));
                    }
                }
            }

        }.execute("");
    }

    public static JSONObject IHttpPostRequest(final Context context, String url, Map<String, String> parameters,
                                               Map<String, String> headers, boolean includeToken, String body) throws Exception {
        URL address = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) address.openConnection();
        conn.setReadTimeout(AppConstants.CONNECTION_READ_TIMEOUT);
        conn.setConnectTimeout(AppConstants.SERVER_CONNECTION_TIMEOUT);
        conn.setRequestMethod("POST");
        conn.setDoInput(true);
        conn.setDoOutput(true);
        Uri.Builder builder = new Uri.Builder();
        if (headers != null)
            for (String header : headers.keySet())
                conn.setRequestProperty(header, headers.get(header));
        if (includeToken) {
//            User user = new User().getModel(context);
//            conn.setRequestProperty(AppConstants.AUTHORIZATION, AppConstants.BEARER + " " + user.getToken());
        }
        if (parameters != null || body != null) {
            for (String param : parameters.keySet())
                builder.appendQueryParameter(param, parameters.get(param));
            String query = builder.build().getEncodedQuery();
            Log.d("facecheck","query=>"+query);
            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, AppConstants.CharacterEncoding.UTF_8.value));
            if (!StringTools.isEmpty(query))
                writer.write(query);
            if (!StringTools.isEmpty(body))
                writer.write(URLEncoder.encode(body, AppConstants.CharacterEncoding.UTF_8.value));
            writer.flush();
            writer.close();
            os.close();
        }
        conn.connect();
        InputStream responseStream;
        if (conn.getResponseCode() >= 400 && conn.getResponseCode() < 600)
            responseStream = conn.getErrorStream();
        else
            responseStream = conn.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(responseStream));
        String l;
        StringBuffer sb = new StringBuffer();
        while ((l = br.readLine()) != null)
            sb.append(l);
        Log.d("JSONObject Debug", sb.toString());
//        jObj = new JSONObject(json);
        return new JSONObject(sb.toString());
    }

    public static JSONObject HttpGetRequest(final Context context, final String url,
                                            final Map<String, String> parameters, final Map<String, String> headers, final boolean includeToken)
            throws Exception {
        return Executors.newSingleThreadExecutor().submit(new Callable<JSONObject>() {

            @Override
            public JSONObject call() throws Exception {
                // TODO Auto-generated method stub
                return IHttpGetRequest(context, url, parameters, headers, includeToken);
            }
        }).get();
    }

    public static void HttpGetRequest(final Context context, final String url, final Map<String, String> parameters,
                                      final boolean showLoading, final Map<String, String> headers, final boolean includeToken,
                                      final HttpRequesterCallback callback) {
        new AsyncTask<String, Void, JSONObject>() {

            @Override
            protected JSONObject doInBackground(String... params) {
                // TODO Auto-generated method stub
                try {
                    return IHttpGetRequest(context, url, parameters, headers, includeToken);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPreExecute() {
                if (showLoading)
                    showLoadingDialog(context);
            }

            ;

            @Override
            protected void onPostExecute(JSONObject result) {
                // TODO Auto-generated method stub
                if (showLoading)
                    dismissLoadingDialog();
                if (result == null)
                    callback.onError(AndroidUtils.isNetworkAvailable(context)
                            ? context.getString(R.string.CONNECTION_ERROR_MESSAGE)
                            : context.getString(R.string.NETWORK_UNAVAILABLE));
                else {
                    try {
                        if (result.getString(AppConstants.STATUS).equals(AppConstants.ERROR))
                            callback.onError(JSONHandler.getJSONValue(result, AppConstants.MESSAGE, String.class));
                        else
                            callback.onSuccess(result);
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        callback.onError(AndroidUtils.isNetworkAvailable(context)
                                ? context.getString(R.string.CONNECTION_ERROR_MESSAGE)
                                : context.getString(R.string.NETWORK_UNAVAILABLE));
                    }
                }
            }

        }.execute("");
    }

    private static JSONObject IHttpGetRequest(final Context context, String url, Map<String, String> parameters,
                                              Map<String, String> headers, boolean includeToken) throws Exception {
        URL address;
        if (parameters != null && !parameters.isEmpty()) {
            Uri.Builder builder = new Uri.Builder();
            for (String param : parameters.keySet())
                builder.appendQueryParameter(param, parameters.get(param));
            String query = builder.build().getEncodedQuery();
            address = new URL(url + "?" + query);
        } else
            address = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) address.openConnection();
        conn.setReadTimeout(AppConstants.CONNECTION_READ_TIMEOUT);
        conn.setConnectTimeout(AppConstants.SERVER_CONNECTION_TIMEOUT);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        if (headers != null)
            for (String header : headers.keySet())
                conn.setRequestProperty(header, headers.get(header));
        if (includeToken) {
//            User user = new User().getModel(context);
//            conn.setRequestProperty(AppConstants.AUTHORIZATION, AppConstants.BEARER + " " + user.getToken());
        }
        conn.connect();
        InputStream responseStream;
        if (conn.getResponseCode() >= 400 && conn.getResponseCode() < 600)
            responseStream = conn.getErrorStream();
        else
            responseStream = conn.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(responseStream));
        String l;
        StringBuffer sb = new StringBuffer();
        while ((l = br.readLine()) != null)
            sb.append(l);
        return new JSONObject(sb.toString());
    }

    public static JSONObject HttpMultipartRequest(final Context context, final String url,
                                                  final Map<String, String> parameters, final Map<String, File> files, final Map<String, String> headers)
            throws Exception {
        return Executors.newSingleThreadExecutor().submit(new Callable<JSONObject>() {

            @Override
            public JSONObject call() throws Exception {
                // TODO Auto-generated method stub
                return IHttpMultipartRequest(context, url, parameters, files, headers);
            }
        }).get();
    }

    public static void HttpMultipartRequest(final Context context, final String url,
                                            final Map<String, String> parameters, final Map<String, File> files, final Map<String, String> headers,
                                            final boolean showLoading, final HttpRequesterCallback callback) {
        new AsyncTask<String, Void, JSONObject>() {

            @Override
            protected JSONObject doInBackground(String... params) {
                // TODO Auto-generated method stub
                try {
                    return IHttpMultipartRequest(context, url, parameters, files, headers);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPreExecute() {
                if (showLoading)
                    showLoadingDialog(context);
            }

            ;

            @Override
            protected void onPostExecute(JSONObject result) {
                // TODO Auto-generated method stub
                if (showLoading)
                    dismissLoadingDialog();
                if (result == null)
                    callback.onError(AndroidUtils.isNetworkAvailable(context)
                            ? context.getString(R.string.CONNECTION_ERROR_MESSAGE)
                            : context.getString(R.string.NETWORK_UNAVAILABLE));
                else {
                    try {
                        if (result.getString(AppConstants.STATUS).equals(AppConstants.ERROR))
                            callback.onError(JSONHandler.getJSONValue(result, AppConstants.MESSAGE, String.class));
                        else
                            callback.onSuccess(result);
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        callback.onError(AndroidUtils.isNetworkAvailable(context)
                                ? context.getString(R.string.CONNECTION_ERROR_MESSAGE)
                                : context.getString(R.string.NETWORK_UNAVAILABLE));
                    }
                }
            }

        }.execute("");
    }

    private static JSONObject IHttpMultipartRequest(Context context, String url, Map<String, String> parameters,
                                                    Map<String, File> files, Map<String, String> headers) throws Exception {
        MultipartUtility multipartUtility = new MultipartUtility(url);
        if (parameters != null)
            for (String param : parameters.keySet())
                multipartUtility.addFormField(param, parameters.get(param));
        if (files != null)
            for (String file : files.keySet())
                multipartUtility.addFilePart(file, files.get(file));
        if (headers != null)
            for (String header : headers.keySet())
                multipartUtility.addHeaderField(header, headers.get(header));
        return multipartUtility.finish();
    }

    private static class MultipartUtility {
        private final String boundary;
        private static final String LINE_FEED = "\r\n";
        private HttpURLConnection httpConn;
        private String charset;
        private OutputStream outputStream;
        private PrintWriter writer;

        /**
         * This constructor initializes a new HTTP POST request with content
         * type is set to multipart/form-data
         *
         * @param requestURL
         * @throws IOException
         */
        public MultipartUtility(String requestURL) throws IOException {
            this.charset = AppConstants.CharacterEncoding.UTF_8.value;

            // creates a unique boundary based on time stamp
            boundary = "===" + System.currentTimeMillis() + "===";
            URL url = new URL(requestURL);
            httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setUseCaches(false);
            httpConn.setDoOutput(true); // indicates POST method
            httpConn.setDoInput(true);
            httpConn.setRequestProperty("Content-Type", AppConstants.MimeType.MULTIPART_FORM_DATA.value + "; boundary=" + boundary);
            outputStream = httpConn.getOutputStream();
            writer = new PrintWriter(new OutputStreamWriter(outputStream, charset), true);
        }

        /**
         * Adds a form field to the request
         *
         * @param name  field name
         * @param value field value
         */
        public void addFormField(String name, String value) {
            writer.append("--" + boundary).append(LINE_FEED);
            writer.append("Content-Disposition: form-data; name=\"" + name + "\"").append(LINE_FEED);
            writer.append("Content-Type: ;" + AppConstants.MimeType.PLAIN.value + " charset=" + charset).append(LINE_FEED);
            writer.append(LINE_FEED);
            writer.append(value).append(LINE_FEED);
            writer.flush();
        }

        /**
         * Adds a upload file section to the request
         *
         * @param fieldName  name attribute in <input type="file" name="..." />
         * @param uploadFile a File to be uploaded
         * @throws IOException
         */
        public void addFilePart(String fieldName, File uploadFile) throws IOException {
            String fileName = uploadFile.getName();
            writer.append("--" + boundary).append(LINE_FEED);
            writer.append("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + fileName + "\"")
                    .append(LINE_FEED);
            writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(fileName)).append(LINE_FEED);
            writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
            writer.append(LINE_FEED);
            writer.flush();

            FileInputStream inputStream = new FileInputStream(uploadFile);
            byte[] buffer = new byte[4096];
            int bytesRead = -1;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
            inputStream.close();
            writer.append(LINE_FEED);
            writer.flush();
        }

        /**
         * Adds a header field to the request.
         *
         * @param name  - name of the header field
         * @param value - value of the header field
         */
        public void addHeaderField(String name, String value) {
            writer.append(name + ": " + value).append(LINE_FEED);
            writer.flush();
        }

        /**
         * Completes the request and receives response from the server.
         *
         * @return a list of Strings as response in case the server returned
         * status OK, otherwise an exception is thrown.
         * @throws Exception
         */
        public JSONObject finish() throws Exception {
            writer.append(LINE_FEED).flush();
            writer.append("--" + boundary + "--").append(LINE_FEED);
            writer.close();
            StringBuffer sb = new StringBuffer();
            // checks server's status code first
            int status = httpConn.getResponseCode();
            if (status == HttpURLConnection.HTTP_OK) {
                InputStream responseStream;
                if (httpConn.getResponseCode() >= 400 && httpConn.getResponseCode() < 600)
                    responseStream = httpConn.getErrorStream();
                else
                    responseStream = httpConn.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(responseStream));
                String l;
                while ((l = br.readLine()) != null)
                    sb.append(l);
                br.close();
                httpConn.disconnect();
            } else {
                throw new IOException("Server returned non-OK status: " + status);
            }
            return new JSONObject(sb.toString());
        }
    }

    public static void showLoadingDialog(Context context) {
        try {
            if (progress != null)
                progress = null;
            progress = new ProgressDialog(context);
            try {
                progress.show();
            } catch (WindowManager.BadTokenException e) {

            }
            progress.setCancelable(false);
            progress.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            ProgressBar progressBar = new ProgressBar(context);
            progressBar.setIndeterminate(true);
            progress.setContentView(progressBar);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void dismissLoadingDialog() {
        try {
            if (progress != null)
                progress.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

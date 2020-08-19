package co.vaango.attendance.multibiometric.multimodal;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import co.vaango.attendance.R;
import co.vaango.attendance.multibiometric.utils.AndroidUtils;
import co.vaango.attendance.multibiometric.utils.AppConstants;
import co.vaango.attendance.multibiometric.utils.BaseActivity;
import co.vaango.attendance.multibiometric.utils.HttpRequester;
import co.vaango.attendance.multibiometric.utils.PopUpHelper;

public class WelcomeScreenActivity extends BaseActivity implements View.OnClickListener {


    private TextView welcome_text;
    private  ImageView welcome_image;
    private LinearLayout welcome_screen_ll;
    private ImageView company_logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_screen);
        welcome_text = (TextView) findViewById(R.id.welcome_text);
        welcome_image = (ImageView) findViewById(R.id.welcome_image);
        welcome_screen_ll = (LinearLayout) findViewById(R.id.welcome_screen_ll);
        company_logo = (ImageView) findViewById(R.id.company_logo);
        welcome_screen_ll.setOnClickListener(this);
        Log.d("facecheck", "Btn Click");
        AndroidUtils.showLoadingDialog(this);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("facecheck", "Start API Call Device Info ");
                try {
                    Map<String, String> parameters = new HashMap<String, String>();
                    String token = AndroidUtils.readPreference(WelcomeScreenActivity.this, AppConstants.token, "");
                    Log.d("facecheck","token = "+token);
                    parameters.put("token", token);
                    final JSONObject result = HttpRequester.IHttpPostRequest(WelcomeScreenActivity.this, AppConstants.DEVICE_INFO, parameters, null, true, null);
                    Log.d("facecheck", String.valueOf(result));
                    if (!result.getString("status").equals("success")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AndroidUtils.dismissLoadingDialog();
                                try {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(WelcomeScreenActivity.this);
                                    builder.setMessage(result.getString("message"))
                                            .setCancelable(false)
                                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    AndroidUtils.writePreference(WelcomeScreenActivity.this, AppConstants.token, "");
                                                    Intent intent = new Intent(WelcomeScreenActivity.this, DeviceOTPActivity.class);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(intent);
                                                }
                                            });
                                    AlertDialog alert = builder.create();
                                    alert.show();
                                    //PopUpHelper.ok(BiometricActivity.this, result.getString("error"), false);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.d("facecheck", String.valueOf(result));
                                try {
                                    JSONObject company_settings = result.getJSONObject("company");
                                    Log.d("facecheck", "welcome_text_plain = "+company_settings.getString("welcome_text_plain"));
                                    Log.d("facecheck", "welcome_screen = "+company_settings.getString("welcome_screen"));
                                    Log.d("facecheck", "logo = "+company_settings.getString("logo"));

                                    welcome_text.setText(company_settings.getString("welcome_text_plain"));
                                    welcome_screen_ll.setBackgroundColor(Color.parseColor(company_settings.getString("welcome_color")));
                                    new ImageLoadTask(company_settings.getString("welcome_screen"), welcome_image).execute();
                                    new ImageLoadTask(company_settings.getString("logo"), company_logo).execute();
                                    AndroidUtils.dismissLoadingDialog();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.welcome_screen_ll:
                Log.d("facecheck", "welcome_screen_ll Click");
                Intent intent = new Intent(WelcomeScreenActivity.this, MultiModalActivity.class);
                startActivity(intent);
                break;

        }
    }

    public class ImageLoadTask extends AsyncTask<Void, Void, Bitmap> {

        private String url;
        private ImageView imageView;

        public ImageLoadTask(String url, ImageView imageView) {
            this.url = url;
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            try {
                URL urlConnection = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) urlConnection
                        .openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                return myBitmap;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            imageView.setImageBitmap(result);
        }

    }
}



package co.vaango.attendance.multibiometric.multimodal;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import co.vaango.attendance.R;
import co.vaango.attendance.multibiometric.utils.AndroidUtils;
import co.vaango.attendance.multibiometric.utils.AppConstants;
import co.vaango.attendance.multibiometric.utils.BaseActivity;
import co.vaango.attendance.multibiometric.utils.HttpRequester;
import co.vaango.attendance.multibiometric.utils.PopUpHelper;

public class FaceRecognitionActivity extends BaseActivity implements View.OnClickListener {
    private String baseEncodeStr;
    private ImageView person_image;
    @SuppressLint("WrongThread")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.face_recognition);
        Button otp = (Button) findViewById(R.id.continue_button);
        person_image = (ImageView) findViewById(R.id.person_image);
        otp.setOnClickListener(this);
        Bitmap bitmapOrg = BitmapFactory.decodeResource(getResources(),  R.drawable.vinoth);
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        bitmapOrg.compress(Bitmap.CompressFormat.JPEG, 100, bao);
        byte [] ba = bao.toByteArray();
        baseEncodeStr = "data:image/png;base64,"+Base64.encodeToString(ba,Base64.DEFAULT);
        AndroidUtils.writePreference(FaceRecognitionActivity.this, "person_image", Base64.encodeToString(ba,Base64.DEFAULT));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.continue_button:
                Log.d("facecheck", "Btn Click");
                AndroidUtils.showLoadingDialog(this);
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("facecheck", "Start API Call - baseEncodeStr = "+baseEncodeStr);
                        String token = AndroidUtils.readPreference(FaceRecognitionActivity.this, AppConstants.token, "");
                        try {
                            Map<String, String> parameters = new HashMap<String, String>();
                            parameters.put("profile", baseEncodeStr);
                            parameters.put("token", token);
                            parameters.put("flow_id", "1");
                            final JSONObject result = HttpRequester.IHttpPostRequest(FaceRecognitionActivity.this, AppConstants.FACE_API, parameters, null, true, null);
                            Log.d("facecheck", String.valueOf(result));
                            if (result == null) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        AndroidUtils.dismissLoadingDialog();
                                        PopUpHelper.ok(FaceRecognitionActivity.this, getString(R.string.CONNECTION_ERROR_MESSAGE), false);
                                    }
                                });
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        AndroidUtils.dismissLoadingDialog();
                                        Log.d("facecheck", String.valueOf(result));
                                        try {
                                            JSONObject user = result.getJSONObject("user");
                                            JSONObject work_hours = user.getJSONObject("work_hours");

                                            Intent i = new Intent(FaceRecognitionActivity.this, WelcomeBackActivity.class);
                                            i.putExtra("user_name", user.getString("user_name"));
                                            i.putExtra("last_visit", user.getString("last_visit"));
                                            i.putExtra("footer_text", user.getString("footer_text"));
                                            i.putExtra("today", work_hours.getString("today"));
                                            i.putExtra("week", work_hours.getString("week"));
                                            i.putExtra("month", work_hours.getString("month"));
                                            startActivity(i);
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
                break;
        }
    }
}

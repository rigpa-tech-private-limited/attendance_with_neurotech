package co.vaango.attendance.multibiometric.multimodal;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;

import co.vaango.attendance.R;
import co.vaango.attendance.multibiometric.utils.AndroidUtils;
import co.vaango.attendance.multibiometric.utils.AppConstants;
import co.vaango.attendance.multibiometric.utils.BaseActivity;

public class WelcomeBackActivity extends BaseActivity {

    private LinearLayout footer_ll,user_name_ll,user_img_ll,oops_ll;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_back);

        footer_ll = (LinearLayout) findViewById(R.id.footer_text_ll);
        user_name_ll = (LinearLayout) findViewById(R.id.user_name_ll);
        user_img_ll = (LinearLayout) findViewById(R.id.user_img_ll);
        oops_ll = (LinearLayout) findViewById(R.id.oops_ll);
        String user_name = (String) getIntent().getSerializableExtra("user_name");
        String last_visit = (String) getIntent().getSerializableExtra("last_visit");
        String footer_text = (String) getIntent().getSerializableExtra("footer_text");
        String today = (String) getIntent().getSerializableExtra("today");
        String week = (String) getIntent().getSerializableExtra("week");
        String month = (String) getIntent().getSerializableExtra("month");
        Boolean check_out = (Boolean) getIntent().getSerializableExtra("check_out");
        Boolean new_user = (Boolean) getIntent().getSerializableExtra("new_user");
        String person_image = AndroidUtils.readPreference(WelcomeBackActivity.this, "person_image", "");
        Log.d("facecheck", person_image);
        TextView username = (TextView) findViewById(R.id.user_name);
        username.setText(user_name);

        TextView footertext = (TextView) findViewById(R.id.footer_text);
        footertext.setText(footer_text);
        if(new_user) {
            oops_ll.setVisibility(View.VISIBLE);
            user_name_ll.setVisibility(View.GONE);
            user_img_ll.setVisibility(View.GONE);
            footer_ll.setVisibility(View.GONE);
            byte[] imageAsBytes = Base64.decode(person_image.getBytes(), Base64.DEFAULT);
            ImageView image = (ImageView) this.findViewById(R.id.oops_image);

            image.setImageBitmap(BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length));
            Handler h = new Handler();
            int delay = 3 * 1000; //1 second=1000 miliseconds
            Runnable runnable;

            h.postDelayed(runnable = new Runnable() {
                public void run() {
                    try {
                        Intent i = new Intent(WelcomeBackActivity.this, FaceActivity.class);
                        startActivity(i);
                        Log.d("facecheck", "WelcomeScreenActivity call");
                    } catch (Exception e) {
                        Log.d("facecheck", "WelcomeScreenActivity error = " + e);
                    }
                }
            }, delay);
        } else {
            oops_ll.setVisibility(View.GONE);
            user_name_ll.setVisibility(View.VISIBLE);
            user_img_ll.setVisibility(View.VISIBLE);
            footer_ll.setVisibility(View.VISIBLE);
            if (check_out) {
                footer_ll.setBackgroundColor(getResources().getColor(R.color.checkout_red));
            } else {
                footer_ll.setBackgroundColor(getResources().getColor(R.color.checkin_green));
            }

            TextView work_hours = (TextView) findViewById(R.id.work_hours);
            work_hours.setText("Today : " + today + "\nWeek : " + week + "\nMonth : " + month);

            byte[] imageAsBytes = Base64.decode(person_image.getBytes(), Base64.DEFAULT);
            ImageView image = (ImageView) this.findViewById(R.id.person_image);

            image.setImageBitmap(BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length));
            Handler h = new Handler();
            int delay = 2 * 1000; //1 second=1000 miliseconds
            Runnable runnable;

            h.postDelayed(runnable = new Runnable() {
                public void run() {
                    try {
                        Intent i = new Intent(WelcomeBackActivity.this, FaceActivity.class);
                        startActivity(i);
                        Log.d("facecheck", "WelcomeScreenActivity call");
                    } catch (Exception e) {
                        Log.d("facecheck", "WelcomeScreenActivity error = " + e);
                    }
                }
            }, delay);
        }
    }

    public Bitmap rotateImage(int angle, Bitmap bitmapSrc) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(bitmapSrc, 0, 0, bitmapSrc.getWidth(), bitmapSrc.getHeight(), matrix, true);
    }
}

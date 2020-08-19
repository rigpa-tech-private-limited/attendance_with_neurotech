package co.vaango.attendance.multibiometric.multimodal;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import co.vaango.attendance.R;
import co.vaango.attendance.multibiometric.utils.AndroidUtils;
import co.vaango.attendance.multibiometric.utils.BaseActivity;
import cdflynn.android.library.checkview.CheckView;


public class AttendanceViewActivity extends BaseActivity {

    CheckView checkInTick,checkOutTick;
    Boolean check_out = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attendance_view);
        LinearLayout user_img_ll = (LinearLayout) findViewById(R.id.user_img_ll);
        LinearLayout oops_ll = (LinearLayout) findViewById(R.id.oops_ll);
        String user_name = (String) getIntent().getSerializableExtra("user_name");
        String footer_text = (String) getIntent().getSerializableExtra("footer_text");
        check_out = (Boolean) getIntent().getSerializableExtra("check_out");
        Boolean user_exist = (Boolean) getIntent().getSerializableExtra("user_exist");
        String person_image = AndroidUtils.readPreference(AttendanceViewActivity.this, "person_image", "");
        Log.d("facecheck", person_image);
        TextView username = (TextView) findViewById(R.id.user_name);
        username.setText(user_name);

        TextView footertext = (TextView) findViewById(R.id.footer_text);
        footertext.setText(footer_text);
        if(!user_exist) {
            oops_ll.setVisibility(View.VISIBLE);
            user_img_ll.setVisibility(View.GONE);
            byte[] imageAsBytes = Base64.decode(person_image.getBytes(), Base64.DEFAULT);
            ImageView image = (ImageView) this.findViewById(R.id.oops_image);

            image.setImageBitmap(BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length));
            Handler h = new Handler();
            int delay = 2 * 1000;
            Runnable runnable;
            h.postDelayed(runnable = new Runnable() {
                public void run() {
                    try {
                        Intent i = new Intent(AttendanceViewActivity.this, FaceActivity.class);
                        startActivity(i);
                        Log.d("facecheck", "WelcomeScreenActivity call");
                    } catch (Exception e) {
                        Log.d("facecheck", "WelcomeScreenActivity error = " + e);
                    }
                }
            }, delay);
        } else {
            oops_ll.setVisibility(View.GONE);
            user_img_ll.setVisibility(View.VISIBLE);

            byte[] imageAsBytes = Base64.decode(person_image.getBytes(), Base64.DEFAULT);
            de.hdodenhof.circleimageview.CircleImageView image = (de.hdodenhof.circleimageview.CircleImageView) this.findViewById(R.id.person_image);

            image.setImageBitmap(BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length));

            checkInTick = (CheckView) findViewById(R.id.check_in);
            checkOutTick = (CheckView) findViewById(R.id.check_out);
            if (check_out) {
//                user_img_ll.setBackgroundColor(getResources().getColor(R.color.checkout_red));
                image.setBorderColor(getResources().getColor(R.color.checkout_red));
                footertext.setTextColor(getResources().getColor(R.color.checkout_red));
            } else {
                image.setBorderColor(getResources().getColor(R.color.checkin_green));
                footertext.setTextColor(getResources().getColor(R.color.checkin_green));
            }
            Handler h = new Handler();
            int delay = 500;
            Runnable runnable;

            h.postDelayed(runnable = new Runnable() {
                public void run() {
                    if (check_out) {
                        checkInTick.setVisibility(View.GONE);
                        checkOutTick.setVisibility(View.VISIBLE);
                        checkOutTick.check();
                    } else {
                        checkOutTick.setVisibility(View.GONE);
                        checkInTick.setVisibility(View.VISIBLE);
                        checkInTick.check();
                    }
                }
            }, delay);


            Handler h1 = new Handler();
            int delay1 = 2 * 1000;
            Runnable runnable1;

            h1.postDelayed(runnable1 = new Runnable() {
                public void run() {
                    try {
                        Intent i = new Intent(AttendanceViewActivity.this, FaceActivity.class);
                        startActivity(i);
                        Log.d("facecheck", "WelcomeScreenActivity call");
                    } catch (Exception e) {
                        Log.d("facecheck", "WelcomeScreenActivity error = " + e);
                    }
                }
            }, delay1);
        }
    }

    public Bitmap rotateImage(int angle, Bitmap bitmapSrc) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(bitmapSrc, 0, 0, bitmapSrc.getWidth(), bitmapSrc.getHeight(), matrix, true);
    }
}

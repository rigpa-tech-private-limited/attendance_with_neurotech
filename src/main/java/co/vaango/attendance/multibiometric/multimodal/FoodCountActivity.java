package co.vaango.attendance.multibiometric.multimodal;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import co.vaango.attendance.R;
import co.vaango.attendance.multibiometric.utils.AndroidUtils;
import co.vaango.attendance.multibiometric.utils.BaseActivity;

public class FoodCountActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.food_count);
        LinearLayout user_img_ll = (LinearLayout) findViewById(R.id.user_img_ll);
        LinearLayout oops_ll = (LinearLayout) findViewById(R.id.oops_ll);
        String user_name = (String) getIntent().getSerializableExtra("user_name");
        String footer_text = (String) getIntent().getSerializableExtra("footer_text");
        String foodstatus = (String) getIntent().getSerializableExtra("foodstatus");
        Boolean user_exist = (Boolean) getIntent().getSerializableExtra("user_exist");
        String person_image = AndroidUtils.readPreference(FoodCountActivity.this, "person_image", "");
        Log.d("facecheck", person_image);
        TextView username = (TextView) findViewById(R.id.user_name);
        username.setText("Hi "+user_name);

        TextView footertext = (TextView) findViewById(R.id.footer_text);
        footertext.setText(footer_text);
        if(!user_exist) {
            oops_ll.setVisibility(View.VISIBLE);
            user_img_ll.setVisibility(View.GONE);
            byte[] imageAsBytes = Base64.decode(person_image.getBytes(), Base64.DEFAULT);
            ImageView image = (ImageView) this.findViewById(R.id.oops_image);

            image.setImageBitmap(BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length));
            Handler h = new Handler();
            int delay = 3 * 1000; //1 second=1000 miliseconds
            Runnable runnable;

            h.postDelayed(runnable = new Runnable() {
                public void run() {
                    try {
                        Intent i = new Intent(FoodCountActivity.this, FaceActivity.class);
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
            ImageView image = (ImageView) this.findViewById(R.id.food_icon);
            if(foodstatus.equals("breakfast")){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    image.setImageDrawable(getDrawable(R.drawable.breakfast));
                }
            } else if(foodstatus.equals("lunch")){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    image.setImageDrawable(getDrawable(R.drawable.lunch));
                }
            } else if(foodstatus.equals("dinner")){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    image.setImageDrawable(getDrawable(R.drawable.dinner));
                }
            }

//            byte[] imageAsBytes = Base64.decode(person_image.getBytes(), Base64.DEFAULT);
//            ImageView image = (ImageView) this.findViewById(R.id.food_icon);
//
//            image.setImageBitmap(BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length));
            Handler h = new Handler();
            int delay = 2 * 1000; //1 second=1000 miliseconds
            Runnable runnable;

            h.postDelayed(runnable = new Runnable() {
                public void run() {
                    try {
                        Intent i = new Intent(FoodCountActivity.this, FaceActivity.class);
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
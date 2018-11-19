package com.example.s1636469.coinz;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.HashMap;

public class ProfileActivity extends Activity {
    private String TAG = "Profile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        fillInValues();
    }

    protected void fillInValues() {
        if (FriendListFragment.pass_to_profile != null ){
            String name = (String) FriendListFragment.pass_to_profile.get("name");
            Bitmap img = (Bitmap) FriendListFragment.pass_to_profile.get("img");
            HashMap<String, String> curs = (HashMap<String, String>) FriendListFragment.pass_to_profile.get("currencies");

            //TODO: get higher res img
            CircularImageView profile_img = (CircularImageView) findViewById(R.id.profile_img);
            profile_img.setImageBitmap(img);

            TextView name_view = (TextView) findViewById(R.id.profile_name);
            name_view.setText(name);

            LinearLayout ll = (LinearLayout) findViewById(R.id.currency_amount_filler);
            for (String k : curs.keySet()) {
                TextView tv = new TextView(this);
                tv.setText(k + ": " + curs.get(k));
                ll.addView(tv);
            }

        }
    }
}

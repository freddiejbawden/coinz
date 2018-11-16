package com.example.s1636469.coinz;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity
{
    private SectionsPageAdapter mSecionsPageAdapter;
    private NoSwipingViewPager mViewPager;
    private MenuItem prevMenuItem;
    private FirebaseAuth mAuth;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: Starting");

        //TODO: Change this so we take it from the log in
        SharedPreferences sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username","test");

        mAuth = FirebaseAuth.getInstance();

        mSecionsPageAdapter = new SectionsPageAdapter(getSupportFragmentManager());
        mViewPager = (NoSwipingViewPager) findViewById(R.id.container);
        setupViewPager(mViewPager);

        BottomNavigationView bnv = (BottomNavigationView) findViewById(R.id.bottomNavigationView);
        bnv.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        switch(menuItem.getItemId()) {
                            case (R.id.action_map):
                                Log.d("UI UPDATE", "map pressed");
                                mViewPager.setCurrentItem(0);
                                break;
                            case (R.id.action_bank):
                                Log.d("UI UPDATE", "bank pressed");
                                mViewPager.setCurrentItem(2);
                                break;
                            case (R.id.action_community):
                                Log.d("UI UPDATE", "community pressed");
                                mViewPager.setCurrentItem(3);
                                break;
                            case (R.id.action_wallet):
                                Log.d("UI UPDATE", "profile pressed");
                                mViewPager.setCurrentItem(1);
                                break;
                        }
                        return true;                    }
                }
        );

        mViewPager.addOnPageChangeListener(
                new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int i, float v, int i1) {
                        return;
                    }

                    @Override
                    public void onPageSelected(int i) {
                        if (prevMenuItem != null) {
                            prevMenuItem.setChecked(false);
                        } else {
                            bnv.getMenu().getItem(0).setChecked(false);
                        }
                        Log.d("UI UPDATE", "onPageSeleted" + i);
                        bnv.getMenu().getItem(i).setChecked(true);
                        prevMenuItem = bnv.getMenu().getItem(i);
                    }

                    @Override
                    public void onPageScrollStateChanged(int i) {

                    }
                }
        );

    }
    public void setupViewPager(ViewPager viewPager) {
        SectionsPageAdapter adapter = new SectionsPageAdapter((getSupportFragmentManager()));
        adapter.addFragment(new MapFragment(), "MapFragment");
        adapter.addFragment(new WalletFragment(), "WalletFragment");
        adapter.addFragment(new BankFragment(), "BankFragment");
        adapter.addFragment(new CommunityFragment(), "CommunityFragment");
        viewPager.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            mAuth.signInAnonymously().addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    Log.d("STATUS","signed in anon");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w("STATUS","Log in error", e);
                }
            });
        } else {
            Log.d("STATUS", user.toString() + " logged in");
        }
    }
}

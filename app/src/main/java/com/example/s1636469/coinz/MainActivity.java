/*
 *  MainActivity
 *
 *  The primary activity for the game, holds a view pager which displays fragments of the game
 *
 */

package com.example.s1636469.coinz;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;


public class MainActivity extends AppCompatActivity
{
    private SectionsPageAdapter mSecionsPageAdapter;
    private NoSwipingViewPager mViewPager;
    private MenuItem prevMenuItem;
    private Thread watcher;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "Starting Main Activity");

        // Set up the fragment manager
        mSecionsPageAdapter = new SectionsPageAdapter(getSupportFragmentManager());
        mViewPager = findViewById(R.id.container);
        setupViewPager(mViewPager);

        setUpBottomNavView();

    }
    private void setUpBottomNavView() {

        //Populate Bottom Naviagation menu
        BottomNavigationView bnv = findViewById(R.id.bottomNavigationView);

        bnv.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        // Set up what will happen when a menu item is clicked
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

        // set up view pager to keep track of which page is being viewed
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
                        Log.d("UI UPDATE", "onPageSelected" + i);
                        bnv.getMenu().getItem(i).setChecked(true);
                        prevMenuItem = bnv.getMenu().getItem(i);
                    }

                    @Override
                    public void onPageScrollStateChanged(int i) {

                    }
                }
        );
    }

    @Override
    protected void onStop() {
        super.onStop();
        FriendWatcher.STOP_FLAG = false;
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Start Runnable to monitor friends gold value
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        String u_id = firebaseAuth.getCurrentUser().getUid();
        watcher = new Thread(new FriendWatcher(u_id,watcher));
        watcher.start();
    }


    public void setupViewPager(ViewPager viewPager) {

        // Add Fragments for the viewpager to display
        SectionsPageAdapter adapter = new SectionsPageAdapter((getSupportFragmentManager()));
        adapter.addFragment(new MapFragment(), "MapFragment");
        adapter.addFragment(new WalletFragment(), "WalletFragment");
        adapter.addFragment(new BankFragment(), "BankFragment");
        adapter.addFragment(new CommunityFragment(), "CommunityFragment");
        viewPager.setAdapter(adapter);
    }
}

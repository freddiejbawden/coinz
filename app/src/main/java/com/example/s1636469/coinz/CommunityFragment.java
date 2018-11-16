package com.example.s1636469.coinz;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class CommunityFragment extends Fragment {
    private static final String TAG = "CommunityFragment";

    private NoSwipingViewPager mViewPager;
    private MenuItem prevMenuItem;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.community_fragment,container,false);
        super.onViewCreated(view, savedInstanceState);
        TabLayout tabLayout  = (TabLayout) view.findViewById(R.id.community_tabs);
        mViewPager = (NoSwipingViewPager) view.findViewById(R.id.community_container);
        setupViewPager(mViewPager);
        tabLayout.setupWithViewPager(mViewPager);
        return view;
    }
    public void setupViewPager(ViewPager viewPager) {
        SectionsPageAdapter adapter = new SectionsPageAdapter((getChildFragmentManager()));
        adapter.addFragment(new TradesFragment(), "Trades");
        adapter.addFragment(new LeaderboardFragment(), "Leaderboard");
        adapter.addFragment(new FriendContainerFragment(), "Friends");
        viewPager.setAdapter(adapter);
    }
}

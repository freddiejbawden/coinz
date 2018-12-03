/*
 *  CommunityFragment
 *
 *  Holds TabLayout and ViewPager for Friends,Leaderboard and Trading Fragments
 *
 */

package com.example.s1636469.coinz;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class CommunityFragment extends Fragment {

    private NoSwipingViewPager mViewPager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.community_fragment,container,false);
        super.onViewCreated(view, savedInstanceState);

        // Set up tab layout
        TabLayout tabLayout  =  view.findViewById(R.id.community_tabs);
        mViewPager =  view.findViewById(R.id.community_container);
        setupViewPager(mViewPager);
        tabLayout.setupWithViewPager(mViewPager);

        return view;
    }
    public void setupViewPager(ViewPager viewPager) {

        // Populate Tab Layout
        SectionsPageAdapter adapter = new SectionsPageAdapter((getChildFragmentManager()));
        adapter.addFragment(new TradesFragment(), "Trades");
        adapter.addFragment(new LeaderboardFragment(), "Leaderboard");
        adapter.addFragment(new FriendContainerFragment(), "Friends");
        viewPager.setAdapter(adapter);
    }
}

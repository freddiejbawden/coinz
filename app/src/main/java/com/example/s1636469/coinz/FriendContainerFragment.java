/*
 *  FriendContainerFragment
 *
 *  Container for the viewpager for the friends list and search fragmentd
 *
 */

package com.example.s1636469.coinz;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

public class FriendContainerFragment extends Fragment {

    private NoSwipingViewPager mViewPager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend_container,container,false);
        super.onViewCreated(view, savedInstanceState);

        // Initialise the view pager
        mViewPager = view.findViewById(R.id.friend_container);
        setupViewPager(mViewPager);
        setupSearchBar(view);
        return view;
    }
    /*
     *  setUpViewPager
     *
     *  attaches Fragments to the viewpager
     */
    public void setupViewPager(ViewPager viewPager) {
        SectionsPageAdapter adapter = new SectionsPageAdapter((getChildFragmentManager()));
        adapter.addFragment(new FriendListFragment(),"List");
        adapter.addFragment(new SearchFriendsFragment(),"Search");
        viewPager.setAdapter(adapter);
    }
    /*
     *  setupSearchBar
     *
     *  changes screen when the search is clikced
     *
     */
    private void setupSearchBar(View v) {
        SearchView searchView = v.findViewById(R.id.searchView);
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewPager.setCurrentItem(1);
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                mViewPager.setCurrentItem(0);
                return false;
            }
        });

    }
}

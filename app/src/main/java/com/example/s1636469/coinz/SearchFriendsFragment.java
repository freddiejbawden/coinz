package com.example.s1636469.coinz;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class SearchFriendsFragment extends Fragment {

    private View rootView;
    private RecyclerView mRecyclerView;
    public FriendListAdapter mFriendAdapter;
    private ArrayList<FriendsInfo> data = new ArrayList<FriendsInfo>();
    private String TAG = "FriendsSearchFragment";
    @Override
    @NonNull
    public View onCreateView(LayoutInflater inflater, @NonNull ViewGroup container, @NonNull Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_search_friends, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.search_friends_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mFriendAdapter = new FriendListAdapter(getContext(), data);
        mRecyclerView.setAdapter(mFriendAdapter);
        return inflater.inflate(R.layout.fragment_search_friends, container, false);
    }

}

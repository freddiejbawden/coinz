/*
 *  WalletFragment
 *
 *  Wrapper for the wallet contents
 */
package com.example.s1636469.coinz;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class WalletFragment extends Fragment {
    private static final String TAG = "WalletFragment";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wallet_fragment,container,false);
        return view;
    }
}

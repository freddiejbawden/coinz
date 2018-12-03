/*
 *  WalletContentsAdapter
 *
 *  Adapter for wallet contents
 */
package com.example.s1636469.coinz;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class WalletContentsAdapter extends  RecyclerView.Adapter<WalletContentsAdapter.ViewHolder>  {
    private List<WalletCurrency> mData;
    private LayoutInflater mInflater;
    private WalletContentsAdapter.ItemClickListener mClickListener;

    // data is passed into the constructor
    WalletContentsAdapter(Context context, List<WalletCurrency> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public WalletContentsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.wallet_recycler_row, parent, false);
        return new WalletContentsAdapter.ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(WalletContentsAdapter.ViewHolder holder, int position) {

        WalletCurrency currencyInfo = mData.get(position);
        holder.walletTypeView.setText(currencyInfo.getType());
        holder.walletValueView.setText(""+ Config.round(currencyInfo.getValue(),Config.CUR_VALUE_DP));
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView walletTypeView;
        TextView walletValueView;

        ViewHolder(View itemView) {
            super(itemView);
            walletTypeView = itemView.findViewById(R.id.wallet_cur_type);
            walletValueView = itemView.findViewById(R.id.wallet_cur_value);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    WalletCurrency getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    void setClickListener(WalletContentsAdapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}

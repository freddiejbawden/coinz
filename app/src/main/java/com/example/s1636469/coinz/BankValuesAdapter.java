package com.example.s1636469.coinz;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class BankValuesAdapter extends RecyclerView.Adapter<BankValuesAdapter.ViewHolder> {


    private List<BankInfo> mData;
    private LayoutInflater mInflater;
    private BankValuesAdapter.ItemClickListener mClickListener;
    private Context c;
    // data is passed into the constructor
    BankValuesAdapter(Context context, List<BankInfo> data) {
        this.c =context;
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public BankValuesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.bank_recycler_row, parent, false);
        return new BankValuesAdapter.ViewHolder(view);
    }


    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(BankValuesAdapter.ViewHolder holder, int position) {
        Log.d("BANK ADAPTER",mData.toString());
        BankInfo bankInfo = mData.get(position);
        holder.currencyTypeTextView.setText(bankInfo.getCurrency());
        holder.currencyValueTextView.setText(""+Config.round(bankInfo.getValue(),Config.CUR_VALUE_DP));
        if (bankInfo.getChange() < 1e-3 && bankInfo.getChange() > -1e-3) {
            holder.currencyValueChangeTextView.setText("0");
            holder.currencyValueChangeTextView.setTextColor(c.getColor(R.color.colorTrendUnknown));
            Toast.makeText(this.c, "Unable to calculate coin values, please try again later",Toast.LENGTH_LONG).show();
        } else {
            holder.currencyValueChangeTextView.setText(""+Config.round(bankInfo.getChange(),Config.CUR_VALUE_DP));
            if (bankInfo.getChange() >= 0) {
                holder.currencyValueChangeTextView.setTextColor(c.getColor(R.color.colorTrendUp));
            } else {
                holder.currencyValueChangeTextView.setTextColor(c.getColor(R.color.colorTrendDown));
            }
        }
    }
    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView currencyTypeTextView;
        TextView currencyValueTextView;
        TextView currencyValueChangeTextView;

        ViewHolder(View itemView) {
            super(itemView);
            currencyTypeTextView = itemView.findViewById(R.id.bank_currency);
            currencyValueTextView = itemView.findViewById(R.id.bank_value);
            currencyValueChangeTextView = itemView.findViewById(R.id.bank_value_change);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    BankInfo getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    void setClickListener(BankValuesAdapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}

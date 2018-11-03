package com.example.s1636469.coinz;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class CurrencyListAdapter extends RecyclerView.Adapter<CurrencyListAdapter.ViewHolder> {

    private List<CurrencyInfo> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    // data is passed into the constructor
    CurrencyListAdapter(Context context, List<CurrencyInfo> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.currency_recycler_row, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        CurrencyInfo currencyInfo = mData.get(position);
        holder.currencyTextView.setText(currencyInfo.getType());
        holder.valueTextView.setText(""+ currencyInfo.getValue());
        holder.trendTextView.setText(""+ currencyInfo.getTrend());
        if (currencyInfo.getTrend() > 0) {
            holder.trendTextView.setTextColor(ContextCompat.getColor(holder.trendTextView.getContext(), R.color.colorTrendUp));
        } else {
            holder.trendTextView.setTextColor(ContextCompat.getColor(holder.trendTextView.getContext(), R.color.colorTrendDown));
        }
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView currencyTextView;
        TextView valueTextView;
        TextView trendTextView;
        ViewHolder(View itemView) {
            super(itemView);
            currencyTextView = itemView.findViewById(R.id.row_currency_type);
            valueTextView = itemView.findViewById(R.id.row_currency_value);
            trendTextView = itemView.findViewById(R.id.row_currency_trend);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    CurrencyInfo getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}

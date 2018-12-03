/*
 *  TradeAdapter
 *
 *  Adapter for Trade History Recycler
 */
package com.example.s1636469.coinz;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class TradeAdapter extends  RecyclerView.Adapter<TradeAdapter.ViewHolder>  {
    private List<TradeData> mData;
    private LayoutInflater mInflater;
    private TradeAdapter.ItemClickListener mClickListener;

    // data is passed into the constructor
    TradeAdapter(Context context, List<TradeData> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public TradeAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.trade_history_row, parent, false);
        return new TradeAdapter.ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(TradeAdapter.ViewHolder holder, int position) {
        TradeData ta = mData.get(position);
        if (ta.getFromUser()) {
            holder.trade_icon.setImageResource(R.drawable.trade_loss);
        } else {
            holder.trade_icon.setImageResource(R.drawable.trade_gain);
        }
        holder.trade_from.setText(ta.getFrom());
        holder.trade_to.setText(ta.getTo());
        holder.trade_amount.setText(ta.getAmount() + " " + ta.get_cur_type());
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView trade_to;
        TextView trade_from;
        TextView trade_amount;
        ImageView trade_icon;

        private SectionsPageAdapter mAdapter;
        private String TAG ="Holder";
        ;
        ViewHolder(View itemView) {
            super(itemView);
            trade_from = itemView.findViewById(R.id.trade_from);
            trade_to = itemView.findViewById(R.id.trade_to);
            trade_amount = itemView.findViewById(R.id.trade_amount);
            trade_icon = itemView.findViewById(R.id.trade_img);
            itemView.setOnClickListener(this);
        }

        // On List Item Pressed
        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    TradeData getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    void setClickListener(TradeAdapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}

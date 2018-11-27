package com.example.s1636469.coinz;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

    private List<PlayerInfo> mData;
    private LayoutInflater mInflater;
    private LeaderboardAdapter.ItemClickListener mClickListener;

    // data is passed into the constructor
    LeaderboardAdapter(Context context, List<PlayerInfo> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public LeaderboardAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.leaderboard_recycler_row, parent, false);
        return new LeaderboardAdapter.ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(LeaderboardAdapter.ViewHolder holder, int position) {
        Log.d("LEADERBOARD ADAPTER",mData.toString());
        PlayerInfo playerInfo = mData.get(position);
        holder.placeTextView.setText(playerInfo.getPlace());
        holder.nameTextView.setText(playerInfo.getU_name());
        holder.goldTextView.setText(Config.round(Double.parseDouble(playerInfo.getGold()),Config.CUR_VALUE_DP) + "");
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
       TextView placeTextView;
       TextView nameTextView;
       TextView goldTextView;

        ViewHolder(View itemView) {
            super(itemView);
            placeTextView = itemView.findViewById(R.id.place_leaderboard);
            nameTextView = itemView.findViewById(R.id.name_leaderboard);
            goldTextView = itemView.findViewById(R.id.leaderboard_gold);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    PlayerInfo getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    void setClickListener(LeaderboardAdapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}

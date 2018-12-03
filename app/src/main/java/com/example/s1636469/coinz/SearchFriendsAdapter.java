/*
 *  SearchFriendsAdapter
 *
 *  Adapter class for the friend search list
 */
package com.example.s1636469.coinz;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.List;

import static android.app.PendingIntent.getActivity;

public class SearchFriendsAdapter extends RecyclerView.Adapter<SearchFriendsAdapter.ViewHolder> {

    private List<FriendsInfo> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    // data is passed into the constructor
    SearchFriendsAdapter(Context context, List<FriendsInfo> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.friend_recycler_row, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        FriendsInfo fi = mData.get(position);
        holder.username.setText(fi.getName());
        Bitmap test = fi.getImg();
        holder.circularImageView.setImageBitmap(fi.getImg());
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView username;
        CircularImageView circularImageView;
        private SectionsPageAdapter mAdapter;
        private String TAG ="Holder";
        ;
        ViewHolder(View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.friend_name);
            circularImageView = itemView.findViewById(R.id.profile_img_preview);
            itemView.setOnClickListener(this);
        }

        // On List Item Pressed
        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    FriendsInfo getItem(int id) {
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

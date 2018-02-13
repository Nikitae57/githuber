package com.example.nikit.githubapp;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by nikit on 01.02.2018.
 */

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    private int mItemNumber;
    private JSONArray itemsArray;

    public MyAdapter(int mItemNumber, JSONArray itemsArray) {
        this.itemsArray = itemsArray;
        this.mItemNumber = mItemNumber;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.list_item, parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return mItemNumber;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView listItem;
        TextView starsCounter;

        public MyViewHolder(View itemView) {
            super(itemView);
            listItem = itemView.findViewById(R.id.tvItem);
            starsCounter = itemView.findViewById(R.id.tvStars);
        }

        JSONObject jsonObject;
        public void bind(int index) {
            try {
                jsonObject = (JSONObject) itemsArray.get(index);
                listItem.setText(jsonObject.getString("full_name"));
                starsCounter.append(jsonObject.getString("stargazers_count"));

            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
    }
}

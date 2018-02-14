package com.example.nikit.githubapp;

import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

/**
 * Created by nikit on 01.02.2018.
 */

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    private int mItemNumber;
    private JSONArray itemsArray;
    private boolean descriptionStateArray[];

    public MyAdapter(int mItemNumber, JSONArray itemsArray) {
        this.itemsArray = itemsArray;
        this.mItemNumber = mItemNumber;

        descriptionStateArray = new boolean[mItemNumber];
        Arrays.fill(descriptionStateArray, false);
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

    class MyViewHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener {

        TextView fullName, starsCounter, description,
                 language, forkNumber;

        public MyViewHolder(View itemView) {
            super(itemView);
            fullName = itemView.findViewById(R.id.tvItem);
            starsCounter = itemView.findViewById(R.id.tvStars);

            description = itemView.findViewById(R.id.description);
            description.setTextSize(TypedValue.COMPLEX_UNIT_SP, 0);

            language = itemView.findViewById(R.id.tvLanguage);
            forkNumber = itemView.findViewById(R.id.tvFork);

            itemView.setOnClickListener(this);
        }


        JSONObject jsonObject;
        public void bind(int index) {

            try {
                jsonObject = (JSONObject) itemsArray.get(index);

                StringBuilder starsNumberStr = new StringBuilder("★");
                double starsNumberDouble = jsonObject.getDouble("stargazers_count");
                if (starsNumberDouble > 1000) {

                    starsNumberDouble /= 1000;
                    starsNumberDouble = (double) Math.round(starsNumberDouble * 10) / 10;
                    starsNumberStr = starsNumberStr.append(String.valueOf(starsNumberDouble)).append("k");

                } else {
                    starsNumberStr = starsNumberStr.append((int) starsNumberDouble);
                }

                starsCounter.setText(starsNumberStr);
                fullName.setText(jsonObject.getString("full_name"));
                description.setText(jsonObject.getString("description"));
                forkNumber.setText(jsonObject.getString("forks_count"));
                language.setText(jsonObject.getString("language"));

                if (descriptionStateArray[index]) {
                    description.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                } else {
                    description.setTextSize(TypedValue.COMPLEX_UNIT_SP, 0);
                }

            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void onClick(View view) {
            int position = this.getAdapterPosition();
            if (!descriptionStateArray[position]) {

                description.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                descriptionStateArray[position] = true;

            } else {
                description.setTextSize(TypedValue.COMPLEX_UNIT_SP, 0);
                descriptionStateArray[position] = false;
            }
        }
    }
}

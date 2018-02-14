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

/**
 * Created by nikit on 01.02.2018.
 */

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    private int mItemNumber;
    private JSONArray itemsArray;
    private boolean descriptionIsShown;

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

    class MyViewHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener {

        TextView fullName;
        TextView starsCounter;
        TextView description;

        public MyViewHolder(View itemView) {
            super(itemView);
            fullName = itemView.findViewById(R.id.tvItem);
            starsCounter = itemView.findViewById(R.id.tvStars);

            description = itemView.findViewById(R.id.description);
            descriptionIsShown = false;
            description.setTextSize(TypedValue.COMPLEX_UNIT_SP, 0);

            itemView.setOnClickListener(this);
        }

        JSONObject jsonObject;
        String descriptionText;
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

                fullName.setText(jsonObject.getString("full_name"));
                starsCounter.setText(starsNumberStr);
                descriptionText = jsonObject.getString("description");

            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void onClick(View view) {
            if (descriptionText == null || descriptionText.equals(""))
                return;

            if (!descriptionIsShown) {

                description.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                description.setText(descriptionText);
                descriptionIsShown = true;

            } else {
                description.setTextSize(TypedValue.COMPLEX_UNIT_SP, 0);
                description.setText("");
                descriptionIsShown = false;
            }
        }
    }
}

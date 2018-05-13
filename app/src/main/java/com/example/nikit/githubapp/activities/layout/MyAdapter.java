package com.example.nikit.githubapp.activities.layout;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.nikit.githubapp.R;
import com.example.nikit.githubapp.activities.ReadmeActivity;
import com.example.nikit.githubapp.networkUtil.NetworkUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    private int mItemNumber;
    private JSONArray itemsArray;
    private Context context;

    public MyAdapter(int mItemNumber, JSONArray itemsArray, Context context) {
        this.itemsArray = itemsArray;
        this.mItemNumber = mItemNumber;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.main_activity_rv_list_item, parent, false);

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
        ImageView languageImage;
        ImageButton btn_openRepo;

        public MyViewHolder(View itemView) {
            super(itemView);
            fullName = itemView.findViewById(R.id.tvItem);
            starsCounter = itemView.findViewById(R.id.tvStars);

            languageImage = itemView.findViewById(R.id.imageLanguage);

            description = itemView.findViewById(R.id.description);
            description.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);

            language = itemView.findViewById(R.id.tvLanguage);
            forkNumber = itemView.findViewById(R.id.tvFork);

            btn_openRepo = itemView.findViewById(R.id.btn_openRepo);
            btn_openRepo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    try {
                        int position = getAdapterPosition();
                        String urlStr = ((JSONObject) itemsArray.get(position)).getString("html_url");

                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(urlStr));

                        if (intent.resolveActivity(context.getPackageManager()) != null) {
                            context.startActivity(intent);
                        }

                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    }
                }
            });

            itemView.setOnClickListener(this);
        }

        JSONObject jsonObject;
        public void bind(int index) {

            try {
                jsonObject = (JSONObject) itemsArray.get(index);

                // Setting stars count, depending in it's number
                StringBuilder starsNumberStr = new StringBuilder("★");
                double starsNumberDouble = jsonObject.getDouble("stargazers_count");
                if (starsNumberDouble > 1000) {

                    starsNumberDouble /= 1000;
                    starsNumberDouble = (double) Math.round(starsNumberDouble * 10) / 10;
                    starsNumberStr = starsNumberStr.append(String.valueOf(starsNumberDouble)).append("k");
                } else {
                    starsNumberStr = starsNumberStr.append((int) starsNumberDouble);
                }

                // Show description only if it's provided
                String descriptionText = jsonObject.getString("description");
                if (descriptionText == null || descriptionText.equals("") || descriptionText.equals("null")) {
                    description.setTextSize(TypedValue.COMPLEX_UNIT_SP, 0);
                } else {
                    description.setText(jsonObject.getString("description"));
                }

                // If no language, remove language image
                String languageText = jsonObject.getString("language");
                if (languageText == null || languageText.equals("") || languageText.equals("null")) {
                    languageImage.setVisibility(View.INVISIBLE);
                    language.setText("");
                } else {
                    language.setText(jsonObject.getString("language"));
                }

                starsCounter.setText(starsNumberStr);
                fullName.setText(jsonObject.getString("full_name"));
                forkNumber.setText(jsonObject.getString("forks_count"));

            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();

            String repoFullName = null;
            String repoUrl = null;
            String repoJsonStr = null;
            try {
                repoFullName = ((JSONObject) itemsArray.get(position)).getString("full_name");
                repoUrl = ((JSONObject) itemsArray.get(position)).getString("html_url");
                repoJsonStr = itemsArray.get(position).toString();
                Log.d("STR", repoJsonStr);
            } catch (JSONException ex) {
                ex.printStackTrace();
            }

            URL readmeUrl = NetworkUtil.makeReadmeUrl(repoFullName);

            Intent readmeIntent = new Intent(context, ReadmeActivity.class);
            Bundle extras = new Bundle();
            extras.putString("readmeUrl", readmeUrl.toString());
            extras.putString("repoUrl", repoUrl);
            extras.putString("repoFullName", repoFullName);
            extras.putString("repoJSON", repoJsonStr);
            readmeIntent.putExtras(extras);
            context.startActivity(readmeIntent);
        }
    }
}

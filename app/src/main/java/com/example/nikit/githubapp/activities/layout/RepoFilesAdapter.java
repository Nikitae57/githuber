package com.example.nikit.githubapp.activities.layout;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.nikit.githubapp.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RepoFilesAdapter
        extends RecyclerView.Adapter<RepoFilesAdapter.RepoFilesViewHolder> {

    private int ITEM_COUNT;
    private JSONArray filesJsonArray;

    public RepoFilesAdapter(JSONArray filesJsonArray) {

        this.filesJsonArray = filesJsonArray;
        ITEM_COUNT = filesJsonArray.length();
        Log.d("FILES", "count " + ITEM_COUNT);
        Log.d("FILES", filesJsonArray.toString());
    }

    @NonNull
    @Override
    public RepoFilesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.
                inflate(R.layout.readme_activity_rv_list_item, parent, false);

        Log.d("HOLDER", "holder created");

        return new RepoFilesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RepoFilesViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() { return ITEM_COUNT; }

    class RepoFilesViewHolder extends RecyclerView.ViewHolder {

        private TextView tvFileName;
        private ImageView fileImg;

        public RepoFilesViewHolder(View itemView) {
            super(itemView);

            tvFileName = itemView.findViewById(R.id.readme_file_name);
            fileImg = itemView.findViewById(R.id.readme_iv_file_type);
        }

        int fileSize;
        String fileNameStr, fileType;
        JSONObject fileJSON;
        public void bind(int position) {

            try {

                fileJSON = filesJsonArray.getJSONObject(position);
                fileNameStr = fileJSON.getString("path");
                tvFileName.setText(fileNameStr);

                fileType = fileJSON.getString("type");
                if (fileType.equals("tree")) {
                    fileImg.setImageResource(R.drawable.ic_folder_black_24dp);

                } else {

                    fileSize = fileJSON.getInt("size");

                    

                    fileImg.setImageResource(R.drawable.ic_insert_drive_file_black_24dp);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}

package com.example.nikit.githubapp.activities.layout;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.nikit.githubapp.R;
import com.example.nikit.githubapp.files_util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class RepoFilesAdapter
        extends RecyclerView.Adapter<RepoFilesAdapter.RepoFilesViewHolder> {

    private int ITEM_COUNT;
    private JSONArray currentDirJsonArray, parentDirJsonArray, allFilesJsonArray, rootDirJsonArray;
    private FileClickedListener fileClickedListener;
    public static boolean browsingRootDir = true;
    private String parentPath = "";

    public RepoFilesAdapter(JSONArray allFilesJsonArray) {
        this.allFilesJsonArray = allFilesJsonArray;

        rootDirJsonArray = Util.makeRootDirCollection(allFilesJsonArray);
        currentDirJsonArray = rootDirJsonArray;
        parentDirJsonArray = currentDirJsonArray;
        ITEM_COUNT = currentDirJsonArray.length();
    }

    public void setFilesClickedListener(FileClickedListener listener) {
        this.fileClickedListener = listener;
    }

    public void homePressed() {
        currentDirJsonArray = rootDirJsonArray;
        parentDirJsonArray = rootDirJsonArray;
        parentPath = "";
        ITEM_COUNT = currentDirJsonArray.length();
        browsingRootDir = true;
    }

    @NonNull
    @Override
    public RepoFilesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view;

        view = layoutInflater.
                inflate(R.layout.readme_activity_rv_list_item, parent, false);

        return new RepoFilesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RepoFilesViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() { return ITEM_COUNT; }

    public interface FileClickedListener {
        void fileClicked();
    }

    class RepoFilesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView tvFileName, tvFileSize;
        private ImageView fileImg;

        RepoFilesViewHolder(View itemView) {
            super(itemView);

            tvFileName = itemView.findViewById(R.id.readme_file_name);
            tvFileSize = itemView.findViewById(R.id.readme_tv_size);
            fileImg = itemView.findViewById(R.id.readme_iv_file_type);

            itemView.setOnClickListener(this);
        }

        double fileSize;
        String fileNameStr, fileType, fileSizeStr;
        JSONObject fileJSON;
        void bind(int position) {

            DecimalFormat df = new DecimalFormat("#.##");
            df.setRoundingMode(RoundingMode.CEILING);

            try {
                fileJSON = currentDirJsonArray.getJSONObject(position);

                if (browsingRootDir) {
                    fileNameStr = fileJSON.getString("path");
                } else {
                    fileNameStr = Util.childSimpleName(parentPath, fileJSON.getString("path"));
                }

                tvFileName.setText(fileNameStr);

                fileType = fileJSON.getString("type");
                if (fileType.equals("tree")) {
                    fileImg.setImageResource(R.drawable.ic_folder_black_24dp);
                    tvFileSize.setVisibility(View.INVISIBLE);

                } else {
                    fileSize = fileJSON.getDouble("size");
                    if (fileSize > (1 << 30)) {
                        fileSize /= (1 << 30);
                        fileSizeStr = "Gb";

                    } else if (fileSize > (1 << 20)) {
                        fileSize /= (1 << 20);
                        fileSizeStr = "Mb";

                    } else if (fileSize > (1 << 10)) {
                        fileSize /= (1 << 10);
                        fileSizeStr = "Kb";

                    } else {
                        fileSizeStr = "B";
                    }

                    fileSizeStr = df.format(fileSize) + fileSizeStr;
                    tvFileSize.setText(fileSizeStr);
                    tvFileSize.setVisibility(View.VISIBLE);
                    fileImg.setImageResource(R.drawable.ic_insert_drive_file_black_24dp);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onClick(View view) {

            String clickedFilePath = null;
            try {
                clickedFilePath = fileJSON.getString("path");
                if (Util.isFile(clickedFilePath)) { return; }
            } catch (JSONException jsonEx) { jsonEx.printStackTrace(); }

            JSONArray children = Util.makeChildCollection(allFilesJsonArray, clickedFilePath);

            if (clickedFilePath != null) {
                parentPath = clickedFilePath;
            }
            parentDirJsonArray = currentDirJsonArray;
            currentDirJsonArray = children;

            ITEM_COUNT = children.length();
            browsingRootDir = false;
            fileClickedListener.fileClicked();
        }
    }

}

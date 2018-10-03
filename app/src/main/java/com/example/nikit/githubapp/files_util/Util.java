package com.example.nikit.githubapp.files_util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Util {
    public static boolean isChild(String parent, String child) {
        return child.startsWith(parent)
                && !parent.equals(child)
                && countSlashes(child) - countSlashes(parent) == 1;
    }

    public static String childSimpleName(String parent, String child) {
        child = child.replaceFirst(parent + "/", "");

        if (child.contains("/")) {
            return child.substring(0, child.indexOf('/'));
        }

        return child;
    }

    public static int countSlashes(String string) {
        int counter = 0;
        for (int i = 0; i < string.length(); i++) {
            if (string.charAt(i) == '/') {
                counter++;
            }
        }

        return counter;
    }

    public static JSONArray makeChildCollection(JSONArray parentJson, String childPath) {

        JSONArray children = null;
        try {

            children = new JSONArray();
            for (int i = 0; i < parentJson.length(); i++) {
                JSONObject possibleChild = parentJson.getJSONObject(i);
                String possibleChildPath = possibleChild.getString("path");

                if (Util.isChild(childPath, possibleChildPath)) {
                    children.put(possibleChild);
                }
            }

        } catch (JSONException jsonEx) { jsonEx.printStackTrace(); }

        return children;
    }

    public static JSONArray makeRootDirCollection(JSONArray allFiles) {
        JSONArray root = new JSONArray();

        try {
            for (int i = 0; i < allFiles.length(); i++) {
                JSONObject possibleChild = allFiles.getJSONObject(i);
                if (countSlashes(possibleChild.getString("path")) == 0) {
                    root.put(possibleChild);
                }
            }
        } catch (JSONException jsonEx) { jsonEx.printStackTrace(); }

        return root;
    }

    public static boolean isFile(String path) {
        return path.matches("\\S+\\.(\\w)+");
    }

    public static JSONArray makeParentCollection(String currentPath, JSONArray allFiles) {
        String parentPath = currentPath.substring(0, currentPath.lastIndexOf('/'));
        return makeChildCollection(allFiles, parentPath);
    }
}

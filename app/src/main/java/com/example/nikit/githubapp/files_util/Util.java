package com.example.nikit.githubapp.files_util;

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
}

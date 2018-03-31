package com.example.nikit.githubapp;

import org.json.JSONObject;

public class User {

    private String login, password;
    private JSONObject userJSON;

    public User(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public void setUserJSON(JSONObject userJSON) {
        this.userJSON = userJSON;
    }

    public String getLogin() {

        return login;
    }

    public String getPassword() {
        return password;
    }

    public JSONObject getUserJSON() {
        return userJSON;
    }

    public User(String login, String password, JSONObject json) {
        this(login, password);

        userJSON = json;
    }
}

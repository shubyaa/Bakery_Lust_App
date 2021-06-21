package com.example.bakery_lust.User;

public class EmailUser {
    private String name;
    private String email;
    private String phoneNO;
    private String location;
    private String password;

    public EmailUser() {
    }

    public EmailUser(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public EmailUser(String name, String email, String password,String phoneNO, String location) {
        this.name = name;
        this.email = email;
        this.phoneNO = phoneNO;
        this.location = location;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNO() {
        return phoneNO;
    }

    public void setPhoneNO(String phoneNO) {
        this.phoneNO = phoneNO;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

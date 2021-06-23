package com.example.bakery_lust.User;

public class EmailUser {
    private String name;
    private String email;
    private String phoneNO;
    private String location;
    private String password;
    private String verification;
    private String status;

    public EmailUser() {
    }

    public EmailUser(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public EmailUser(String name, String email, String password, String status) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.verification = verification;
    }

    public EmailUser(String name, String email, String password, String verification, String status) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.verification = verification;
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getVerification() {
        return verification;
    }

    public void setVerification(String verification) {
        this.verification = verification;
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

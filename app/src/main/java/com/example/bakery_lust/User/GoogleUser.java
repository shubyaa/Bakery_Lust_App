package com.example.bakery_lust.User;

public class GoogleUser {
    private String name;
    private String email;
    private String verification;
    private String status;
    private String phoneNO;
    private String location;

    public GoogleUser() {
    }

    public GoogleUser(String name, String email){
        this.name = name;
        this.email = email;
    }

    public GoogleUser(String name, String email, String verification){
        this.name = name;
        this.email = email;
        this.verification = verification;
    }

    public GoogleUser(String name, String email, String verification, String status){
        this.name = name;
        this.email = email;
        this.verification = verification;
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
}

package com.example.bakery_lust.User;

public class GoogleUser {
    private String name;
    private String email;
    private String phoneNO;
    private String location;

    public GoogleUser() {
    }

    public GoogleUser(String name, String email){
        this.name = name;
        this.email = email;
    }

    public GoogleUser(String name, String email, String phoneNO, String location) {
        this.name = name;
        this.email = email;
        this.phoneNO = phoneNO;
        this.location = location;
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

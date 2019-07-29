package com.example.swimmaster;

public class User {
    private String name;
    private String email;
    private Integer height;
    private Integer weight;
    private Integer age;
    private boolean gender;

    User(){

    }

    User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    // ========== Setters ==========
    public void setAge(Integer age) {
        this.age = age;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setGender(boolean gender) {
        this.gender = gender;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    // =============================
    // ========== Getters ==========
    public Integer getAge() {
        return age;
    }

    public String getEmail() {
        return email;
    }

    public boolean getGender() {
        return gender;
    }

    public Integer getHeight() {
        return height;
    }

    public String getName() {
        return name;
    }

    public Integer getWeight() {
        return weight;
    }
    // =============================
}

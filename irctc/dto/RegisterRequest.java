package com.irctc.dto;

public class RegisterRequest {
    private String username;
    private String password;
    private String email;
    private String phone;
    private String fullName;
    private Integer age;
    private String gender;
    private String address;
    private String city;
    private String state;
    private String pincode;
    
    // Default constructor
    public RegisterRequest() {}
    
    // Getters
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getFullName() { return fullName; }
    public Integer getAge() { return age; }
    public String getGender() { return gender; }
    public String getAddress() { return address; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public String getPincode() { return pincode; }
    
    // Setters
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setAge(Integer age) { this.age = age; }
    public void setGender(String gender) { this.gender = gender; }
    public void setAddress(String address) { this.address = address; }
    public void setCity(String city) { this.city = city; }
    public void setState(String state) { this.state = state; }
    public void setPincode(String pincode) { this.pincode = pincode; }
}
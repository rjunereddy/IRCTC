package com.irctc.model;

import com.irctc.model.enums.UserRole;
import jakarta.persistence.*;


@Entity
@Table(name = "administrators")
public class Administrator extends User {
    
    @Column(unique = true, nullable = false, length = 20)
    private String employeeCode;
    
    private String department;
    private String designation;
    private String accessLevel;
    
    public Administrator() {}
    
    public Administrator(String username, String password, String email, String phone,
                         String employeeCode, String department) {
        super(username, password, email, phone, UserRole.ADMIN);
        this.employeeCode = employeeCode;
        this.department = department;
        this.accessLevel = "MODERATOR";
    }
    
    // Getters and Setters
    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }
    
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    
    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }
    
    public String getAccessLevel() { return accessLevel; }
    public void setAccessLevel(String accessLevel) { this.accessLevel = accessLevel; }
}
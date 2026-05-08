package com.irctc.model;

import com.irctc.model.enums.UserRole;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "passengers")
public class Passenger extends User {
    
    @Column(nullable = false, length = 100)
    private String fullName;
    
    private Integer age;
    private String gender;
    
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;
    
    @Column(name = "id_proof_type")
    private String idProofType;
    
    @Column(name = "id_proof_number")
    private String idProofNumber;
    
    private String address;
    private String city;
    private String state;
    private String pincode;
    
    @Column(name = "is_senior_citizen")
    private boolean isSeniorCitizen;
    
    @Column(name = "is_student")
    private boolean isStudent;
    
    @Column(name = "concession_type")
    private String concessionType;
    
    @OneToMany(mappedBy = "passenger", cascade = CascadeType.ALL)
    private List<Ticket> tickets = new ArrayList<>();
    
    public Passenger() {}
    
    public Passenger(String username, String password, String email, String phone, 
                     String fullName, Integer age, String gender) {
        super(username, password, email, phone, UserRole.PASSENGER);
        this.fullName = fullName;
        this.age = age;
        this.gender = gender;
        this.isSeniorCitizen = (age != null && ((gender.equals("MALE") && age >= 60) || 
                               (gender.equals("FEMALE") && age >= 58)));
    }
    
    // Getters and Setters
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    
    public String getIdProofType() { return idProofType; }
    public void setIdProofType(String idProofType) { this.idProofType = idProofType; }
    
    public String getIdProofNumber() { return idProofNumber; }
    public void setIdProofNumber(String idProofNumber) { this.idProofNumber = idProofNumber; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    
    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }
    
    public boolean isSeniorCitizen() { return isSeniorCitizen; }
    public void setSeniorCitizen(boolean seniorCitizen) { isSeniorCitizen = seniorCitizen; }
    
    public boolean isStudent() { return isStudent; }
    public void setStudent(boolean student) { isStudent = student; }
    
    public String getConcessionType() { return concessionType; }
    public void setConcessionType(String concessionType) { this.concessionType = concessionType; }
    
    public List<Ticket> getTickets() { return tickets; }
    public void setTickets(List<Ticket> tickets) { this.tickets = tickets; }
    
    public void addTicket(Ticket ticket) {
        tickets.add(ticket);
        ticket.setPassenger(this);
    }
}
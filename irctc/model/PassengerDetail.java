package com.irctc.model;

import com.irctc.model.enums.BerthType;
import jakarta.persistence.*;

@Entity
@Table(name = "passenger_details")
public class PassengerDetail {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "ticket_pnr")
    private Ticket ticket;
    
    @Column(nullable = false)
    private String passengerName;
    
    private Integer age;
    private String gender;
    private String berthAllotted;
    
    @Enumerated(EnumType.STRING)
    private BerthType berthPreference;
    
    private String concessionType;
    private String idProofType;
    private String idProofNumber;
    private String mealPreference;
    
    public PassengerDetail() {}
    
    public PassengerDetail(String passengerName, Integer age, String gender) {
        this.passengerName = passengerName;
        this.age = age;
        this.gender = gender;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Ticket getTicket() { return ticket; }
    public void setTicket(Ticket ticket) { this.ticket = ticket; }
    
    public String getPassengerName() { return passengerName; }
    public void setPassengerName(String passengerName) { this.passengerName = passengerName; }
    
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    
    public String getBerthAllotted() { return berthAllotted; }
    public void setBerthAllotted(String berthAllotted) { this.berthAllotted = berthAllotted; }
    
    public BerthType getBerthPreference() { return berthPreference; }
    public void setBerthPreference(BerthType berthPreference) { this.berthPreference = berthPreference; }
    
    public String getConcessionType() { return concessionType; }
    public void setConcessionType(String concessionType) { this.concessionType = concessionType; }
    
    public String getIdProofType() { return idProofType; }
    public void setIdProofType(String idProofType) { this.idProofType = idProofType; }
    
    public String getIdProofNumber() { return idProofNumber; }
    public void setIdProofNumber(String idProofNumber) { this.idProofNumber = idProofNumber; }
    
    public String getMealPreference() { return mealPreference; }
    public void setMealPreference(String mealPreference) { this.mealPreference = mealPreference; }
}
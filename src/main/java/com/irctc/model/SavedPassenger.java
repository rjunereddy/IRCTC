package com.irctc.model;

import jakarta.persistence.*;

@Entity
@Table(name = "saved_passengers")
public class SavedPassenger {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "passenger_id", nullable = false)
    private Passenger passenger;

    private String name;
    private Integer age;
    private String gender;
    
    @Column(name = "id_proof_type")
    private String idProofType;
    
    @Column(name = "id_proof_number")
    private String idProofNumber;

    public SavedPassenger() {}

    public Long getId() { return id; }
    public Passenger getPassenger() { return passenger; }
    public void setPassenger(Passenger passenger) { this.passenger = passenger; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getIdProofType() { return idProofType; }
    public void setIdProofType(String idProofType) { this.idProofType = idProofType; }
    public String getIdProofNumber() { return idProofNumber; }
    public void setIdProofNumber(String idProofNumber) { this.idProofNumber = idProofNumber; }
}

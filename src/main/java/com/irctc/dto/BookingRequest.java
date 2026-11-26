package com.irctc.dto;

import com.irctc.model.enums.BerthType;
import com.irctc.model.enums.ClassType;
import com.irctc.model.enums.QuotaType;
import java.time.LocalDate;
import java.util.List;

public class BookingRequest {
    private String trainNo;
    private String journeyDate;
    private ClassType classType;
    private QuotaType quotaType;
    private List<PassengerInfo> passengers;
    private BerthType berthPreference;
    private boolean isTatkal;
    private String paymentMethod;
    private String cardNumber;
    private String cardExpiry;
    private String cardCvv;
    private String upiId;
    private String bankName;
    private String foodOrderDetails;
    private boolean insuranceOpted;
    private String sourceStationCode;
    private String destinationStationCode;
    
    // Default constructor
    public BookingRequest() {}
    
    // Getters
    public String getTrainNo() { return trainNo; }
    public String getJourneyDate() { return journeyDate; }
    public ClassType getClassType() { return classType; }
    public QuotaType getQuotaType() { return quotaType; }
    public List<PassengerInfo> getPassengers() { return passengers; }
    public BerthType getBerthPreference() { return berthPreference; }
    public boolean isTatkal() { return isTatkal; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getCardNumber() { return cardNumber; }
    public String getCardExpiry() { return cardExpiry; }
    public String getCardCvv() { return cardCvv; }
    public String getUpiId() { return upiId; }
    public String getBankName() { return bankName; }
    public String getFoodOrderDetails() { return foodOrderDetails; }
    public boolean isInsuranceOpted() { return insuranceOpted; }
    public String getSourceStationCode() { return sourceStationCode; }
    public String getDestinationStationCode() { return destinationStationCode; }
    
    // Setters
    public void setTrainNo(String trainNo) { this.trainNo = trainNo; }
    public void setJourneyDate(String journeyDate) { this.journeyDate = journeyDate; }
    public void setClassType(ClassType classType) { this.classType = classType; }
    public void setQuotaType(QuotaType quotaType) { this.quotaType = quotaType; }
    public void setPassengers(List<PassengerInfo> passengers) { this.passengers = passengers; }
    public void setBerthPreference(BerthType berthPreference) { this.berthPreference = berthPreference; }
    public void setTatkal(boolean tatkal) { isTatkal = tatkal; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
    public void setCardExpiry(String cardExpiry) { this.cardExpiry = cardExpiry; }
    public void setCardCvv(String cardCvv) { this.cardCvv = cardCvv; }
    public void setUpiId(String upiId) { this.upiId = upiId; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    public void setFoodOrderDetails(String foodOrderDetails) { this.foodOrderDetails = foodOrderDetails; }
    public void setInsuranceOpted(boolean insuranceOpted) { this.insuranceOpted = insuranceOpted; }
    public void setSourceStationCode(String sourceStationCode) { this.sourceStationCode = sourceStationCode; }
    public void setDestinationStationCode(String destinationStationCode) { this.destinationStationCode = destinationStationCode; }
    
    // Inner class
    public static class PassengerInfo {
        private String name;
        private Integer age;
        private String gender;
        private String idProofType;
        private String idProofNumber;
        private String mealPreference;
        
        public PassengerInfo() {}
        
        public String getName() { return name; }
        public Integer getAge() { return age; }
        public String getGender() { return gender; }
        public String getIdProofType() { return idProofType; }
        public String getIdProofNumber() { return idProofNumber; }
        public String getMealPreference() { return mealPreference; }
        
        public void setName(String name) { this.name = name; }
        public void setAge(Integer age) { this.age = age; }
        public void setGender(String gender) { this.gender = gender; }
        public void setIdProofType(String idProofType) { this.idProofType = idProofType; }
        public void setIdProofNumber(String idProofNumber) { this.idProofNumber = idProofNumber; }
        public void setMealPreference(String mealPreference) { this.mealPreference = mealPreference; }
    }
}
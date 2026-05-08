package com.irctc.dto;

import java.time.LocalDate;

public class ReportCriteria {
    private LocalDate startDate;
    private LocalDate endDate;
    private String trainNo;
    private String reportType;
    private String format;
    
    public ReportCriteria() {}
    
    // Getters
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public String getTrainNo() { return trainNo; }
    public String getReportType() { return reportType; }
    public String getFormat() { return format; }
    
    // Setters
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public void setTrainNo(String trainNo) { this.trainNo = trainNo; }
    public void setReportType(String reportType) { this.reportType = reportType; }
    public void setFormat(String format) { this.format = format; }
}
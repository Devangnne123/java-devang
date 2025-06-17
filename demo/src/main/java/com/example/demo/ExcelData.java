package com.example.demo;

import jakarta.persistence.*;

@Entity
@Table(name = "excel_data")
public class ExcelData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "linkedin_id", nullable = false, unique = true)
    private String linkedinId;

    @Column(name = "person_name")
    private String personName;

    @Column(name = "mobile_number")
    private String mobileNumber;

    @Column(name = "mobile_number_2")
    private String mobileNumber2;

    @Column(name = "person_location")
    private String personLocation;

    @Column(name = "linkedin_url", nullable = false)
    private String linkedinUrl;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public String getLinkedinId() {
        return linkedinId;
    }

    public void setLinkedinId(String linkedinId) {
        this.linkedinId = linkedinId;
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getMobileNumber2() {
        return mobileNumber2;
    }

    public void setMobileNumber2(String mobileNumber2) {
        this.mobileNumber2 = mobileNumber2;
    }

    public String getPersonLocation() {
        return personLocation;
    }

    public void setPersonLocation(String personLocation) {
        this.personLocation = personLocation;
    }

    public String getLinkedinUrl() {
        return linkedinUrl;
    }

    public void setLinkedinUrl(String linkedinUrl) {
        this.linkedinUrl = linkedinUrl;
    }
}

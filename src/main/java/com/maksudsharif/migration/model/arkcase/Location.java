package com.maksudsharif.migration.model.arkcase;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.voodoodyne.jackson.jsog.JSOGGenerator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = JSOGGenerator.class, scope = Organization.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Location
{
    private Long id;
    private String className = "com.maksudsharif.Location";
    private String type = "Business";
    private String country;
    private String district;
    private String streetAddress;
    private String city;
    private String addressName;
    private String facilityActivities;
    private String tgaLocationId;
    private String state;
    private String streetAddress2;
    private String zip;
    private String clientReferenceNum;
    private String pmdaNum;
    private String fdaRegistrationNum;
    private String fdaFei;
    private String brazilianGmpRequestNum;
    private String anvisaNum;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private Date created;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private Date modified;
    private String creator;
    private String modifier;
    private String status = "ACTIVE";
}

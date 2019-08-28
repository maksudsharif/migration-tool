package com.maksudsharif.migration.model.arkcase;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.voodoodyne.jackson.jsog.JSOGGenerator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = JSOGGenerator.class, scope = Organization.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Organization
{
    private String className = "com.maksudsharif.Organization";
    private String organizationId;
    private List<String> organizationTypes;
    private String organizationType = "Manufacturer";
    private List<ContactMethod> contactMethods = new ArrayList<>();
    private List<Location> addresses = new ArrayList<>();
    private List<Manufacturer> manufacturers = new ArrayList<>();
    private List<OrganizationDBA> organizationDBAs = new ArrayList<>();
    private String modificationStatus = "N/A";
    private Boolean isCertificateHolder;
    private Boolean isLegalManufacturer = false;
    private Boolean isCampus;
    private String status = "ACTIVE";
    private String referenceString = "";
    private String organizationValue;
    private String responsibleAo;
    private String dunsNum;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private Date initialAuditStartDate;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private Date initialAuditEndDate;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private Date activeDate;
    @JsonProperty("isUnderJurisdictionUSA")
    private Boolean isUnderJurisdictionUSA = false;
    @JsonProperty("isUnderJurisdictionJAP")
    private Boolean isUnderJurisdictionJAP = false;
    @JsonProperty("isUnderJurisdictionBRA")
    private Boolean isUnderJurisdictionBRA = false;
    @JsonProperty("isUnderJurisdictionAUS")
    private Boolean isUnderJurisdictionAUS = false;
    @JsonProperty("isUnderJurisdictionCAN")
    private Boolean isUnderJurisdictionCAN = false;
    private Location defaultAddress;
    private String additionalInformation;
    private List<PersonOrganizationAssociation> personAssociations;
    private PersonOrganizationAssociation primaryContact;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private Date created;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private Date modified;
    private String creator;
    private String modifier;
    private List<OrganizationAssociation> associationsToObjects;

    @Override
    public String toString()
    {
        return "Organization{" +
                "organizationId='" + organizationId + '\'' +
                ", organizationValue='" + organizationValue + '\'' +
                ", responsibleAo='" + responsibleAo + '\'' +
                '}';
    }
}

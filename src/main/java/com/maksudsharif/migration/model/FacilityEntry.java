package com.maksudsharif.migration.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.annotations.Expose;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVRecord;
import com.maksudsharif.migration.model.arkcase.ContactMethod;
import com.maksudsharif.migration.model.arkcase.Manufacturer;
import com.maksudsharif.migration.model.arkcase.Location;
import com.maksudsharif.migration.model.arkcase.Organization;
import com.maksudsharif.migration.model.arkcase.OrganizationDBA;
import com.maksudsharif.migration.model.arkcase.Person;
import com.maksudsharif.migration.model.arkcase.PersonOrganizationAssociation;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@NoArgsConstructor
@Data
public class FacilityEntry implements Cloneable
{
    @JsonIgnore
    private List<ValidationResult> errors = new ArrayList<>();
    @JsonIgnore
    private CSVRecord primary;

    @Expose
    private String id;

    @NonNull
    @JsonIgnore
    private List<CSVRecord> recordList;

    @Expose
    private String created;

    @Expose
    private String facilityName;

    @Expose
    private String duns;

    @Expose
    private String initialAuditStartDate;

    @Expose
    private String initialAuditEndDate;

    @Expose
    private String responsibleAO;

    @Expose
    private String additionalInformation;

    @Expose
    private String isCertificateHolder;

    @Expose
    private List<PhoneNumber> phoneNumbers;

    @Expose
    private List<Fax> faxes;

    @Expose
    private List<Email> emails;

    @Expose
    private List<URL> urls;

    @Expose
    private String campus;

    @Expose
    private List<Address> addresses;

    @Expose
    private Jurisdictions jurisdictions;

    @Expose
    private OtherTradeNames otherTradeNames;

    @Expose
    private List<CertficateHolder> certficateHolders;

    @Expose
    private String referenceString;

    @Expose
    private List<ContactPerson> contactPersons;

    @JsonIgnore
    private PersonOrganizationAssociation primaryContact;
    @JsonIgnore
    private Person primaryPerson;
    @JsonIgnore
    private Location defaultAddress;
    @JsonIgnore
    private List<Person> arkcaseContactPersons;
    @JsonIgnore
    private List<Location> arkcaseAddresses;
    @JsonIgnore
    private List<ContactMethod> arkcaseContactMethods;
    @JsonIgnore
    private List<OrganizationDBA> organizationDBAs;
    @JsonIgnore
    private List<PersonOrganizationAssociation> personAssociations;
    @JsonIgnore
    private List<Manufacturer> manufacturers;

    @Expose
    private Organization organization;

    public FacilityEntry addValidation(ValidationResult validationResult)
    {
        synchronized (this)
        {
            errors.add(validationResult);
        }
        return this;
    }

    public boolean hasError()
    {
        return errors.stream().flatMap(validationResult -> validationResult.getMetadataErrors().stream()).findAny().isPresent();
    }

    @Override
    public String toString()
    {
        return "FacilityEntry{" +
                "id='" + id + '\'' +
                ", facilityName='" + facilityName + '\'' +
                ", duns='" + duns + '\'' +
                '}';
    }
}

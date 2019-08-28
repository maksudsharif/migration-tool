package com.maksudsharif.migration.model.arkcase;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voodoodyne.jackson.jsog.JSOGGenerator;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@JsonIdentityInfo(generator = JSOGGenerator.class, scope = Organization.class)
public class Person
{
    private String className = "com.armedia.acm.plugins.person.model.Person";
    private List<Object> identifications = new ArrayList<>();
    private List<Object> associationsFromObjects = new ArrayList<>();
    private List<Object> addresses = new ArrayList<>();
    private String familyName;
    private String givenName;
    private String title;
    private List<ContactMethod> contactMethods;
    private ContactMethod defaultPhone;
    private ContactMethod defaultEmail;
    private String defaultUrl;

    // -- core fields --
    private Long id;
    private String company;
    private String status;
    private String middleName;
    private String hairColor;
    private String eyeColor;
    private Long heightInInches;
    private Long weightInPounds;
    private LocalDate dateOfBirth;
    private String placeOfBirth;
    private LocalDate dateMarried;
    private Date created;
    private String creator;
    private Date modified;
    private String modifier;
    private List<Object> securityTags = new ArrayList<>();
    private List<Object> personAliases = new ArrayList<>();
    private List<Object> associationsToObjects = new ArrayList<>();
    private List<Object> organizations = new ArrayList<>();
    @JsonIgnore
    private Object container;
    private Object defaultPicture;
    private String objectType = "PERSON";
    private Object defaultAddress;
    private Object defaultAlias;
    private Object defaultIdentification;
    private Object defaultOrganization;
    private String details;
    private List<Object> organizationAssociations = new ArrayList<>();

}

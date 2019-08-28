package com.maksudsharif.migration.model.arkcase;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.voodoodyne.jackson.jsog.JSOGGenerator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = JSOGGenerator.class, scope = Organization.class)
public class PersonOrganizationAssociation
{
    private Long id;
    private String organizationToPersonAssociationType = "Employer";
    private String personToOrganizationAssociationType = "Employee";
    private Person person;
    private Organization organization;

    private String description;
    private Date created;
    private String creator;
    private Date modified;
    private String modifier;
    private String className = "com.armedia.acm.plugins.person.model.PersonOrganizationAssociation";
    private String objectType;
}

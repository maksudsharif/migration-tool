package com.maksudsharif.migration.model.arkcase;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.voodoodyne.jackson.jsog.JSOGGenerator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIdentityInfo(generator = JSOGGenerator.class, scope = Organization.class)
public class PrimaryContactDto
{
    private PersonOrganizationAssociation primaryPersonAssociation;
    private Person person;
}

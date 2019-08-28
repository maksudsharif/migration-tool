package com.maksudsharif.migration.model.arkcase;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.voodoodyne.jackson.jsog.JSOGGenerator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIdentityInfo(generator = JSOGGenerator.class, scope = Organization.class)
public class MdmProfileDto
{
    private Organization mdmProfile;
    private PrimaryContactDto primaryContact;
    private List<Person> peopleInfo;
    private boolean certificateHolderChanged;
    private boolean relatedFacilitiesChanged;
    private boolean newOrganization;
}

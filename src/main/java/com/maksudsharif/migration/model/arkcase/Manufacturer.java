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
public class Manufacturer
{
    private Long id;
    private String parentType = "MDM_PROFILE";
    private String certificationScope;
    private String tgaClientId;
    private String hcCompanyId;

    private String mdmProfile;
    private String objectType = "MANUFACTURER";
    private String primaryName;
    private String otherName;
    private String address;
    private Date created;
    private String creator;
    private Date modified;
    private String modifier;
}

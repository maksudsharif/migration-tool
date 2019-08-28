package com.maksudsharif.migration.model.arkcase;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.voodoodyne.jackson.jsog.JSOGGenerator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = JSOGGenerator.class, scope = Organization.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContactMethod
{
    private Long id;
    private String type;
    private String value;
    private String subType;
    private Date created;
    private String creator;
    private Date modified;
    private String modifier;
    private String status;
    private String description;
    private List<String> types = new ArrayList<>();
    private String className = "com.armedia.acm.plugins.addressable.model.ContactMethod";

    public ContactMethod(String value, String type, String subType)
    {
        this.value = value;
        this.type = type;
        this.subType = subType;
    }
}


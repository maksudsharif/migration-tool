package com.maksudsharif.migration.model.arkcase;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.Expose;
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
public class OrganizationAssociation
{
    @Expose
    private Long id;
    @Expose
    private Organization organization;
    @Expose
    private String associationType;
    @Expose
    private Long parentId;
    @Expose
    private String parentType;
    @Expose
    private String description;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Expose
    private Date created;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Expose
    private Date modified;
    @Expose
    private String creator;
    @Expose
    private String modifier;
}

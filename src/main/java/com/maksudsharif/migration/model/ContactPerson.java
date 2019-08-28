package com.maksudsharif.migration.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.annotations.Expose;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Data
@NoArgsConstructor
public class ContactPerson
{
    @Expose
    private String lastName;

    @Expose
    private String firstName;

    @Expose
    private String phoneNumber;

    @Expose
    private String email;

    @Expose
    private String title;

    @Expose
    private String primary;

    @JsonIgnore
    private String id;

    public boolean isEmpty()
    {
        return StringUtils.isAllEmpty(getLastName(), getFirstName(), getPhoneNumber(), getEmail(), getTitle(), getPrimary());
    }
}

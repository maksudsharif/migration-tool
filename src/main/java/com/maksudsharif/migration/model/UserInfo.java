package com.maksudsharif.migration.model;

import com.google.gson.annotations.Expose;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class UserInfo
{
    @Expose
    private String userId;

    @Expose
    private String fullName;

    @Expose
    private String mail;

    @Expose
    private String firstName;

    @Expose
    private String lastName;

    @Expose
    private List<String> authorities;

    @Expose
    private List<String> privileges;

    @Expose
    private String directoryName;

    @Expose
    private String country;

    @Expose
    private String countryAbbreviation;

    @Expose
    private String department;

    @Expose
    private String company;

    @Expose
    private String title;
}

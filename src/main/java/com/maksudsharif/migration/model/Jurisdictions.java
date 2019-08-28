package com.maksudsharif.migration.model;

import com.google.gson.annotations.Expose;
import lombok.Data;
import lombok.NonNull;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class Jurisdictions
{
    @Expose
    private Set<String> jurisdictions;

    public Jurisdictions(@NonNull String csv)
    {
        try
        {
            jurisdictions = Arrays.stream(csv.split(";")).map(String::trim).collect(Collectors.toSet());
        } catch (Exception ignored)
        {
            // Ignored
        }
    }
}

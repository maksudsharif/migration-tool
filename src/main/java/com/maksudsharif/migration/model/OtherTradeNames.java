package com.maksudsharif.migration.model;

import com.google.gson.annotations.Expose;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class OtherTradeNames
{
    @Expose
    private Set<String> names;

    public OtherTradeNames(String name)
    {
        try
        {
            if (StringUtils.isNotEmpty(name.trim()))
            {
                names = Arrays.stream(name.trim().split(",")).map(String::trim).filter(StringUtils::isNotEmpty).collect(Collectors.toSet());
            } else
            {
                names = Collections.emptySet();
            }
        } catch (Exception ignored)
        {
            // Ignored
        }
    }
}

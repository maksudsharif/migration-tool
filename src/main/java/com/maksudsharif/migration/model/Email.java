package com.maksudsharif.migration.model;

import com.google.gson.annotations.Expose;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
@AllArgsConstructor
public class Email
{
    @Expose
    String email;

    @Expose
    String type;

    public boolean isEmpty()
    {
        return StringUtils.isAllEmpty(getEmail(), getType());
    }
}

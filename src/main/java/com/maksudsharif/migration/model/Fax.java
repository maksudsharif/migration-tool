package com.maksudsharif.migration.model;

import com.google.gson.annotations.Expose;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
@AllArgsConstructor
public class Fax
{
    @Expose
    String number;

    @Expose
    String type;

    public boolean isEmpty()
    {
        return StringUtils.isAllEmpty(getNumber(), getType());
    }
}

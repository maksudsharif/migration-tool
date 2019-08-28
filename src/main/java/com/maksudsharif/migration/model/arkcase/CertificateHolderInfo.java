package com.maksudsharif.migration.model.arkcase;

import com.google.gson.annotations.Expose;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificateHolderInfo
{
    @Expose
    private Long mdmProfileId;
    @Expose
    private String name;
    @Expose
    private String title;
}

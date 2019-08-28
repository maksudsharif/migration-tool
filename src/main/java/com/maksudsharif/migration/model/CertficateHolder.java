package com.maksudsharif.migration.model;

import com.google.gson.annotations.Expose;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertficateHolder
{
    @Expose
    private String tgaClientId;

    @Expose
    private String hcCompanyId;

    @Expose
    private String scopeOfCertification;

    @Expose
    private String relatedCertificateHolderFirmName;

    @Expose
    private String relatedCertificateHolderDunsNum;

    public boolean isEmpty()
    {
        return StringUtils.isAllEmpty(getTgaClientId(), getHcCompanyId(), getScopeOfCertification(), getRelatedCertificateHolderFirmName());
    }
}

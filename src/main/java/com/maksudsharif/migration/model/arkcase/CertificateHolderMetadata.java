package com.maksudsharif.migration.model.arkcase;

import com.google.gson.annotations.Expose;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CertificateHolderMetadata
{
    @Expose
    private List<CertificateHolderInfo> holderInfo;
}

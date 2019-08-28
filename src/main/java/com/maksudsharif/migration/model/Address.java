package com.maksudsharif.migration.model;

import com.google.gson.annotations.Expose;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Data
@NoArgsConstructor
public class Address
{

    @Expose
    private String aoClientReference;

    @Expose
    private String primaryCampus;

    @Expose
    private String campusBuildingName;

    @Expose
    private String address1;

    @Expose
    private String address2;

    @Expose
    private String country;

    @Expose
    private String city;

    @Expose
    private String state;

    @Expose
    private String zip;

    @Expose
    private String tgaLocationId;

    @Expose
    private String mhwPmdaRegNum;

    @Expose
    private String fdaReg;

    @Expose
    private String fdaFei;

    @Expose
    private String fdaDistrict;

    @Expose
    private String gmpReqNum;

    @Expose
    private String anvisaNum;

    @Expose
    private String facilityActivities;

    public boolean isEmpty()
    {
        return StringUtils.isAllEmpty(getAoClientReference(), getPrimaryCampus(), getCampusBuildingName()
                , getAddress1(), getAddress2(), getCountry(), getCity(), getState(), getZip(), getTgaLocationId()
                , getMhwPmdaRegNum(), getFdaReg(), getFdaFei(), getFdaDistrict(), getGmpReqNum(), getAnvisaNum(), getFacilityActivities());
    }
}

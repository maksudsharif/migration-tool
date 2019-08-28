package com.maksudsharif.migration.services.validators;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import com.maksudsharif.migration.model.Address;
import com.maksudsharif.migration.model.FacilityEntry;
import com.maksudsharif.migration.model.HeaderConstants;
import com.maksudsharif.migration.model.MetadataError;
import com.maksudsharif.migration.model.ValidationResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
@Log4j2
public class AddressesValidator extends AbstractValidator
{

    @Override
    public FacilityEntry validate(FacilityEntry entry)
    {
        List<MetadataError> errors = new ArrayList<>();

        validateRequired(entry, errors);

        List<Address> addresses = entry.getAddresses();

        if ("YES".equalsIgnoreCase(entry.getCampus()))
        {
            int primaries = 0;
            for (Address address : addresses)
            {
                // find duplicate primary cammpus
                if ("YES".equalsIgnoreCase(address.getPrimaryCampus()))
                {
                    primaries++;
                }
            }

            if (primaries > 1)
            {
                addInvalidError(errors, HeaderConstants.PRIMARY_CAMPUS, "Yes");
            }

            if (primaries == 0)
            {
                addInvalidError(errors, HeaderConstants.PRIMARY_CAMPUS, "No");
            }
        } else
        {
            if (addresses.size() > 1)
            {
                addInvalidError(errors, HeaderConstants.ADDRESSES, "Too Many Addresses, and not campus.");
            }
        }

        List<String> stateCountries = Arrays.asList("AU,BR,CA,JP,US".split(","));
        Map<String, String> countryLookups = getResourceService().getCountryLookups();
        Map<String, String> districtLookups = getResourceService().getDistrictLookups();

        for (Address address : addresses)
        {
            if (StringUtils.isNotEmpty(address.getCountry()) && !countryLookups.containsKey(address.getCountry()))
            {
                addInvalidError(errors, HeaderConstants.COUNTRY, address.getCountry());
            }

            if (stateCountries.contains(address.getCountry()))
            {
                String state = address.getState();
                if (!getResourceService().getCodesForCountry(address.getCountry()).contains(state))
                {
                    addInvalidError(errors, HeaderConstants.STATE_PROVINCE, state);
                }
            } else
            {
                // Just check max length of state
                if (StringUtils.isNotEmpty(address.getState()) && StringUtils.length(address.getState()) > 100)
                {
                    addInvalidError(errors, HeaderConstants.STATE_PROVINCE, address.getState(), "Value is greater than 100 characters.");
                }
            }

            if (StringUtils.length(address.getAddress1()) > 255)
            {
                addInvalidError(errors, HeaderConstants.ADDRESS_1, address.getAddress1(), "Value is greater than 255 characters.");
            }

            if (StringUtils.isNotEmpty(address.getAddress2()) && StringUtils.length(address.getAddress2()) > 255)
            {
                addInvalidError(errors, HeaderConstants.ADDRESS_2, address.getAddress2(), "Value is greater than 255 characters.");
            }

            if (StringUtils.isNotEmpty(address.getFdaDistrict()) && !districtLookups.containsKey(address.getFdaDistrict()))
            {
                addInvalidError(errors, HeaderConstants.FDA_DISTRICT, address.getFdaDistrict());
            }

            if (StringUtils.isNotEmpty(address.getZip()) && StringUtils.length(address.getZip()) > 20)
            {
                addInvalidError(errors, HeaderConstants.ZIP_POSTAL_CODE, address.getZip(), "Value is greater than 20 characters.");
            }

            if (StringUtils.isNotEmpty(address.getTgaLocationId()) && StringUtils.length(address.getTgaLocationId()) > 15)
            {
                addInvalidError(errors, HeaderConstants.TGA_LOCATION_ID, address.getTgaLocationId(), "Value is greater than 15 characters.");
            }

            if (StringUtils.isNotEmpty(address.getMhwPmdaRegNum()) && StringUtils.length(address.getMhwPmdaRegNum()) > 15)
            {
                addInvalidError(errors, HeaderConstants.MHLW_PMDA_REGISTRATION_NUM, address.getTgaLocationId(), "Value is greater than 15 characters.");
            }

            if (StringUtils.isNotEmpty(address.getFdaReg()) && StringUtils.length(address.getFdaReg()) > 10)
            {
                addInvalidError(errors, HeaderConstants.FDA_REGISTRATION_NUM, address.getTgaLocationId(), "Value is greater than 10 characters.");
            }

            if (StringUtils.isNotEmpty(address.getFdaFei()) && StringUtils.length(address.getFdaFei()) > 10)
            {
                addInvalidError(errors, HeaderConstants.FDA_FEI, address.getTgaLocationId(), "Value is greater than 10 characters.");
            }

            if (StringUtils.isNotEmpty(address.getGmpReqNum()) && StringUtils.length(address.getGmpReqNum()) > 15)
            {
                addInvalidError(errors, HeaderConstants.BRAZILIAN_GMP_REQUEST_NUM, address.getTgaLocationId(), "Value is greater than 15 characters.");
            }

            if (StringUtils.isNotEmpty(address.getAnvisaNum()) && StringUtils.length(address.getAnvisaNum()) > 15)
            {
                addInvalidError(errors, HeaderConstants.ANVISA_NUM, address.getTgaLocationId(), "Value is greater than 15 characters.");
            }

            if (StringUtils.isNotEmpty(address.getAoClientReference()) && StringUtils.length(address.getAoClientReference()) > 125)
            {
                addInvalidError(errors, HeaderConstants.AO_CLIENT_REFERENCE_NUM, address.getAoClientReference(), "Value is greater than 125 characters");
            }

        }

        return errors.isEmpty() ? entry : entry.addValidation(new ValidationResult(errors));
    }

    private void validateRequired(FacilityEntry entry, List<MetadataError> errors)
    {
        if (entry.getAddresses() == null)
        {
            addRequiredError(errors, HeaderConstants.ADDRESSES);
        }

        List<Address> addresses = entry.getAddresses();

        if (addresses.isEmpty())
        {
            addRequiredError(errors, HeaderConstants.ADDRESSES);
        }

        String campus = entry.getCampus();
        if (StringUtils.isEmpty(campus))
        {
            addRequiredError(errors, HeaderConstants.CAMPUS);
        } else
        {
            if (!Arrays.asList("YES", "NO").contains(campus.toUpperCase()))
            {
                addInvalidError(errors, HeaderConstants.CAMPUS, campus);
            }
        }

        boolean isCampus = "YES".equalsIgnoreCase(campus);

        for (Address address : addresses)
        {
            if (isCampus)
            {
                String primaryCampus = address.getPrimaryCampus();
                if (StringUtils.isEmpty(primaryCampus))
                {
                    addRequiredError(errors, HeaderConstants.PRIMARY_CAMPUS);
                }

                if (!Arrays.asList("YES", "NO").contains(primaryCampus.toUpperCase()))
                {
                    addInvalidError(errors, HeaderConstants.PRIMARY_CAMPUS, primaryCampus);
                }

                String campusBuildingName = address.getCampusBuildingName();
                if (StringUtils.isEmpty(campusBuildingName))
                {
                    addRequiredError(errors, HeaderConstants.CAMPUS_BUILDING_NAME);
                }

                if (StringUtils.length(campusBuildingName) > 255)
                {
                    addInvalidError(errors, HeaderConstants.CAMPUS_BUILDING_NAME, "Value is greater than 255 characters.");
                }
            }

            String address1 = address.getAddress1();
            if (StringUtils.isEmpty(address1))
            {
                addRequiredError(errors, HeaderConstants.ADDRESS_1);
            }

            String country = address.getCountry();
            if (StringUtils.isEmpty(country))
            {
                addRequiredError(errors, HeaderConstants.COUNTRY);
            }

            String city = address.getCity();
            if (StringUtils.isEmpty(city))
            {
                addRequiredError(errors, HeaderConstants.CITY);
            }
            if (StringUtils.length(city) > 255)
            {
                addInvalidError(errors, HeaderConstants.CITY, "Value is greater than 255 characters.");
            }

            String facilityActivities = address.getFacilityActivities();
            if (StringUtils.isEmpty(facilityActivities))
            {
                addRequiredError(errors, HeaderConstants.FACILITY_ACTIVITIES);
            }
            if (StringUtils.length(facilityActivities) > 3000)
            {
                addInvalidError(errors, HeaderConstants.FACILITY_ACTIVITIES, "Value is greater than 3000 characters.");
            }

        }
    }
}

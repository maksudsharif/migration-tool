package com.maksudsharif.migration.services.validators;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import com.maksudsharif.migration.model.FacilityEntry;
import com.maksudsharif.migration.model.HeaderConstants;
import com.maksudsharif.migration.model.MetadataError;
import com.maksudsharif.migration.model.PhoneNumber;
import com.maksudsharif.migration.model.ValidationResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Log4j2
public class PhoneNumbersValidator extends AbstractValidator
{

    @Override
    public FacilityEntry validate(FacilityEntry entry)
    {
        List<MetadataError> errors = new ArrayList<>();

        validateInvalid(entry, errors);

        return errors.isEmpty() ? entry : entry.addValidation(new ValidationResult(errors));
    }

    private void validateInvalid(FacilityEntry entry, List<MetadataError> errors)
    {
        for (PhoneNumber phoneNumber : entry.getPhoneNumbers())
        {
            if (StringUtils.isEmpty(phoneNumber.getNumber()) || StringUtils.length(phoneNumber.getNumber()) > 1024)
            {
                addInvalidError(errors, HeaderConstants.PHONE_NUMBER, phoneNumber.getNumber());
            }

            if (StringUtils.isNotEmpty(phoneNumber.getNumber()) && StringUtils.isEmpty(phoneNumber.getType()))
            {
                addInvalidError(errors, HeaderConstants.PHONE_TYPE, phoneNumber.getType());
            }
        }
    }
}

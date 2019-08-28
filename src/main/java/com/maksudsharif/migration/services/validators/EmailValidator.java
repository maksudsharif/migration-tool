package com.maksudsharif.migration.services.validators;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import com.maksudsharif.migration.model.Email;
import com.maksudsharif.migration.model.FacilityEntry;
import com.maksudsharif.migration.model.HeaderConstants;
import com.maksudsharif.migration.model.MetadataError;
import com.maksudsharif.migration.model.ValidationResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Log4j2
public class EmailValidator extends AbstractValidator
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
        for (Email email : entry.getEmails())
        {
            if (StringUtils.isEmpty(email.getEmail()) || StringUtils.length(email.getEmail()) > 1024)
            {
                addInvalidError(errors, HeaderConstants.EMAIL, email.getEmail());
            }

            if (StringUtils.isNotEmpty(email.getEmail()) && StringUtils.isEmpty(email.getType()))
            {
                addInvalidError(errors, HeaderConstants.EMAIL_TYPE, email.getType());
            }
        }
    }
}

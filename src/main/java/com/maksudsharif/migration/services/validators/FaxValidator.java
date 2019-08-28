package com.maksudsharif.migration.services.validators;

import com.maksudsharif.migration.model.FacilityEntry;
import com.maksudsharif.migration.model.ValidationResult;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import com.maksudsharif.migration.model.Fax;
import com.maksudsharif.migration.model.HeaderConstants;
import com.maksudsharif.migration.model.MetadataError;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Log4j2
public class FaxValidator extends AbstractValidator
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
        for (Fax fax : entry.getFaxes())
        {
            if (StringUtils.isEmpty(fax.getNumber()) || StringUtils.length(fax.getNumber()) > 1024)
            {
                addInvalidError(errors, HeaderConstants.FAX, fax.getNumber());
            }

            if (StringUtils.isNotEmpty(fax.getNumber()) && StringUtils.isEmpty(fax.getType()))
            {
                addInvalidError(errors, HeaderConstants.FAX_TYPE, fax.getType());
            }
        }
    }
}

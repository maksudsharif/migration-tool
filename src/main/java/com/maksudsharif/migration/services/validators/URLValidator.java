package com.maksudsharif.migration.services.validators;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import com.maksudsharif.migration.model.FacilityEntry;
import com.maksudsharif.migration.model.HeaderConstants;
import com.maksudsharif.migration.model.MetadataError;
import com.maksudsharif.migration.model.URL;
import com.maksudsharif.migration.model.ValidationResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Log4j2
public class URLValidator extends AbstractValidator
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
        for (URL url : entry.getUrls())
        {
            if (StringUtils.isEmpty(url.getUrl()) || StringUtils.length(url.getUrl()) > 1024)
            {
                addInvalidError(errors, HeaderConstants.URL, url.getUrl());
            }

            if (StringUtils.isNotEmpty(url.getUrl()) && StringUtils.isEmpty(url.getType()))
            {
                addInvalidError(errors, HeaderConstants.URL_TYPE, url.getType());
            }
        }
    }
}

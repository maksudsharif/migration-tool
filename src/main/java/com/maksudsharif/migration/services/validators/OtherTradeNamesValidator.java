package com.maksudsharif.migration.services.validators;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import com.maksudsharif.migration.model.FacilityEntry;
import com.maksudsharif.migration.model.HeaderConstants;
import com.maksudsharif.migration.model.MetadataError;
import com.maksudsharif.migration.model.ValidationResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
@Log4j2
public class OtherTradeNamesValidator extends AbstractValidator
{

    @Override
    public FacilityEntry validate(FacilityEntry entry)
    {
        List<MetadataError> errors = new ArrayList<>();

        if (entry.getOtherTradeNames() != null)
        {
            Set<String> names = entry.getOtherTradeNames().getNames();
            names.forEach(name -> {
                if (StringUtils.length(name) > 1024)
                {
                    addInvalidError(errors, HeaderConstants.OTHER_TRADE_NAMES, name);
                }
            });

        }

        return errors.isEmpty() ? entry : entry.addValidation(new ValidationResult(errors));
    }
}

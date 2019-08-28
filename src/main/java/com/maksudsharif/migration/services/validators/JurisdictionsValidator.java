package com.maksudsharif.migration.services.validators;

import com.google.common.collect.Sets;
import lombok.extern.log4j.Log4j2;
import com.maksudsharif.migration.model.FacilityEntry;
import com.maksudsharif.migration.model.HeaderConstants;
import com.maksudsharif.migration.model.Jurisdictions;
import com.maksudsharif.migration.model.MetadataError;
import com.maksudsharif.migration.model.ValidationResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
@Log4j2
public class JurisdictionsValidator extends AbstractValidator
{
    private final LinkedHashSet<String> knownJurisdictions = Sets.newLinkedHashSet(Arrays.asList("Australia - TGA", "Brazil - ANVISA", "Canada - Health Canada", "Japan - MHLW and PMDA", "United States - FDA"));

    @Override
    public FacilityEntry validate(FacilityEntry entry)
    {
        List<MetadataError> errors = new ArrayList<>();

        validateRequired(entry, errors);

        Set<String> set = entry.getJurisdictions().getJurisdictions();
        set.forEach(jurisdiction -> {
            if (!knownJurisdictions.contains(jurisdiction))
            {
                addInvalidError(errors, HeaderConstants.JURISDICTION, jurisdiction);
            }
        });

        return errors.isEmpty() ? entry : entry.addValidation(new ValidationResult(errors));
    }

    private void validateRequired(FacilityEntry entry, List<MetadataError> errors)
    {
        if (entry.getJurisdictions() == null)
        {
            addRequiredError(errors, HeaderConstants.JURISDICTION);
        }

        Jurisdictions jurisdictions = entry.getJurisdictions();

        Set<String> set = jurisdictions.getJurisdictions();
        if (set.isEmpty())
        {
            addRequiredError(errors, HeaderConstants.JURISDICTION);
        }
    }
}

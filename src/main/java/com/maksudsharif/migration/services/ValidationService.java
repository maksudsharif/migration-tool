package com.maksudsharif.migration.services;

import lombok.extern.log4j.Log4j2;
import com.maksudsharif.migration.model.FacilityEntry;
import com.maksudsharif.migration.model.Validator;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log4j2
public class ValidationService
{
    private List<Validator> validators;

    public ValidationService(List<Validator> validators)
    {
        this.validators = validators;
    }

    public FacilityEntry validate(FacilityEntry facilityEntry)
    {
        for (Validator validator : validators)
        {
            log.debug("Validating {} with {}", facilityEntry.getId(), validator.getClass().getTypeName());
            facilityEntry = validator.validate(facilityEntry);
        }
        log.debug("Validated {}", facilityEntry.getId());
        return facilityEntry;
    }
}

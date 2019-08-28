package com.maksudsharif.migration.services.validators;

import com.maksudsharif.migration.services.ArkCaseTransformService;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import com.maksudsharif.migration.model.FacilityEntry;
import com.maksudsharif.migration.model.MetadataError;
import com.maksudsharif.migration.model.Validator;
import com.maksudsharif.migration.services.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Getter
@Log4j2
public abstract class AbstractValidator implements Validator
{
    private final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    protected ArkCaseTransformService arkCaseTransformService;

    @Autowired
    protected ResourceService resourceService;

    @Override
    public abstract FacilityEntry validate(FacilityEntry entry);

    public void addRequiredError(List<MetadataError> errors, String field)
    {
        errors.add(new MetadataError(String.format("Missing required field: %s", field)));
    }

    public void addInvalidError(List<MetadataError> errors, String field, String value)
    {
        errors.add(new MetadataError(String.format("Invalid value for field: %s => %s", field, value)));
    }

    public void addInvalidError(List<MetadataError> errors, String field, String value, String description)
    {
        errors.add(new MetadataError(String.format("Invalid value for field: %s => %s (%s)", field, value, description)));
    }

    public boolean isValidDate(String date)
    {
        try
        {
            return StringUtils.isNotEmpty(date) && LocalDate.parse(date, format).toString().equalsIgnoreCase(date);
        } catch (DateTimeParseException dtpe)
        {
            log.error("Unable to parse date {}", date, dtpe);
            return false;
        }
    }
}

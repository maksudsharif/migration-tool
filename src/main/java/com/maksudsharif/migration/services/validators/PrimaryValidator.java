package com.maksudsharif.migration.services.validators;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import com.maksudsharif.migration.model.FacilityEntry;
import com.maksudsharif.migration.model.HeaderConstants;
import com.maksudsharif.migration.model.MetadataError;
import com.maksudsharif.migration.model.ValidationResult;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
@Log4j2
public class PrimaryValidator extends AbstractValidator
{
    @Override
    public FacilityEntry validate(FacilityEntry entry)
    {
        List<MetadataError> errors = new ArrayList<>();

        validateRequired(entry, errors);

        // Validate date formats
        String initialAuditStartDate = entry.getInitialAuditStartDate();
        boolean validInitialAuditStartDate = isValidDate(initialAuditStartDate);
        if (!validInitialAuditStartDate)
        {
            addInvalidError(errors, HeaderConstants.INITIAL_AUDIT_START_DATE, initialAuditStartDate);
        }

        String initialAuditEndDate = entry.getInitialAuditEndDate();
        boolean validInitialAuditEndDate = isValidDate(initialAuditEndDate);
        if (!validInitialAuditEndDate)
        {
            addInvalidError(errors, HeaderConstants.INITIAL_AUDIT_END_DATE, initialAuditEndDate);
        }

        if (StringUtils.length(entry.getResponsibleAO()) != 4)
        {
            addInvalidError(errors, HeaderConstants.RESPONSIBLE_AOID, entry.getResponsibleAO());
        }

        if (StringUtils.isNotEmpty(entry.getCreated()))
        {
            String createdDate = entry.getCreated();
            boolean isCreatedValidDate = isValidDate(createdDate);
            if (!isCreatedValidDate)
            {
                String today = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                entry.setCreated(today);
                log.trace("Entry missing create date, setting to today's date: [{}] => [{}]", entry, today);
            }
        } else
        {
            String today = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            entry.setCreated(today);
            log.trace("Entry missing create date, setting to today's date: [{}] => [{}]", entry, today);
        }

        if (StringUtils.length(entry.getFacilityName()) > 255)
        {
            addInvalidError(errors, HeaderConstants.FACILITY_NAME, entry.getFacilityName());
        }

        if (StringUtils.length(entry.getDuns()) > 9)
        {
            addInvalidError(errors, HeaderConstants.DUNS_NUM, entry.getDuns());
        }

        if (StringUtils.isNotEmpty(entry.getAdditionalInformation()) && StringUtils.length(entry.getAdditionalInformation()) > 3000)
        {
            addInvalidError(errors, HeaderConstants.ADDITIONAL_INFORMATION, entry.getAdditionalInformation(), "Value is greater than 3000 characters.");
        }

        return errors.isEmpty() ? entry : entry.addValidation(new ValidationResult(errors));
    }

    private void validateRequired(FacilityEntry entry, List<MetadataError> errors)
    {
        if (StringUtils.isEmpty(entry.getFacilityName()))
        {
            addRequiredError(errors, HeaderConstants.FACILITY_NAME);
        }

        if (StringUtils.isEmpty(entry.getInitialAuditStartDate()))
        {
            addRequiredError(errors, HeaderConstants.INITIAL_AUDIT_START_DATE);
        }

        if (StringUtils.isEmpty(entry.getInitialAuditEndDate()))
        {
            addRequiredError(errors, HeaderConstants.INITIAL_AUDIT_END_DATE);
        }

        if (StringUtils.isEmpty(entry.getResponsibleAO()))
        {
            addRequiredError(errors, HeaderConstants.RESPONSIBLE_AOID);
        }
    }
}

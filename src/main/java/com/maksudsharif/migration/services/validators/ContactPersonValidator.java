package com.maksudsharif.migration.services.validators;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import com.maksudsharif.migration.model.ContactPerson;
import com.maksudsharif.migration.model.FacilityEntry;
import com.maksudsharif.migration.model.HeaderConstants;
import com.maksudsharif.migration.model.MetadataError;
import com.maksudsharif.migration.model.ValidationResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@Log4j2
public class ContactPersonValidator extends AbstractValidator
{

    @Override
    public FacilityEntry validate(FacilityEntry entry)
    {
        List<MetadataError> errors = new ArrayList<>();

        validateRequired(entry, errors);

        List<ContactPerson> contactPersons = entry.getContactPersons();

        int primaries = 0;
        for (ContactPerson person : contactPersons)
        {
            // find duplicate primary cammpus
            if ("YES".equalsIgnoreCase(person.getPrimary()))
            {
                primaries++;
            }


            if (StringUtils.isNotEmpty(person.getLastName()) && StringUtils.length(person.getLastName()) > 50)
            {
                addInvalidError(errors, HeaderConstants.LAST_NAME, person.getLastName(), "Value is greater than 30 characters.");
            }

            if (StringUtils.isNotEmpty(person.getFirstName()) && StringUtils.length(person.getFirstName()) > 50)
            {
                addInvalidError(errors, HeaderConstants.FIRST_NAME, person.getFirstName(), "Value is greater than 50 characters.");
            }

            if (StringUtils.isNotEmpty(person.getPhoneNumber()) && StringUtils.length(person.getPhoneNumber()) > 50)
            {
                addInvalidError(errors, HeaderConstants.CONTACT_PERSON_PHONE_NUMBER, person.getPhoneNumber(), "Value is greater than 15 characters.");
            }

            if (StringUtils.isNotEmpty(person.getEmail()) && StringUtils.length(person.getEmail()) > 255)
            {
                addInvalidError(errors, HeaderConstants.CONTACT_PERSON_EMAIL, person.getEmail(), "Value is greater than 255 characters.");
            }

            if (StringUtils.isNotEmpty(person.getTitle()) && StringUtils.length(person.getTitle()) > 255)
            {
                addInvalidError(errors, HeaderConstants.TITLE, person.getTitle(), "Value is greater than 255 characters.");
            }
        }

        if (primaries > 1)
        {
            addInvalidError(errors, HeaderConstants.CONTACT_PERSON_PRIMARY_YES_NO, "Yes");
        }

        if (primaries == 0)
        {
            addInvalidError(errors, HeaderConstants.CONTACT_PERSON_PRIMARY_YES_NO, "No", "Must have at least 1 primary Contact Person");
        }


        return errors.isEmpty() ? entry : entry.addValidation(new ValidationResult(errors));
    }

    private void validateRequired(FacilityEntry entry, List<MetadataError> errors)
    {
        List<ContactPerson> contactPersons = entry.getContactPersons();

        if (CollectionUtils.isEmpty(contactPersons))
        {
            addRequiredError(errors, HeaderConstants.CONTACT_PERSONS);
        }

        int primaries = 0;
        for (ContactPerson contactPerson : contactPersons)
        {
            String primary = contactPerson.getPrimary();
            if (StringUtils.isEmpty(primary))
            {
                addRequiredError(errors, HeaderConstants.CONTACT_PERSON_PRIMARY_YES_NO);
            } else
            {
                if (!Arrays.asList("YES", "NO").contains(primary.toUpperCase()))
                {
                    addInvalidError(errors, HeaderConstants.CONTACT_PERSON_PRIMARY_YES_NO, primary);
                }
            }

            if (StringUtils.isEmpty(contactPerson.getLastName()))
            {
                addRequiredError(errors, HeaderConstants.LAST_NAME);
            }

            if (StringUtils.isEmpty(contactPerson.getFirstName()))
            {
                addRequiredError(errors, HeaderConstants.FIRST_NAME);
            }

            if (StringUtils.isEmpty(contactPerson.getEmail()))
            {
                addRequiredError(errors, HeaderConstants.CONTACT_PERSON_EMAIL);
            }
        }
    }
}

package com.maksudsharif.migration.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.csv.CSVRecord;
import com.maksudsharif.migration.model.Address;
import com.maksudsharif.migration.model.CertficateHolder;
import com.maksudsharif.migration.model.ContactPerson;
import com.maksudsharif.migration.model.Email;
import com.maksudsharif.migration.model.FacilityEntry;
import com.maksudsharif.migration.model.Fax;
import com.maksudsharif.migration.model.HeaderConstants;
import com.maksudsharif.migration.model.Jurisdictions;
import com.maksudsharif.migration.model.OtherTradeNames;
import com.maksudsharif.migration.model.PhoneNumber;
import com.maksudsharif.migration.model.URL;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Log4j2
@Service
public class CSVTransformService
{
    private static final int PRIMARY = 0;

    private final Gson gson;
    private final ObjectMapper objectMapper;

    public CSVTransformService(Gson gson, ObjectMapper objectMapper)
    {
        this.gson = gson;
        this.objectMapper = objectMapper;
    }

    public FacilityEntry transform(FacilityEntry entry)
    {
        try
        {
            List<CSVRecord> recordList = entry.getRecordList();
            CSVRecord primary = recordList.get(PRIMARY);
            entry.setPrimary(primary);

            Field mapping = ReflectionUtils.findField(primary.getClass(), "mapping");
            Objects.requireNonNull(mapping).setAccessible(true);
            Map<String, Integer> headers = ((Map<String, Integer>) mapping.get(primary))
                    .entrySet()
                    .stream()
                    .map(e -> new HashMap.SimpleEntry<>(sanitize(e.getKey()), e.getValue()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            String index = getFromRecord(primary, headers, HeaderConstants.INDEX);
            entry.setId(index);

            String facilityName = getFromRecord(primary, headers, HeaderConstants.FACILITY_NAME);
            entry.setFacilityName(facilityName);

            String createdDate = getFromRecord(primary, headers, HeaderConstants.CREATED_DATE);
            entry.setCreated(createdDate);

            String duns = getFromRecord(primary, headers, HeaderConstants.DUNS_NUM);
            entry.setDuns(duns);

            String initialAuditStartDate = getFromRecord(primary, headers, HeaderConstants.INITIAL_AUDIT_START_DATE);
            String initialAuditEndDate = getFromRecord(primary, headers, HeaderConstants.INITIAL_AUDIT_END_DATE);
            entry.setInitialAuditStartDate(initialAuditStartDate);
            entry.setInitialAuditEndDate(initialAuditEndDate);

            String aoId = getFromRecord(primary, headers, HeaderConstants.RESPONSIBLE_AOID);
            entry.setResponsibleAO(aoId);

            String jurisdictionsValue = getFromRecord(primary, headers, HeaderConstants.JURISDICTION);
            entry.setJurisdictions(new Jurisdictions(jurisdictionsValue));

            String otherTradeNamesValue = getFromRecord(primary, headers, HeaderConstants.OTHER_TRADE_NAMES);
            entry.setOtherTradeNames(new OtherTradeNames(otherTradeNamesValue));

            String additionalInformation = getFromRecord(primary, headers, HeaderConstants.ADDITIONAL_INFORMATION);
            entry.setAdditionalInformation(additionalInformation);

            // Addresses
            String campus = getFromRecord(primary, headers, HeaderConstants.CAMPUS);
            entry.setCampus(campus);
            List<Address> addresses = recordList.stream()
                    .map(record -> getAddressFromRecord(headers, record))
                    .filter(address -> !address.isEmpty())
                    .collect(Collectors.toList());
            entry.setAddresses(addresses);

            // MDSAP Certificate Holders
            entry.setIsCertificateHolder(getFromRecord(primary, headers, HeaderConstants.CERTIFICATE_HOLDER));
            List<CertficateHolder> certficateHolders = recordList.stream()
                    .map(record -> getCertificateHolderFromRecord(headers, record))
                    .filter(certficateHolder -> !certficateHolder.isEmpty())
                    .collect(Collectors.toList());
            entry.setCertficateHolders(certficateHolders);

            // Phone numbers
            List<PhoneNumber> phoneNumbers = recordList.stream()
                    .map(record -> getPhoneNumberFromRecord(headers, record))
                    .filter(phoneNumber -> !phoneNumber.isEmpty())
                    .collect(Collectors.toList());
            entry.setPhoneNumbers(phoneNumbers);

            // Fax
            List<Fax> faxes = recordList.stream()
                    .map(record -> getFaxFromRecord(headers, record))
                    .filter(fax -> !fax.isEmpty())
                    .collect(Collectors.toList());
            entry.setFaxes(faxes);

            // Email
            List<Email> emails = recordList.stream()
                    .map(record -> getEmailFromRecord(headers, record))
                    .filter(email -> !email.isEmpty())
                    .collect(Collectors.toList());
            entry.setEmails(emails);

            // URLs
            List<URL> urls = recordList.stream()
                    .map(record -> getURLFromRecord(headers, record))
                    .filter(url -> !url.isEmpty())
                    .collect(Collectors.toList());
            entry.setUrls(urls);

            // Contact Persons
            List<ContactPerson> persons = recordList.stream()
                    .map(record -> getContactPersonFromRecord(headers, record))
                    .filter(person -> !person.isEmpty())
                    .collect(Collectors.toList());
            entry.setContactPersons(persons);

            log.info("Transformed Facility Entry {}", getFromRecord(primary, headers, HeaderConstants.INDEX));
            log.trace(toJson(entry));
        } catch (Exception e)
        {
            log.info("Failed to transform: {}", entry, e);
        }


        return entry;
    }

    private Address getAddressFromRecord(Map<String, Integer> headers, CSVRecord record)
    {

        Address address = new Address();
        address.setAoClientReference(getFromRecord(record, headers, HeaderConstants.AO_CLIENT_REFERENCE_NUM));
        address.setPrimaryCampus(getFromRecord(record, headers, HeaderConstants.PRIMARY_CAMPUS));
        address.setCampusBuildingName(getFromRecord(record, headers, HeaderConstants.CAMPUS_BUILDING_NAME));
        address.setAddress1(getFromRecord(record, headers, HeaderConstants.ADDRESS_1));
        address.setAddress2(getFromRecord(record, headers, HeaderConstants.ADDRESS_2));
        address.setCity(getFromRecord(record, headers, HeaderConstants.CITY));
        address.setState(getFromRecord(record, headers, HeaderConstants.STATE_PROVINCE));
        address.setCountry(getFromRecord(record, headers, HeaderConstants.COUNTRY));
        address.setZip(getFromRecord(record, headers, HeaderConstants.ZIP_POSTAL_CODE));
        address.setTgaLocationId(getFromRecord(record, headers, HeaderConstants.TGA_LOCATION_ID));
        address.setMhwPmdaRegNum(getFromRecord(record, headers, HeaderConstants.MHLW_PMDA_REGISTRATION_NUM));
        address.setFdaReg(getFromRecord(record, headers, HeaderConstants.FDA_REGISTRATION_NUM));
        address.setFdaFei(getFromRecord(record, headers, HeaderConstants.FDA_FEI));
        address.setFdaDistrict(getFromRecord(record, headers, HeaderConstants.FDA_DISTRICT, "CDRH"));
        address.setGmpReqNum(getFromRecord(record, headers, HeaderConstants.BRAZILIAN_GMP_REQUEST_NUM));
        address.setAnvisaNum(getFromRecord(record, headers, HeaderConstants.ANVISA_NUM));

        String facilityActivities = getFromRecord(record, headers, HeaderConstants.FACILITY_ACTIVITIES);
        address.setFacilityActivities(sanitize(facilityActivities));
        return address;
    }

    private PhoneNumber getPhoneNumberFromRecord(Map<String, Integer> headers, CSVRecord record)
    {
        String phone = getFromRecord(record, headers, HeaderConstants.PHONE_NUMBER);
        String type = getFromRecord(record, headers, HeaderConstants.PHONE_TYPE, "Work");
        return new PhoneNumber(phone, type);
    }

    private Fax getFaxFromRecord(Map<String, Integer> headers, CSVRecord record)
    {
        String phone = getFromRecord(record, headers, HeaderConstants.FAX);
        String type = getFromRecord(record, headers, HeaderConstants.FAX_TYPE, "Work");
        return new Fax(phone, type);
    }

    private Email getEmailFromRecord(Map<String, Integer> headers, CSVRecord record)
    {
        String phone = getFromRecord(record, headers, HeaderConstants.EMAIL);
        String type = getFromRecord(record, headers, HeaderConstants.EMAIL_TYPE, "Business");
        return new Email(phone, type);
    }

    private URL getURLFromRecord(Map<String, Integer> headers, CSVRecord record)
    {
        String phone = getFromRecord(record, headers, HeaderConstants.URL);
        String type = getFromRecord(record, headers, HeaderConstants.URL_TYPE, "Web Site");
        return new URL(phone, type);
    }

    private CertficateHolder getCertificateHolderFromRecord(Map<String, Integer> headers, CSVRecord record)
    {
        String hcCompanyId = getFromRecord(record, headers, HeaderConstants.HC_COMPANY_ID);
        String tgaClientId = getFromRecord(record, headers, HeaderConstants.TGA_CLIENT_ID);
        String relatedCertificateHolderFirmName = getFromRecord(record, headers, HeaderConstants.RELATED_CERTIFICATE_HOLDER_FIRM_NAME);
        String scopeOfMdsapCertification = getFromRecord(record, headers, HeaderConstants.SCOPE_OF_MDSAP_CERTIFICATION);

        CertficateHolder certficateHolder = new CertficateHolder();
        certficateHolder.setHcCompanyId(sanitize(hcCompanyId, true));
        certficateHolder.setTgaClientId(sanitize(tgaClientId, true));
        certficateHolder.setRelatedCertificateHolderFirmName(sanitize(relatedCertificateHolderFirmName));
        certficateHolder.setScopeOfCertification(sanitize(scopeOfMdsapCertification));

        return certficateHolder;
    }

    private ContactPerson getContactPersonFromRecord(Map<String, Integer> headers, CSVRecord record)
    {
        ContactPerson person = new ContactPerson();

        person.setLastName(getFromRecord(record, headers, HeaderConstants.LAST_NAME));
        person.setFirstName(getFromRecord(record, headers, HeaderConstants.FIRST_NAME));
        person.setEmail(getFromRecord(record, headers, HeaderConstants.CONTACT_PERSON_EMAIL));
        person.setTitle(getFromRecord(record, headers, HeaderConstants.TITLE));
        person.setPhoneNumber(getFromRecord(record, headers, HeaderConstants.CONTACT_PERSON_PHONE_NUMBER));
        person.setPrimary(getFromRecord(record, headers, HeaderConstants.CONTACT_PERSON_PRIMARY_YES_NO));

        return person;
    }

    private String getFromRecord(CSVRecord record, Map<String, Integer> headers, String field)
    {
        log.debug("Getting {}", field);
        return getFromRecord(record, headers, field, null);
    }

    private String getFromRecord(CSVRecord record, Map<String, Integer> headers, String field, String defaultValue)
    {
        String value = record.get(headers.get(field));
        return value != null ? value.trim() : defaultValue;
    }

    public String toJson(FacilityEntry entry)
    {
        return gson.toJson(entry);
    }

    private String sanitize(String toSanitize)
    {
        return sanitize(toSanitize, false);
    }

    private String sanitize(String toSanitize, boolean removeSpaces)
    {
        if (toSanitize != null)
        {
            String retval = toSanitize;

            // Remove new lines
            retval = retval.replaceAll("\n", "").replaceAll("\t", "").replaceAll("\r", "").trim();

            if (removeSpaces)
            {
                retval = retval.replaceAll(" ", "");
            }
            return retval;
        }

        return "";
    }
}

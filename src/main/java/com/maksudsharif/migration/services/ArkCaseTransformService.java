package com.maksudsharif.migration.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import com.maksudsharif.migration.model.Address;
import com.maksudsharif.migration.model.CertficateHolder;
import com.maksudsharif.migration.model.ContactPerson;
import com.maksudsharif.migration.model.Email;
import com.maksudsharif.migration.model.FacilityEntry;
import com.maksudsharif.migration.model.Fax;
import com.maksudsharif.migration.model.MetadataError;
import com.maksudsharif.migration.model.OtherTradeNames;
import com.maksudsharif.migration.model.PhoneNumber;
import com.maksudsharif.migration.model.URL;
import com.maksudsharif.migration.model.ValidationResult;
import com.maksudsharif.migration.model.arkcase.CertificateHolderInfo;
import com.maksudsharif.migration.model.arkcase.CertificateHolderMetadata;
import com.maksudsharif.migration.model.arkcase.ContactMethod;
import com.maksudsharif.migration.model.arkcase.Manufacturer;
import com.maksudsharif.migration.model.arkcase.Location;
import com.maksudsharif.migration.model.arkcase.Organization;
import com.maksudsharif.migration.model.arkcase.OrganizationDBA;
import com.maksudsharif.migration.model.arkcase.Person;
import com.maksudsharif.migration.model.arkcase.PersonOrganizationAssociation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
@Service
@Scope(value = "singleton")
public class ArkCaseTransformService
{
    private static final int PRIMARY = 0;
    public static List<FacilityEntry> currentEntries;
    private final Gson gson;
    private final ObjectMapper objectMapper;
    private ArkCaseClient client;
    @Value("${migration.createNonCertificateHolders}")
    private Boolean createNonCertificateHolders;


    @Value("${migration.createCertificateHolders}")
    private Boolean createCertificateHolders;

    public ArkCaseTransformService(Gson gson, ObjectMapper objectMapper, ArkCaseClient client)
    {
        this.gson = gson;
        this.objectMapper = objectMapper;
        this.client = client;
    }

    public FacilityEntry transformPersons(FacilityEntry entry)
    {
        List<Person> persons = new ArrayList<>();
        List<ContactPerson> contactPersons = entry.getContactPersons();
        for (ContactPerson contactPerson : contactPersons)
        {
            Person person = new Person();
            person.setFamilyName(contactPerson.getLastName());
            person.setGivenName(contactPerson.getFirstName());
            person.setTitle(contactPerson.getTitle());

            List<ContactMethod> contactMethods = new ArrayList<>();
            if (StringUtils.isNotEmpty(contactPerson.getPhoneNumber()))
            {
                ContactMethod phone = new ContactMethod();
                phone.setType("phone");
                phone.setValue(contactPerson.getPhoneNumber());
                contactMethods.add(phone);
                person.setDefaultPhone(phone);
            }

            if (StringUtils.isNotEmpty(contactPerson.getEmail()))
            {
                ContactMethod email = new ContactMethod();
                email.setType("email");
                email.setValue(contactPerson.getEmail());
                contactMethods.add(email);
                person.setDefaultEmail(email);
            }

            person.setContactMethods(contactMethods);
            if ("YES".equalsIgnoreCase(contactPerson.getPrimary()))
            {
                entry.setPrimaryPerson(person);
            } else
            {
                persons.add(person);
            }
        }

        entry.setArkcaseContactPersons(persons);
        return entry;
    }

    public FacilityEntry transformOrganization(FacilityEntry entry)
    {
        Organization organization = new Organization();
        organization.setOrganizationValue(entry.getFacilityName());
        organization.setDunsNum(entry.getDuns());
        organization.setIsCertificateHolder("YES".equalsIgnoreCase(entry.getIsCertificateHolder()));
        organization.setIsCampus("YES".equalsIgnoreCase(entry.getCampus()));
        organization.setIsUnderJurisdictionUSA(entry.getJurisdictions().getJurisdictions().stream().map(String::toUpperCase).collect(Collectors.toSet()).contains("UNITED STATES - FDA"));
        organization.setIsUnderJurisdictionJAP(entry.getJurisdictions().getJurisdictions().stream().map(String::toUpperCase).collect(Collectors.toSet()).contains("JAPAN - MHLW AND PMDA"));
        organization.setIsUnderJurisdictionBRA(entry.getJurisdictions().getJurisdictions().stream().map(String::toUpperCase).collect(Collectors.toSet()).contains("BRAZIL - ANVISA"));
        organization.setIsUnderJurisdictionAUS(entry.getJurisdictions().getJurisdictions().stream().map(String::toUpperCase).collect(Collectors.toSet()).contains("AUSTRALIA - TGA"));
        organization.setIsUnderJurisdictionCAN(entry.getJurisdictions().getJurisdictions().stream().map(String::toUpperCase).collect(Collectors.toSet()).contains("CANADA - HEALTH CANADA"));
        organization.setResponsibleAo(entry.getResponsibleAO());
        organization.setAddresses(entry.getArkcaseAddresses());
        organization.setDefaultAddress(entry.getDefaultAddress());
        organization.setContactMethods(entry.getArkcaseContactMethods());
        organization.setOrganizationDBAs(entry.getOrganizationDBAs());
        organization.setInitialAuditStartDate(Date.from(LocalDate.parse(entry.getInitialAuditStartDate()).atStartOfDay().toInstant(ZoneOffset.UTC)));
        organization.setInitialAuditEndDate(Date.from(LocalDate.parse(entry.getInitialAuditEndDate()).atStartOfDay().toInstant(ZoneOffset.UTC)));
        organization.setAdditionalInformation(entry.getAdditionalInformation());
        organization.setCreated(Date.from(LocalDate.parse(entry.getCreated()).atStartOfDay().toInstant(ZoneOffset.UTC)));
        organization.setActiveDate(Date.from(LocalDate.parse(entry.getCreated()).atStartOfDay().toInstant(ZoneOffset.UTC)));

        if (organization.getIsCertificateHolder())
        {
            organization.setManufacturers(entry.getManufacturers());
        }

        entry.setOrganization(organization);

        return entry;
    }

    public FacilityEntry transformOrganizationDBAs(FacilityEntry entry)
    {
        OtherTradeNames otherTradeNames = entry.getOtherTradeNames();
        if (otherTradeNames.getNames() != null)
        {
            List<OrganizationDBA> dbas = otherTradeNames.getNames().stream().map(this::getOrganizationDBA).collect(Collectors.toList());
            entry.setOrganizationDBAs(dbas);
        }
        return entry;
    }

    public FacilityEntry transformCertificateHolders(FacilityEntry entry)
    {
        if ("YES".equalsIgnoreCase(entry.getIsCertificateHolder()))
        {
            List<Manufacturer> manufacturers = entry.getCertficateHolders().stream().map(certficateHolder -> {
                Manufacturer manufacturer = new Manufacturer();
                manufacturer.setCertificationScope(certficateHolder.getScopeOfCertification());
                manufacturer.setTgaClientId(certficateHolder.getTgaClientId());
                manufacturer.setHcCompanyId(certficateHolder.getHcCompanyId());
                return manufacturer;
            }).collect(Collectors.toList());
            entry.setManufacturers(manufacturers);
        }
        return entry;
    }

    public FacilityEntry transformMdmLocations(FacilityEntry entry)
    {
        List<Location> locations = entry.getAddresses().stream()
                .map(address -> {
                    Location location = getMdmLocation(address);
                    if ("YES".equalsIgnoreCase(entry.getCampus()) && "YES".equalsIgnoreCase(address.getPrimaryCampus()))
                    {
                        entry.setDefaultAddress(location);
                    }
                    return location;
                })
                .collect(Collectors.toList());

        entry.setArkcaseAddresses(locations);

        return entry;
    }

    private Location getMdmLocation(Address address)
    {
        Location location = new Location();
        location.setStreetAddress(address.getAddress1());
        location.setStreetAddress2(address.getAddress2());
        location.setCity(address.getCity());
        location.setState(address.getState());
        location.setCountry(address.getCountry());
        location.setDistrict(address.getFdaDistrict());
        location.setFacilityActivities(address.getFacilityActivities());
        location.setAddressName(address.getCampusBuildingName());
        location.setZip(address.getZip());
        location.setClientReferenceNum(address.getAoClientReference());

        location.setPmdaNum(address.getMhwPmdaRegNum());
        location.setTgaLocationId(address.getTgaLocationId());
        location.setFdaFei(address.getFdaFei());
        location.setFdaRegistrationNum(address.getFdaReg());
        location.setAnvisaNum(address.getAnvisaNum());
        location.setBrazilianGmpRequestNum(address.getGmpReqNum());
        return location;
    }

    private OrganizationDBA getOrganizationDBA(String name)
    {
        OrganizationDBA organizationDBA = new OrganizationDBA();
        organizationDBA.setType("Trade Name");
        organizationDBA.setValue(name);
        return organizationDBA;
    }

    public FacilityEntry transformContactMethods(FacilityEntry entry)
    {
        List<Email> emails = entry.getEmails();
        List<PhoneNumber> phoneNumbers = entry.getPhoneNumbers();
        List<Fax> faxes = entry.getFaxes();
        List<URL> urls = entry.getUrls();
        List<ContactMethod> arkcaseEmails = emails.stream().map(email -> new ContactMethod(email.getEmail(), "email", email.getType())).collect(Collectors.toList());
        List<ContactMethod> arkcasePhones = phoneNumbers.stream().map(phone -> new ContactMethod(phone.getNumber(), "phone", phone.getType())).collect(Collectors.toList());
        List<ContactMethod> arkcaseFaxes = faxes.stream().map(fax -> new ContactMethod(fax.getNumber(), "fax", fax.getType())).collect(Collectors.toList());
        List<ContactMethod> arkcaseUrls = urls.stream().map(url -> new ContactMethod(url.getUrl(), "url", url.getType())).collect(Collectors.toList());
        List<ContactMethod> contactMethods = Stream.of(arkcaseEmails, arkcasePhones, arkcaseFaxes, arkcaseUrls).flatMap(Collection::stream).collect(Collectors.toList());
        entry.setArkcaseContactMethods(contactMethods);
        return entry;
    }

    public FacilityEntry transformPersonAssociations(FacilityEntry entry)
    {
        List<Person> arkcaseContactPersons = entry.getArkcaseContactPersons();
        List<PersonOrganizationAssociation> associations = new ArrayList<>();
        Organization organization = entry.getOrganization();
        // Primary contact
        Person primaryPerson = entry.getPrimaryPerson();
        if (primaryPerson != null)
        {
            PersonOrganizationAssociation primaryAssociation = new PersonOrganizationAssociation();
            primaryAssociation.setPerson(primaryPerson);
            primaryAssociation.setOrganization(organization);
            associations.add(primaryAssociation);
            entry.setPrimaryContact(primaryAssociation);
        }

        for (Person arkcaseContactPerson : arkcaseContactPersons)
        {
            PersonOrganizationAssociation association = new PersonOrganizationAssociation();
            association.setOrganization(organization);
            association.setPerson(arkcaseContactPerson);
            associations.add(association);
            entry.setPrimaryContact(association);
        }

        entry.setPersonAssociations(associations);
        return entry;
    }

    public FacilityEntry createCertificateHolderReferences(FacilityEntry entry)
    {
        if (createNonCertificateHolders && !createCertificateHolders)
        {
            List<CertificateHolderInfo> holderInfos = new ArrayList<>();
            for (CertficateHolder certficateHolder : entry.getCertficateHolders())
            {
                final String relatedCertificateHolderFirmName = certficateHolder.getRelatedCertificateHolderFirmName();
                SolrDocumentList solrDocuments = client.solrRequest(relatedCertificateHolderFirmName, entry.getResponsibleAO());

                long numFound = solrDocuments.getNumFound();
                if (numFound == 0)
                {
                    MetadataError error = new MetadataError(String.format("Invalid value for field: %s => %s", "relatedCertificateHolderFirmName", relatedCertificateHolderFirmName));
                    entry.addValidation(new ValidationResult(Collections.singletonList(error)));
                } else
                {
                    if (solrDocuments.size() > 1)
                    {
                        List<SolrDocument> searchResults = solrDocuments.stream()
                                .filter(sd -> relatedCertificateHolderFirmName.equalsIgnoreCase((String) sd.get("title_parseable")))
                                .collect(Collectors.toList());
                        if (searchResults.isEmpty())
                        {
                            MetadataError error = new MetadataError(String.format("Invalid value for field: %s => %s, Unable to find related facility in search.", "relatedCertificateHolderFirmName", relatedCertificateHolderFirmName));
                            entry.addValidation(new ValidationResult(Collections.singletonList(error)));
                        }

                        SolrDocumentList filteredList = new SolrDocumentList();
                        filteredList.addAll(searchResults);

                        CertificateHolderInfo info = getCertificateHolderInfo(filteredList);
                        addHolder(holderInfos, info);
                    } else
                    {
                        CertificateHolderInfo info = getCertificateHolderInfo(solrDocuments);
                        addHolder(holderInfos, info);
                    }
                }
            }

            CertificateHolderMetadata metadata = new CertificateHolderMetadata(holderInfos);
            String referenceString = new GsonBuilder().disableHtmlEscaping().excludeFieldsWithoutExposeAnnotation().serializeNulls().create().toJson(metadata);
            Organization organization = entry.getOrganization();
            if (organization != null)
            {
                organization.setReferenceString(referenceString);
            }
            entry.setReferenceString(referenceString);
        }

        return entry;
    }

    private void addHolder(List<CertificateHolderInfo> holderInfos, CertificateHolderInfo info)
    {
        if (holderInfos.stream().map(CertificateHolderInfo::getName).noneMatch(name -> name.equalsIgnoreCase(info.getName())))
        {
            holderInfos.add(info);
        } else
        {
            log.info("Skipping duplicate related certificate holder");
        }
    }

    private CertificateHolderInfo getCertificateHolderInfo(SolrDocumentList solrDocuments)
    {
        SolrDocument orgInfo = solrDocuments.get(0);
        String object_id_s = String.class.cast(orgInfo.get("object_id_s"));
        String name = String.class.cast(orgInfo.get("name"));
        String title = String.class.cast(orgInfo.get("title_parseable_lcs"));
        CertificateHolderInfo info = new CertificateHolderInfo();
        info.setMdmProfileId(Long.valueOf(object_id_s));
        info.setName(name);
        info.setTitle(title);
        return info;
    }

    public List<FacilityEntry> getCurrentEntries()
    {
        return currentEntries;
    }

    public void setCurrentEntries(List<FacilityEntry> currentEntries)
    {
        ArkCaseTransformService.currentEntries = currentEntries;
    }
}

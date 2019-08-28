package com.maksudsharif.migration.services.validators;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import com.maksudsharif.migration.model.CertficateHolder;
import com.maksudsharif.migration.model.FacilityEntry;
import com.maksudsharif.migration.model.HeaderConstants;
import com.maksudsharif.migration.model.MetadataError;
import com.maksudsharif.migration.model.ValidationResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Component
@Log4j2
public class CertificateHolderValidator extends AbstractValidator
{
    @Value("${migration.validateOnly}")
    private Boolean validateOnly;

    @Override
    public FacilityEntry validate(FacilityEntry entry)
    {
        List<MetadataError> errors = new ArrayList<>();

        validateRequired(entry, errors);

        boolean isCertificateHolder = "YES".equalsIgnoreCase(entry.getIsCertificateHolder());
        List<CertficateHolder> certficateHolders = entry.getCertficateHolders();

        for (CertficateHolder certficateHolder : certficateHolders)
        {
            if (isCertificateHolder)
            {
                if (StringUtils.isNotEmpty(certficateHolder.getTgaClientId()))
                {
                    String tgaClientId = certficateHolder.getTgaClientId();
                    List<String> tgaClientIds = Arrays.asList(tgaClientId.split(","));
                    AtomicBoolean tgaClientIdErrorFlag = new AtomicBoolean();
                    if (tgaClientIds.size() > 6 || tgaClientIds.isEmpty())
                    {
                        addInvalidError(errors, HeaderConstants.TGA_CLIENT_ID, certficateHolder.getTgaClientId(), "Number of TGA Client IDs are greater than 6.");
                    }
                    tgaClientIds.forEach(id -> {
                        if (StringUtils.length(id) > 8 || StringUtils.isEmpty(id))
                        {
                            addInvalidError(errors, HeaderConstants.TGA_CLIENT_ID, id, "Value is greater than 8 characters.");
                            tgaClientIdErrorFlag.getAndSet(true);
                        }
                    });

                    if (tgaClientIdErrorFlag.get())
                    {
                        addInvalidError(errors, HeaderConstants.TGA_CLIENT_ID, certficateHolder.getTgaClientId());
                    }
                }

                if (StringUtils.isNotEmpty(certficateHolder.getHcCompanyId()))
                {
                    String hcCompanyId = certficateHolder.getHcCompanyId();
                    List<String> hcCompanyIds = Arrays.asList(hcCompanyId.split(","));
                    AtomicBoolean hcCompanyIdErrorFlag = new AtomicBoolean();
                    if (hcCompanyIds.size() > 6 || hcCompanyIds.isEmpty())
                    {
                        addInvalidError(errors, HeaderConstants.HC_COMPANY_ID, certficateHolder.getTgaClientId(), "Number of HC Company IDs are greater than 6.");
                    }
                    hcCompanyIds.forEach(id -> {
                        if (StringUtils.length(id) > 15 || StringUtils.isEmpty(id))
                        {
                            addInvalidError(errors, HeaderConstants.HC_COMPANY_ID, id, "Value is greater than 15 characters or empty.");
                            hcCompanyIdErrorFlag.getAndSet(true);
                        }
                    });

                    if (hcCompanyIdErrorFlag.get())
                    {
                        addInvalidError(errors, HeaderConstants.HC_COMPANY_ID, certficateHolder.getTgaClientId());
                    }
                }

                if (StringUtils.isNotEmpty(certficateHolder.getScopeOfCertification()) && StringUtils.length(certficateHolder.getScopeOfCertification()) > 3000)
                {
                    addInvalidError(errors, HeaderConstants.SCOPE_OF_MDSAP_CERTIFICATION, certficateHolder.getScopeOfCertification(), "Value is greater than 3000 characters.");
                }
            } else
            {
                if (StringUtils.isNotEmpty(certficateHolder.getRelatedCertificateHolderFirmName()))
                {
                    String relatedFacilityName = certficateHolder.getRelatedCertificateHolderFirmName();
                    Set<String> names = arkCaseTransformService.getCurrentEntries().stream()
                            .filter(facilityEntry -> "YES".equalsIgnoreCase(facilityEntry.getIsCertificateHolder()))
                            .map(facilityEntry -> facilityEntry.getFacilityName().trim())
                            .filter(StringUtils::isNotEmpty)
                            .map(StringUtils::upperCase)
                            .collect(Collectors.toSet());
                    Set<String> duns = arkCaseTransformService.getCurrentEntries().stream()
                            .filter(facilityEntry -> "YES".equalsIgnoreCase(facilityEntry.getIsCertificateHolder()))
                            .map(facilityEntry -> facilityEntry.getDuns().trim())
                            .filter(StringUtils::isNotEmpty)
                            .map(StringUtils::upperCase)
                            .collect(Collectors.toSet());
                    Set<String> namesAO = arkCaseTransformService.getCurrentEntries().stream()
                            .filter(facilityEntry -> "YES".equalsIgnoreCase(facilityEntry.getIsCertificateHolder()))
                            .filter(facilityEntry -> entry.getResponsibleAO().equalsIgnoreCase(facilityEntry.getResponsibleAO()))
                            .map(facilityEntry -> facilityEntry.getFacilityName().trim())
                            .filter(StringUtils::isNotEmpty)
                            .map(StringUtils::upperCase)
                            .collect(Collectors.toSet());
                    Set<String> dunsAO = arkCaseTransformService.getCurrentEntries().stream()
                            .filter(facilityEntry -> "YES".equalsIgnoreCase(facilityEntry.getIsCertificateHolder()))
                            .filter(facilityEntry -> entry.getResponsibleAO().equalsIgnoreCase(facilityEntry.getResponsibleAO()))
                            .map(facilityEntry -> facilityEntry.getDuns().trim())
                            .filter(StringUtils::isNotEmpty)
                            .map(StringUtils::upperCase)
                            .collect(Collectors.toSet());

                    if (!names.contains(relatedFacilityName.toUpperCase()) || (!names.contains(relatedFacilityName.toUpperCase()) && !duns.contains(relatedFacilityName.toUpperCase())))
                    {
                        addInvalidError(errors, HeaderConstants.RELATED_CERTIFICATE_HOLDER_FIRM_NAME, String.format("Related Certificate Holder Firm Name not found among facilities. =>  '%s'", relatedFacilityName));
                    }

                    if (!namesAO.contains(relatedFacilityName.toUpperCase()) || (!namesAO.contains(relatedFacilityName.toUpperCase()) && !dunsAO.contains(relatedFacilityName.toUpperCase())))
                    {
                        addInvalidError(errors, HeaderConstants.RELATED_CERTIFICATE_HOLDER_FIRM_NAME, String.format("Related Certificate Holder Firm Name found but related facility in different Auditing Organization. =>  '%s'", relatedFacilityName));
                    }

                    if (validateOnly)
                    {
//                        HashMap<String, Integer> nameCounts = countFacilityNames(entry.getResponsibleAO());
//                        Integer count = nameCounts.get(relatedFacilityName.toLowerCase());
//
//                        if (count > 1)
//                        {
//                            addInvalidError(errors, HeaderConstants.RELATED_CERTIFICATE_HOLDER_FIRM_NAME, String.format("Related Certificate Holder Firm Name ambiguous. Found multiple matching certificate holders. =>  '%s'", relatedFacilityName));
//                        }
//
//                        if (count == 0)
//                        {
//                            addInvalidError(errors, HeaderConstants.RELATED_CERTIFICATE_HOLDER_FIRM_NAME, String.format("Related Certificate Holder Firm Name not found among facilities. =>  '%s'", relatedFacilityName));
//                        }
                    }
                } else
                {
                    addRequiredError(errors, HeaderConstants.RELATED_CERTIFICATE_HOLDER_FIRM_NAME);
                }
            }
        }

        return errors.isEmpty() ? entry : entry.addValidation(new ValidationResult(errors));
    }

    private void validateRequired(FacilityEntry entry, List<MetadataError> errors)
    {
        List<CertficateHolder> certficateHolders = entry.getCertficateHolders();
        if (CollectionUtils.isEmpty(certficateHolders))
        {
            addRequiredError(errors, HeaderConstants.CERTIFICATE_HOLDERS);
        }

        String isCertificateHolderValue = entry.getIsCertificateHolder();
        if (StringUtils.isEmpty(isCertificateHolderValue))
        {
            addRequiredError(errors, HeaderConstants.CERTIFICATE_HOLDER);
        } else
        {
            if (!Arrays.asList("YES", "NO").contains(isCertificateHolderValue.toUpperCase()))
            {
                addInvalidError(errors, HeaderConstants.CERTIFICATE_HOLDER, isCertificateHolderValue);
            }
        }

        boolean isCertificateHolder = "YES".equalsIgnoreCase(isCertificateHolderValue);

        for (CertficateHolder certficateHolder : certficateHolders)
        {
            if (isCertificateHolder)
            {
                if (StringUtils.isEmpty(certficateHolder.getScopeOfCertification()))
                {
                    addRequiredError(errors, HeaderConstants.SCOPE_OF_MDSAP_CERTIFICATION);
                }
                if (StringUtils.isNotEmpty(certficateHolder.getRelatedCertificateHolderFirmName()))
                {
                    addInvalidError(errors, HeaderConstants.RELATED_CERTIFICATE_HOLDER_FIRM_NAME, "Facility is a certificate holder, it shouldn't have a Related Certificate Holder Firm Name");
                }
            } else
            {
                if (StringUtils.isEmpty(certficateHolder.getRelatedCertificateHolderFirmName()))
                {
                    addRequiredError(errors, HeaderConstants.RELATED_CERTIFICATE_HOLDER_FIRM_NAME);
                }
            }
        }
    }

    private HashMap<String, Integer> countFacilityNames(String responsibleAo)
    {
        HashMap<String, Integer> nameCounts = new HashMap<>();
        arkCaseTransformService.getCurrentEntries().stream()
                .filter(facilityEntry -> "YES".equalsIgnoreCase(facilityEntry.getIsCertificateHolder()))
                .filter(facilityEntry -> responsibleAo.equalsIgnoreCase(facilityEntry.getResponsibleAO()))
                .forEach(e -> {
                    String facilityLower = e.getFacilityName().toLowerCase();

                    if (nameCounts.containsKey(facilityLower))
                    {
                        Integer integer = nameCounts.get(facilityLower);
                        integer += 1;
                        nameCounts.put(facilityLower, integer);

                    } else
                    {
                        nameCounts.put(facilityLower, 1);
                    }
                });

        return nameCounts;
    }
}

package com.maksudsharif.migration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciitable.CWC_LongestLine;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrDocumentList;
import com.maksudsharif.migration.model.FacilityEntry;
import com.maksudsharif.migration.model.HttpConstants;
import com.maksudsharif.migration.model.MetadataError;
import com.maksudsharif.migration.model.ValidationResult;
import com.maksudsharif.migration.model.arkcase.Organization;
import com.maksudsharif.migration.model.arkcase.Person;
import com.maksudsharif.migration.services.ArkCaseClient;
import com.maksudsharif.migration.services.ArkCaseTransformService;
import com.maksudsharif.migration.services.CSVTransformService;
import com.maksudsharif.migration.services.ParserService;
import com.maksudsharif.migration.services.ResourceService;
import com.maksudsharif.migration.services.ValidationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootApplication
@Log4j2
public class MigrationApplication
{

    @Value("${arkcase.username}")
    private String username;
    @Value("${arkcase.password}")
    private String password;
    @Value("${arkcase.jsessionId}")
    private String jsessionId;
    @Value("${migration.createCertificateHolders}")
    private Boolean createCertificateHolders;
    @Value("${migration.createOrganizations}")
    private Boolean createOrganizations;
    @Value("${migration.validateOnly}")
    private Boolean validateOnly;
    @Value("${migration.indexOnly}")
    private Boolean indexOnly;

    @Value("${arkcase.solr.protocol}")
    private String solrProtocol;
    @Value("${arkcase.solr.host}")
    private String solrHost;
    @Value("${arkcase.solr.port}")
    private String solrPort;
    @Value("${arkcase.solr.context}")
    private String solrContext;

    public static void main(String[] args)
    {
        new SpringApplicationBuilder(MigrationApplication.class).web(WebApplicationType.NONE).run(args);
    }

    @Bean
    public CSVFormat csvFormat()
    {
        return CSVFormat.EXCEL.withFirstRecordAsHeader();
    }

    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    Gson gson()
    {
        return new GsonBuilder().disableHtmlEscaping().excludeFieldsWithoutExposeAnnotation().serializeNulls().setPrettyPrinting().create();
    }

    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    ObjectMapper objectMapper()
    {
        return new ObjectMapper();
    }

    @Bean
    public HttpComponentsClientHttpRequestFactory httpComponentsClientHttpRequestFactory() throws Exception
    {
        TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

        SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom()
                .loadTrustMaterial(null, acceptingTrustStrategy)
                .build();

        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);

        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(csf)
                .build();

        HttpComponentsClientHttpRequestFactory requestFactory =
                new HttpComponentsClientHttpRequestFactory();

        requestFactory.setHttpClient(httpClient);

        return requestFactory;
    }

    @Bean
    public HttpHeaders headers()
    {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setConnection(HttpConstants.KEEP_ALIVE);
        headers.setPragma(HttpConstants.NO_CACHE);
        headers.setCacheControl(HttpConstants.NO_CACHE);
        headers.setAccept(ImmutableList.of(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN, MediaType.ALL));
        headers.add("Accept-Encoding", HttpConstants.ACCEPT_ENCODING);
        headers.add("Accept-Language", HttpConstants.ACCEPT_LANGUAGE);
        headers.add("User-Agent", HttpConstants.USER_AGENT);
        if (StringUtils.isNotEmpty(jsessionId))
        {
            headers.add("Cookie", "JSESSIONID=" + jsessionId);
        }
        return headers;
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) throws Exception
    {
        //Use basic Auth or JSESSIONID
        RestTemplate restTemplate = builder.setConnectTimeout(Duration.ofMinutes(10L)).setReadTimeout(Duration.ofMinutes(10L)).build();
        List<HttpMessageConverter<?>> converters = restTemplate.getMessageConverters();
        converters.stream()
                .filter(converter -> converter instanceof MappingJackson2HttpMessageConverter)
                .forEach(converter ->
                {
                    MappingJackson2HttpMessageConverter jsonConverter = (MappingJackson2HttpMessageConverter) converter;
                    jsonConverter.setObjectMapper(new ObjectMapper());
                    jsonConverter.setSupportedMediaTypes(ImmutableList.of(new MediaType("application", "json", MappingJackson2HttpMessageConverter.DEFAULT_CHARSET)
                            , new MediaType("text", "javascript", MappingJackson2HttpMessageConverter.DEFAULT_CHARSET)));
                });

        converters.add(new FormHttpMessageConverter());
        if (StringUtils.isEmpty(jsessionId))
        {
            restTemplate.getInterceptors().add(new BasicAuthenticationInterceptor(username, password));
        }
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler()
        {
            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException
            {
                return super.hasError(response);
            }

            @Override
            public void handleError(ClientHttpResponse response) throws IOException
            {
                log.error("Error response from Server: {}", IOUtils.toString(response.getBody(), StandardCharsets.UTF_8).replace("\n", " ").replace("\r", " "));
                super.handleError(response);
            }
        });
        restTemplate.setRequestFactory(httpComponentsClientHttpRequestFactory());
        return restTemplate;

    }

    @Bean
    public HttpSolrClient solrClient()
    {
        String url = UriComponentsBuilder.newInstance()
                .scheme(solrProtocol)
                .host(solrHost)
                .port(solrPort)
                .path(solrContext)
                .toUriString();

        return new HttpSolrClient.Builder().withBaseSolrUrl(url).build();
    }

    @Bean
    public CommandLineRunner clr(ResourceService resourceService
            , ParserService parserService
            , CSVTransformService csvTransformService
            , ArkCaseTransformService arkCaseTransformService
            , ValidationService validationService
            , ArkCaseClient client)
    {
        return args -> {
            try
            {
                ingest(resourceService, parserService, csvTransformService, arkCaseTransformService, validationService, client);
            } catch (Exception e)
            {
                log.error("Unable to perform migration", e);
            }
        };

    }

    private void tableFormat(ResourceService resourceService) throws IOException
    {
        Resource certHolders = resourceService.getCertHolders();

        List<String> lines = FileUtils.readLines(certHolders.getFile(), "UTF-8");
        List<List<String>> lineLists = lines.stream().map(line -> line.split(" ")).map(Arrays::asList).collect(Collectors.toList());
        List<List<String>> lineListsFiltered = new ArrayList<>();
        lineLists.forEach(list -> {
            List<String> collect = list.stream().filter(StringUtils::isNotEmpty).map(String::trim).collect(Collectors.toList());
            lineListsFiltered.add(collect);
        });

        List<Triple<String, String, String>> retval = new ArrayList<>();
        lineListsFiltered.forEach(list -> {
            String index = list.get(0);
            String orgId = list.get(1);
            list.remove(0);
            list.remove(0);
            retval.add(Triple.of(index, orgId, String.join(" ", list).replaceAll(",", " ")));
        });

        retval.forEach(triple -> {
            System.out.println(String.format("%s,%s,%s", triple.getLeft(), triple.getMiddle(), triple.getRight()));
        });
    }


    private void printTable(List<FacilityEntry> arkcaseEntries)
    {
        Stream<FacilityEntry> sorted = arkcaseEntries.stream().sorted(Comparator.comparing(entry -> Integer.valueOf(entry.getId())));
        AsciiTable at = new AsciiTable();
        at.addRule();
        at.addRow("Index", "Organization ID", "Facility Name");
        at.addRule();
        sorted.forEach(entry -> {
            at.addRow(entry.getId(), entry.getOrganization() != null ? entry.getOrganization().getOrganizationId() : entry.getId(), entry.getFacilityName());
        });
        at.addRule();

        at.getRenderer().setCWC(new CWC_LongestLine());
        String render = at.render();
        log.info("\n{}", render);
    }

    private void ingest(ResourceService resourceService, ParserService parserService, CSVTransformService csvTransformService, ArkCaseTransformService arkCaseTransformService, ValidationService validationService, ArkCaseClient client) throws IOException
    {
        ImmutableSet<String> resourceStrings = resourceService.listOfFiles();
        for (String resourceString : resourceStrings.asList())
        {
            File resourceFile = resourceService.in(resourceString);

            if (indexOnly)
            {
                parserService.parseIndexes(resourceFile);
                continue;
            }

            List<FacilityEntry> entries = parserService.parse(resourceFile);
            // Transform CSVRecords to metadata
            List<FacilityEntry> transformed = entries.stream().map(csvTransformService::transform).collect(Collectors.toList());
            log.info("Transformed {} entries", transformed.size());

            // Validate data
            arkCaseTransformService.setCurrentEntries(transformed);
            List<FacilityEntry> validated = transformed.stream().map(validationService::validate).collect(Collectors.toList());
            log.info("Validated [{}] facilities", validated.size());
            // transform entries to json
            Map<FacilityEntry, String> validatedJsons = validated.parallelStream().map(facilityEntry -> Pair.of(facilityEntry, csvTransformService.toJson(facilityEntry))).collect(Collectors.toMap(Pair::getLeft, Pair::getRight));

            List<FacilityEntry> validFacilities = validatedJsons.keySet().stream().filter(facility -> !facility.hasError()).collect(Collectors.toList());

            if (validateOnly)
            {
                List<FacilityEntry> invalidFacilities = validatedJsons.keySet().stream().filter(FacilityEntry::hasError).collect(Collectors.toList());
                log.info("Invalid facilities:  [{} / {}]", invalidFacilities.size(), validated.size());
                List<FacilityEntry> invalidArkcaseEntries = invalidFacilities.stream()
                        .map(arkCaseTransformService::transformPersons)
                        .map(arkCaseTransformService::transformOrganizationDBAs)
                        .map(arkCaseTransformService::transformCertificateHolders)
                        .map(arkCaseTransformService::transformMdmLocations)
                        .map(arkCaseTransformService::transformContactMethods)
                        .collect(Collectors.toList());

                // write invalid facility json to files
                for (FacilityEntry facilityEntry : invalidArkcaseEntries)
                {
                    resourceService.process(facilityEntry, resourceString);
                }
            }

            log.info("Valid facilities:  [{} / {}]", validFacilities.size(), validated.size());
            List<FacilityEntry> arkcaseEntries = validFacilities.stream()
                    .map(arkCaseTransformService::transformPersons)
                    .map(arkCaseTransformService::transformOrganizationDBAs)
                    .map(arkCaseTransformService::transformCertificateHolders)
                    .map(arkCaseTransformService::transformMdmLocations)
                    .map(arkCaseTransformService::transformContactMethods)
                    .collect(Collectors.toList());

            // write valid facility json to files
            for (FacilityEntry facilityEntry : arkcaseEntries)
            {
                resourceService.process(facilityEntry, resourceString);
            }

            printTable(arkcaseEntries);

            if (validateOnly && !createCertificateHolders)
            {
                printRelatedFacilitiesMismatch(client, arkcaseEntries);
            }

            if (!validateOnly)
            {
                if (createCertificateHolders)
                {
                    log.info("Creating certificate holders!");
                    arkcaseEntries = arkcaseEntries.stream().filter(entry -> "YES".equalsIgnoreCase(entry.getIsCertificateHolder())).collect(Collectors.toList());
                    log.info("Certificate Holders:  [{} / {}]", arkcaseEntries.size(), validated.size());

                } else
                {
                    log.info("Creating non-certificate holders!");
                    arkcaseEntries = arkcaseEntries.stream().filter(entry -> !"YES".equalsIgnoreCase(entry.getIsCertificateHolder())).collect(Collectors.toList());
                    log.info("Non-certificate Holders:  [{} / {}]", arkcaseEntries.size(), validated.size());
                }

                arkCaseTransformService.setCurrentEntries(arkcaseEntries);
                List<FacilityEntry> finalEntries = arkcaseEntries.stream()
                        .map(arkCaseTransformService::transformOrganization)
                        .map(arkCaseTransformService::transformPersonAssociations)
                        .map(arkCaseTransformService::createCertificateHolderReferences)
                        .peek(entry -> {
                            Organization organization = entry.getOrganization();
                            entry.setOrganization(organization);
                        })
                        .collect(Collectors.toList());
                // Remove any invalid facilities for non-certificateHolders
                List<FacilityEntry> certificateHolderErrors = finalEntries.stream().filter(FacilityEntry::hasError).collect(Collectors.toList());

                log.info("Certificate Holder errors:  [{} / {}]", certificateHolderErrors.size(), validated.size());
                log.info("Discarding invalid entries.");
                for (FacilityEntry certificateHolderError : certificateHolderErrors)
                {
                    resourceService.process(certificateHolderError, resourceString);
                }

                // Final remove invalid entries before creation
                finalEntries = finalEntries.stream().filter(entry -> !entry.hasError()).collect(Collectors.toList());
                log.info("Writing out final entries to create in ArkCase");
                for (FacilityEntry entry : finalEntries)
                {
                    resourceService.process(entry.getOrganization(), resourceString);
                }

                log.info("Final list of facilities to create (after removing invalid after transforming to ArkCase Entities:  [{} / {}]", finalEntries.size(), validated.size());

                if (createOrganizations)
                {
                    finalEntries.parallelStream().sorted(Comparator.comparing(entry -> Integer.valueOf(entry.getId()))).forEach(finalEntry -> {
                        try
                        {
                            log.info("Saving Facility: {}", finalEntry.getOrganization());
                            Organization organization = finalEntry.getOrganization();
                            Person primaryPerson = finalEntry.getPrimaryPerson();
                            List<Person> peopleInfo = finalEntry.getArkcaseContactPersons();
                            peopleInfo.add(primaryPerson);
                            Organization saved = client.createOrganization(organization, primaryPerson, peopleInfo);
                            log.info("Saved Facility: {}", saved);
                            if (!organization.getIsCertificateHolder())
                            {
                                Thread.sleep(500);
                                Organization references = client.createReferences(Long.valueOf(saved.getOrganizationId()));
                                if (references != null)
                                {
                                    saved = references;
                                } else
                                {
                                    MetadataError metadataWarning = new MetadataError("Related facilities maybe not have been migrated.");
                                    finalEntry.addValidation(new ValidationResult(Collections.singletonList(metadataWarning)));
                                }

                                log.info("Saved Facility certificate holders {}", saved);
                            }
                            finalEntry.setOrganization(saved);
                            resourceService.process(saved, resourceString);
                            resourceService.process(finalEntry, resourceString);
                        } catch (Exception e)
                        {
                            log.error("Failed to save Facility: {} => {}", finalEntry.getId(), finalEntry, e);
                            MetadataError error = new MetadataError(String.format("Unable to save facility: %s", e.getMessage()));
                            finalEntry.addValidation(new ValidationResult(Collections.singletonList(error)));
                            try
                            {
                                resourceService.error(finalEntry);
                            } catch (IOException e1)
                            {
                                log.error("Unable to save error entry to filesystem", e1);
                            }
                        }
                    });

                    printTable(finalEntries);
                    List<FacilityEntry> savedNoErrors = finalEntries.stream().filter(entry -> !entry.hasError()).collect(Collectors.toList());
                    log.info("Facility Migration result:  [{} / {}]", savedNoErrors.size(), finalEntries.size());
                }
            }
        }
    }

    private void printRelatedFacilitiesMismatch(ArkCaseClient client, List<FacilityEntry> arkcaseEntries)
    {
        List<Pair<FacilityEntry, SolrDocumentList>> relatedFirmNameCheck = arkcaseEntries.stream()
                .sorted(Comparator.comparing(entry -> Integer.valueOf(entry.getId())))
                .filter(entry -> "NO".equalsIgnoreCase(entry.getIsCertificateHolder()))
                .map(entry -> Pair.of(entry, client.solrRequest(entry.getCertficateHolders().get(0).getRelatedCertificateHolderFirmName(), entry.getResponsibleAO())))
                .peek(entry -> {
                    if (entry.getRight().isEmpty())
                    {
                        log.info("No results: {}", entry.getLeft().getId());
                    }
                })
                .filter(entry -> entry.getRight().size() > 1)
                .collect(Collectors.toList());

        if (!relatedFirmNameCheck.isEmpty())
        {
            AsciiTable at = new AsciiTable();
            at.addRule();
            at.addRow("Index", "Facility Name", "Related Facility Name", "Search Results");
            at.addRule();
            relatedFirmNameCheck.forEach(entry -> {
                FacilityEntry left = entry.getLeft();
                SolrDocumentList right = entry.getRight();
                at.addRow(left.getId()
                        , left.getFacilityName()
                        , left.getCertficateHolders().get(0).getRelatedCertificateHolderFirmName()
                        , String.format("%s [%s]", right.get(0).get("title_parseable"), right.get(0).get("object_id_s")));
                if (right.size() > 1)
                {
                    right.remove(0);
                    right.forEach(sd -> {
                        at.addRow("", "", "", String.format("%s [%s]", (String) sd.get("title_parseable"), sd.get("object_id_s")));
                    });
                }
            });
            at.addRule();

            at.getRenderer().setCWC(new CWC_LongestLine());
            String render = at.render();
            log.info("Solr Search Results\n{}", render);
        }
    }
}

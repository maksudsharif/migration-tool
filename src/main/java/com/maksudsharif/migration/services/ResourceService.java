package com.maksudsharif.migration.services;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.stream.JsonWriter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import com.maksudsharif.migration.model.FacilityEntry;
import com.maksudsharif.migration.model.MetadataError;
import com.maksudsharif.migration.model.arkcase.Organization;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Log4j2
public class ResourceService implements ResourceLoaderAware
{
    @Value("${migration.input.directory}")
    private String inputPath;
    @Value("${migration.output.directory}")
    private String outputPath;
    @Value("${migration.error.directory}")
    private String errorPath;

    private Resource inputDirectory;
    private Resource outputDirectory;
    private Resource errorDirectory;
    private Resource tmpDirectory;

    private ResourceLoader resourceLoader;
    private Gson gson;
    private ObjectMapper objectMapper;
    private ConcurrentHashMap<String, File> fileMap;

    private LinkedHashMap<String, List<State>> stateLookups = new LinkedHashMap<>();
    private Map<String, String> countryLookups = new LinkedHashMap<>();
    private Map<String, String> districtLookups = new LinkedHashMap<>();

    @Value("states.json")
    private Resource states;
    @Value("countries.json")
    private Resource countries;
    @Value("districts.json")
    private Resource districts;
    @Value("certholders.txt")
    private Resource certHolders;
    @Value("noncertholders.txt")
    private Resource nonCertHolders;


    public ResourceService(ResourceLoader resourceLoader, Gson gson, ObjectMapper objectMapper)
    {
        this.resourceLoader = resourceLoader;
        this.gson = gson;
        this.objectMapper = objectMapper;
        this.fileMap = new ConcurrentHashMap<>();
    }

    public ImmutableSet<String> listOfFiles()
    {
        return ImmutableSet.<String>builder().addAll(fileMap.keySet()).build();
    }

    public File in(String hashKey)
    {
        return fileMap.get(hashKey);
    }

    public void process(FacilityEntry entry, String resourceString) throws IOException
    {
        if (entry.hasError())
        {
            error(entry);
        } else
        {
            log.debug("Entry has been validated: {}", entry.getId());
            ErrorOutput errorOutput = new ErrorOutput(entry, Collections.emptyList());
            StringBuilder filePath = new StringBuilder(outputDirectory.getFile().getAbsolutePath())
                    .append(File.separator)
                    .append(entry.getId().replaceAll("\\\\|/|\\||:|\\?|\\*|\"|<|>|\\p{Cntrl}", "_"))
                    .append('-')
                    .append(new SimpleDateFormat("yyyyMMddHHmm").format(Date.from(Instant.ofEpochMilli(System.nanoTime()))))
                    .append('-')
                    .append(resourceString);

            File file = new File(filePath.toString() + ".json");
            try (FileWriter writer = new FileWriter(file))
            {
                gson.toJson(errorOutput, writer);
            }
        }

    }

    public void process(Organization organization, String resourceString) throws IOException
    {
        log.info("Output has been written: {}", organization.getOrganizationValue());
        String facilityName = String.join("_", organization.getOrganizationValue().substring(0, Math.min(15, organization.getOrganizationValue().length())).split(" ")).replaceAll("\\\\|/|\\||:|\\?|\\*|\"|<|>|\\p{Cntrl}", "_");
        StringBuilder filePath = new StringBuilder(outputDirectory.getFile().getAbsolutePath());
        filePath.append(File.separator)
                .append("ORG")
                .append(organization.getOrganizationId() != null ? '-' + organization.getOrganizationId() : "")
                .append('-')
                .append(resourceString)
                .append('-')
                .append(facilityName)
                .append('-')
                .append(new SimpleDateFormat("yyyyMMddHHmm").format(Date.from(Instant.ofEpochMilli(System.nanoTime()))));

        File file = new File(filePath.toString() + ".json");
        try (FileWriter writer = new FileWriter(file); JsonWriter jsonWriter = gson.newJsonWriter(writer))
        {
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            String orgJson = objectMapper.writeValueAsString(organization);
            jsonWriter.jsonValue(orgJson);
        }
    }

    public void error(FacilityEntry facilityEntry) throws IOException
    {
        log.debug("Writing failed facility to error: {}", facilityEntry);
        List<MetadataError> metadataErrors = facilityEntry.getErrors().stream().flatMap(validationResult -> validationResult.getMetadataErrors().stream()).collect(Collectors.toList());
        ErrorOutput errorOutput = new ErrorOutput(facilityEntry, metadataErrors);
        File file = new File(errorDirectory.getFile().getAbsolutePath() + File.separator + facilityEntry.getId() + '-' + new SimpleDateFormat("yyyyMMddHHmm").format(new Date()) + ".json");

        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8))
        {
            gson.toJson(errorOutput, writer);
        }
    }

    @PostConstruct
    public void init() throws IOException
    {
        log.info("Initializing resources...");
        log.info("Input Directory: \t\t{}", inputPath);
        log.info("Output Directory: \t\t{}", outputPath);
        log.info("Error Directory: \t\t{}", errorPath);

        try
        {
            tmpDirectory = initDirectory(System.getProperty("user.dir") + "/tmp", true);
            inputDirectory = initDirectory(inputPath, false);
            outputDirectory = initDirectory(outputPath, false);
            errorDirectory = initDirectory(errorPath, false);

            FileUtils.iterateFiles(inputDirectory.getFile(), new String[]{"csv"}, true).forEachRemaining(file -> {
                log.info("Found file: \t\t{}", file);
                String sha1FileName = DigestUtils.md5Hex(file.getName());
                fileMap.put(sha1FileName, file);
            });

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8.toString()))
            {
                MapUtils.debugPrint(ps, "fileMap", fileMap);
                log.info(new String(baos.toByteArray(), Charset.defaultCharset()));
            }

            try (InputStreamReader stateReader = new InputStreamReader(new FileInputStream(states.getFile()), StandardCharsets.UTF_8);
                 InputStreamReader countryReader = new InputStreamReader(new FileInputStream(countries.getFile()), StandardCharsets.UTF_8);
                 InputStreamReader districtReader = new InputStreamReader(new FileInputStream(districts.getFile()), StandardCharsets.UTF_8))
            {
                stateLookups = gson.fromJson(stateReader, new ParameterizedTypeReference<LinkedHashMap<String, List<State>>>()
                {
                }.getType());

                List<Country> countryList = gson.fromJson(countryReader, new ParameterizedTypeReference<List<Country>>()
                {
                }.getType());

                countryLookups = countryList.stream().collect(Collectors.toMap(Country::getCode, Country::getName));

                List<District> districtList = gson.fromJson(districtReader, new ParameterizedTypeReference<List<District>>()
                {
                }.getType());

                districtLookups = districtList.stream().collect(Collectors.toMap(District::getCode, District::getName));

            }

            log.info("Initializing resources...\t\tdone");
        } catch (Exception e)
        {
            log.error("Unable to init resources.", e);
            throw e;
        }
    }

    private Resource initDirectory(String path, boolean clean) throws IOException
    {
        Resource resource = resourceLoader.getResource("file:" + path);

        if (!resource.exists())
        {
            log.debug("Resource directory {} doesn't exist, creating now.", path);
            FileUtils.forceMkdir(new File(path));
            resource = resourceLoader.getResource("file:" + path);
        }

        if (!resource.exists())
        {
            log.error("Something went wrong initializing resources: Unable to create Resource directory.");
            throw new IOException();
        }

        if (!resource.getFile().isDirectory())
        {
            log.error("Resource directory is not a directory: {}", resource.getFile().toString());
            throw new IOException();
        }

        if (clean)
        {
            log.info("Resource directory {} exists, cleaning now...", resource.getFile());
            FileUtils.cleanDirectory(resource.getFile());
        }

        log.info("Initialized resource: {}", resource.getFile());
        return resource;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader)
    {
        this.resourceLoader = resourceLoader;
    }

    public Resource getCertHolders()
    {
        return certHolders;
    }

    public Resource getNonCertHolders()
    {
        return nonCertHolders;
    }

    public LinkedHashSet<String> getCodesForCountry(String country)
    {
        return stateLookups.getOrDefault(country, null).stream().map(State::getCode).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public LinkedHashSet<String> getStatesForCountry(String country)
    {
        return stateLookups.getOrDefault(country, null).stream().map(State::getName).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Map<String, String> getCountryLookups()
    {
        return countryLookups;
    }

    public Map<String, String> getDistrictLookups()
    {
        return districtLookups;
    }

    @Data
    @NoArgsConstructor
    private class State
    {
        @Expose
        private String name;

        @Expose
        private String code;
    }

    @Data
    @NoArgsConstructor
    private class Country
    {
        @Expose
        private String name;

        @Expose
        private String code;
    }

    @Data
    @NoArgsConstructor
    private class District
    {
        @Expose
        private String name;

        @Expose
        private String code;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private class ErrorOutput
    {
        @Expose
        private FacilityEntry entry;
        @Expose
        private List<MetadataError> errors;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private class Output
    {
        @Expose
        private Organization organization;
    }
}

package com.maksudsharif.migration.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.maksudsharif.migration.exceptions.ArkCaseCallFailedException;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import com.maksudsharif.migration.model.UserInfo;
import com.maksudsharif.migration.model.arkcase.MdmProfileDto;
import com.maksudsharif.migration.model.arkcase.Organization;
import com.maksudsharif.migration.model.arkcase.Person;
import com.maksudsharif.migration.model.arkcase.PrimaryContactDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.List;

@Component
@Log4j2
public class ArkCaseClient
{

    @Value("${arkcase.base.url}")
    private String baseUrl;
    @Value("${arkcase.port}")
    private String port;
    @Value("${arkcase.protocol}")
    private String protocol;
    @Value("${arkcase.solr.collection}")
    private String solrCollection;
    @Value("${arkcase.username}")
    private String username;
    @Value("${arkcase.password}")
    private String password;

    private RestTemplate restTemplate;
    private HttpSolrClient httpSolrClient;

    private HttpHeaders headers;

    private Gson gson;
    private ObjectMapper mapper;

    public ArkCaseClient(RestTemplate restTemplate, HttpSolrClient httpSolrClient, HttpHeaders headers, Gson gson, ObjectMapper mapper)
    {
        this.restTemplate = restTemplate;
        this.httpSolrClient = httpSolrClient;
        this.headers = headers;
        this.gson = gson;
        this.mapper = mapper;
        SSLUtilities.trustAllHostnames();
        SSLUtilities.trustAllHttpsCertificates();
    }

    private <T> ResponseEntity<T> rest(final String path, final HttpMethod method, final Class<T> responseType)
    {
        return rest(path, method, null, new LinkedMultiValueMap<>(), responseType);
    }

    private <T> ResponseEntity<T> rest(final String path, final HttpMethod method, final Object body, final Class<T> responseType)
    {
        return rest(path, method, body, new LinkedMultiValueMap<>(), responseType);
    }

    private <T> ResponseEntity<T> rest(final String path, final HttpMethod method, Object body, final LinkedMultiValueMap<String, String> requestParams, final Class<T> responseType)
    {

        try
        {
            String url = UriComponentsBuilder.newInstance()
                    .scheme(protocol)
                    .host(baseUrl)
                    .port(port)
                    .path(path)
                    .queryParams(requestParams)
                    .toUriString();

            HttpEntity<?> entity;

            if (body != null)
            {
                entity = new HttpEntity<>(body, headers);
            } else
            {
                entity = new HttpEntity<>(headers);
            }

            ResponseEntity<T> response = restTemplate.exchange(
                    url,
                    method,
                    entity,
                    responseType
            );

            log.debug("Response: {}", response);
            return response;
        } catch (Exception e)
        {
            log.error("Error", e);
            throw e;
        }
    }

    public UserInfo getMe()
    {
        ResponseEntity<UserInfo> rest = rest("/arkcase/api/latest/users/info", HttpMethod.GET, UserInfo.class);
        return rest != null ? rest.getBody() : null;
    }

    public Person createPerson(Person person) throws ArkCaseCallFailedException
    {
        try
        {
            String personInfo = mapper.writeValueAsString(person);
            if (StringUtils.isNotEmpty(personInfo))
            {
                ResponseEntity<Person> personResponse = rest("/arkcase/api/latest/plugin/people", HttpMethod.POST, personInfo, Person.class);
                return personResponse != null ? personResponse.getBody() : null;
            } else
            {
                throw new ArkCaseCallFailedException("Unable to serialize person to json");
            }
        } catch (Exception e)
        {
            log.error("Unable to create person: {}", person, e);
            throw new ArkCaseCallFailedException("Failed to create person", e);
        }
    }

    public Organization createOrganization(Organization organization, Person primaryPerson, List<Person> peopleInfo) throws ArkCaseCallFailedException
    {
        try
        {
            MdmProfileDto dto = new MdmProfileDto();
            dto.setMdmProfile(organization);
            dto.setNewOrganization(true);

            PrimaryContactDto pdto = new PrimaryContactDto();
            pdto.setPerson(primaryPerson);
            dto.setPrimaryContact(pdto);

            dto.setPeopleInfo(peopleInfo);


            String organizationInfo = mapper.writeValueAsString(dto);
            if (StringUtils.isNotEmpty(organizationInfo))
            {
                ResponseEntity<Organization> organizationResponse = rest("/arkcase/api/latest/plugin/mdsap/mdmprofile", HttpMethod.POST, organizationInfo, Organization.class);
                return organizationResponse != null ? organizationResponse.getBody() : null;
            } else
            {
                throw new ArkCaseCallFailedException("Unable to serialize organization to json");
            }
        } catch (Exception e)
        {
            log.error("Failed to create Organization", e);
            throw new ArkCaseCallFailedException("Failed to create organization", e);
        }
    }

    public SolrDocumentList solrRequest(String relatedCertificateHolderFirmName, String aoId)
    {
        log.info("Searching for {}", relatedCertificateHolderFirmName);
        SolrDocumentList retval = new SolrDocumentList();
        SolrQuery query = new SolrQuery();
        int rows = 0;
        int batch = 25;
        int start = 0;
        query.setRows(rows);
        query.setStart(start);
        query.addFilterQuery("status_lcs:ACTIVE");
        query.addFilterQuery("mdm_certificate_holder_b:true");
        query.addFilterQuery(String.format("ao_id_lcs:%s", aoId));
        query.addFilterQuery("title_parseable:" + "\"" + relatedCertificateHolderFirmName + "\"" + " OR duns_num_s:" + "\"" + relatedCertificateHolderFirmName + "\"");
        query.setQuery("object_type_s:ORGANIZATION");
        log.debug("Searching for related certificate holder: {}", query);
        try
        {
            SolrRequest<QueryResponse> req = new QueryRequest(query, SolrRequest.METHOD.GET);
            QueryResponse response = req.process(httpSolrClient, solrCollection);
            SolrDocumentList results = response.getResults();

            long numFound = results.getNumFound();
            log.debug("'{}' found: {}", query, numFound);
            if (numFound == 0)
            {
                return retval;
            } else
            {
                query.setRows(batch);
                int foundSoFar = 0;
                do
                {
                    SolrRequest<QueryResponse> req2 = new QueryRequest(query, SolrRequest.METHOD.GET);
                    req2.setBasicAuthCredentials("maksud.sharif", "Jenova1171181714!");
//                    QueryResponse acmAdvancedSearch = httpSolrClient.query(solrCollection, query, SolrRequest.METHOD.GET);
                    QueryResponse acmAdvancedSearch = req2.process(httpSolrClient, solrCollection);
                    SolrDocumentList intervalResults = acmAdvancedSearch.getResults();
                    retval.addAll(intervalResults);
                    start += batch;
                    foundSoFar += intervalResults.size();
                    query.setStart(start);
                } while (numFound > foundSoFar);
            }

            retval.setNumFound(retval.size());
            return retval;
        } catch (SolrServerException | IOException e)
        {
            log.error(e);
            return new SolrDocumentList();
        }
    }

    public Organization createReferences(Long organizationId)
    {
        LinkedMultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("organizationId", String.valueOf(organizationId));
        try
        {
            ResponseEntity<String> rest = rest("/arkcase/api/latest/plugin/mdsap/mdmprofile/references", HttpMethod.POST, null, queryParams, String.class);
            if (rest.getStatusCode().is2xxSuccessful())
            {
                try
                {
                    String body = rest.getBody();
                    return mapper.readValue(body, Organization.class);
                } catch (Exception e)
                {
                    log.error("Unable to read value from request (200): {}", rest.getBody(), e);
                    return recover(organizationId);
                }
            } else
            {
                log.error("ArkCase service call error'ed: [{}]", rest.getBody());
                return recover(organizationId);
            }
        } catch (Exception e)
        {
            log.error("Unable to create references", e);
            return recover(organizationId);
        }
    }

    public Organization recover(Long organizationId)
    {
        LinkedMultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("organizationId", String.valueOf(organizationId));
        try
        {
            ResponseEntity<Organization> rest = rest("/arkcase/api/latest/plugin/organizations/" + organizationId, HttpMethod.GET, null, null, Organization.class);
            return rest.getStatusCode().is2xxSuccessful() ? rest.getBody() : null;
        } catch (Exception e)
        {
            log.error("Unable to recover", e);
            return null;
        }
    }

}

package com.maksudsharif.migration.services;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.StringUtils;
import com.maksudsharif.migration.model.FacilityEntry;
import com.maksudsharif.migration.model.HeaderConstants;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Log4j2
@Service
public class ParserService
{
    private CSVFormat csvFormat;

    public ParserService(CSVFormat csvFormat)
    {
        this.csvFormat = csvFormat;
    }

    public List<FacilityEntry> parse(File resource)
    {
        Map<String, Integer> headerMap = getHeaders(resource);
        List<CSVRecord> records = getRecords(resource);
        return parseInternal(headerMap, records);
    }

    public void parseIndexes(File resource)
    {
        List<CSVRecord> records = getRecords(resource);
        AtomicInteger index = new AtomicInteger(0);
        for (CSVRecord record : records)
        {
            record.toMap().computeIfPresent("Campus (Yes/No)", (key, value) -> {
                if (StringUtils.isNotEmpty(value.trim()))
                {
                    int val = index.incrementAndGet();
                    System.out.println(val);
                } else
                {
                    System.out.println("");
                }
                return "OK";
            });

        }
    }

    private List<FacilityEntry> parseInternal(Map<String, Integer> headers, List<CSVRecord> records)
    {
        Preconditions.checkNotNull(headers, "Headers are required");
        Preconditions.checkNotNull(records, "CSVRecords are required");

        Stopwatch watch = Stopwatch.createStarted();

        LinkedHashSet<Long> indexes = getIndexRecordNumbers(headers, records);
        List<FacilityEntry> facilityEntries = parseInternal(records, indexes);

        watch.stop();

        log.info("Parsed {} facilities in {}", facilityEntries.size(), watch.toString());

        return facilityEntries;
    }

    private List<FacilityEntry> parseInternal(List<CSVRecord> records, LinkedHashSet<Long> indexes)
    {
        Preconditions.checkNotNull(indexes, "Indexes are required");
        Preconditions.checkNotNull(records, "CSVRecords are required");

        List<FacilityEntry> entries = new ArrayList<>();
        ListIterator<CSVRecord> iterator = records.listIterator();
        List<CSVRecord> temporaryRecords = new ArrayList<>();
        do
        {
            CSVRecord next = iterator.next();
            if (indexes.contains(next.getRecordNumber()) && iterator.nextIndex() != 1)
            {
                entries.add(new FacilityEntry(temporaryRecords));
                // start new facility entry
                temporaryRecords = new ArrayList<>();
            }
            temporaryRecords.add(next);
        } while (iterator.hasNext());

        // Add last iteration of records
        entries.add(new FacilityEntry(temporaryRecords));

        return entries;
    }

    private LinkedHashSet<Long> getIndexRecordNumbers(Map<String, Integer> headers, List<CSVRecord> records)
    {
        return records.parallelStream()
                .filter(record -> !StringUtils.isEmpty(record.get(headers.get(HeaderConstants.INDEX))))
                .map(CSVRecord::getRecordNumber)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private List<CSVRecord> getRecords(File file)
    {
        try (Reader reader = new InputStreamReader(new BOMInputStream(new FileInputStream(file), ByteOrderMark.UTF_8)))
        {
            return CSVParser.parse(reader, csvFormat).getRecords();
        } catch (IOException e)
        {
            log.error("Unable to read CSV", e);
            return Collections.emptyList();
        }
    }

    private Map<String, Integer> getHeaders(File file)
    {
        try (Reader reader = new InputStreamReader(new BOMInputStream(new FileInputStream(file), ByteOrderMark.UTF_8)))
        {
            return CSVParser.parse(reader, csvFormat).getHeaderMap();
        } catch (IOException e)
        {
            log.error("Unable to read CSV", e);
            return Collections.emptyMap();
        }
    }
}

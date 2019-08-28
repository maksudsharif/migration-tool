package com.maksudsharif.migration.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ValidationResult
{
    private List<MetadataError> metadataErrors = new ArrayList<>();

    public void addError(MetadataError metadataError)
    {
        if (metadataError != null)
        {
            metadataErrors.add(metadataError);
        }
    }

    public void addErrors(Collection<MetadataError> metadataErrors)
    {
        if (metadataErrors != null)
        {
            this.metadataErrors.addAll(metadataErrors);
        }
    }

    public boolean hasError()
    {
        return !this.metadataErrors.isEmpty();
    }

    public String prettyPrintErrors()
    {
        return this.metadataErrors.stream().map(MetadataError::getError).collect(Collectors.joining("\n"));
    }
}

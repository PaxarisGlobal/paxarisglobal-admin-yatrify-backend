package com.yatrify.organizer.dto;

import com.yatrify.organizer.model.OrganizerProfile;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateOrganizerRequest {

    @NotBlank(message = "Organization name is required")
    @Size(min = 3, max = 255, message = "Organization name must be between 3 and 255 characters")
    private String organizationName;

    @NotNull(message = "Organization type is required")
    private OrganizerProfile.OrganizationType organizationType;

    private String gstin;
    private String panNumber;
    private String website;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    private String bankAccountNumber;
    private String bankIfscCode;
    private String bankName;
    private String bankAccountHolder;
}

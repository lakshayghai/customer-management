package com.lakshayghai.customermanagement.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
public class PhoneNumberDTO {
    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "\\+?[0-9]{10,15}",
            message = "Phone number must be a valid international number (E.164 format)"
    )
    private String phoneNumber;

    @NotBlank(message = "Phone number type is required")
    @Pattern(
            regexp = "MOBILE|HOME|WORK",
            message = "Type must be one of MOBILE, HOME, or WORK"
    )
    private String phoneType;

    @NotBlank(message = "Country code is required")
    @Size(max = 10, message = "Country code must not exceed 10 characters")
    private String countryCode;

    @NotNull(message = "Verification status is required")
    private boolean isVerified;
}

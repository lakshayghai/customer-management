package com.lakshayghai.customermanagement.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Data
@Validated
public class CustomerDTO {
    @NotBlank(message = "First name is required")
    @Size(max = 255, message = "First name must not exceed 255 characters")
    private String firstName;

    private String middleName;

    @NotBlank(message = "Last name is required")
    @Size(max = 255, message = "Last name must not exceed 255 characters")
    private String lastName;

    @NotBlank(message = "Email address is required")
    @Email(message = "Invalid email address")
    private String emailAddress;

    @NotEmpty(message = "At least one phone number is required")
    @Valid
    private List<PhoneNumberDTO> phoneNumbers;

}


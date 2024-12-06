package com.lakshayghai.customermanagement.service;

import com.lakshayghai.customermanagement.entity.Customer;
import com.lakshayghai.customermanagement.entity.PhoneNumber;
import com.lakshayghai.customermanagement.model.CustomerDTO;
import com.lakshayghai.customermanagement.model.PhoneNumberDTO;
import com.lakshayghai.customermanagement.repository.CustomerRepository;
import com.lakshayghai.customermanagement.repository.PhoneNumberRepository;
import com.lakshayghai.customermanagement.util.ValidationUtil;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final PhoneNumberRepository phoneNumberRepository;

    public CustomerService(CustomerRepository customerRepository, PhoneNumberRepository phoneNumberRepository) {
        this.customerRepository = customerRepository;
        this.phoneNumberRepository = phoneNumberRepository;
    }

    @Transactional(dontRollbackOn = DataIntegrityViolationException.class)
    public ResponseEntity<?> createCustomerWithPhoneNumbers(CustomerDTO customerDTO) {
        Map<String, String> errors = new HashMap<>();

        // Validate email
        if (!ValidationUtil.isValidEmail(customerDTO.getEmailAddress())) {
            errors.put("emailAddress", "Invalid email address: " + customerDTO.getEmailAddress());
        }

        // First check if email exists
        if (customerRepository.existsByEmailAddress(customerDTO.getEmailAddress())) {
            errors.put("emailAddress", "Email address already exists");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
        }

        // Validate phone numbers
        List<PhoneNumber> phoneNumbers = new ArrayList<>();
        for (PhoneNumberDTO phoneDTO : customerDTO.getPhoneNumbers()) {
            try {
                PhoneNumber phoneNumber = createPhoneNumberEntity(phoneDTO);
                phoneNumbers.add(phoneNumber);
            } catch (IllegalArgumentException ex) {
                errors.put("phoneNumber", ex.getMessage());
            }
        }

        // If validation errors exist, return bad request
        if (!errors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
        }
        try {
            // Create and save customer
            Customer customer = new Customer();
            customer.setFirstName(customerDTO.getFirstName());
            customer.setMiddleName(customerDTO.getMiddleName());
            customer.setLastName(customerDTO.getLastName());
            customer.setEmailAddress(customerDTO.getEmailAddress());
            customer = customerRepository.save(customer);

            // Associate phone numbers
            Customer finalCustomer = customer;
            phoneNumbers.forEach(phoneNumber -> phoneNumber.setCustomer(finalCustomer));
            phoneNumberRepository.saveAll(phoneNumbers);

            customer.setPhoneNumbers(phoneNumbers);
            return ResponseEntity.status(HttpStatus.CREATED).body(customer);
        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation while creating customer", e);
            Map<String, String> violationErrors = new HashMap<>();
            violationErrors.put("emailAddress", "Email address already exists");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(violationErrors);
        }
    }

    @Transactional
    public ResponseEntity<?> updateCustomer(UUID id, CustomerDTO customerDTO) {
        Optional<Customer> existingCustomerOpt = customerRepository.findById(id);
        if (existingCustomerOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Customer not found"));
        }

        Map<String, String> errors = new HashMap<>();

        // Validate email
        if (!ValidationUtil.isValidEmail(customerDTO.getEmailAddress())) {
            errors.put("emailAddress", "Invalid email address: " + customerDTO.getEmailAddress());
        }

        // Validate phone numbers
        List<PhoneNumber> phoneNumbers = new ArrayList<>();
        for (PhoneNumberDTO phoneDTO : customerDTO.getPhoneNumbers()) {
            try {
                PhoneNumber phoneNumber = createPhoneNumberEntity(phoneDTO);
                phoneNumbers.add(phoneNumber);
            } catch (IllegalArgumentException ex) {
                errors.put("phoneNumber", ex.getMessage());
            }
        }

        // If validation errors exist, return bad request
        if (!errors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
        }

        Customer existingCustomer = existingCustomerOpt.get();

        // Update customer details
        existingCustomer.setFirstName(customerDTO.getFirstName());
        existingCustomer.setMiddleName(customerDTO.getMiddleName());
        existingCustomer.setLastName(customerDTO.getLastName());
        existingCustomer.setEmailAddress(customerDTO.getEmailAddress());

        // Compare and update phone numbers
        List<PhoneNumber> existingPhoneNumbers = existingCustomer.getPhoneNumbers();
        List<PhoneNumber> toAdd = phoneNumbers.stream()
                .filter(newPhone -> !existingPhoneNumbers.contains(newPhone)) // Only add distinct ones
                .collect(Collectors.toList());

        List<PhoneNumber> toDelete = existingPhoneNumbers.stream()
                .filter(existingPhone -> !phoneNumbers.contains(existingPhone)) // Remove ones not in new list
                .collect(Collectors.toList());

        // Delete old phone numbers
        if (!toDelete.isEmpty()) {
            phoneNumberRepository.deleteAll(toDelete);
        }

        // Add new phone numbers
        toAdd.forEach(phoneNumber -> phoneNumber.setCustomer(existingCustomer));
        phoneNumberRepository.saveAll(toAdd);

        // Update the customer's phoneNumbers list
        existingPhoneNumbers.removeAll(toDelete);
        existingPhoneNumbers.addAll(toAdd);

        customerRepository.save(existingCustomer);

        return ResponseEntity.status(HttpStatus.OK).body(existingCustomer);
    }

    private PhoneNumber createPhoneNumberEntity(PhoneNumberDTO phoneDTO) {
        if (!ValidationUtil.isValidPhoneNumber(phoneDTO.getPhoneNumber(), phoneDTO.getCountryCode())) {
            throw new IllegalArgumentException("Invalid phone number: " + phoneDTO.getPhoneNumber());
        }

        PhoneNumber phoneNumber = new PhoneNumber();
        phoneNumber.setPhoneNumber(phoneDTO.getPhoneNumber());
        phoneNumber.setPhoneType(phoneDTO.getPhoneType());
        phoneNumber.setCountryCode(phoneDTO.getCountryCode());
        phoneNumber.setVerified(phoneDTO.isVerified());
        return phoneNumber;
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public ResponseEntity<?> getCustomerById(UUID id) {
        return customerRepository.findByIdWithPhoneNumbers(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Customer not found")));
    }

    @Transactional
    public ResponseEntity<?> deleteCustomer(UUID id) {
        if (!customerRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Customer not found"));
        }

        customerRepository.deleteById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}

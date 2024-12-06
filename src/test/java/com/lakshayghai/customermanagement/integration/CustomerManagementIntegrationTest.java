package com.lakshayghai.customermanagement.integration;

import com.lakshayghai.customermanagement.TestcontainersConfiguration;
import com.lakshayghai.customermanagement.entity.Customer;
import com.lakshayghai.customermanagement.entity.PhoneNumber;
import com.lakshayghai.customermanagement.model.CustomerDTO;
import com.lakshayghai.customermanagement.model.PhoneNumberDTO;
import com.lakshayghai.customermanagement.repository.CustomerRepository;
import com.lakshayghai.customermanagement.repository.PhoneNumberRepository;
import com.lakshayghai.customermanagement.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class CustomerManagementIntegrationTest {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PhoneNumberRepository phoneNumberRepository;

    private CustomerDTO customerDTO;
    private List<PhoneNumberDTO> phoneNumberDTOs;

    @BeforeEach
    void setUp() {
        // Clean up the database
        phoneNumberRepository.deleteAll();
        customerRepository.deleteAll();

        // Initialize phone numbers
        phoneNumberDTOs = new ArrayList<>();

        PhoneNumberDTO mobilePhone = new PhoneNumberDTO();
        mobilePhone.setPhoneNumber("+12125552368");
        mobilePhone.setPhoneType("MOBILE");
        mobilePhone.setCountryCode("US");
        mobilePhone.setVerified(true);
        phoneNumberDTOs.add(mobilePhone);

        PhoneNumberDTO homePhone = new PhoneNumberDTO();
        homePhone.setPhoneNumber("+12125552369");
        homePhone.setPhoneType("HOME");
        homePhone.setCountryCode("US");
        homePhone.setVerified(false);
        phoneNumberDTOs.add(homePhone);

        // Initialize customer DTO
        customerDTO = new CustomerDTO();
        customerDTO.setFirstName("Jane");
        customerDTO.setMiddleName("Marie");
        customerDTO.setLastName("Smith");
        customerDTO.setEmailAddress("jane.smith@example.com");
        customerDTO.setPhoneNumbers(phoneNumberDTOs);
    }

    @Test
    void createCustomerFlow_CompleteTest() {
        // Create customer
        ResponseEntity<?> createResponse = customerService.createCustomerWithPhoneNumbers(customerDTO);
        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        Customer createdCustomer = (Customer) createResponse.getBody();

        assertNotNull(createdCustomer);
        assertNotNull(createdCustomer.getId());
        assertEquals(2, createdCustomer.getPhoneNumbers().size());
        assertEquals(customerDTO.getEmailAddress(), createdCustomer.getEmailAddress());

        // Verify phone numbers were saved correctly
        List<PhoneNumber> savedPhoneNumbers = createdCustomer.getPhoneNumbers();
        assertTrue(savedPhoneNumbers.stream()
                .anyMatch(phone -> phone.getPhoneType().equals("MOBILE") && phone.isVerified()));
        assertTrue(savedPhoneNumbers.stream()
                .anyMatch(phone -> phone.getPhoneType().equals("HOME") && !phone.isVerified()));
    }

    @Test
    void updateCustomerFlow_PhoneNumberModification() {
        // First create a customer
        ResponseEntity<?> createResponse = customerService.createCustomerWithPhoneNumbers(customerDTO);
        Customer initialCustomer = (Customer) createResponse.getBody();
        assertNotNull(initialCustomer);

        // Modify the customer DTO
        customerDTO.setFirstName("Janet");
        PhoneNumberDTO workPhone = new PhoneNumberDTO();
        workPhone.setPhoneNumber("+12125552370");
        workPhone.setPhoneType("WORK");
        workPhone.setCountryCode("US");
        workPhone.setVerified(true);

        List<PhoneNumberDTO> updatedPhones = new ArrayList<>();
        updatedPhones.add(workPhone); // Only keep work phone
        customerDTO.setPhoneNumbers(updatedPhones);

        // Update the customer
        ResponseEntity<?> updateResponse = customerService.updateCustomer(initialCustomer.getId(), customerDTO);
        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());

        Customer updatedCustomer = (Customer) updateResponse.getBody();
        assertNotNull(updatedCustomer);
        assertEquals("Janet", updatedCustomer.getFirstName());
        assertEquals(1, updatedCustomer.getPhoneNumbers().size());
        assertEquals("WORK", updatedCustomer.getPhoneNumbers().get(0).getPhoneType());
    }

    @Test
    void customerConstraints_UniqueEmailValidation() {
        // Create first customer
        ResponseEntity<?> firstResponse = customerService.createCustomerWithPhoneNumbers(customerDTO);
        assertEquals(HttpStatus.CREATED, firstResponse.getStatusCode());

        // Try to create second customer with same email
        CustomerDTO duplicateCustomer = new CustomerDTO();
        duplicateCustomer.setFirstName("John");
        duplicateCustomer.setLastName("Doe");
        duplicateCustomer.setEmailAddress(customerDTO.getEmailAddress()); // Same email
        duplicateCustomer.setPhoneNumbers(phoneNumberDTOs);

        ResponseEntity<?> duplicateResponse = customerService.createCustomerWithPhoneNumbers(duplicateCustomer);
        assertEquals(HttpStatus.BAD_REQUEST, duplicateResponse.getStatusCode());

        Map<String, String> errors = (Map<String, String>) duplicateResponse.getBody();
        assertNotNull(errors);
        assertTrue(errors.containsKey("emailAddress"));
        assertTrue(errors.get("emailAddress").contains("Email address already exists"));
    }

    @Test
    void customerLifecycle_CompleteTest() {
        // Create
        ResponseEntity<?> createResponse = customerService.createCustomerWithPhoneNumbers(customerDTO);
        Customer customer = (Customer) createResponse.getBody();
        assertNotNull(customer);

        // Read
        ResponseEntity<?> readResponse = customerService.getCustomerById(customer.getId());
        assertEquals(HttpStatus.OK, readResponse.getStatusCode());
        assertNotNull(readResponse.getBody());

        // Update
        customerDTO.setFirstName("Updated");
        ResponseEntity<?> updateResponse = customerService.updateCustomer(customer.getId(), customerDTO);
        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
        Customer updatedCustomer = (Customer) updateResponse.getBody();
        assertEquals("Updated", updatedCustomer.getFirstName());

        // Delete
        ResponseEntity<?> deleteResponse = customerService.deleteCustomer(customer.getId());
        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());

        // Verify deletion
        ResponseEntity<?> verifyResponse = customerService.getCustomerById(customer.getId());
        assertEquals(HttpStatus.NOT_FOUND, verifyResponse.getStatusCode());
    }

    @Test
    void batchOperations_MultipleCustomers() {
        // Create first customer with original phone numbers
        customerService.createCustomerWithPhoneNumbers(customerDTO);

        // Create second customer with different phone numbers
        CustomerDTO secondCustomer = new CustomerDTO();
        secondCustomer.setFirstName("John");
        secondCustomer.setLastName("Doe");
        secondCustomer.setEmailAddress("john.doe@example.com");

        // Create new phone numbers with different numbers
        List<PhoneNumberDTO> newPhoneNumbers = new ArrayList<>();

        PhoneNumberDTO mobilePhone = new PhoneNumberDTO();
        mobilePhone.setPhoneNumber("+12125552888");  // Different number
        mobilePhone.setPhoneType("MOBILE");
        mobilePhone.setCountryCode("US");
        mobilePhone.setVerified(true);
        newPhoneNumbers.add(mobilePhone);

        PhoneNumberDTO homePhone = new PhoneNumberDTO();
        homePhone.setPhoneNumber("+12125552999");    // Different number
        homePhone.setPhoneType("HOME");
        homePhone.setCountryCode("US");
        homePhone.setVerified(false);
        newPhoneNumbers.add(homePhone);

        secondCustomer.setPhoneNumbers(newPhoneNumbers);
        customerService.createCustomerWithPhoneNumbers(secondCustomer);

        // Get all customers
        List<Customer> allCustomers = customerService.getAllCustomers();
        assertEquals(2, allCustomers.size());

        // Verify phone numbers for all customers
        allCustomers.forEach(customer -> {
            assertFalse(customer.getPhoneNumbers().isEmpty());
            assertTrue(customer.getPhoneNumbers().size() >= 1);
        });
    }
}
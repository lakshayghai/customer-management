package com.lakshayghai.customermanagement.service;

import com.lakshayghai.customermanagement.entity.Customer;
import com.lakshayghai.customermanagement.entity.PhoneNumber;
import com.lakshayghai.customermanagement.model.CustomerDTO;
import com.lakshayghai.customermanagement.model.PhoneNumberDTO;
import com.lakshayghai.customermanagement.repository.CustomerRepository;
import com.lakshayghai.customermanagement.repository.PhoneNumberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PhoneNumberRepository phoneNumberRepository;

    @InjectMocks
    private CustomerService customerService;

    private CustomerDTO validCustomerDTO;
    private Customer validCustomer;
    private UUID testUuid;

    @BeforeEach
    void setUp() {
        testUuid = UUID.randomUUID();

        // Setup valid phone number DTO
        PhoneNumberDTO phoneNumberDTO = new PhoneNumberDTO();
        phoneNumberDTO.setPhoneNumber("+12125552368");
        phoneNumberDTO.setPhoneType("MOBILE");
        phoneNumberDTO.setCountryCode("US");
        phoneNumberDTO.setVerified(true);

        // Setup valid customer DTO
        validCustomerDTO = new CustomerDTO();
        validCustomerDTO.setFirstName("John");
        validCustomerDTO.setLastName("Doe");
        validCustomerDTO.setEmailAddress("john.doe@example.com");
        validCustomerDTO.setPhoneNumbers(List.of(phoneNumberDTO));

        // Setup valid customer entity
        validCustomer = new Customer();
        validCustomer.setId(testUuid);
        validCustomer.setFirstName("John");
        validCustomer.setLastName("Doe");
        validCustomer.setEmailAddress("john.doe@example.com");

        PhoneNumber phoneNumber = new PhoneNumber();
        phoneNumber.setPhoneNumber("+12125552368");
        phoneNumber.setPhoneType("MOBILE");
        phoneNumber.setCountryCode("US");
        phoneNumber.setVerified(true);
        phoneNumber.setCustomer(validCustomer);

        List<PhoneNumber> phoneNumbers = new ArrayList<>();
        phoneNumbers.add(phoneNumber);
        validCustomer.setPhoneNumbers(phoneNumbers);
    }

    @Test
    void createCustomer_ValidData_Success() {
        when(customerRepository.save(any(Customer.class))).thenReturn(validCustomer);
        when(phoneNumberRepository.saveAll(any())).thenReturn(new ArrayList<>());

        ResponseEntity<?> response = customerService.createCustomerWithPhoneNumbers(validCustomerDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(customerRepository, times(1)).save(any(Customer.class));
        verify(phoneNumberRepository, times(1)).saveAll(any());
    }

    @Test
    void createCustomer_InvalidEmail_ReturnsBadRequest() {
        validCustomerDTO.setEmailAddress("invalid-email");

        ResponseEntity<?> response = customerService.createCustomerWithPhoneNumbers(validCustomerDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(((Map<?, ?>)response.getBody()).containsKey("emailAddress"));
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void createCustomer_InvalidPhoneNumber_ReturnsBadRequest() {
        validCustomerDTO.getPhoneNumbers().get(0).setPhoneNumber("invalid");

        ResponseEntity<?> response = customerService.createCustomerWithPhoneNumbers(validCustomerDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(((Map<?, ?>)response.getBody()).containsKey("phoneNumber"));
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void getCustomerById_ExistingCustomer_ReturnsCustomer() {
        when(customerRepository.findByIdWithPhoneNumbers(testUuid)).thenReturn(Optional.of(validCustomer));

        ResponseEntity<?> response = customerService.getCustomerById(testUuid);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(validCustomer, response.getBody());
    }

    @Test
    void getCustomerById_NonExistingCustomer_ReturnsNotFound() {
        when(customerRepository.findByIdWithPhoneNumbers(testUuid)).thenReturn(Optional.empty());

        ResponseEntity<?> response = customerService.getCustomerById(testUuid);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(((Map<?, ?>)response.getBody()).containsKey("error"));
    }

    @Test
    void getAllCustomers_ReturnsAllCustomers() {
        List<Customer> customers = List.of(validCustomer);
        when(customerRepository.findAll()).thenReturn(customers);

        List<Customer> response = customerService.getAllCustomers();

        assertEquals(customers.size(), response.size());
        assertEquals(customers.get(0), response.get(0));
    }

    @Test
    void updateCustomer_ValidData_Success() {
        when(customerRepository.findById(testUuid)).thenReturn(Optional.of(validCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(validCustomer);
        when(phoneNumberRepository.saveAll(any())).thenReturn(new ArrayList<>());

        ResponseEntity<?> response = customerService.updateCustomer(testUuid, validCustomerDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    void updateCustomer_NonExistingCustomer_ReturnsNotFound() {
        when(customerRepository.findById(testUuid)).thenReturn(Optional.empty());

        ResponseEntity<?> response = customerService.updateCustomer(testUuid, validCustomerDTO);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void updateCustomer_InvalidEmail_ReturnsBadRequest() {
        when(customerRepository.findById(testUuid)).thenReturn(Optional.of(validCustomer));
        validCustomerDTO.setEmailAddress("invalid-email");

        ResponseEntity<?> response = customerService.updateCustomer(testUuid, validCustomerDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(((Map<?, ?>)response.getBody()).containsKey("emailAddress"));
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void deleteCustomer_ExistingCustomer_Success() {
        when(customerRepository.existsById(testUuid)).thenReturn(true);

        ResponseEntity<?> response = customerService.deleteCustomer(testUuid);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(customerRepository, times(1)).deleteById(testUuid);
    }

    @Test
    void deleteCustomer_NonExistingCustomer_ReturnsNotFound() {
        when(customerRepository.existsById(testUuid)).thenReturn(false);

        ResponseEntity<?> response = customerService.deleteCustomer(testUuid);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(customerRepository, never()).deleteById(any());
    }

    @Test
    void updateCustomer_PhoneNumberChanges_HandlesCorrectly() {
        when(customerRepository.findById(testUuid)).thenReturn(Optional.of(validCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(validCustomer);

        // Add a new phone number to the DTO
        PhoneNumberDTO newPhoneDTO = new PhoneNumberDTO();
        newPhoneDTO.setPhoneNumber("+12125552369");
        newPhoneDTO.setPhoneType("HOME");
        newPhoneDTO.setCountryCode("US");
        newPhoneDTO.setVerified(true);

        validCustomerDTO.setPhoneNumbers(List.of(
                validCustomerDTO.getPhoneNumbers().get(0),
                newPhoneDTO
        ));

        ResponseEntity<?> response = customerService.updateCustomer(testUuid, validCustomerDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(phoneNumberRepository, times(1)).saveAll(any());
    }
}
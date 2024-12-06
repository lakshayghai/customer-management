package com.lakshayghai.customermanagement.repository;

import com.lakshayghai.customermanagement.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    boolean existsByEmailAddress(String emailAddress);

    @Override
    @Query("SELECT DISTINCT c FROM Customer c LEFT JOIN FETCH c.phoneNumbers")
    List<Customer> findAll();

    @Query("SELECT c FROM Customer c LEFT JOIN FETCH c.phoneNumbers WHERE c.id = :id")
    Optional<Customer> findByIdWithPhoneNumbers(@Param("id") UUID id);
}


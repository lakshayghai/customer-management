package com.lakshayghai.customermanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Objects;
import java.util.UUID;

@Data
@Entity
@Table(name = "phone_number")
@EqualsAndHashCode(callSuper = false)
public class PhoneNumber extends AuditableEntity {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    private Customer customer;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String phoneType;

    @Column(nullable = false)
    private String countryCode;

    @Column(nullable = false)
    private boolean isVerified;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhoneNumber that = (PhoneNumber) o;
        return Objects.equals(normalize(phoneNumber), normalize(that.phoneNumber)) &&
                Objects.equals(phoneType, that.phoneType) &&
                Objects.equals(countryCode, that.countryCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(normalize(phoneNumber), phoneType, countryCode);
    }

    /**
     * Normalize the phone number to ensure consistent comparison.
     * Removes spaces, dashes, and other non-numeric characters.
     */
    private String normalize(String phoneNumber) {
        if (phoneNumber == null) return null;
        return phoneNumber.replaceAll("[^+0-9]", ""); // Keep only digits and '+'
    }
}

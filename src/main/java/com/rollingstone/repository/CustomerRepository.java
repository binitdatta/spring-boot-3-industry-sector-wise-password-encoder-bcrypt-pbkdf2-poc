package com.rollingstone.repository;

import com.rollingstone.model.AppUser;
import com.rollingstone.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByEmail(String email);  // ✅ valid
    List<Customer> findByName(String name);        // ✅ valid
}


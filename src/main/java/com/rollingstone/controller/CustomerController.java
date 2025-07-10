package com.rollingstone.controller;


import com.rollingstone.model.Customer;
import com.rollingstone.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class CustomerController {

    @Autowired
    private CustomerRepository customerRepository;

    @GetMapping("/customers")
    public String listCustomers(Model model) {
        List<Customer> customers = customerRepository.findAll();

        model.addAttribute("customers", customers);
        return "customers"; // resolves to src/main/resources/templates/customers.html
    }
}


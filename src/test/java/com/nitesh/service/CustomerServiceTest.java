package com.nitesh.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class CustomerServiceTest {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerRepository customerRepository;



    @Test
    public void testShouldCreateAndPersistCustomer() {
        Customer customer = customerService.createCustomer("Nitesh", "test@gmail.com");
        Customer persistedCustomer = customerRepository.findById(customer.getId()).get();

        assertEquals("Nitesh", persistedCustomer.getName());
        assertEquals("test@gmail.com", persistedCustomer.getEmail());

        assertTrue(persistedCustomer.hasToken());


    }
}
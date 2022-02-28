package com.nitesh.service;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Objects;

/**
 * url : https://youtu.be/h8TWQM6fKNQ
 */
@Service
public class TransactionalDemo {

}

@Entity
@Data
@NoArgsConstructor
class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String token;

    public Customer(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public void activatedWithToken(String token) {
        this.token = token;
    }

    public boolean hasToken() {
        return !StringUtils.isEmpty(this.token);
    }
}

@Repository
interface CustomerRepository extends JpaRepository<Customer, Long> {
}

@Service
class CustomerService {

    private final CustomerRepository customerRepository;
    private final ApplicationEventPublisher publisher;

    CustomerService(CustomerRepository customerRepository, ApplicationEventPublisher publisher) {
        this.customerRepository = customerRepository;
        this.publisher = publisher;
    }

    @Transactional
    public Customer createCustomer(String name, String email) {
        final Customer customer = customerRepository.save(new Customer(name, email));
        customer.setEmail("hello@test.com");
        customerRepository.save(customer);
        publisher.publishEvent(new CustomerCreated(customer.getId()));
        return customer;
    }
}

@Service
class TokenGenerator {
    private final CustomerRepository customerRepository;


    TokenGenerator(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public void generateToken (Customer customer) {
        final String token = String.valueOf(customer.hashCode());
        customer.activatedWithToken(token);
        customerRepository.save(customer);
    }
}

final class CustomerCreated {
    private final Long customerId;

    public CustomerCreated(Long customerId) {
        this.customerId = customerId;
    }

    public Long getCustomerId() {
        return customerId;
    }
}

@Component
@Slf4j
class CustomerCreatedListener {

    private final CustomerRepository customerRepository;
    private final TokenGenerator tokenGenerator;

    CustomerCreatedListener(CustomerRepository customerRepository, TokenGenerator tokenGenerator) {
        this.customerRepository = customerRepository;
        this.tokenGenerator = tokenGenerator;
    }

    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle (CustomerCreated customerCreated) throws InterruptedException {
        Thread.sleep(50000);
        Customer customer = customerRepository.findById(customerCreated.getCustomerId()).get();
        log.info("Transactional listener Customer Created {}", customerCreated.getCustomerId());
        log.info("Transactional listener Customer Fetched {} ", Objects.nonNull(customer) ? customer.getId() : null);
        tokenGenerator.generateToken(customer);
    }
}

@RestController
@RequestMapping("/customer")
class CustomerController2 {

    @Autowired
    private CustomerService customerService;

    @GetMapping
    public ResponseEntity<String> createCustomer() {
        customerService.createCustomer("Nandan", "abc@gmail.com");
        return ResponseEntity.ok("Created");
    }
}

package org.wflux.demo;

import lombok.AllArgsConstructor;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.wflux.demo.model.Customer;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@RestController
public class CDataController {
    // injected in constructor:
    private ReactiveMongoTemplate reactiveMongoTemplate;

    @PostMapping("/customer/create")
    public Mono<Customer> createCustomer(@RequestBody
                                             // Spring WebFlex receives request as Mono<T> and then passes it as <T>, i.e.
                                             // conversion is already done to simplify POST request not adding Mono:
                                             Customer customer) {
        return reactiveMongoTemplate.save(customer);
    }
}

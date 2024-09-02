package org.wflux.demo;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.wflux.demo.model.Customer;
import reactor.core.publisher.Mono;

@RestController
public class CDataController {
    @Qualifier("customReactiveMongoTemplate")
    private final ReactiveMongoTemplate reactiveMongoTemplate;

    public CDataController(ReactiveMongoTemplate reactiveMongoTemplate) {
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    @PostMapping("/customer/create")
    public Mono<Customer> createCustomer(@RequestBody
                                             // Spring WebFlex receives request as Mono<T> and then passes it as <T>, i.e.
                                             // conversion is already done to simplify POST request not adding Mono:
                                             Customer customer) {
        return reactiveMongoTemplate.save(customer);
    }
}

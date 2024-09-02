package org.wflux.demo;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.*;
import org.wflux.demo.model.Customer;
import reactor.core.publisher.Flux;
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

    // e.g.: curl -X GET 'http://localhost:8080/customer/search?name=Cami'
    @GetMapping("/customer/search")
    public Flux<Customer> searchCustomerByName(@RequestParam("name") String name) {
        Criteria criteria = Criteria.where("name").is(name);
        Query query = Query.query(criteria);
        return reactiveMongoTemplate.find(query, Customer.class)
                .log();
    }
}

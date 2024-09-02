package org.wflux.demo;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.*;
import org.wflux.demo.model.Customer;
import org.wflux.demo.model.Order;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.math.BigDecimal;
import java.util.Map;

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
        return reactiveMongoTemplate.find(Query.query(criteria), Customer.class)
                .log();
    }

    @PostMapping("/order/create")
    public Mono<Order> createOrder(@RequestBody Order order) {
        return reactiveMongoTemplate.save(order);
    }

    @GetMapping("/sales/summary")
    public Mono<Map<String, BigDecimal>> calculateSalesSummary() {
        return reactiveMongoTemplate.findAll(Customer.class)
                .flatMap(customer -> Mono.zip(Mono.just(customer), this.calculateOrderSum(customer.getId())))
                .collectMap(t2 -> t2.getT1().toString(), Tuple2::getT2);
    }

    private Mono<BigDecimal> calculateOrderSum(final String customerId) {
        // aggregate reactive data, ie. using reduce function):
        Criteria criteria = Criteria.where("customerId").is(customerId);
        return reactiveMongoTemplate.find(Query.query(criteria), Order.class)
                .map(o -> o.getTotal().subtract(o.getDiscount()))
                // reduce to Sum of all Order Totals - Discounts:
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

package org.wflux.demo.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

// database entity for MongoDB:
@Document
@Getter
@Setter
public class Customer {
    private String id;
    private String name;
    private String job;

    public Customer(final String name, final String job) {
        this.id = UUID.randomUUID().toString();
    }
}

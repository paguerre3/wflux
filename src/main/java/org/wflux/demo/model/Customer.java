package org.wflux.demo.model;

import lombok.Getter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

// database entity for MongoDB:
@Document
@Getter
@ToString
public class Customer {
    // automatically converted to _id metadata field,
    // if a more complex field as id is required use @MongoId instead.
    @Id
    final private String id;
    private String name;
    private String job;

    protected Customer() {
        this.id = UUID.randomUUID().toString();
    }

    public Customer(final String name, final String job) {
        this();
        this.name = name;
        this.job = job;
    }
}

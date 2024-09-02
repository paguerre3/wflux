package org.wflux.demo.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

// database entity for MongoDB:
@Document
@Getter
@Setter
@ToString
public class Customer {
    // automatically converted to _id metadata field,
    // if a more complex field as id is required use @MongoId instead.
    @Id
    private String id;
    private String name;
    private String job;

    // used for queries:
    public Customer() {
        // don't set random Id here!
    }

    public Customer(final String name, final String job) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.job = job;
    }
}

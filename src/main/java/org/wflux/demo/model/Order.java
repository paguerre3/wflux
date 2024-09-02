package org.wflux.demo.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.UUID;

@Document
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Order {
    private String id;
    private String customerId;
    private BigDecimal total;
    private BigDecimal discount;

    // don't set random Id here in no-args constructor as its being used by queries.

    public Order(final String customerId, final BigDecimal total, final BigDecimal discount) {
        this.id = UUID.randomUUID().toString();
        Assert.notNull(customerId, "customerId cannot be null");
        this.customerId = customerId;
        this.total = total;
        this.discount = discount;
    }
}

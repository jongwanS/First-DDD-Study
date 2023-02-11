package chapter9.domain;

import lombok.Getter;

@Getter
public class Product {
    String name;
    ProductId id;

    public Product(String name, ProductId id) {
        this.name = name;
        this.id = id;
    }
}

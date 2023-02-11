package chapter9.domain;

import lombok.Getter;

@Getter
public class ProductId {
    String id;

    public ProductId(String id) {
        this.id = id;
    }
}

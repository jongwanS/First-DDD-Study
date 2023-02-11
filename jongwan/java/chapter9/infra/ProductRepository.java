package chapter9.infra;

import chapter9.domain.Product;
import chapter9.domain.ProductId;

public class ProductRepository {
    public Product findById(ProductId productId) {
        return new Product("테스트상품", productId);
    }
}

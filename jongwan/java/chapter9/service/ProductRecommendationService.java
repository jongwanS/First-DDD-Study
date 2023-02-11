package chapter9.service;

import chapter9.domain.Product;
import chapter9.domain.ProductId;

import java.util.List;

public interface ProductRecommendationService {
    List<Product> getRecommendationsOf(ProductId id);
}

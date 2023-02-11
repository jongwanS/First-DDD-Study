package chapter9;

import chapter9.domain.Product;
import chapter9.domain.ProductId;
import chapter9.infra.RecSystemClient;
import chapter9.service.ProductRecommendationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.testng.annotations.Test;

import java.util.List;


public class TestCode {
    private ProductRecommendationService productRecommendationService = new RecSystemClient();
    @Test
    @DisplayName("상품상세에 카탈로그 + 추천시스템, 10번상품을 전달하면 추천상품은 100,200,300 번이 온다.")
    void 상품상세_조회(){
        String PRODUCT_ID = "10";
        List<Product> products = productRecommendationService.getRecommendationsOf(new ProductId(PRODUCT_ID));
        Assertions.assertAll(
                () -> products.get(0).getId().equals("100"),
                () -> products.get(1).getId().equals("200"),
                () -> products.get(2).getId().equals("300")
        );
    }
}

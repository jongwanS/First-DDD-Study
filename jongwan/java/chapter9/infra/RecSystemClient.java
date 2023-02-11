package chapter9.infra;

import chapter9.service.ProductRecommendationService;
import chapter9.domain.recommend.RecommendationItem;
import chapter9.domain.Product;
import chapter9.domain.ProductId;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RecSystemClient implements ProductRecommendationService {

    private ProductRepository productRepository = new ProductRepository();

    @Override
    public List<Product> getRecommendationsOf(ProductId id) {
        List<RecommendationItem> items = getRecItems(id.getId());
        return toProducts(items);
    }

    private List<RecommendationItem> getRecItems(String id) {
        //외부 추천 시스템 통신결과
        return Arrays.asList(
            new RecommendationItem("100"),
                new RecommendationItem("200"),
                new RecommendationItem("300")
        );
    }

    private List<Product> toProducts(List<RecommendationItem> items){
        return items.stream()
                .map(item -> toProductId(item.getItemId()))
                .map(productId -> productRepository.findById(productId))
                .collect(Collectors.toList());
    }
    private ProductId toProductId(String itemId){
        return new ProductId(itemId);
    }
}

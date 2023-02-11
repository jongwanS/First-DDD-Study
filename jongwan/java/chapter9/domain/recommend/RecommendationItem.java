package chapter9.domain.recommend;

import lombok.Getter;

@Getter
public class RecommendationItem {
    String itemId;

    public RecommendationItem(String itemId) {
        this.itemId = itemId;
    }
}

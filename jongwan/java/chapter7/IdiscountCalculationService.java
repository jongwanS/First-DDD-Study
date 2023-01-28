package chapter7;

import java.util.List;

public interface IdiscountCalculationService {
    Money calculaeDiscountAmounts(List<OrderLine> orderLines, List<Coupon> usedCoupons, MemberGrade grade);
}

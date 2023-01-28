package chapter7;


import lombok.Builder;

import java.util.List;

@Builder
public class Order {
    private Orderer orderer;
    private List<OrderLine> orderLines;
    private List<Coupon> usedCoupons;
    private Money totalPayment;

    private Money calculateTotalAmounts() {
        int amount = 5;
        int price = 1000;
        return new Money(price).multiply(amount);
    }

    public void calculateAmounts(
        IdiscountCalculationService discountService, MemberGrade grade
    ){
        Money totalAmounts = calculateTotalAmounts();
        Money discountAmounts = discountService.calculaeDiscountAmounts(this.orderLines, this.usedCoupons, grade);
        this.totalPayment = totalAmounts.minus(discountAmounts);
    }
}

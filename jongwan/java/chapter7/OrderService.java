package chapter7;


import java.util.Arrays;

public class OrderService {
    private DiscountCalculationService discountCalculationService;

    private Order createOrder(){
        Order order = Order.builder().orderer(Orderer.builder().build())
                .usedCoupons(Arrays.asList(new Coupon(), new Coupon()))
                .orderLines(Arrays.asList(
                    new OrderLine(new Product("상품1","001"), new Money(200),2,new Money(400)),
                    new OrderLine(new Product("상품3","003"), new Money(100),1,new Money(100))
                ))
                .build();
        order.calculateAmounts(discountCalculationService, new MemberGrade());
        return order;
    }
}

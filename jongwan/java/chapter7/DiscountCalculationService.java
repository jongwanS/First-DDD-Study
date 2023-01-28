package chapter7;

import java.util.List;

public class DiscountCalculationService implements IdiscountCalculationService{
    public Money calculaeDiscountAmounts(
            List<OrderLine> orderLines,
            List<Coupon> coupons,
            MemberGrade grade
    ){
        Money totalAmounts = calculateTotalAmounts();    //상품수량x가격
        Money couponDiscount = calculateCouponDiscount();//쿠폰할인
        Money memberDiscount = calculateDiscountGrade(); //등급할인
        return totalAmounts.minus(couponDiscount).minus(memberDiscount);
    }

    private Money calculateCouponDiscount() {
        return new Money(123);
    }
    private Money calculateDiscountGrade() {
        return new Money(123);
    }
    private Money calculateTotalAmounts() {
        return new Money(123);
    }
}

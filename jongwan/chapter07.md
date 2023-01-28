# 도메인 서비스

### 여러 애그리거트가 필요한 기능
- 예를들어 결제 금액 계산 로직이 있다.
  - 상품 애그리거트 : 구매 상품가격, 상품별 배송비
  - 주문 애그리거트 : 상품별 구매 개수
  - 쿠폰 애그리거트 : 쿠폰적용 금액 할인
  - 등급 애그리거트 : 등급적용 금액 할인

````JAVA
# 하나의 주문 책임 어그리거트 생성
- 이와같이 한 애그리거트에 다 넣었을시, 자신의 책임 범위를 넘어선다.
- 의존성이 높아진다.
- 코드를 복잡하게 만든다.

public class Order {
    private Orderer orderer;
    private List<OrderLine> orderLines;
    private List<Coupon> usedCoupons;
    private Money totalPayment;

    private void calculatePayAmounts(){
        Money totalAmounts = calculateTotalAmounts();    //상품수량x가격
        Money couponDiscount = calculateCouponDiscount();//쿠폰할인
        Money memberDiscount = calculateDiscountGrade(); //등급할인
        this.totalPayment = totalAmounts.minus(couponDiscount).minus(memberDiscount);
    }
    private Money calculateCouponDiscount() {
        //복잡한 로직 ...
        return new Money(123);
    }
    private Money calculateDiscountGrade() {
        //복잡한 로직 ...
        return new Money(123);
    }
    private Money calculateTotalAmounts() {
        return new Money(123);
    }
}
````

### 도메인 서비스
- 도메인 서비스는 도메인 영역에 위치한 도메인 로직을 표현할 때 사용한다.
  - `계산 로직` : 여러 애그리거트가 필요한 계산로직. 
  - `외부 시스템 연동 필요 로직` : 타 시스템 연동 도메인 로직

#### 계산 로직과 도메인 서비스
- 할인 금액 규칙계산 같은 한 애그리거트에 넣기 애매한 것은 `도메인 서비스`를 별도로 둔다.
- `도메인 서비스`는 도메인과 다르게 `상태 없이 로직만 구현`한다.
- 사용법
  - 애그리거트 메서드에게 서비스 인자 전달 (애그리거트 -> 서비스 기능 실행)
  - 도메인 서비스 기능을 실행(서비스 -> 애그리거트 기능 실행)
````JAVA
※ 애그리거트 메서드에게 서비스 인자 전달방식

# 메인 서비스
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
    //service를 주입하여 가격 계산을 진행
    //할인정책이 변경되었을경우, DiscountCalculationService를 수정하면 된다.
    order.calculateAmounts(discountCalculationService, new MemberGrade());
    return order;
  }
}

# 애그리거트
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
        DiscountCalculationService discountService, //service 주입 
        MemberGrade grade
    ){
        Money totalAmounts = calculateTotalAmounts();
        //discountService 도메인 서비스 사용부분
        Money discountAmounts = discountService.calculaeDiscountAmounts(this.orderLines, this.usedCoupons, grade);
        this.totalPayment = totalAmounts.minus(discountAmounts);
    }
}

# 도메인 서비스
 > 상태 없이 로직만 구현
public class DiscountCalculationService {
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
````
````JAVA
※ 도메인 서비스 기능을 실행(서비스 -> 애그리거트 기능 실행)
public class TransferService{
    public void transfer(Account from, Account to, Money amount){
        from.withdraw(amount);  //계좌 출금
        to.credit(amount);      //다른사람에게 이체
    }
}
````
#### 외부 시스템 연동과 도메인 서비스
- 만약 `도메인 서비스`와 `외부 시스템과 연동`을 해야할 경우에는 인터페이스를 통해, 외부 시스템에 접근하자.

#### 도메인 서비스의 패키지 위치
- `도메인 서비스`의 위치는 해당 기능을 사용하는 애그리거트와 같은 패키지에 위치 시킨다.

#### 도메인 서비스의 인터페이스와 클래스
- 도메인 서비스의 로직이 고정되어 있지 않은 경우, 도메인 서비스를 인터페이스로 구현, 구현 클래스를 둔다.
- 도메인이 특정 구현에 종속되는 것을 방지한다.
````JAVA
public interface IdiscountCalculationService {
  Money calculaeDiscountAmounts(List<OrderLine> orderLines, List<Coupon> usedCoupons, MemberGrade grade);
}
public class DiscountCalculationService implements IdiscountCalculationService{
    @Override
    public Money calculaeDiscountAmounts(
            List<OrderLine> orderLines,
            List<Coupon> coupons,
            MemberGrade grade
    ){
      //.....
      return totalAmounts.minus(couponDiscount).minus(memberDiscount);
    }
}
public class Order {
  private Orderer orderer;
  private List<OrderLine> orderLines;
  ...

  public void calculateAmounts(
          IdiscountCalculationService discountService, MemberGrade grade
  ){
    Money totalAmounts = calculateTotalAmounts();
    //interface 실행
    Money discountAmounts = discountService.calculaeDiscountAmounts(this.orderLines, this.usedCoupons, grade);
    this.totalPayment = totalAmounts.minus(discountAmounts);
  }
}
````
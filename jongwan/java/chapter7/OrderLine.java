package chapter7;

import lombok.Getter;

@Getter
public class OrderLine {
    private Product product;
    private Money price;
    private int quantity;
    private Money amounts;

    public OrderLine(Product product, Money price, int quantity, Money amounts) {
        this.product = product;
        this.price = new Money(price.getValue());
        this.quantity = quantity;
        this.amounts = calculateAmounts();
    }

    private Money calculateAmounts() {
        return price.multiply(quantity);
    }

    public int getAmounts() {
        return this.amounts.getValue();
    }

    public void verifyValidProduct(Product product) {
        if(product.name.equals("notOrderAbleProduct")){
            throw new IllegalArgumentException("못사는 상품이에요");
        }
    }
}

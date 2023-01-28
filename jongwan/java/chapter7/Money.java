package chapter7;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Money {
    private int value;
    public Money(int value) {
        this.value = value;
    }

    public Money add(Money money){
        return new Money(this.value + money.value);
    }

    public Money multiply(int multiplier){
        return new Money(this.value * multiplier);
    }
    public Money minus(Money money){
        return new Money(this.value-money.value);
    }
}

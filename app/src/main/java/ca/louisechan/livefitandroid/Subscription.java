package ca.louisechan.livefitandroid;

import java.io.Serializable;

public class Subscription implements Serializable {
    private double months;
    private double discount;
    private double price;

    public Subscription(double months, double discount, double price) {
        this.months = months;
        this.discount = discount;
        this.price = price;
    }

    public double getMonths() {
        return months;
    }

    public void setMonths(double months) {
        this.months = months;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}

package ca.louisechan.livefitandroid;

import java.io.Serializable;
import java.time.LocalDate;

public class Order implements Serializable {
    private String orderCode;
    private String dateTime;
    private String customerName;
    private String customerEmail;
    private String mealPackage;
    private int subscriptionType;
    private String amountPaid;

    public Order(String orderCode, String dateTime, String customerName, String customerEmail, String mealPackage, int subscriptionType, String amountPaid) {
        this.orderCode = orderCode;
        this.dateTime = dateTime;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.mealPackage = mealPackage;
        this.subscriptionType = subscriptionType;
        this.amountPaid = amountPaid;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getMealPackage() {
        return mealPackage;
    }

    public void setMealPackage(String mealPackage) {
        this.mealPackage = mealPackage;
    }

    public int getSubscriptionType() {
        return subscriptionType;
    }

    public void setSubscriptionType(int subscriptionType) {
        this.subscriptionType = subscriptionType;
    }

    public String getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(String amountPaid) {
        this.amountPaid = amountPaid;
    }
}

package model;

import java.time.LocalDateTime;

public class Payment {
    private int paymentId;
    private int orderId;
    private double amount;
    private LocalDateTime paymentDate;
    private String paymentMethod;
    private String comment;

    public int getPaymentId() { return paymentId; }
    public void setPaymentId(int paymentId) { this.paymentId = paymentId; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public LocalDateTime getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}

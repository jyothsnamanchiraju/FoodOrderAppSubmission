package com.upgrad.FoodOrderingApp.service.entity;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name="orders")
@NamedQueries({
        @NamedQuery(name = "ordersByCustomer", query = "SELECT q FROM OrderEntity q WHERE q.customer = :customerId ORDER BY q.date desc "),
        @NamedQuery(name = "ordersByRestaurant", query = "select q from OrderEntity q where q.restaurant = :restaurant"),
})
public class OrderEntity implements Serializable {

    @Id
    @Column(name="id")                  //id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "uuid")              //uuid
    @NotNull
    @Size(max = 200)
    private String uuid;

    @Column(name="bill")
    @NotNull
    private Double bill;

    @ManyToOne
    @JoinColumn(name="coupon_id")
    private CouponEntity coupon;

    @Column(name="discount")
    private Double discount;

    @Column(name = "date")
    @NotNull
    private Date date;

    @OneToOne
    @JoinColumn(name="payment_id")
    private PaymentEntity payment;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name="customer_id")
    @NotNull
    private CustomerEntity customer;

    @ManyToOne
    @JoinColumn(name="address_id")
    @NotNull
    private AddressEntity address;

    @ManyToOne
    @JoinColumn(name="restaurant_id")
    @NotNull
    private RestaurantEntity restaurant;

    public OrderEntity() {

    }

    public OrderEntity(@NotNull @Size(max = 200) String uuid, @NotNull Double bill, CouponEntity coupon, @NotNull Double discount, @NotNull Date date, @NotNull PaymentEntity payment, @NotNull CustomerEntity customer, @NotNull AddressEntity address, RestaurantEntity restaurant) {
        this.uuid = uuid;
        this.bill = new Double(bill);
        this.coupon = coupon;
        this.discount = new Double(discount);
        this.date = date;
        this.payment = payment;
        this.customer = customer;
        this.address = address;
        this.restaurant = restaurant;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Double getBill() {
        return bill;
    }

    public void setBill(Double bill) {
        this.bill = bill;
    }

    public CouponEntity getCoupon() {
        return coupon;
    }

    public void setCoupon(CouponEntity coupon) {
        this.coupon = coupon;
    }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public PaymentEntity getPayment() {
        return payment;
    }

    public void setPayment(PaymentEntity payment) {
        this.payment = payment;
    }

    public CustomerEntity getCustomer() {
        return customer;
    }

    public void setCustomer(CustomerEntity customer) {
        this.customer = customer;
    }

    public AddressEntity getAddress() {
        return address;
    }

    public void setAddress(AddressEntity address) {
        this.address = address;
    }

    public RestaurantEntity getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(RestaurantEntity restaurant) {
        this.restaurant = restaurant;
    }
}

package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.*;
import com.upgrad.FoodOrderingApp.service.entity.*;
import com.upgrad.FoodOrderingApp.service.exception.AddressNotFoundException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.CouponNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private CustomerDao customerDao;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private AddressDao addressDao;

    @Autowired
    private CustomerAddressDao customerAddressDao;

    @Autowired
    private OrderItemDao orderItemDao;

    public CouponEntity getCouponByCouponName(final String couponName) throws CouponNotFoundException {
        if (couponName.equals("")) {
            throw new CouponNotFoundException("CPF-002", "Coupon name field should not be empty");
        }
        CouponEntity couponEntity = orderDao.getCouponByCouponName(couponName);
        if (couponEntity == null) {
            throw new CouponNotFoundException("CPF-001", "No coupon by this name");
        }
        return couponEntity;
    }

    public List<OrdersEntity> getOrdersByCustomers(String customerId) {
        List<OrdersEntity> orderEntityList = new ArrayList<>();
        for (OrdersEntity orderEntity : orderDao.getOrdersByCustomers(customerDao.getCustomerByUUID(customerId))) {
            orderEntityList.add(orderEntity);
        }
        return orderEntityList;
    }

    public CouponEntity getCouponByCouponId(String uuid) throws CouponNotFoundException {
        CouponEntity couponEntity = orderDao.getCouponByCouponUUID(uuid);

        if (couponEntity == null) {
            throw new CouponNotFoundException("CPF-001", "No coupon by this id");
        }
        return couponEntity;
    }

    //Creating/Saving new order by customer
    @Transactional(propagation = Propagation.REQUIRED)
    public OrdersEntity saveOrder(OrdersEntity orderEntity) {
        return orderDao.createOrder(orderEntity);
    }

    //Save items included in an order
    @Transactional(propagation = Propagation.REQUIRED)
    public OrderItemEntity saveOrderItem(OrderItemEntity orderItemEntity) {
        return orderItemDao.createOrderItemEntity(orderItemEntity);
    }
}

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

    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerEntity authenticateByAccessToken(final String accessToken) throws AuthorizationFailedException {
        CustomerAuthEntity customerAuthEntity = customerDao.getCustomerAuthByToken(accessToken);

        if(customerAuthEntity == null) {
            throw new AuthorizationFailedException("ATHR-001","Customer is not Logged in.");
        }
        if(customerAuthEntity.getLogoutAt() != null) {
            throw new AuthorizationFailedException("ATHR-002","Customer is logged out. Log in again to access this endpoint.");
        }

        final ZonedDateTime now = ZonedDateTime.now();

        if((customerAuthEntity.getExpiresAt().compareTo(now)) < 0){
            throw new AuthorizationFailedException("ATHR-003","Your session is expired. Log in again to access this endpoint.");
        }

        return customerAuthEntity.getCustomer();
    }

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

    public List<OrdersEntity> getOrdersByCustomers(CustomerEntity customerEntity) {
        List<OrdersEntity> orderEntityList = new ArrayList<>();
        for (OrdersEntity orderEntity : orderDao.getOrdersByCustomers(customerEntity)) {
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

    public AddressEntity getAddressByUUID(final String addressUuid, final CustomerEntity customerEntity)
            throws AuthorizationFailedException, AddressNotFoundException {

        AddressEntity addressEntity = addressDao.getAddressByUuid(addressUuid);
      //  AddressEntity addressEntity = addressDao.getAddressByAddressUuid(addressUuid);
        //if address id is incorrect and no such address exist in database
        if(addressEntity == null){
            throw new AddressNotFoundException("ANF-003", "No address by this id");
        }
        else {
            final CustomerAddressEntity customerAddressEntity = customerAddressDao.getCustomerAddressByAddressId(addressEntity);
            final CustomerEntity belongsToAddressEntity = customerAddressEntity.getCustomer();
            if(!belongsToAddressEntity.getUuid().equals(customerEntity.getUuid())){
                throw new AuthorizationFailedException("ATHR-004", "You are not authorized to view/update/delete any one else's address ");
            }
            else {
                return addressEntity;
            }
        }
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

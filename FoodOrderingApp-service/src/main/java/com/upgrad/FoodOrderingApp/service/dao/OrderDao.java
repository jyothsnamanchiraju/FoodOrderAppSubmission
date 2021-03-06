package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.CouponEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.entity.OrderEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class OrderDao {

    @PersistenceContext
    private EntityManager entityManager;

    public CouponEntity getCouponByCouponName(final String couponName) {
        try {
            return entityManager.createNamedQuery("couponByCouponName", CouponEntity.class).setParameter("couponName", couponName).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    public List<OrderEntity> getOrdersByCustomers(CustomerEntity customerEntity) {
        try {
            return entityManager.createNamedQuery("ordersByCustomer", OrderEntity.class).setParameter("customerId", customerEntity).getResultList();
        } catch (NoResultException nre) {
            return null;
        }
    }

    //Get coupon by coupon UUID
    public CouponEntity getCouponByCouponUUID(String uuid) {
        try {
            return entityManager.createNamedQuery("couponByUUID", CouponEntity.class).setParameter("uuid", uuid).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    //Creating/Saving new order
    public OrderEntity createOrder(OrderEntity orderEntity) {
        entityManager.persist(orderEntity);
        return orderEntity;
    }
}

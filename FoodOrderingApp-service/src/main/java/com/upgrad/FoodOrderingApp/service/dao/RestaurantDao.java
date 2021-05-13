package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class RestaurantDao {

    @PersistenceContext
    private EntityManager entityManager;

    public RestaurantEntity getRestaurantById(String restaurantId) {
        try{
            return (RestaurantEntity) entityManager.createNativeQuery("select r.* from restaurant r where r.uuid = ?", RestaurantEntity.class)
                    .setParameter(1, restaurantId)
                    .getSingleResult();
        }
        catch(NoResultException exception){
            return null;
        }
    }

    public List<RestaurantEntity> getRestaurantsByName(String restaurantName){
        restaurantName="%"+restaurantName+"%";

        try{
            return entityManager.createNativeQuery("select r.* from restaurant r where LOWER(r.restaurant_name) similar to Lower(?);", RestaurantEntity.class)
                    .setParameter(1, restaurantName)
                    .getResultList();
        }
        catch (NoResultException exception){
            return null;
        }
    }


    public List<RestaurantEntity> getAllRestaurant() {
        try{
            return entityManager.createNativeQuery("select r.* from restaurant r;", RestaurantEntity.class)
                    .getResultList();
        }
        catch(NoResultException exception){
            return null;
        }
    }

    public StateEntity getState(Integer id) {
        try{
            return (StateEntity) entityManager.createNativeQuery("select s.* from state s inner join (select ad.* from address ad where ad.id = ?) address on s.id = address.state_id;", StateEntity.class)
                    .setParameter(1, id)
                    .getSingleResult();
        }
        catch(NoResultException exception){
            return null;
        }
    }


    public AddressEntity getAddress(Integer id) {

        try{
            return (AddressEntity) entityManager.createNativeQuery("select a.* from address a inner join (select r.* from restaurant r where r.id = ?) ra on a.id = ra.address_id;", AddressEntity.class)
                    .setParameter(1, id)
                    .getSingleResult();
        }
        catch(NoResultException exception){
            return null;
        }
    }

    public List<RestaurantEntity> getByCategory(String categoryId){
        try{
            return entityManager.createNativeQuery("select r.* from restaurant r inner join (select cr.* from restaurant_category cr where cr.category_id = (select c.id from category c where c.uuid= ?)) rc on r.id = rc.restaurant_id;", RestaurantEntity.class)
                    .setParameter(1, categoryId)
                    .getResultList();
        }
        catch (NoResultException exception){
            return null;
        }
    }

    public CategoryEntity checkCategory(String categoryId) {
        try{
            return (CategoryEntity) entityManager.createNativeQuery("select c.* from category c where c.uuid = ?;", CategoryEntity.class).setParameter(1, categoryId).getSingleResult();
        }
        catch (NoResultException exception){
            return null;
        }
    }

    public List<ItemEntity> getItemsOnCategoryForRestaurant(Integer restaurantId, Integer categoryId) {
        try{
            return entityManager.createNativeQuery("select i.* from item i inner join (select i3.item_id from (select ci.item_id from category_item ci where ci.category_id = :category) i3 inner join (select ri.item_id from restaurant_item ri where ri.restaurant_id = :restaurant) i4 on i3.item_id = i4.item_id) i2 on i.id=i2.item_id;", ItemEntity.class)
                    .setParameter("category", categoryId)
                    .setParameter("restaurant", restaurantId)
                    .getResultList();
        }
        catch(NoResultException exception){
            return null;
        }
    }

    public CustomerAuthEntity authoriseUser(String authorization) {
        try{
            return (CustomerAuthEntity) entityManager.createNativeQuery("select c.* from customer_auth c where c.access_token = ?;", CustomerAuthEntity.class)
                    .setParameter(1, authorization)
                    .getSingleResult();
        }
        catch(NoResultException exception){
            return null;
        }
    }

    public CustomerAuthEntity authoriseUserLogout(String authorization) {
        try {
            return (CustomerAuthEntity) entityManager.createNativeQuery("select c.* from customer_auth c where c.access_token = ? and c.logout_at is null;", CustomerAuthEntity.class)
                    .setParameter(1, authorization)
                    .getSingleResult();
        }
        catch(NoResultException exception){
            return null;
        }
    }

    public CustomerAuthEntity authoriseUserSession(String authorization) {
        try{
            return (CustomerAuthEntity) entityManager.createNativeQuery("select c.* from customer_auth c where c.access_token = ? and c.expires_at>now();", CustomerAuthEntity.class)
                    .setParameter(1, authorization)
                    .getSingleResult();
        }
        catch(NoResultException exception){
            return null;
        }
    }

    @Transactional
    public RestaurantEntity updateRatings(RestaurantEntity restaurant) {
        try{
            entityManager.merge(restaurant);
            return restaurant;
        }
        catch(NoResultException exception){
            return null;
        }
    }

    //Return restaurant details by restaurant UUID
    public RestaurantEntity getRestaurantByUUID(String uuid) {
        try {
            return entityManager.createNamedQuery("restaurantByUUID", RestaurantEntity.class).setParameter("uuid", uuid).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }
}

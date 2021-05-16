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

    //Return restaurant list sorted based on customer rating
    public List<RestaurantEntity> restaurantsByRating() {
        try {
            return entityManager.createNamedQuery("allRestaurantsByRating", RestaurantEntity.class).getResultList();
        } catch (NoResultException nre) {
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

    // get restaurants by category ID
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

    // update restaurant ratings
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

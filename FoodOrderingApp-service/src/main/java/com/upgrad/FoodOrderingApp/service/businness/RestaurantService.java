package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.RestaurantDao;
import com.upgrad.FoodOrderingApp.service.entity.*;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.CategoryNotFoundException;
import com.upgrad.FoodOrderingApp.service.exception.RestaurantNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

@Service
public class RestaurantService {

    @Autowired
    private RestaurantDao restaurantDao;

    public List<RestaurantEntity> getAllRestaurants(){
        return restaurantDao.getAllRestaurant();
    }

    public List<RestaurantEntity> getRestaurantsByName(String restaurantName){
        return restaurantDao.getRestaurantsByName(restaurantName);
    }


    public StateEntity getRestaurantState(Integer id) {
        return restaurantDao.getState(id);
    }

    public AddressEntity getRestaurantAddress(Integer id) {
        return restaurantDao.getAddress(id);
    }

    public List<RestaurantEntity> getRestaurantByCategory(String categoryId) throws CategoryNotFoundException {

        if(restaurantDao.checkCategory(categoryId)==null){
            throw new CategoryNotFoundException("CNF-002","No category by this id");
        }

        List<RestaurantEntity> result = restaurantDao.getByCategory(categoryId);

        return result;
    }

    public RestaurantEntity getrestaurantById(String restaurantId) throws RestaurantNotFoundException {
        RestaurantEntity result = restaurantDao.getRestaurantById(restaurantId);

        if(result==null){
            throw new RestaurantNotFoundException("RNF-001", "No restaurant by this id");
        }

        return result;
    }

    public List<ItemEntity> getItemsOnCategory(Integer restaurantId, Integer categoryId) {
        return restaurantDao.getItemsOnCategoryForRestaurant(restaurantId, categoryId);
    }

    public void authorize(String authorization) throws AuthorizationFailedException {
        
        CustomerAuthEntity authorizedUser = restaurantDao.authoriseUser(authorization);

        if(authorizedUser==null){
            throw new AuthorizationFailedException("ATHR-001", "Customer is not Logged in.");
        }
       // else if(restaurantDao.authoriseUserLogout(authorization)==null){
        else if(authorizedUser.getLogoutAt() != null){
            throw new AuthorizationFailedException("ATHR-002", "Customer is logged out. Log in again to access this endpoint.");
        }
        //else if(restaurantDao.authoriseUserSession(authorization)==null){
        else if((authorizedUser.getExpiresAt().compareTo(ZonedDateTime.now())) < 0){
            throw new AuthorizationFailedException("ATHR-003", "Your session is expired. Log in again to access this endpoint.");
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public RestaurantEntity updateNewratingsForRestaurant(RestaurantEntity restaurant) {
        return restaurantDao.updateRatings(restaurant);
    }

    //Display restaurant details by restaurant UUID
    public RestaurantEntity restaurantByUUID(String uuid) throws RestaurantNotFoundException {
        if (uuid.equals("")) {
            throw new RestaurantNotFoundException("RNF-002", "Restaurant id field should not be empty");
        }

        RestaurantEntity restaurantEntity = restaurantDao.getRestaurantByUUID(uuid);

        if (restaurantEntity == null) {
            throw new RestaurantNotFoundException("RNF-001", "No restaurant by this id");
        }
        return restaurantEntity;
    }
}

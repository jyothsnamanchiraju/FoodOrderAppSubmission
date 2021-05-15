package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.RestaurantDao;
import com.upgrad.FoodOrderingApp.service.entity.*;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.CategoryNotFoundException;
import com.upgrad.FoodOrderingApp.service.exception.InvalidRatingException;
import com.upgrad.FoodOrderingApp.service.exception.RestaurantNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RestaurantService {

    @Autowired
    private RestaurantDao restaurantDao;

    public List<RestaurantEntity> getAllRestaurants(){
        return restaurantDao.getAllRestaurant();
    }

    public List<RestaurantEntity> restaurantsByName(String restaurantName) throws RestaurantNotFoundException {

        if(restaurantName.equals("")) {
            throw new RestaurantNotFoundException("RNF-003", "Restaurant name field should not be empty");
        }
        return restaurantDao.getRestaurantsByName(restaurantName);
    }


    public StateEntity getRestaurantState(Integer id) {
        return restaurantDao.getState(id);
    }

    public AddressEntity getRestaurantAddress(Integer id) {
        return restaurantDao.getAddress(id);
    }

    public List<RestaurantEntity> restaurantByCategory(String categoryId) throws CategoryNotFoundException {

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
        else if(restaurantDao.authoriseUserLogout(authorization)==null){
            throw new AuthorizationFailedException("ATHR-002", "Customer is logged out. Log in again to access this endpoint.");
        }
        else if(restaurantDao.authoriseUserSession(authorization)==null){
            throw new AuthorizationFailedException("ATHR-003", "Your session is expired. Log in again to access this endpoint.");
        }
    }

    //List all restaurants sorted by rating - Descending order
    public List<RestaurantEntity> restaurantsByRating() {
        return restaurantDao.restaurantsByRating();
    }

    public RestaurantEntity updateRestaurantRating(RestaurantEntity restaurant, Double rating) throws InvalidRatingException {

        if(rating<1 || rating>5){
            throw new InvalidRatingException("IRE-001", "Restaurant should be in the range of 1 to 5");
        }

        Double ratingIndex = (restaurant.getCustomerRating()*restaurant.getNumberCustomersRated());

        restaurant.setNumberCustomersRated(restaurant.getNumberCustomersRated()+1);

        restaurant.setCustomerRating((ratingIndex+rating)/restaurant.getNumberCustomersRated());
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

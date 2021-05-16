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

    public List<RestaurantEntity> restaurantsByName(String restaurantName) throws RestaurantNotFoundException {

        if(restaurantName.equals("")) {
            throw new RestaurantNotFoundException("RNF-003", "Restaurant name field should not be empty");
        }
        return restaurantDao.getRestaurantsByName(restaurantName);
    }

    public List<RestaurantEntity> restaurantByCategory(String categoryId) throws CategoryNotFoundException {

        if(restaurantDao.checkCategory(categoryId)==null){
            throw new CategoryNotFoundException("CNF-002","No category by this id");
        }

        List<RestaurantEntity> result = restaurantDao.getByCategory(categoryId);

        return result;
    }

    //List all restaurants sorted by rating - Descending order
    public List<RestaurantEntity> restaurantsByRating() {
        return restaurantDao.restaurantsByRating();
    }

    public RestaurantEntity updateRestaurantRating(RestaurantEntity restaurant, Double CustomerRating) throws InvalidRatingException {

        if(CustomerRating<1 || CustomerRating>5){
            throw new InvalidRatingException("IRE-001", "Restaurant should be in the range of 1 to 5");
        }

        Double ratingIndex = ((restaurant.getCustomerRating()*restaurant.getNumberCustomersRated()) + CustomerRating)/
                (restaurant.getNumberCustomersRated()+1);
        ratingIndex = Math.floor(ratingIndex*100)/100;

        restaurant.setNumberCustomersRated(restaurant.getNumberCustomersRated()+1);

        restaurant.setCustomerRating(ratingIndex);
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

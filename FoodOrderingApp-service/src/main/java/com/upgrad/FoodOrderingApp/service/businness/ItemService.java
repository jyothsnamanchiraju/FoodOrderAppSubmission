package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.ItemDao;
import com.upgrad.FoodOrderingApp.service.dao.RestaurantDao;
import com.upgrad.FoodOrderingApp.service.entity.ItemEntity;
import com.upgrad.FoodOrderingApp.service.entity.RestaurantEntity;
import com.upgrad.FoodOrderingApp.service.exception.RestaurantNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemService {

    @Autowired
    private ItemDao itemDao;

    @Autowired
    private RestaurantDao restaurantDao;

    public List<ItemEntity> getItemsByCategory(Integer categoryId) {

        return itemDao.getItemsforCategory(categoryId);
    }

    public List<ItemEntity> getTopItems(String restaurantId) throws RestaurantNotFoundException {
        RestaurantEntity restaurant = restaurantDao.getRestaurantById(restaurantId);

        if(restaurant.equals(null)){
            throw new RestaurantNotFoundException("RNF-001", "No restaurant by this id");
        }

        return itemDao.getTopItems(restaurant.getId());
    }
}

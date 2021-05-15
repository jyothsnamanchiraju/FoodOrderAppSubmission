package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.*;
import com.upgrad.FoodOrderingApp.service.entity.*;
import com.upgrad.FoodOrderingApp.service.exception.ItemNotFoundException;
import com.upgrad.FoodOrderingApp.service.exception.RestaurantNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ItemService {

    @Autowired
    private ItemDao itemDao;

    @Autowired
    private RestaurantDao restaurantDao;

    @Autowired
    private OrderItemDao orderItemDao;

    @Autowired
    private CategoryDao categoryDao;

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

    public List<OrderItemEntity> getItemsByOrder(OrdersEntity orderEntity) {
        return orderItemDao.getItemsByOrder(orderEntity);
    }

    //Get item entity by item UUID
    public ItemEntity getItemByUUID(String uuid) throws ItemNotFoundException {
        ItemEntity itemEntity = itemDao.getItemByUUID(uuid);
        if (itemEntity == null) {
            throw new ItemNotFoundException("INF-003", "No item by this id exist");
        }
        return itemEntity;
    }

    //List top 5 items in a restaurant
    //Sorted by number of orders - descending
    public List<ItemEntity> getItemsByPopularity(RestaurantEntity restaurantEntity) {
        return itemDao.getTopItems(restaurantEntity.getId());
    }

    //Get items from a restaurant grouped by category
    public List<ItemEntity> getItemsByCategoryAndRestaurant(String restaurantUUID, String categoryUUID) {
        RestaurantEntity restaurantEntity = restaurantDao.getRestaurantByUUID(restaurantUUID);
        CategoryEntity categoryEntity = categoryDao.getCategoryById(categoryUUID);
        List<ItemEntity> restaurantItemEntityList = new ArrayList<ItemEntity>();

        for (ItemEntity restaurantItemEntity : restaurantEntity.getItems()) {
            for (ItemEntity categoryItemEntity : categoryEntity.getItems()) {
                if (restaurantItemEntity.getUuid().equals(categoryItemEntity.getUuid())) {
                    restaurantItemEntityList.add(restaurantItemEntity);
                }
            }
        }
        restaurantItemEntityList.sort(Comparator.comparing(ItemEntity::getItemName));

        return restaurantItemEntityList;
    }
}

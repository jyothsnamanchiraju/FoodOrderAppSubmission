package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.ItemList;
import com.upgrad.FoodOrderingApp.api.model.ItemListResponse;
import com.upgrad.FoodOrderingApp.service.businness.ItemService;
import com.upgrad.FoodOrderingApp.service.businness.RestaurantService;
import com.upgrad.FoodOrderingApp.service.entity.ItemEntity;
import com.upgrad.FoodOrderingApp.service.entity.RestaurantEntity;
import com.upgrad.FoodOrderingApp.service.exception.RestaurantNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class ItemController {

    @Autowired
    private ItemService itemService;

    @Autowired
    private RestaurantService restaurantService;

    @CrossOrigin
    @GetMapping(path = "/item/restaurant/{restaurant_id}")
    @ResponseBody
    public ResponseEntity<ItemListResponse> getTopItems(@PathVariable(value = "restaurant_id") String restaurantId) throws RestaurantNotFoundException {

        RestaurantEntity restaurantEntity = restaurantService.restaurantByUUID(restaurantId);
        List<ItemEntity> topItems = itemService.getItemsByPopularity(restaurantEntity);

        ItemListResponse response = new ItemListResponse();

        for(ItemEntity item:topItems){
            ItemList.ItemTypeEnum itemType = ItemList.ItemTypeEnum.fromValue(item.getType().equals("0")?"VEG":"NON_VEG");
            response.add(new ItemList().itemName(item.getItemName())
                    .itemType(itemType)
                    .id(UUID.fromString(item.getUuid()))
                    .price(item.getPrice())
            );
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}

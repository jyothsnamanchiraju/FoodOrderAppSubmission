package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.ItemList;
import com.upgrad.FoodOrderingApp.api.model.ItemListResponse;
import com.upgrad.FoodOrderingApp.service.businness.ItemService;
import com.upgrad.FoodOrderingApp.service.entity.ItemEntity;
import com.upgrad.FoodOrderingApp.service.exception.RestaurantNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class ItemController {

    @Autowired
    private ItemService itemService;

    @GetMapping(path = "/item/restaurant/{restaurant_id}")
    @ResponseBody
    public ResponseEntity<ItemListResponse> getTopItems(@PathVariable(value = "restaurant_id") String restaurantId) throws RestaurantNotFoundException {
        List<ItemEntity> topItems = itemService.getTopItems(restaurantId);

        ItemListResponse response = new ItemListResponse();

        for(ItemEntity item:topItems){
            ItemList.ItemTypeEnum itemType = ItemList.ItemTypeEnum.fromValue(Integer.parseInt(item.getType())==0?"VEG":"NON_VEG");
            response.add(new ItemList().itemName(item.getItemName())
                    .itemType(itemType)
                    .id(UUID.fromString(item.getUuid()))
                    .price(item.getPrice())
            );
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}

package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.CategoriesListResponse;
import com.upgrad.FoodOrderingApp.api.model.CategoryDetailsResponse;
import com.upgrad.FoodOrderingApp.api.model.CategoryListResponse;
import com.upgrad.FoodOrderingApp.api.model.ItemList;
import com.upgrad.FoodOrderingApp.service.businness.CategoryService;
import com.upgrad.FoodOrderingApp.service.businness.ItemService;
import com.upgrad.FoodOrderingApp.service.entity.CategoryEntity;
import com.upgrad.FoodOrderingApp.service.entity.ItemEntity;
import com.upgrad.FoodOrderingApp.service.exception.CategoryNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @CrossOrigin
    @GetMapping(path = "/category")
    @ResponseBody
    public ResponseEntity<CategoriesListResponse> getCategories () throws CategoryNotFoundException {

        List<CategoryEntity> result = categoryService.getAllCategoriesOrderedByName();

        CategoriesListResponse response = new CategoriesListResponse();

        for(CategoryEntity category:result){
            response.addCategoriesItem(new CategoryListResponse().id(UUID.fromString(category.getUuid())).categoryName(category.getCategoryName()));
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping(path = "/category/{category_id}")
    @ResponseBody
    public ResponseEntity<CategoryDetailsResponse> getCategoriesById (@PathVariable(value = "category_id", required = false) String categoryId) throws CategoryNotFoundException {

        if(categoryId.isEmpty() || categoryId.equals(null)){
            throw new CategoryNotFoundException("CNF-001", "Category id field should not be empty");
        }

        CategoryEntity result = categoryService.getCategoryById(categoryId);

        CategoryDetailsResponse response = new CategoryDetailsResponse()
                                            .id(UUID.fromString(result.getUuid()))
                                            .categoryName(result.getCategoryName());

        for(ItemEntity item : result.getItems()){
            ItemList.ItemTypeEnum itemType = ItemList.ItemTypeEnum.fromValue(item.getType().equals("0")?"VEG":"NON_VEG");
            response.addItemListItem(new ItemList().itemName(item.getItemName())
                    .itemType(itemType)
                    .id(UUID.fromString(item.getUuid()))
                    .price(item.getPrice())
            );
        }

        return new ResponseEntity<CategoryDetailsResponse>(response, HttpStatus.OK);
    }

}

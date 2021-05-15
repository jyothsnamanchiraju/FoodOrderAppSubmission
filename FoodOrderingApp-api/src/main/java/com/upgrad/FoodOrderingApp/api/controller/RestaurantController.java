package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.*;
import com.upgrad.FoodOrderingApp.service.businness.CategoryService;
import com.upgrad.FoodOrderingApp.service.businness.CustomerService;
import com.upgrad.FoodOrderingApp.service.businness.ItemService;
import com.upgrad.FoodOrderingApp.service.businness.RestaurantService;
import com.upgrad.FoodOrderingApp.service.entity.*;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.CategoryNotFoundException;
import com.upgrad.FoodOrderingApp.service.exception.InvalidRatingException;
import com.upgrad.FoodOrderingApp.service.exception.RestaurantNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
public class RestaurantController {

    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ItemService itemService;

    @CrossOrigin
    @GetMapping(path = "/restaurant")
    @ResponseBody
    public ResponseEntity<RestaurantListResponse> getRestaurants(){
        List<RestaurantEntity> restaurants = restaurantService.restaurantsByRating();

        RestaurantListResponse response = new RestaurantListResponse();
        for(RestaurantEntity restaurant:restaurants){

            RestaurantDetailsResponseAddressState responseAddressState = new RestaurantDetailsResponseAddressState()
                    .stateName(restaurant.getAddress().getState().getStateName())
                    .id(UUID.fromString(restaurant.getAddress().getState().getUuid()));

            RestaurantDetailsResponseAddress responseAddress = new RestaurantDetailsResponseAddress()
                    .id(UUID.fromString(restaurant.getAddress().getUuid()))
                    .city(restaurant.getAddress().getCity())
                    .flatBuildingName(restaurant.getAddress().getFlatBuilNo())
                    .locality(restaurant.getAddress().getLocality())
                    .pincode(restaurant.getAddress().getPincode())
                    .state(responseAddressState);

            List<CategoryEntity> categories = categoryService.getCategoriesByRestaurant(restaurant.getUuid());
            String categoryString= categories.stream().map(x->x.getCategoryName()).collect(Collectors.joining(","));
            response.addRestaurantsItem(
                    new RestaurantList()
                    .id(UUID.fromString(restaurant.getUuid()))
                    .restaurantName(restaurant.getRestaurantName())
                    .photoURL(restaurant.getPhotoUrl())
                    .customerRating( new BigDecimal(restaurant.getCustomerRating(), MathContext.DECIMAL64))
                    .averagePrice(restaurant.getAvgPrice())
                    .numberCustomersRated(restaurant.getNumberCustomersRated())
                    .address(responseAddress)
                    .categories(categoryString)
            );
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping(path = "/restaurant/name/{restaurant_name}")
    @ResponseBody
    public ResponseEntity<RestaurantListResponse> getRestaurantsByName(@PathVariable(value = "restaurant_name") String restaurantName) throws RestaurantNotFoundException {

        if(restaurantName.isEmpty() || restaurantName.equals(null)){
            throw new RestaurantNotFoundException("RNF-003", "Restaurant name field should not be empty");
        }

        List<RestaurantEntity> restaurants = restaurantService.restaurantsByName(restaurantName);

        RestaurantListResponse response = new RestaurantListResponse();
        for(RestaurantEntity restaurant:restaurants){

            RestaurantDetailsResponseAddressState responseAddressState = new RestaurantDetailsResponseAddressState()
                    .stateName(restaurant.getAddress().getState().getStateName())
                    .id(UUID.fromString(restaurant.getAddress().getState().getUuid()));

            RestaurantDetailsResponseAddress responseAddress = new RestaurantDetailsResponseAddress()
                    .id(UUID.fromString(restaurant.getAddress().getUuid()))
                    .city(restaurant.getAddress().getCity())
                    .flatBuildingName(restaurant.getAddress().getFlatBuilNo())
                    .locality(restaurant.getAddress().getLocality())
                    .pincode(restaurant.getAddress().getPincode())
                    .state(responseAddressState);

            List<CategoryEntity> categories = categoryService.getCategoriesByRestaurant(restaurant.getUuid());
            String categoryString= categories.stream().map(x->x.getCategoryName()).collect(Collectors.joining(","));

            response.addRestaurantsItem(
                    new RestaurantList()
                            .id(UUID.fromString(restaurant.getUuid()))
                            .restaurantName(restaurant.getRestaurantName())
                            .photoURL(restaurant.getPhotoUrl())
                            .customerRating( new BigDecimal(restaurant.getCustomerRating(), MathContext.DECIMAL64))
                            .averagePrice(restaurant.getAvgPrice())
                            .numberCustomersRated(restaurant.getNumberCustomersRated())
                            .address(responseAddress)
                            .categories(categoryString)
            );
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping(path = "/restaurant/category/{category_id}")
    @ResponseBody
    public ResponseEntity<RestaurantListResponse> getRestaurantByCategory(@PathVariable(value = "category_id") String categoryId) throws CategoryNotFoundException {

        if(categoryId.isEmpty() || categoryId.equals(null)){
            throw new CategoryNotFoundException("CNF-001", "Category id field should not be empty");
        }

        List<RestaurantEntity> restaurants = restaurantService.restaurantByCategory(categoryId);

        RestaurantListResponse response = new RestaurantListResponse();
        for(RestaurantEntity restaurant:restaurants){

            RestaurantDetailsResponseAddressState responseAddressState = new RestaurantDetailsResponseAddressState()
                    .stateName(restaurant.getAddress().getState().getStateName())
                    .id(UUID.fromString(restaurant.getAddress().getState().getUuid()));

            RestaurantDetailsResponseAddress responseAddress = new RestaurantDetailsResponseAddress()
                    .id(UUID.fromString(restaurant.getAddress().getUuid()))
                    .city(restaurant.getAddress().getCity())
                    .flatBuildingName(restaurant.getAddress().getFlatBuilNo())
                    .locality(restaurant.getAddress().getLocality())
                    .pincode(restaurant.getAddress().getPincode())
                    .state(responseAddressState);

            List<CategoryEntity> categories = categoryService.getCategoriesByRestaurant(restaurant.getUuid());
            String categoryString= categories.stream().map(x->x.getCategoryName()).collect(Collectors.joining(","));

            response.addRestaurantsItem(
                    new RestaurantList()
                            .id(UUID.fromString(restaurant.getUuid()))
                            .restaurantName(restaurant.getRestaurantName())
                            .photoURL(restaurant.getPhotoUrl())
                            .customerRating( new BigDecimal(restaurant.getCustomerRating(), MathContext.DECIMAL64))
                            .averagePrice(restaurant.getAvgPrice())
                            .numberCustomersRated(restaurant.getNumberCustomersRated())
                            .address(responseAddress)
                            .categories(categoryString)
            );
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping(path = "/api/restaurant/{restaurant_id}")
    @ResponseBody
    public ResponseEntity<RestaurantDetailsResponse> getRestaurantById(@PathVariable(value = "restaurant_id", required = false) String restaurantId) throws RestaurantNotFoundException {

        if(restaurantId.isEmpty() || restaurantId.equals(null)){
            throw new RestaurantNotFoundException("RNF-002", "Restaurant id field should not be empty");
        }

        RestaurantEntity restaurant = restaurantService.restaurantByUUID(restaurantId);

        RestaurantDetailsResponseAddressState responseAddressState = new RestaurantDetailsResponseAddressState()
                .stateName(restaurant.getAddress().getState().getStateName())
                .id(UUID.fromString(restaurant.getAddress().getState().getUuid()));

        RestaurantDetailsResponseAddress responseAddress = new RestaurantDetailsResponseAddress()
                .id(UUID.fromString(restaurant.getAddress().getUuid()))
                .city(restaurant.getAddress().getCity())
                .flatBuildingName(restaurant.getAddress().getFlatBuilNo())
                .locality(restaurant.getAddress().getLocality())
                .pincode(restaurant.getAddress().getPincode())
                .state(responseAddressState);

        RestaurantDetailsResponse response = new RestaurantDetailsResponse()
                .id(UUID.fromString(restaurant.getUuid()))
                .restaurantName(restaurant.getRestaurantName())
                .photoURL(restaurant.getPhotoUrl())
                .customerRating( new BigDecimal(restaurant.getCustomerRating(), MathContext.DECIMAL64))
                .averagePrice(restaurant.getAvgPrice())
                .numberCustomersRated(restaurant.getNumberCustomersRated())
                .address(responseAddress);

        List<CategoryEntity> categories = categoryService.getCategoriesByRestaurant(restaurantId);

        for(CategoryEntity category:categories) {
            List<ItemEntity> items = itemService.getItemsByCategoryAndRestaurant(restaurantId, category.getUuid());
            CategoryList categoryList = new CategoryList()
                    .id(UUID.fromString(category.getUuid()))
                    .categoryName(category.getCategoryName());
            for(ItemEntity item:items){

                categoryList.addItemListItem(new ItemList()
                        .id(UUID.fromString(item.getUuid()))
                        .itemType(ItemList.ItemTypeEnum.fromValue(item.getType().equals("0") ? "VEG":"NON_VEG"))
                        .price(item.getPrice())
                        .itemName(item.getItemName()));
            }
            response.addCategoriesItem(categoryList);
        }

        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    @CrossOrigin
    @PutMapping(path = "/api/restaurant/{restaurant_id}")
    @ResponseBody
    public ResponseEntity<RestaurantUpdatedResponse> updateRestaurant(
            @RequestHeader("authorization") final String authorization,
            @PathVariable(value = "restaurant_id", required = false) String restaurantId,
            @RequestParam(value = "customer_rating") Double rating)
            throws AuthorizationFailedException, RestaurantNotFoundException, InvalidRatingException {

        String[] auth = authorization.split(" ");
        customerService.getCustomer(auth[1]);

        if(restaurantId.isEmpty() || restaurantId.equals(null)){
            throw new RestaurantNotFoundException("RNF-002", "Restaurant id field should not be empty");
        }

        RestaurantEntity restaurant = restaurantService.restaurantByUUID(restaurantId);

        RestaurantEntity result = restaurantService.updateRestaurantRating(restaurant, rating);

        RestaurantUpdatedResponse response = new RestaurantUpdatedResponse()
                .id(UUID.fromString(restaurantId))
                .status("RESTAURANT RATING UPDATED SUCCESSFULLY");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}

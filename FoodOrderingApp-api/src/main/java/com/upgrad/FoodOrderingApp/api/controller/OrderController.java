package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.*;
import com.upgrad.FoodOrderingApp.service.businness.*;
import com.upgrad.FoodOrderingApp.service.entity.*;
import com.upgrad.FoodOrderingApp.service.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private AddressService addressService;

    //Get coupon by coupon name
    @CrossOrigin
    @RequestMapping(method = RequestMethod.GET, path = "/order/coupon/{coupon_name}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CouponDetailsResponse> getCouponByCouponName(
            @PathVariable("coupon_name") final String couponName,
            @RequestHeader("authorization") final String authorization)
            throws AuthorizationFailedException, CouponNotFoundException
    {
        if (couponName.equals("")) {
            throw new CouponNotFoundException("CPF-002", "Coupon name field should not be empty");
        }

        String[] bearerToken = authorization.split("Bearer "); //splitting authorization string to get access token
        CustomerEntity customerEntity = customerService.getCustomer(bearerToken[1]);

        CouponEntity couponEntity = orderService.getCouponByCouponName(couponName);

        CouponDetailsResponse couponDetailsResponse = new CouponDetailsResponse()
                .id(UUID.fromString(couponEntity.getUuid()))
                .couponName(couponEntity.getCouponName())
                .percent(couponEntity.getPercent());

        return new ResponseEntity<CouponDetailsResponse>(couponDetailsResponse, HttpStatus.OK);
    }

    // API to get all the orders placed by a customer
    @CrossOrigin
    @RequestMapping(method = RequestMethod.GET, path = "/order", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CustomerOrderResponse> getCustomerOrders(
            @RequestHeader("authorization") final String authorization)
            throws AuthorizationFailedException {
        String[] bearerToken = authorization.split("Bearer "); //splitting authorization string to get access token
        CustomerEntity customerEntity = customerService.getCustomer(bearerToken[1]);

        // Get all orders by customer
        List<OrderEntity> orderEntityList = orderService.getOrdersByCustomers(customerEntity.getUuid());

        CustomerOrderResponse customerOrderResponse = new CustomerOrderResponse();

        if (orderEntityList != null) {

            for (OrderEntity orderEntity : orderEntityList) { // loop through all the order entities
                OrderListCoupon orderListCoupon = null;

                /* Handling null value for coupon_id
                   Included this logic considering that not all orders will have a coupon_id
                 */
                if (orderEntity.getCoupon() == null) {
                    orderListCoupon = new OrderListCoupon()
                            .id(null)
                            .couponName(null)
                            .percent(null);
                } else {
                    orderListCoupon = new OrderListCoupon()
                            .id(UUID.fromString(orderEntity.getCoupon().getUuid()))
                            .couponName(orderEntity.getCoupon().getCouponName())
                            .percent(orderEntity.getCoupon().getPercent());
                }

                OrderListCustomer orderListCustomer = new OrderListCustomer()
                        .id(UUID.fromString(orderEntity.getCustomer().getUuid()))
                        .firstName(orderEntity.getCustomer().getFirstName())
                        .lastName(orderEntity.getCustomer().getLastName())
                        .emailAddress(orderEntity.getCustomer().getEmail())
                        .contactNumber(orderEntity.getCustomer().getContactNumber());

                OrderListAddressState orderListAddressState = new OrderListAddressState()
                        .id(UUID.fromString(orderEntity.getAddress().getState().getUuid()))
                        .stateName(orderEntity.getAddress().getState().getStateName());

                OrderListPayment orderListPayment = new OrderListPayment()
                        .id(UUID.fromString(orderEntity.getPayment().getUuid()))
                        .paymentName(orderEntity.getPayment().getPaymentName());

                OrderListAddress orderListAddress = new OrderListAddress()
                        .id(UUID.fromString(orderEntity.getAddress().getUuid()))
                        .flatBuildingName(orderEntity.getAddress().getFlatBuilNo())
                        .locality(orderEntity.getAddress().getLocality())
                        .city(orderEntity.getAddress().getCity())
                        .pincode(orderEntity.getAddress().getPincode())
                        .state(orderListAddressState);

                OrderList orderList = new OrderList()
                        .id(UUID.fromString(orderEntity.getUuid()))
                        .bill(new BigDecimal(orderEntity.getBill()))
                        .coupon(orderListCoupon)
                        .discount(new BigDecimal(orderEntity.getDiscount()))
                        .date(orderEntity.getDate().toString())
                        .payment(orderListPayment)
                        .customer(orderListCustomer)
                        .address(orderListAddress);

                for (OrderItemEntity orderItemEntity : itemService.getItemsByOrder(orderEntity)) {

                    ItemQuantityResponseItem itemQuantityResponseItem = new ItemQuantityResponseItem()
                            .id(UUID.fromString(orderItemEntity.getItem().getUuid()))
                            .itemName(orderItemEntity.getItem().getItemName())
                            .itemPrice(orderItemEntity.getItem().getPrice())
                            .type(ItemQuantityResponseItem.TypeEnum.fromValue(Integer.parseInt(orderItemEntity.getItem().getType()) == 0? "VEG": "NON_VEG"));

                    ItemQuantityResponse itemQuantityResponse = new ItemQuantityResponse()
                            .item(itemQuantityResponseItem)
                            .quantity(orderItemEntity.getQuantity())
                            .price(orderItemEntity.getPrice());

                    orderList.addItemQuantitiesItem(itemQuantityResponse);
                }

                customerOrderResponse.addOrdersItem(orderList);
            }
        }
        return new ResponseEntity<CustomerOrderResponse>(customerOrderResponse, HttpStatus.OK);
    }

    //Creating/Saving new order by customer
    @CrossOrigin
    @RequestMapping(method = RequestMethod.POST, path = "/order", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SaveOrderResponse> saveOrder(
            @RequestBody final SaveOrderRequest saveOrderRequest,
            @RequestHeader("authorization") final String authorization)
            throws AuthorizationFailedException, CouponNotFoundException,
            AddressNotFoundException, PaymentMethodNotFoundException,
            RestaurantNotFoundException, ItemNotFoundException, SaveOrderException {

        String[] bearerToken = authorization.split("Bearer "); //splitting authorization string to get access token
        CustomerEntity customerEntity = customerService.getCustomer(bearerToken[1]);

        /* Ensuring no fields are empty except coupon_id and discount
         * Included this logic considering that not all orders will have a coupon_id and discount
         * but other values are required
        */
        if(saveOrderRequest.getAddressId() == null
                || saveOrderRequest.getItemQuantities().get(0).getItemId() == null
                || saveOrderRequest.getItemQuantities().get(0).getPrice() == null
                || saveOrderRequest.getItemQuantities().get(0).getQuantity() == null
                || saveOrderRequest.getRestaurantId() == null
                || saveOrderRequest.getPaymentId() == null
                || saveOrderRequest.getBill() == null){
            throw new SaveOrderException("SOR-001","No field should be empty except Coupon_Id and discount");
        }

        final OrderEntity orderEntity = new OrderEntity();
        orderEntity.setUuid(UUID.randomUUID().toString());

        if(saveOrderRequest.getCouponId() != null) {
            CouponEntity couponByCouponId = orderService.getCouponByCouponId(saveOrderRequest.getCouponId().toString());
            orderEntity.setCoupon(couponByCouponId);
        }

        PaymentEntity payment = paymentService.getPaymentByUUID(saveOrderRequest.getPaymentId().toString());
        AddressEntity tempAddressEntity = addressService.getAddressByUUID(saveOrderRequest.getAddressId(), customerEntity);

        /* Allowing null value for discount
           Defaulting to 0.00 if no value entered
           Included this logic considering that not all orders will have a discount
        */
        if(saveOrderRequest.getDiscount() != null) {
            orderEntity.setDiscount(saveOrderRequest.getDiscount().doubleValue());
        } else {
            orderEntity.setDiscount(0.00);
        }

        orderEntity.setCustomer(customerEntity);

        orderEntity.setAddress(tempAddressEntity);
        orderEntity.setPayment(payment);
        orderEntity.setRestaurant(restaurantService.restaurantByUUID(saveOrderRequest.getRestaurantId().toString()));
        orderEntity.setDate(new Date());
        orderEntity.setBill(saveOrderRequest.getBill().doubleValue());
        OrderEntity savedOrderEntity = orderService.saveOrder(orderEntity);

        for (ItemQuantity itemQuantity : saveOrderRequest.getItemQuantities()) {
            OrderItemEntity orderItemEntity = new OrderItemEntity();
            orderItemEntity.setOrders(savedOrderEntity);
            orderItemEntity.setItem(itemService.getItemByUUID(itemQuantity.getItemId().toString()));
            orderItemEntity.setQuantity(itemQuantity.getQuantity());
            orderItemEntity.setPrice(itemQuantity.getPrice());
            orderItemEntity.setPrice(itemQuantity.getPrice());
            orderService.saveOrderItem(orderItemEntity);
        }

        SaveOrderResponse saveOrderResponse = new SaveOrderResponse()
                .id(savedOrderEntity.getUuid()).status("ORDER SUCCESSFULLY PLACED");

        return new ResponseEntity<SaveOrderResponse>(saveOrderResponse, HttpStatus.CREATED);
    }
}

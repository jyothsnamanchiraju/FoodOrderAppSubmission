package com.upgrad.FoodOrderingApp.api.controller;


import com.upgrad.FoodOrderingApp.api.model.AddressList;
import com.upgrad.FoodOrderingApp.api.model.AddressListResponse;
import com.upgrad.FoodOrderingApp.api.model.AddressListState;
import com.upgrad.FoodOrderingApp.api.model.DeleteAddressResponse;
import com.upgrad.FoodOrderingApp.api.model.StatesListResponse;
import com.upgrad.FoodOrderingApp.api.model.StatesList;
import com.upgrad.FoodOrderingApp.service.businness.AddressService;

import com.upgrad.FoodOrderingApp.api.model.SaveAddressRequest;
import com.upgrad.FoodOrderingApp.api.model.SaveAddressResponse;

import com.upgrad.FoodOrderingApp.service.businness.CustomerService;
import com.upgrad.FoodOrderingApp.service.dao.CustomerAddressDao;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.exception.*;

import com.upgrad.FoodOrderingApp.service.entity.AddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.StateEntity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;

import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.util.Base64;

@RestController
@RequestMapping("/")

public class AddressController {

    @Autowired
    private AddressService addressService;

    @Autowired
    private CustomerService customerService;

    //API for creating/saving new address of a customer
    @CrossOrigin
    @RequestMapping(method= RequestMethod.POST, path ="/address", consumes=MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SaveAddressResponse> createCustomerAddress(
            @RequestBody(required = false) final SaveAddressRequest saveAddressRequest,
            @RequestHeader("authorization") final String authorization)
            throws AuthorizationFailedException, SaveAddressException, AddressNotFoundException {

        String[] bearerToken = authorization.split("Bearer ");
        String customerAccessToken = bearerToken[1];

        CustomerEntity customerEntity = customerService.getCustomer(customerAccessToken);

        StateEntity stateEntity = addressService.getStateByUUID(saveAddressRequest.getStateUuid());

        AddressEntity addressEntity = new AddressEntity();
        addressEntity.setUuid(UUID.randomUUID().toString());
        addressEntity.setFlatBuilNo(saveAddressRequest.getFlatBuildingName());
        addressEntity.setLocality(saveAddressRequest.getLocality());
        addressEntity.setCity(saveAddressRequest.getCity());
        addressEntity.setPincode(saveAddressRequest.getPincode());
        addressEntity.setState(stateEntity);
        addressEntity.setActive(1);

        final AddressEntity addressCreated = addressService.saveAddress(addressEntity, customerEntity);
        SaveAddressResponse saveAddressResponse = new SaveAddressResponse()
                .id(addressCreated.getUuid())
                .status("ADDRESS SUCCESSFULLY REGISTERED") ;

        return new ResponseEntity<SaveAddressResponse>(saveAddressResponse, HttpStatus.CREATED);
    }

    //API to list all saved addresses of a customer
    @CrossOrigin
    @RequestMapping(method= RequestMethod.GET, path ="/address/customer", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AddressListResponse> getAllAddresses(@RequestHeader("authorization") final String authorization)
            throws AuthorizationFailedException {

        String[] bearerToken = authorization.split("Bearer ");
        String customerAccessToken = bearerToken[1];
        CustomerEntity customerEntity = customerService.getCustomer(customerAccessToken);

        List<AddressList> listOfAddresses = new <AddressList> ArrayList();
        List<AddressEntity> customerAddressList = addressService.getAllAddress(customerEntity);

        for(AddressEntity addr: customerAddressList){
            AddressList customerAddress = new AddressList();
            customerAddress.setId(UUID.fromString(addr.getUuid()));
            customerAddress.setFlatBuildingName(addr.getFlatBuilNo());
            customerAddress.setLocality(addr.getLocality());
            customerAddress.setCity(addr.getCity());
            customerAddress.setPincode(addr.getPincode());

            StateEntity state = addr.getState();
            AddressListState aState = new AddressListState();
            aState.setId(UUID.fromString(state.getUuid()));
            aState.setStateName(state.getStateName());

            customerAddress.setState(aState);

            listOfAddresses.add(customerAddress);
        }

        AddressListResponse addressListResponse = new AddressListResponse().addresses(listOfAddresses);
        return new ResponseEntity<AddressListResponse>(addressListResponse, HttpStatus.OK);

    }

    //API to delete address of a customer using address ID from list of addresses created by a customer
    @CrossOrigin
    @RequestMapping(method= RequestMethod.DELETE, path ="/address/{address_id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<DeleteAddressResponse> deleteAddress(
            @RequestHeader("authorization") final String authorization,
            @PathVariable("address_id") final String addressUuid)
            throws AuthorizationFailedException, AddressNotFoundException {

        String[] bearerToken = authorization.split("Bearer ");
        String customerAccessToken = bearerToken[1];
        CustomerEntity customerEntity = customerService.getCustomer(customerAccessToken);

        final AddressEntity addressEntity = addressService.getAddressByUUID(addressUuid,customerEntity);
        final String uuid = addressService.deleteAddress(addressEntity).getUuid();

        DeleteAddressResponse deleteAddressResponse = new DeleteAddressResponse();
        deleteAddressResponse.id(UUID.fromString(uuid)).status("ADDRESS DELETED SUCCESSFULLY");
        return new ResponseEntity<DeleteAddressResponse>(deleteAddressResponse, HttpStatus.OK);

    }

    //List all States in the table
    @CrossOrigin
    @RequestMapping(method= RequestMethod.GET, path ="/states", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<StatesListResponse> getAllStates() {

        List<StateEntity> listOfAllStates = addressService.getAllStates();

        List<StatesList> statesList = new ArrayList<>();

        for(StateEntity s: listOfAllStates){
            StatesList state = new StatesList();
            state.setId(UUID.fromString(s.getUuid()));
            state.setStateName(s.getStateName());
            statesList.add(state);
        }

        StatesListResponse statesListResponse = new StatesListResponse();
        if (statesList.size() != 0) {

            statesListResponse.states(statesList);
        } else {
            statesListResponse.states(null);
        }

        return new ResponseEntity<StatesListResponse>(statesListResponse, HttpStatus.OK);
    }

}

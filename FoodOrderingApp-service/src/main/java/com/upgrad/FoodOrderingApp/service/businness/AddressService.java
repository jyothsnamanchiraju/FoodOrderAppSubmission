package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.businness.PasswordCryptographyProvider;

import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthEntity;
import com.upgrad.FoodOrderingApp.service.entity.AddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.StateEntity;
import com.upgrad.FoodOrderingApp.service.exception.*;
import com.upgrad.FoodOrderingApp.service.dao.AddressDao;
import com.upgrad.FoodOrderingApp.service.dao.CustomerDao;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class AddressService {

    @Autowired
    private CustomerDao customerDao;

    @Autowired
    private AddressDao addressDao;

    @Autowired
    private PasswordCryptographyProvider cryptographyProvider;

    @Transactional(propagation = Propagation.REQUIRED)
    public AddressEntity createAddress(final String customerAccessToken, final AddressEntity addressEntity, final String stateUuid)
            throws AuthorizationFailedException, SaveAddressException, AddressNotFoundException {

        CustomerAuthEntity customerAuthEntity =  checkAuthorization(customerAccessToken);

        StateEntity state = addressDao.getStateDetails(stateUuid);
        if(state==null){
            throw new AddressNotFoundException("ANF-002", "No state by this id");
        }

        addressEntity.setState(state);
        AddressEntity createdAddress = addressDao.createNewAddress(addressEntity);

        CustomerAddressEntity customerAddressEntity = new CustomerAddressEntity();
        customerAddressEntity.setCustomer(customerAuthEntity.getCustomer());
        customerAddressEntity.setAddress(createdAddress);

        CustomerAddressEntity newrecord = addressDao.recordCustomerAddressEntity(customerAddressEntity);

        return createdAddress;

    }

    @Transactional(propagation = Propagation.REQUIRED)
    public List<AddressEntity> getAddressList(String customerAccessToken) throws AuthorizationFailedException{

        CustomerAuthEntity customerAuthEntity =  checkAuthorization(customerAccessToken);
        CustomerEntity customer = customerAuthEntity.getCustomer();

        List<CustomerAddressEntity> customerAddressList = new <CustomerAddressEntity> ArrayList();
        customerAddressList = addressDao.getAllAddresses(customer);

        List<AddressEntity> addresses = new <AddressEntity> ArrayList();

        for(CustomerAddressEntity c: customerAddressList){
            AddressEntity address = new AddressEntity();
            Integer addrId = c.getAddress().getId();
            address = addressDao.getAddressById(addrId);

            addresses.add(address);
        }
        return addresses;
    }

    //deleteAddress(customerAccessToken, addressUuid);
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteAddress(final String customerAccessToken, String addressUuid)
            throws AuthorizationFailedException, AddressNotFoundException {

        CustomerAuthEntity customerAuthEntity =  checkAuthorization(customerAccessToken);

        CustomerEntity customer = customerAuthEntity.getCustomer();
        List<CustomerAddressEntity> customerAddressList = new <CustomerAddressEntity> ArrayList();
        customerAddressList = addressDao.getAllAddresses(customer);

        List<AddressEntity> customerAddresses = new <AddressEntity> ArrayList();

        for(CustomerAddressEntity c: customerAddressList){
            AddressEntity addressEntity = new AddressEntity();
            Integer addrId = c.getAddress().getId();
            addressEntity = addressDao.getAddressById(addrId);

            customerAddresses.add(addressEntity);
        }



        if(addressUuid == null || addressUuid == " "){
            throw new AddressNotFoundException("ANF-005", "Address id can not be empty");
        }

        AddressEntity address = addressDao.getAddressByUuid(addressUuid);
        if(address == null){
            throw new AddressNotFoundException("ANF-003", "No address by this id");
        }

        if(!(customerAddresses.contains(address))){
            throw new AuthorizationFailedException("ATHR-004", "You are not authorized to view/update/delete any one else's address");
        }
        addressDao.deleteAddress(address);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public List<StateEntity> getAllStates(){

        List<StateEntity> statesList = addressDao.getAllStates();
        return statesList;

    }


    private CustomerAuthEntity checkAuthorization(String customerAccessToken) throws AuthorizationFailedException{

        CustomerAuthEntity customerAuthEntity = customerDao.getCustomerAuthByToken(customerAccessToken);


        if(customerAuthEntity == null) {
            throw new AuthorizationFailedException("ATHR-001","Customer is not Logged in.");
        }
        if(customerAuthEntity.getLogoutAt() != null){
            throw new AuthorizationFailedException("ATHR-002","Customer is logged out. Log in again to access this endpoint.");
        }

        final ZonedDateTime now = ZonedDateTime.now();

        if((customerAuthEntity.getExpiresAt().compareTo(now)) < 0){
            throw new AuthorizationFailedException("ATHR-003","Your session is expired. Log in again to access this endpoint.");
        }

        return customerAuthEntity;
    }



}

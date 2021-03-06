package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.CustomerAddressDao;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.entity.AddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.StateEntity;
import com.upgrad.FoodOrderingApp.service.exception.*;
import com.upgrad.FoodOrderingApp.service.dao.AddressDao;
import com.upgrad.FoodOrderingApp.service.dao.CustomerDao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class AddressService {

    @Autowired
    private AddressDao addressDao;

    @Autowired
    private CustomerAddressDao customerAddressDao;

    @Autowired
    private PasswordCryptographyProvider cryptographyProvider;

    // save address of a customer
    @Transactional(propagation = Propagation.REQUIRED)
    public AddressEntity saveAddress(final AddressEntity addressEntity, final CustomerEntity customerEntity)
            throws SaveAddressException {

        //check for null values
        try{
            addressEntity.getFlatBuilNo().isEmpty();
            addressEntity.getLocality().isEmpty();
            addressEntity.getCity().isEmpty();
            addressEntity.getPincode().isEmpty();
            addressEntity.getState().getUuid().isEmpty();
        } catch(Exception e) {
            throw new SaveAddressException("SAR-001", "No field can be empty.");
        }

        if(addressEntity.getFlatBuilNo().trim().isEmpty()
                || addressEntity.getLocality().trim().isEmpty()
                || addressEntity.getCity().trim().isEmpty()
                || addressEntity.getPincode().trim().isEmpty()
                || addressEntity.getUuid().trim().isEmpty()){
            throw new SaveAddressException("SAR-001", "No field can be empty.");
        }

        validatePincode(addressEntity.getPincode()); // validate if pincode is correct
        AddressEntity createdAddress = addressDao.createNewAddress(addressEntity);

        CustomerAddressEntity customerAddressEntity = new CustomerAddressEntity();
        customerAddressEntity.setCustomer(customerEntity);
        customerAddressEntity.setAddress(createdAddress);
        addressDao.recordCustomerAddressEntity(customerAddressEntity);

        return createdAddress;
    }

    public AddressEntity getAddressByUUID(final String addressUuid, final CustomerEntity customerEntity) throws AddressNotFoundException, AuthorizationFailedException {
        //if address id is empty, throw exception
        if(addressUuid.isEmpty()){
            throw new AddressNotFoundException("ANF-005", "Address id can not be empty");
        }

        AddressEntity addressEntityToBeDeleted = addressDao.getAddressByUuid(addressUuid);

        //if address id is incorrect and no such address exist in database
        if(addressEntityToBeDeleted == null){
            throw new AddressNotFoundException("ANF-003", "No address by this id");
        }
        else {
            final CustomerAddressEntity customerAddressEntity = getCustomerAddressByAddressId(addressEntityToBeDeleted);
            final CustomerEntity belongsToAddressEntity = customerAddressEntity.getCustomer();
            if(!belongsToAddressEntity.getUuid().equals(customerEntity.getUuid())){
                throw new AuthorizationFailedException("ATHR-004", "You are not authorized to view/update/delete any one else's address ");
            }
            else {
                return addressEntityToBeDeleted;
            }
        }
    }

    public CustomerAddressEntity getCustomerAddressByAddressId(final AddressEntity addressEntity){
        return customerAddressDao.getCustomerAddressByAddressId(addressEntity);
    }

    // get all the addresses of a customer
    @Transactional(propagation = Propagation.REQUIRED)
    public List<AddressEntity> getAllAddress(final CustomerEntity customerEntity) {

        List<CustomerAddressEntity> customerAddressList = addressDao.getAllAddresses(customerEntity);

        List<AddressEntity> addresses = new <AddressEntity> ArrayList();

        for(CustomerAddressEntity c: customerAddressList){
            Integer addrId = c.getAddress().getId();
            AddressEntity address = addressDao.getAddressById(addrId);

            addresses.add(address);
        }

        return addresses;
    }

    //deleteAddress of a customer in Address table
    @Transactional(propagation = Propagation.REQUIRED)
    public AddressEntity deleteAddress(final AddressEntity addressEntity) {
        addressDao.deleteAddress(addressEntity);
        return addressEntity;
    }

    //Get state details by UUID
    public StateEntity getStateByUUID(final String stateUuid) throws AddressNotFoundException, SaveAddressException{

        StateEntity stateEntity = addressDao.getStateDetails(stateUuid);
        if(stateEntity == null){
            throw new AddressNotFoundException("ANF-002","No state by this id");
        }
        else {
            return stateEntity;
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public List<StateEntity> getAllStates(){
        return addressDao.getAllStates();
    }

    public String validatePincode(String pincode) throws SaveAddressException {
        if(pincode.length() !=6)
            throw new SaveAddressException("SAR-002", "Invalid pincode");

        boolean allNumeric = true;

        for(int i=0; i<pincode.length(); i++){
            if(!((int)pincode.charAt(i) >=48 && (int)pincode.charAt(i)<=57)){
                allNumeric = false;
            }
        }

        if(allNumeric) {
            return pincode;
        } else {
            throw new SaveAddressException("SAR-002", "Invalid pincode");
        }
    }

}

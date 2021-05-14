package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.businness.PasswordCryptographyProvider;

import com.upgrad.FoodOrderingApp.service.exception.*;
import com.upgrad.FoodOrderingApp.service.dao.CustomerDao;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthEntity;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class CustomerService {
    @Autowired
    private CustomerDao customerDao;

    @Autowired
    private PasswordCryptographyProvider cryptographyProvider;

    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerAuthEntity authenticate(final String contactNumber, final String password) throws AuthenticationFailedException {

        CustomerEntity customerEntity = customerDao.getCustomerByContactNumber(contactNumber);
        if (customerEntity == null) {
            throw new AuthenticationFailedException("ATH-001", "This contact number has not been registered!");
        }

        final String encryptedPassword = cryptographyProvider.encrypt(password, customerEntity.getSalt());

        if (encryptedPassword.equals(customerEntity.getPassword())) {
            JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(encryptedPassword);

            CustomerAuthEntity customerAuthEntity = new CustomerAuthEntity();
            customerAuthEntity.setCustomer(customerEntity);
            final ZonedDateTime now = ZonedDateTime.now();
            final ZonedDateTime expiresAt = now.plusHours(8);
            customerAuthEntity.setAccessToken(jwtTokenProvider.generateToken(customerEntity.getUuid(), now, expiresAt));

            customerAuthEntity.setUuid(UUID.randomUUID().toString());
            customerAuthEntity.setLoginAt(now);
            customerAuthEntity.setExpiresAt(expiresAt);

            customerDao.createCustomerAuth(customerAuthEntity);

            return customerAuthEntity;
        } else {
            throw new AuthenticationFailedException("ATH-002", "Invalid Credentials");
        }

    }

    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerAuthEntity logout(final String accessToken) throws AuthorizationFailedException{
        CustomerAuthEntity customerAuthEntity = customerDao.getCustomerAuthByToken(accessToken);

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


        customerAuthEntity.setLogoutAt(now);
        customerDao.updateLogoutTime(customerAuthEntity);

        return customerAuthEntity;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerEntity updateCustomer(final CustomerEntity customerEntity) {

        CustomerEntity updatedCustomer = customerDao.updateCustomerEntity(customerEntity);
        return updatedCustomer;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerEntity updateCustomerPassword(final String oldPassword, final String newPassword, final CustomerEntity customerEntity)
            throws UpdateCustomerException {

        if(!checkPasswordStrength(newPassword)){
            throw new UpdateCustomerException("UCR-001", "Weak password!");
        }

        String encryptedOldPassword = cryptographyProvider.encrypt(oldPassword, customerEntity.getSalt());

        if(encryptedOldPassword.compareTo(customerEntity.getPassword()) != 0){
            throw new UpdateCustomerException("UCR-004", "Incorrect old password!");
        }

        String[] encryptedText = cryptographyProvider.encrypt(newPassword);
        customerEntity.setSalt(encryptedText[0]);
        customerEntity.setPassword(encryptedText[1]);

        CustomerEntity updatedCustomer  = customerDao.updateCustomerEntity(customerEntity);
        return updatedCustomer;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerEntity saveCustomer(CustomerEntity customerEntity)throws SignUpRestrictedException{
        //check if the contact_number already exists for another account
        CustomerEntity checkCustomer = customerDao.getCustomerByContactNumber(customerEntity.getContactNumber());
        if(checkCustomer !=null){
            throw new SignUpRestrictedException("SGR-001", "This contact number is already registered! Try other contact number.");
        }

        if(!checkEmail(customerEntity.getEmail())){
            throw new SignUpRestrictedException("SGR-002", "Invalid email-id format!");
        }

        String contact = customerEntity.getContactNumber();
        if(contact.length() !=10){
            throw new SignUpRestrictedException("SGR-003", "Invalid contact number!");
        }

        boolean strongPassword = checkPasswordStrength(customerEntity.getPassword());
        if(!strongPassword){
            throw new SignUpRestrictedException("SGR-004", "Weak password!");
        }

        String[] encryptedText = cryptographyProvider.encrypt(customerEntity.getPassword());
        customerEntity.setSalt(encryptedText[0]);
        customerEntity.setPassword(encryptedText[1]);

        return customerDao.createCustomer(customerEntity);

    }

    private boolean checkEmail(String email){
        boolean hasAtSymbol = false;
        boolean hasDot = false;

        for(int i=0; i<email.length(); i++){
            if(email.charAt(i)=='@'){
                hasAtSymbol = true;
            }
            if(email.charAt(i)=='.'){
                hasDot = true;
            }
        }

        int len = email.length();

        if(email.charAt(len-3)!='.' && email.charAt(len-4)!='.'){
            hasDot = false;
        }

        if(hasAtSymbol && hasDot)
            return true;
        else
            return false;

    }

    private boolean checkPasswordStrength(String password){
        if (password.length()<8)
            return false;

        boolean hasNumber = false;
        boolean hasCaps = false;
        boolean hasSplChar = false;
        Set<Character> splChar = new HashSet<Character>();
        Character[] A= {'#', '@','$','%','&','*','!','^' };
        splChar.addAll(Arrays.asList(A));

        for(int i=0; i<password.length(); i++){
            if(password.charAt(i)>='A' && password.charAt(i)<='Z'){
                hasCaps= true;
            }
            if((int)password.charAt(i)>=48 && (int)password.charAt(i)<=57)
                hasNumber= true;

            if (splChar.contains(password.charAt(i)))
                hasSplChar = true;
        }

        if(hasNumber && hasCaps && hasSplChar)
            return true;
        else
            return false;
    }

    //This method is the Bearer authorization method
    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerEntity getCustomer(final String accessToken)throws AuthorizationFailedException{

        CustomerAuthEntity customerAuthEntity = customerDao.getCustomerAuthByToken(accessToken);

        if(customerAuthEntity == null){
            throw new AuthorizationFailedException("ATHR-001","Customer is not Logged in.");
        }
        else if (customerAuthEntity != null && customerAuthEntity.getLogoutAt()!= null){
            throw new AuthorizationFailedException("ATHR-002","Customer is logged out. Log in again to access this endpoint.");
        }
        else if (customerAuthEntity != null && ZonedDateTime.now().isAfter(customerAuthEntity.getExpiresAt())){
            throw new AuthorizationFailedException("ATHR-003","Your session is expired. Log in again to access this endpoint.");
        }
        else {
            return customerAuthEntity.getCustomer();
        }
    }
}


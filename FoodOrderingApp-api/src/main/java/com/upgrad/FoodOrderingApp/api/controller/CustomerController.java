package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.SignupCustomerRequest;
import com.upgrad.FoodOrderingApp.api.model.SignupCustomerResponse;
import com.upgrad.FoodOrderingApp.api.model.LoginResponse;
import com.upgrad.FoodOrderingApp.api.model.LogoutResponse;
import com.upgrad.FoodOrderingApp.api.model.UpdateCustomerRequest;
import com.upgrad.FoodOrderingApp.api.model.UpdateCustomerResponse;
import com.upgrad.FoodOrderingApp.api.model.UpdatePasswordRequest;
import com.upgrad.FoodOrderingApp.api.model.UpdatePasswordResponse;

import com.upgrad.FoodOrderingApp.service.exception.*;

import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthEntity;
import com.upgrad.FoodOrderingApp.service.businness.CustomerService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;

import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.Base64;

@RestController
@RequestMapping("/")

public class CustomerController {
    @Autowired
    private CustomerService customerService;

    // API to register a customer
    @CrossOrigin
    @RequestMapping(method= RequestMethod.POST, path ="/customer/signup", consumes=MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SignupCustomerResponse> signup(@RequestBody final SignupCustomerRequest signupCustomerRequest) throws SignUpRestrictedException{

        final CustomerEntity customerEntity = new CustomerEntity();

        customerEntity.setUuid(UUID.randomUUID().toString());
        try {
            signupCustomerRequest.getFirstName().isEmpty();
            signupCustomerRequest.getEmailAddress().isEmpty();
            signupCustomerRequest.getContactNumber().isEmpty();
            signupCustomerRequest.getPassword().isEmpty();
        } catch (Exception e) {
            throw new SignUpRestrictedException("SGR-005", "Except last name all fields should be filled");
        }

        if(signupCustomerRequest.getFirstName().equals("") || signupCustomerRequest.getEmailAddress().equals("") ||
        signupCustomerRequest.getContactNumber().equals("") || signupCustomerRequest.getPassword().equals("")) {
            throw new SignUpRestrictedException("SGR-005", "Except last name all fields should be filled");
        }

        customerEntity.setFirstName(signupCustomerRequest.getFirstName());
        customerEntity.setLastName(signupCustomerRequest.getLastName());
        customerEntity.setEmail(signupCustomerRequest.getEmailAddress());
        customerEntity.setContactNumber(signupCustomerRequest.getContactNumber());
        customerEntity.setPassword(signupCustomerRequest.getPassword());

        final CustomerEntity createdCustomerEntity = customerService.saveCustomer(customerEntity);
        SignupCustomerResponse signupCustomerResponse = new SignupCustomerResponse().id(createdCustomerEntity.getUuid()).status("CUSTOMER SUCCESSFULLY REGISTERED") ;
        return new ResponseEntity<SignupCustomerResponse>(signupCustomerResponse, HttpStatus.CREATED);
    }

    // API for logging in
    @CrossOrigin
    @RequestMapping(method = RequestMethod.POST, path="/customer/login", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<LoginResponse> login(@RequestHeader("authorization") final String authorization) throws AuthenticationFailedException{

        byte[] decoded = null;
        String[] decodedArray = null;

        try {
            decoded = Base64.getDecoder().decode(authorization.split("Basic ")[1]);
            String decodedText = new String(decoded);
            String temp = decodedText.split(":")[1];
            decodedArray = decodedText.split(":");
        } catch (Exception e) {
            throw  new AuthenticationFailedException("ATH-003", "Incorrect format of decoded customer name and password");
        }

        CustomerAuthEntity customerAuth = customerService.authenticate(decodedArray[0], decodedArray[1]);

        CustomerEntity customer = customerAuth.getCustomer();

        String customerUuid = customer.getUuid();

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setId(customerUuid);
        loginResponse.setMessage("LOGGED IN SUCCESSFULLY");
        loginResponse.setFirstName(customer.getFirstName());
        loginResponse.setLastName(customer.getLastName());
        loginResponse.setEmailAddress(customer.getEmail());
        loginResponse.setContactNumber(customer.getContactNumber());

        HttpHeaders headers = new HttpHeaders();
        headers.add("access-token", customerAuth.getAccessToken());
        return new ResponseEntity<LoginResponse>(loginResponse, headers, HttpStatus.OK);
    }

    // API to logout of app and update logout time in table
    @CrossOrigin
    @RequestMapping(method = RequestMethod.POST, path = "/customer/logout", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<LogoutResponse> logout(@RequestHeader("authorization") final String authorization) throws AuthorizationFailedException {

        String[] bearerToken = authorization.split("Bearer ");
        String accessToken = bearerToken[1];

        CustomerAuthEntity customerAuthEntity = customerService.logout(accessToken);
        CustomerEntity customerEntity = customerAuthEntity.getCustomer();

        LogoutResponse logoutResponse = new LogoutResponse();
        logoutResponse.setId(customerEntity.getUuid());
        logoutResponse.setMessage("Logged OUT SUCCESSFULLY");

        return new ResponseEntity<LogoutResponse>(logoutResponse, HttpStatus.OK);
    }

    // API to update customer details
    @CrossOrigin
    @RequestMapping(method = RequestMethod.PUT, path = "/customer", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<UpdateCustomerResponse> updateCustomer(
            @RequestHeader("authorization") final String authorization,
            @RequestBody final UpdateCustomerRequest updateCustomerRequest)
            throws UpdateCustomerException, AuthorizationFailedException {

        String firstName = updateCustomerRequest.getFirstName();
        String lastName= updateCustomerRequest.getLastName();

        try {
            firstName.isEmpty();
        } catch (Exception e) {
            throw new UpdateCustomerException("UCR-002", "First name field should not be empty");
        }

        if(firstName.trim().isEmpty()){
            throw new UpdateCustomerException("UCR-002", "First name field should not be empty");
        }

        String[] bearerToken = authorization.split("Bearer ");
        String accessToken = bearerToken[1];

        CustomerEntity customerEntity = customerService.getCustomer(accessToken);
        customerEntity.setFirstName(firstName);
        customerEntity.setLastName(lastName);

        final CustomerEntity updatedCustomer = customerService.updateCustomer(customerEntity);

        UpdateCustomerResponse updateCustomerResponse = new UpdateCustomerResponse();
        updateCustomerResponse.setId(updatedCustomer.getUuid());
        updateCustomerResponse.setStatus("CUSTOMER DETAILS UPDATED SUCCESSFULLY");
        updateCustomerResponse.setFirstName(firstName);
        updateCustomerResponse.setLastName(lastName);

        return new ResponseEntity<UpdateCustomerResponse>(updateCustomerResponse,HttpStatus.OK);
    }

    // API to update customer password
    @CrossOrigin
    @RequestMapping(method = RequestMethod.PUT, path = "/customer/password", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<UpdatePasswordResponse> changeCustomerPassword(
            @RequestHeader("authorization") final String authorization,
            @RequestBody final UpdatePasswordRequest updatePasswordRequest)
            throws UpdateCustomerException, AuthorizationFailedException {

        String oldPassword = updatePasswordRequest.getOldPassword();
        String newPassword = updatePasswordRequest.getNewPassword();
        try {
            oldPassword.isEmpty();
            newPassword.isEmpty();
        } catch (Exception e) {
            throw new UpdateCustomerException("UCR-003","No field should be empty");
        }

        if(newPassword.equals("") || oldPassword.equals("")){
            throw new UpdateCustomerException("UCR-003","No field should be empty");
        }

        String[] bearerToken = authorization.split("Bearer ");
        String accessToken = bearerToken[1];

        CustomerEntity customerEntity = customerService.getCustomer(accessToken);

        CustomerEntity updatedCustomer = customerService.updateCustomerPassword(oldPassword, newPassword, customerEntity);

        UpdatePasswordResponse updatePasswordResponse =  new UpdatePasswordResponse();
        updatePasswordResponse.setId(updatedCustomer.getUuid());
        updatePasswordResponse.setStatus("CUSTOMER PASSWORD UPDATED SUCCESSFULLY");

        return new ResponseEntity<UpdatePasswordResponse>(updatePasswordResponse, HttpStatus.OK);
    }

}

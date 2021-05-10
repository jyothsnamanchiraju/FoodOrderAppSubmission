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
import com.upgrad.FoodOrderingApp.service.businness.CustomerSignupService;
import com.upgrad.FoodOrderingApp.service.businness.CustomerService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;

import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.Base64;

@RestController
@RequestMapping("/customer")

public class CustomerController {
   @Autowired
    private CustomerSignupService customerSignupService;

    @RequestMapping(method= RequestMethod.POST, path ="/signup", consumes=MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SignupCustomerResponse> signup(final SignupCustomerRequest signupCustomerRequest) throws SignUpRestrictedException{

        final CustomerEntity customerEntity = new CustomerEntity();

        customerEntity.setUuid(UUID.randomUUID().toString());
        customerEntity.setFirstName(signupCustomerRequest.getFirstName());
        customerEntity.setLastName(signupCustomerRequest.getLastName());
        customerEntity.setEmail(signupCustomerRequest.getEmailAddress());
        customerEntity.setContactNumber(signupCustomerRequest.getContactNumber());
        customerEntity.setPassword(signupCustomerRequest.getPassword());

        final CustomerEntity createdCustomerEntity = customerSignupService.signup(customerEntity);
        SignupCustomerResponse signupCustomerResponse = new SignupCustomerResponse().id(createdCustomerEntity.getUuid()).status("CUSTOMER SUCCESSFULLY REGISTERED") ;
        return new ResponseEntity<SignupCustomerResponse>(signupCustomerResponse, HttpStatus.CREATED);
    }


    @Autowired
    private CustomerService customerService;

    @RequestMapping(method = RequestMethod.POST, path="/login", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<LoginResponse> login(@RequestHeader("authorization") final String authorization) throws AuthenticationFailedException{

        if(!(authorization.split(" ")[0].equals("Basic"))){
            throw new AuthenticationFailedException("ATH-003","Incorrect format of decoded customer name and password");
        }

        byte[] decode = Base64.getDecoder().decode(authorization.split("Basic ")[1]);
        String decodedText = new String(decode);
        String[] decodedArray = decodedText.split(":");

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


    @RequestMapping(method = RequestMethod.POST, path = "/logout", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<LogoutResponse> Logout(@RequestHeader("authorization") final String authorization) throws AuthorizationFailedException {

        String[] bearerToken = authorization.split("Bearer ");
        String accessToken = bearerToken[1];

        CustomerEntity customerEntity = customerService.logout(accessToken);
        String uuid = customerEntity.getUuid();

        LogoutResponse logoutResponse = new LogoutResponse();
        logoutResponse.setId(uuid);
        logoutResponse.setMessage("Logged OUT SUCCESSFULLY");

        return new ResponseEntity<LogoutResponse>(logoutResponse, HttpStatus.OK);
    }


    @RequestMapping(method = RequestMethod.PUT, path = "/update", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<UpdateCustomerResponse> updateCustomer(@RequestHeader("authorization") final String authorization,
                                                                 final UpdateCustomerRequest updateCustomerRequest)
            throws UpdateCustomerException, AuthorizationFailedException {

        String[] bearerToken = authorization.split("Bearer ");
        String accessToken = bearerToken[1];

        if(updateCustomerRequest.getFirstName() == null || updateCustomerRequest.getFirstName()==" "){
            throw new UpdateCustomerException("UCR-002", "First name field should not be empty");
        }
        String firstName = updateCustomerRequest.getFirstName();
        String lastName= updateCustomerRequest.getLastName();

        CustomerEntity customerEntity = customerService.updateCustomer(accessToken,firstName,lastName);
        String uuid = customerEntity.getUuid();

        UpdateCustomerResponse updateCustomerResponse = new UpdateCustomerResponse();
        updateCustomerResponse.setId(uuid);
        updateCustomerResponse.setStatus("CUSTOMER DETAILS UPDATED SUCCESSFULLY");
      //  updateCustomerResponse.setStatus("201");
        updateCustomerResponse.setFirstName(firstName);
        updateCustomerResponse.setLastName(lastName);

        return new ResponseEntity<UpdateCustomerResponse>(updateCustomerResponse,HttpStatus.OK);
    }


    @RequestMapping(method = RequestMethod.PUT, path = "/password", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<UpdatePasswordResponse> changeCustomerPassword(@RequestHeader("authorization") final String authorization,
                                                                 final UpdatePasswordRequest updatePasswordRequest)
            throws UpdateCustomerException, AuthorizationFailedException {

        String[] bearerToken = authorization.split("Bearer ");
        String accessToken = bearerToken[1];

        if(updatePasswordRequest.getNewPassword()== null || updatePasswordRequest.getNewPassword()== " " ||
           updatePasswordRequest.getOldPassword()==null ||  updatePasswordRequest.getOldPassword()== " "){
            throw new UpdateCustomerException("UCR-003","No field should be empty");
        }
            String oldPassword = updatePasswordRequest.getOldPassword();
            String newPassword = updatePasswordRequest.getNewPassword();
            CustomerEntity customerEntity = customerService.updatePassword(accessToken, oldPassword, newPassword);

            String uuid = customerEntity.getUuid();

            UpdatePasswordResponse updatePasswordResponse =  new UpdatePasswordResponse();
            updatePasswordResponse.setId(uuid);
            updatePasswordResponse.setStatus("CUSTOMER PASSWORD UPDATED SUCCESSFULLY");

            return new ResponseEntity<UpdatePasswordResponse>(updatePasswordResponse, HttpStatus.OK);
    }

}

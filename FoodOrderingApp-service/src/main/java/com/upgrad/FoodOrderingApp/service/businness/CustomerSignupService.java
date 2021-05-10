package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.businness.PasswordCryptographyProvider;

import com.upgrad.FoodOrderingApp.service.exception.*;
import com.upgrad.FoodOrderingApp.service.dao.CustomerDao;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class CustomerSignupService {

    @Autowired
    private CustomerDao customerDao;

    @Autowired
    private PasswordCryptographyProvider cryptographyProvider;

    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerEntity signup(CustomerEntity customerEntity)throws SignUpRestrictedException{

        //check if the contact_number already exists for another account
        CustomerEntity checkCustomer = customerDao.getCustomerByContactNumber(customerEntity.getContactNumber());
        if(checkCustomer !=null){
            throw new SignUpRestrictedException("SGR-001", "This contact number is already registered! Try other contact number.");
        }

        //check if all fields are filled
        if(customerEntity.getFirstName() == null || customerEntity.getFirstName() == " " ||
           customerEntity.getEmail() == null || customerEntity.getEmail() == " " ||
           customerEntity.getContactNumber() == null || customerEntity.getContactNumber() == " " ||
           customerEntity. getPassword() == null || customerEntity.getPassword()==" " ){
            throw new SignUpRestrictedException("SGR-005", "Except last name all fields should be filled");
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


}


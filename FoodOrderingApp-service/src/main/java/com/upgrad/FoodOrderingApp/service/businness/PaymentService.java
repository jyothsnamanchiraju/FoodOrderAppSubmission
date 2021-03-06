package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.PaymentDao;
import com.upgrad.FoodOrderingApp.service.entity.PaymentEntity;
import com.upgrad.FoodOrderingApp.service.exception.PaymentMethodNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentService {

    @Autowired
    private PaymentDao paymentDao;

    //get all payment methods
    public List<PaymentEntity> getAllPaymentMethods() {
        return paymentDao.getAllPaymentMethods();
    }

    // get single payment by UUID
    public PaymentEntity getPaymentByUUID(String paymentUuid) throws PaymentMethodNotFoundException {
        PaymentEntity paymentEntity = paymentDao.getMethodbyId(paymentUuid);
        if(paymentEntity==null){
            throw new PaymentMethodNotFoundException("PNF-002","No payment method found by this id");
        } else {
            return paymentEntity;
        }
    }
}

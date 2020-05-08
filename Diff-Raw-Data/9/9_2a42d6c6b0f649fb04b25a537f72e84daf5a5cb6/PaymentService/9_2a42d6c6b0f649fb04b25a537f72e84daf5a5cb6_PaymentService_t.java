 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.uhsarp.billrive.services;
 
 import com.uhsarp.billrive.dao.GenericDao;
 import com.uhsarp.billrive.domain.Payment;
 import javax.annotation.Resource;
 import org.springframework.stereotype.Service;
 
 /**
  *
  * @author Sravan
  */
 @Service("PaymentService")
 public class PaymentService {
//    @Resource(name= "neo4jDAO")
     GenericDao genericDAO;
     
         public Payment createPayment(int userId, Payment paymentObj)
     {
         Payment payment = null;
 //        payment=genericDAO.createPayment(userId, paymentObj);
         return payment;
     }
 }

 package com.in6k.mypal.controller;
 
 import com.in6k.mypal.dao.TransactionDao;
 import com.in6k.mypal.dao.UserDao;
 import com.in6k.mypal.domain.Transaction;
 import com.in6k.mypal.domain.User;
import com.in6k.mypal.service.Inviter;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import javax.servlet.http.HttpServletRequest;
 import java.io.IOException;
 
 @Controller
 @RequestMapping(value = "/founds")
 public class FundsTransfer {
 
     @RequestMapping(value = "/transfer/add", method = RequestMethod.GET)
     public String showTransferPage() {
         return "founds_transfer/foundsTransfer";
     }
 
     @RequestMapping(value = "/transfer/add", method = RequestMethod.POST)
     public String addTransfer(HttpServletRequest request) throws IOException {
 
         User userByEmail = UserDao.getByEmail(request.getParameter("email"));
         User currentUser = UserDao.getByEmail("system@gmail.com");
 
         Transaction transaction = new Transaction();
 
         boolean isUserExist = userByEmail != null;
 
         if(!isUserExist) {
             User user = new User();
             user.setEmail(request.getParameter("email"));
             user.setFirstName("inactive");
             user.setLastName("inactive");
             user.setPassword("inactive");
             user.setActive(false);
 
             UserDao.save(user);
 
             transaction.setDebit(currentUser);
             transaction.setCredit(user);
             transaction.setSum(Double.parseDouble(request.getParameter("transfer_value")));
             TransactionDao.create(transaction);
 
            Inviter.sendEmail(currentUser.getFirstName() + " " + currentUser.getLastName(), user.getEmail(), transaction.getSum());
             return "founds_transfer/foundsTransfer";
         }
 
         transaction.setDebit(currentUser);
         transaction.setCredit(userByEmail);
         transaction.setSum(Double.parseDouble(request.getParameter("transfer_value")));
         TransactionDao.create(transaction);
 
         return "founds_transfer/foundsTransfer";
     }
 }

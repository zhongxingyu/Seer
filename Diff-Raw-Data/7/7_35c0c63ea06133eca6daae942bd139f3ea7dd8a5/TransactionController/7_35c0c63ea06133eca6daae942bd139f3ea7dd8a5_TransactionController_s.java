 package com.in6k.mypal.controller;
 
 import com.in6k.mypal.dao.TransactionDAO;
 import com.in6k.mypal.domain.Transaction;
 import com.in6k.mypal.domain.User;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import java.sql.SQLException;
 
 @Controller
 @RequestMapping(value = "/transaction")
 public class TransactionController {
 
     @RequestMapping(value = "/create", method = RequestMethod.GET)
     public String creationForm(ModelMap model) {
         User user = new User();
         user.setEmail("petrov@gmail.com");
 
         model.addAttribute(user);
         return "transaction/create";
     }
 
     @RequestMapping(value = "/create", method = RequestMethod.POST)
    public String create(@RequestParam("debit") String debit, @RequestParam("credit") String credit, @RequestParam("credit") double sum) {
         Transaction transaction = new Transaction();
         transaction.setDebit(debit);
         transaction.setCredit(credit);
         transaction.setSum(sum);
//        TransactionDAO.
         return "transaction/create";
     }
 
     @RequestMapping(value = "/list")
     public String list() {
         return "transaction/list";
     }
 
     @RequestMapping(value = "/delete")
     public String delete(@RequestParam("id") int id) throws SQLException {
 
         TransactionDAO.delete(id);
         return "transaction/list";
     }
 
 }

 package com.in6k.mypal.controller;
 
 import com.in6k.mypal.dao.TransactionDao;
 import com.in6k.mypal.dao.UserDao;
 import com.in6k.mypal.domain.User;
 import com.in6k.mypal.service.CreditCard.IncreaseBalanсeService;
 import com.in6k.mypal.service.CreditCard.ValidCreditCardService;
 import com.in6k.mypal.service.SessionValidService;
 import com.in6k.mypal.service.TransactionService;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.List;
 
 @Controller
 @RequestMapping(value = "/transaction")
 public class TransactionController {
 
     @RequestMapping(value = "/create", method = RequestMethod.GET)
     public String creationForm(ModelMap model, HttpServletRequest request) {
         HttpSession session = request.getSession();
 
         User userSession = (User) session.getAttribute("LoggedUser");
         if (null == userSession) {
             return "redirect:/login";
         }
 
         model.addAttribute("sess", userSession);
         model.addAttribute("balance", UserDao.getBalance(userSession));
 
         return "transaction/create";
     }
 
     @RequestMapping(value = "/create", method = RequestMethod.POST)
     public String create(HttpServletRequest request) throws IOException {
         HttpSession session = request.getSession();
         TransactionService.create((User) session.getAttribute("LoggedUser"), UserDao.getByEmail(request.getParameter("debit")), request.getParameter("sum"));
         return "redirect:/transaction/create";
     }
 
     @RequestMapping(value = "/history")
     public String history(ModelMap model, HttpServletRequest request) throws IOException, SQLException {
         HttpSession session = request.getSession();
 
         User userSession = (User) session.getAttribute("LoggedUser");
         if (userSession == null) {
             return "redirect:/login";
         }
         model.addAttribute("sess", userSession);
         model.addAttribute("balance", UserDao.getBalance(userSession));
         model.addAttribute("transactions", TransactionDao.findAllForUser(userSession));
 
         return "transaction/list";
     }
 
     @RequestMapping(value = "/list")
     public String list(ModelMap model, HttpServletRequest request) throws IOException, SQLException {
         HttpSession session = request.getSession();
 
         User userSession = (User) session.getAttribute("LoggedUser");
         if (userSession == null) {
             return "redirect:/login";
         }
         model.addAttribute("sess", userSession);
         //model.addAttribute("transactions", TransactionDao.list());
         model.addAttribute("transactions", TransactionDao.list());
 
         return "transaction/list";
     }
 
     @RequestMapping(value = "/delete")
     public String delete(@RequestParam("id") int id) throws SQLException {
 
         TransactionDao.delete(id);
         return "transaction/list";
     }
 
     @RequestMapping(value = "/create/creditfromcard", method = RequestMethod.POST)
     public String createTransactionDebetFromCard(HttpServletRequest request,
                                                  @RequestParam("card_number") String cardNumber, @RequestParam("expiry_date") String expiryDate,
                                                  @RequestParam("name_on_card") String nameOnCard, @RequestParam("sum") String sum,
                                                  @RequestParam("cvv") String cvv, @RequestParam("id_Account") int id,
                                                  ModelMap model) throws IOException {
         HttpSession session = request.getSession();
         ValidCreditCardService isValidCard = new ValidCreditCardService();
         List validateCardInfo = isValidCard.validateCardInfo(cardNumber, sum);
 
         if ((validateCardInfo.size()>0)){
             model.addAttribute("validateCardInfo", validateCardInfo);
             return "creditcard/create";
         }
 
         boolean fromCard = true;
 
         IncreaseBalanсeService.moneyFromCreditCard(cardNumber, sum, SessionValidService.ValidUser(session).getId(), fromCard);
 
         return "creditcard/create";
     }
 
     @RequestMapping(value = "/create/creditfromcard", method = RequestMethod.GET)
     public String creationFormDebetFromCard(HttpServletRequest request, ModelMap model){
         HttpSession session = request.getSession();
 
         if (SessionValidService.ValidUser(session) == null) {
             return "redirect:/login";
         }
 
         return "creditcard/create";
     }
 
     @RequestMapping(value = "/create/debitedtothecard", method = RequestMethod.GET)
     public String creationDebitedToTheCard(HttpServletRequest request, ModelMap model){
         HttpSession session = request.getSession();
 
         if (SessionValidService.ValidUser(session) == null) {
             return "redirect:/login";
         }
 
         return "creditcard/transfer";
     }
 
     @RequestMapping(value = "/create/debitedtothecard", method = RequestMethod.POST)
     public String createTransactionDebitedToTheCard(HttpServletRequest request,
                                                     @RequestParam("card_number") String cardNumber,
                                                     @RequestParam("sum") String sum,
                                                     @RequestParam("id_Account") int id, ModelMap model){
         HttpSession session = request.getSession();
         ValidCreditCardService isValidCard = new ValidCreditCardService();
         List validateCardInfo = isValidCard.validateCardInfo(cardNumber, sum);
 
         if ((validateCardInfo.size()>0)){
             model.addAttribute("validateCardInfo", validateCardInfo);
             return "creditcard/transfer";
         }
 
         boolean fromCard = false;
 
         IncreaseBalanсeService.moneyFromCreditCard(cardNumber, sum, SessionValidService.ValidUser(session).getId(), fromCard);
 
         return "creditcard/transfer";
     }
 }

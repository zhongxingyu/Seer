 package com.exadel.borsch.web.controllers;
 
 import com.exadel.borsch.checker.OrderChangesHolder;
 import com.exadel.borsch.entity.*;
 import com.exadel.borsch.managers.ManagerFactory;
 import com.exadel.borsch.managers.OrderManager;
 import com.exadel.borsch.managers.PriceManager;
 import com.exadel.borsch.notifier.PayOrderNotifier;
 import com.exadel.borsch.util.DateTimeUtils;
 import com.exadel.borsch.web.users.UserUtils;
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormat;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.access.annotation.Secured;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import java.security.Principal;
 import java.util.List;
 
 import org.joda.time.Weeks;
 
 /**
  *
  * @author Vlad
  */
 @Controller
 public class HomeController {
     @Autowired
     private ManagerFactory managerFactory;
     @Autowired
     private PayOrderNotifier notifier;
 
     public void fillInPageModel(Model model, Principal principal, DateTime date) {
         OrderManager orderManager = managerFactory.getOrderManager();
         User user = UserUtils.getUserByPrincipal(principal);
 
         DateTime prevWeek = date.minusWeeks(1);
         if (orderManager.findOrderAtDateForUser(user, prevWeek) != null) {
             model.addAttribute("prevWeek", prevWeek.toString("dd-MM-yyy"));
         }
         DateTime nextWeek = date.plusWeeks(1);
         if (orderManager.findOrderAtDateForUser(user, nextWeek) != null) {
             model.addAttribute("nextWeek", nextWeek.toString("dd-MM-yyy"));
         }
         model.addAttribute("currentWeekCode",
                 Weeks.weeksBetween(DateTimeUtils.getStartOfNextWeek(), date).getWeeks() + 2);
     }
 
     @Secured("ROLE_EDIT_MENU_SELF")
     @RequestMapping("/home")
     public String processPageRequest(Model model, Principal principal) {
         OrderManager orderManager = managerFactory.getOrderManager();
         User user = UserUtils.getUserByPrincipal(principal);
 
        Order order = orderManager.findOrderAtDateForUser(user,DateTime.now().plusWeeks(1));
         model.addAttribute(order);
 
         fillInPageModel(model, principal, DateTimeUtils.getStartOfNextWeek());
 
         return ViewURLs.HOME_PAGE;
     }
 
     @Secured("ROLE_EDIT_MENU_SELF")
     @RequestMapping("/home/{date}")
     public String processPageRequestForDate(Model model, Principal principal,
         @PathVariable String date) {
         OrderManager orderManager = managerFactory.getOrderManager();
         User user = UserUtils.getUserByPrincipal(principal);
 
         DateTime orderDate = DateTime.parse(date, DateTimeFormat.forPattern("dd-MM-yyy"));
         Order order = orderManager.findOrderAtDateForUser(user, orderDate);
         if (order == null) {
             return "redirect:/home";
         }
 
         fillInPageModel(model, principal, orderDate);
 
         model.addAttribute(order);
         return ViewURLs.HOME_PAGE;
     }
 
     @RequestMapping("/gentestorders")
     public String processGenTestOrders() {
         OrderManager orderManager = managerFactory.getOrderManager();
         // Test data
         User admin = managerFactory.getUserManager().getUserByLogin("admin");
         Order testOrder1 = orderManager.getCurrentOrderForUser(admin);
         testOrder1.setStartDate(testOrder1.getStartDate().minusWeeks(1));
         testOrder1.setEndDate(testOrder1.getEndDate().minusWeeks(1));
 
         orderManager.deleteOrderById(testOrder1.getId());
 
         Order testOrder2 = orderManager.getCurrentOrderForUser(admin);
         testOrder2.setStartDate(testOrder2.getStartDate().minusWeeks(2));
         testOrder2.setEndDate(testOrder2.getEndDate().minusWeeks(2));
 
         orderManager.deleteOrderById(testOrder2.getId());
 
         orderManager.getCurrentOrderForUser(admin);
         orderManager.addOrder(testOrder1);
         orderManager.fillOrderWithItems(testOrder1);
         orderManager.addOrder(testOrder2);
         orderManager.fillOrderWithItems(testOrder2);
 
         return "redirect:/home";
     }
 
     @Secured("ROLE_EDIT_MENU_SELF")
     @RequestMapping("/")
     public String processDefaultPageRequest(Model model, Principal principal) {
         return "redirect:/home";
     }
 
     @ResponseBody
     @Secured("ROLE_EDIT_MENU_SELF")
     @RequestMapping("/home/orders/{day}")
     public List<Dish> processOrderRequest(Principal principal, @PathVariable int day) {
 
         OrderManager orderManager = managerFactory.getOrderManager();
         User user = UserUtils.getUserByPrincipal(principal);
 
         Order order = orderManager.getCurrentOrderForUser(user);
         if (day < 0 || day > order.getOrder().size()) {
             return null;
         }
 
         return order.getOrder().get(day).getChoices();
     }
 
     @ResponseBody
     @Secured("ROLE_EDIT_MENU_SELF")
     @RequestMapping("/home/orders/{date}/{itemId}")
     public OrderResult processOrderModification(Principal principal, @PathVariable String date,
         @PathVariable Long itemId) {
         OrderChange change;
         PriceManager priceManager = managerFactory.getPriceManager();
         OrderManager orderManager = managerFactory.getOrderManager();
         User user = UserUtils.getUserByPrincipal(principal);
 
 
         Dish dish = priceManager.getCurrentPriceList().getDishById(itemId);
         DateTime orderDate = DateTime.parse(date, DateTimeFormat.forPattern("dd-MM-yyy"));
         Order order = orderManager.findOrderAtDateForUser(user, orderDate);
         int day = orderDate.getDayOfWeek() - 1;
         if (dish == null || order == null || day >= order.getOrder().size()) {
             return new OrderResult("fail", null);
         }
 
         MenuItem menuItem = order.getOrder().get(day);
         if (menuItem.getChoices().contains(dish)) {
             menuItem.removeDish(dish);
             orderManager.removeDishFormMenuItem(menuItem, dish);
             change = new OrderChange(dish.getId(), user.getId(), menuItem.getId(),
                     DateTime.now(), ChangeAction.REMOVED_DISH);
             OrderChangesHolder.addChange(change);
             return new OrderResult("removed", dish);
         }
 
         menuItem.addDish(dish);
         orderManager.addDishFormMenuItem(menuItem, dish);
         change = new OrderChange(dish.getId(), user.getId(), menuItem.getId(),
                 DateTime.now(), ChangeAction.ADDED_NEW_DISH);
         OrderChangesHolder.addChange(change);
         return new OrderResult("added", dish);
     }
 
     public static class OrderResult {
         private String status;
         private Dish dish;
 
         public OrderResult(String status, Dish dish) {
             this.status = status;
             this.dish = dish;
         }
 
         public String getStatus() {
             return status;
         }
 
         public void setStatus(String status) {
             this.status = status;
         }
 
         public Dish getDish() {
             return dish;
         }
 
         public void setDish(Dish dish) {
             this.dish = dish;
         }
 
     }
 }

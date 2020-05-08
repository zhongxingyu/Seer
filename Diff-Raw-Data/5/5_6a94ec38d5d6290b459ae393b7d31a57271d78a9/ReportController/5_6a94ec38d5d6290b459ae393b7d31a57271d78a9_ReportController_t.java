 package com.exadel.borsch.web.controllers;
 
 import com.exadel.borsch.dao.MenuItem;
 import com.exadel.borsch.dao.Order;
 import com.exadel.borsch.dao.User;
 import com.exadel.borsch.managers.ManagerFactory;
 import com.exadel.borsch.managers.MenuManager;
 import com.exadel.borsch.util.DateTimeUtils;
 import com.google.common.collect.ArrayListMultimap;
 import com.google.common.collect.ListMultimap;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.access.prepost.PreAuthorize;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.UUID;
 
 /**
  * @author Andrey Zhilka
  */
 @Controller
 public class ReportController {
 
     @Autowired
     private ManagerFactory managerFactory;
 
     @ResponseBody
     @RequestMapping(value = "/report/setPaid/{orderId}/{menuId}", method = RequestMethod.POST)
     public AjaxResponse processAjaxRequest(@PathVariable String orderId, @PathVariable String menuId) {
         MenuManager menuManager = managerFactory.getMenuManager();
         AjaxResponse response = new AjaxResponse();
 
         Order order = menuManager.getOrderById(UUID.fromString(orderId));
         MenuItem menuItem = order.getMenuById(UUID.fromString(menuId));
         menuItem.setIsPaid(true);
         menuManager.updateOrder(order);
         response.setResponseSucceed(true);
 
         return response;
     }
 
     @PreAuthorize("hasRole('ROLE_PRINT_ORDER')")
     @RequestMapping("/report")
     public String processPageRequest(ModelMap model) {
         MenuManager menuManager = managerFactory.getMenuManager();
         ListMultimap<Integer, DailyOrder> report = ArrayListMultimap.create();
         List<Order> allOrders;
 
         allOrders = menuManager.getAllOrders();
 
         for (Order order : allOrders) {
             order.sortOrderByWeekday();
             for (MenuItem item : order.getOrder()) {
                if (item.getChoices().isEmpty()) {
                     continue;
                 }
                 DailyOrder daySummary = new DailyOrder();
                 daySummary.setMenuItem(item);
                 daySummary.setUser(order.getOwner());
                 daySummary.setWeekDay(item.getDate().getDayOfWeek());
                 daySummary.setTotal(item.getTotalPrice());
                 daySummary.setWeekOrderId(order.getId());
                 report.put(item.getDate().getDayOfWeek() - 1, daySummary);
             }
         }
 
         List<List<DailyOrder>> reportFinalVersion = new ArrayList<List<DailyOrder>>();
 
         for (int i = 0; i < DateTimeUtils.WORKING_DAYS_IN_WEEK; i++) {
             reportFinalVersion.add(report.get(i));
         }
 
         model.addAttribute("report", reportFinalVersion);
         model.addAttribute("workingDays", DateTimeUtils.WORKING_DAYS_IN_WEEK);
 
         return ViewURLs.WEEK_ORDER_REPORT;
     }
 
    public static class AjaxResponse {
 
         private boolean responseSucceed = true;
 
         public boolean getResponseSucceed() {
             return this.responseSucceed;
         }
 
         public void setResponseSucceed(boolean status) {
             this.responseSucceed = status;
         }
     }
 
     public static class DailyOrder {
         private Integer weekDay;
         private User user;
         private MenuItem menuItem;
         private Integer total;
         private UUID weekOrderId;
 
 
         public Integer getWeekDay() {
             return weekDay;
         }
 
         public void setWeekDay(Integer weekDay) {
             this.weekDay = weekDay;
         }
 
         public UUID getWeekOrderId() {
             return weekOrderId;
         }
 
         public void setWeekOrderId(UUID weekOrderId) {
             this.weekOrderId = weekOrderId;
         }
 
         public Integer getTotal() {
             return total;
         }
 
         public void setTotal(Integer total) {
             this.total = total;
         }
 
         public User getUser() {
             return user;
         }
 
         public void setUser(User user) {
             this.user = user;
         }
 
         public MenuItem getMenuItem() {
             return menuItem;
         }
 
         public void setMenuItem(MenuItem menuItem) {
             this.menuItem = menuItem;
         }
     }
 }

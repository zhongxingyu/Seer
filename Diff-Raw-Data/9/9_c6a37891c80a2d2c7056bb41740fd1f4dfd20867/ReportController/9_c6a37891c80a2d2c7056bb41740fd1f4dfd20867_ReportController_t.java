 
 package com.exadel.borsch.web.controllers;
 
 import com.exadel.borsch.entity.*;
 import com.exadel.borsch.managers.*;
 import com.exadel.borsch.util.DateTimeUtils;
 import com.google.common.collect.ArrayListMultimap;
 import com.google.common.collect.ListMultimap;
 import org.joda.time.DateTime;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.access.annotation.Secured;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import java.util.*;
 
 /**
  * @author Andrey Zhilka
  */
 @Controller
 public class ReportController {
     @Autowired
     private ManagerFactory managerFactory;
 
     @Secured("ROLE_PRINT_ORDER")
     @ResponseBody
     @RequestMapping(value = "/report/setPaid/{orderId}/{menuId}", method = RequestMethod.POST)
     public void processAjaxRequest(@PathVariable Long orderId, @PathVariable Long menuId) {
         OrderManager orderManager = managerFactory.getOrderManager();
         Order order = orderManager.getOrderById(orderId);
         MenuItem menuItem = order.getMenuById(menuId);
         menuItem.setIsPaid(true);
         orderManager.updateOrder(order, menuItem);
     }
     @Secured("ROLE_PRINT_ORDER")
     @RequestMapping("/orderTable")
     public String loadOrderTable(ModelMap model) {
         OrderManager orderManager = managerFactory.getOrderManager();
         UserManager userManager = managerFactory.getUserManager();
         List<User> users = userManager.getAllUsers();
         ListMultimap<User, MenuItem> orders = ArrayListMultimap.create();
         List<String> date = new ArrayList<>();
         Map<Integer, Integer> totalPrice = new TreeMap<>();
         boolean flag = true;
         for (User user : users) {
             List<MenuItem> items = orderManager.getCurrentOrderForUser(user).getOrder();
             for (MenuItem item : items) {
                 if (flag) {
                     date.add(item.getDate().dayOfMonth().get() + "."
                             + item.getDate().monthOfYear().get() + "."
                             + item.getDate().year().get());
                 }
                 int day = item.getDate().dayOfMonth().get();
                 if (!totalPrice.containsKey(day)) {
                     totalPrice.put(day, item.getTotalPrice());
                 } else {
                     totalPrice.put(day, totalPrice.get(day) + item.getTotalPrice());
                 }
                 orders.put(user, item);
             }
             flag = false;
         }
         model.addAttribute("date", date);
         model.addAttribute("orders", orders.asMap());
         model.addAttribute("totalPrice", totalPrice);
         return ViewURLs.ORDER_TABLE;
     }
     @Secured("ROLE_PRINT_ORDER")
     @RequestMapping("/report/{week}")
     public String processPageRequest(ModelMap model, @PathVariable Integer week) {
         OrderManager orderManager = managerFactory.getOrderManager();
         ListMultimap<Integer, DailyOrder> report = ArrayListMultimap.create();
         List<Order> allOrders;
 
         DateTime startOfWeek;
         switch (week) {
             case 0 : //previous week
                 startOfWeek = DateTimeUtils.getStartOfWeek(DateTime.now().minusDays(DateTimeUtils.DAYS_IN_WEEK));
                 break;
             case 1 : //current week
                 startOfWeek = DateTimeUtils.getStartOfCurrentWeek();
                 break;
             case 2 : //next week
                 startOfWeek = DateTimeUtils.getStartOfNextWeek();
                 break;
             default:
                 startOfWeek = DateTimeUtils.getStartOfCurrentWeek();
                 break;
         }
         allOrders = orderManager.getAllOrders(startOfWeek);
 
         for (Order order : allOrders) {
             for (MenuItem item : order.getOrder()) {
                 if (item.getChoices().isEmpty()) {
                     continue;
                 }
                 DailyOrder daySummary = DailyOrder.mapOrderAndItemToDailyOrder(item, order);
                 report.put(item.getDate().getDayOfWeek() - 1, daySummary);
             }
         }
 
         model.addAttribute("week", week);
         model.addAttribute("report", report.asMap());
 
         return ViewURLs.WEEK_ORDER_REPORT;
     }
 
     @Secured("ROLE_PRINT_ORDER")
     @RequestMapping("/report/summary")
     public String processSummaryRequest(ModelMap model) {
         OrderManager orderManager = managerFactory.getOrderManager();
         int today = DateTime.now().getDayOfWeek();
         Map<Integer, Map<String, Integer>> summary = new TreeMap<>();
         for (int i = today; i <= DateTimeUtils.WORKING_DAYS_IN_WEEK; i++) {
             summary.put(i, new HashMap<String, Integer>());
         }
         DateTime startOfWeek = DateTimeUtils.getStartOfCurrentWeek();
         for (Order order : orderManager.getAllOrders(startOfWeek)) {
             for (MenuItem item : order.getOrder()) {
                 if (item.getDate().isBeforeNow()) {
                     continue; // do not show past orders
                 }
                 Integer dayOfWeek = item.getDate().getDayOfWeek();
                 Map<String, Integer> dayOrders = summary.get(dayOfWeek);
                 for (Dish dish : item.getChoices()) {
                     String dishName = dish.getName();
                     if (!dayOrders.containsKey(dishName)) {
                         dayOrders.put(dishName, 1);
                     } else {
                         dayOrders.put(dishName, dayOrders.get(dishName) + 1);
                     }
                 }
             }
         }
 
         model.addAttribute("summary", summary);
 
         return ViewURLs.ORDERS_SUMMARY_PAGE;
     }
 
     @RequestMapping("/report/changes")
     public String processChangesRequest(ModelMap model) {
         OrderChangeManager changeManager = managerFactory.getChangeManager();
         ListMultimap<Integer, ChangeJSON> report = ArrayListMultimap.create();
         List<OrderChange> changes = changeManager.getActualChanges();
 
         for (OrderChange change : changes) {
             report.put(change.getDateOfChange().getDayOfWeek() - 1, mapChangeToJson(change));
         }
 
         model.addAttribute("report", report.asMap());
         return ViewURLs.CHANGES_REPORT;
     }
 
     private ChangeJSON mapChangeToJson(OrderChange change) {
         UserManager userManager = managerFactory.getUserManager();
         OrderManager orderManager = managerFactory.getOrderManager();
 
         MenuItem item = new MenuItem();
         ChangeJSON json = new ChangeJSON();
         User user = userManager.getUserById(change.getActedUserId());
         json.setUserName(user.getName());
 
         json.setCommittedAction(change.getCommittedAction());
 
         for (Order order : orderManager.getOrdersForUser(user)) {
             item = order.getMenuById(change.getMenuItemId());
             if (!item.getChoices().isEmpty()) {
                 break;
             }
         }
 
         for (Dish dish : item.getChoices()) {
            if (dish.getId().equals(change.getChangedDishId())) {
                 json.setDishName(dish.getName());
             }
         }
 
         return json;
     }
 
 
    public static class ChangeJSON {
        private String userName;
        private String dishName;
        private ChangeAction committedAction;
 
        public String getUserName() {
            return userName;
        }
 
        public void setUserName(String userName) {
            this.userName = userName;
        }
 
        public String getDishName() {
            return dishName;
        }
 
        public void setDishName(String dishName) {
            this.dishName = dishName;
        }
 
        public ChangeAction getCommittedAction() {
            return committedAction;
        }
 
        public void setCommittedAction(ChangeAction committedAction) {
            this.committedAction = committedAction;
        }
    }
 
     public static class DailyOrder {
         private Integer weekDay;
         private User user;
         private MenuItem menuItem;
         private Integer total;
         private Long weekOrderId;
 
 
         public Integer getWeekDay() {
             return weekDay;
         }
 
         public void setWeekDay(Integer weekDay) {
             this.weekDay = weekDay;
         }
 
         public Long getWeekOrderId() {
             return weekOrderId;
         }
 
         public void setWeekOrderId(Long weekOrderId) {
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
 
         public static DailyOrder mapOrderAndItemToDailyOrder(MenuItem item, Order order) {
             DailyOrder daySummary = new DailyOrder();
             daySummary.setMenuItem(item);
             daySummary.setUser(order.getOwner());
             daySummary.setWeekDay(item.getDate().getDayOfWeek());
             daySummary.setTotal(item.getTotalPrice());
             daySummary.setWeekOrderId(order.getId());
             return daySummary;
         }
     }
 }

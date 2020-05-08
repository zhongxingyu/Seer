 package com.exadel.borsch.managers.simple;
 
 import com.exadel.borsch.dao.Order;
 import com.exadel.borsch.dao.PriceList;
 import com.exadel.borsch.dao.User;
 import com.exadel.borsch.managers.ManagerFactory;
 import com.exadel.borsch.managers.MenuManager;
 import com.exadel.borsch.managers.PriceManager;
 import com.exadel.borsch.managers.UserManager;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.UUID;
 
 /**
  *
  * @author Vlad
  */
 public class SimpleManagerFactory implements ManagerFactory {
     private static SimpleUserManager simpleUserManager = null;
     private static SimpleMenuManager simpleMenuManager = null;
     private static SimplePriceManager simplePriceManager = null;
     @Override
     public UserManager getUserManager() {
         if (simpleUserManager == null) {
             simpleUserManager = new SimpleUserManager();
         }
         return simpleUserManager;
     }
 
     @Override
     public MenuManager getMenuManager() {
         if (simpleMenuManager == null) {
             simpleMenuManager = new SimpleMenuManager();
         }
         return simpleMenuManager;
     }
 
     @Override
     public PriceManager getPriceManager() {
         if (simplePriceManager == null) {
             simplePriceManager = new SimplePriceManager();
         }
         return simplePriceManager;
     }
 
    private class SimpleUserManager implements UserManager {
         private List<User> users;
 
         SimpleUserManager() {
             // TODO read frome file
             users = new ArrayList<>();
         }
         @Override
         public User getUserById(UUID userId) {
             for (User user : users) {
                 if (user.getId().equals(userId)) {
                     return user;
                 }
             }
             User.LOGGER.error("User with id=" + userId + " does not exist.");
             return null;
         }
 
         @Override
         public void deleteUserById(UUID userId) {
             ListIterator<User> iter = users.listIterator();
             while (iter.hasNext()) {
                 User curUser = iter.next();
                 if (curUser.getId().equals(userId)) {
                     iter.remove();
                     return;
                 }
             }
             User.LOGGER.error("User with id=" + userId + " does not exist.");
         }
 
         @Override
         public void updateUser(User toUpdate) {
             ListIterator<User> iter = users.listIterator();
             while (iter.hasNext()) {
                 User curUser = iter.next();
                 if (curUser.getId().equals(toUpdate.getId())) {
                     iter.set(toUpdate);
                     return;
                 }
             }
             User.LOGGER.error("User with id=" + toUpdate.getId() + " does not exist.");
         }
 
         @Override
         public User getUserByHash(String hashId) {
              for (User user : users) {
                 if (user.getHash().equals(hashId)) {
                     return user;
                 }
             }
             User.LOGGER.error("User with hash=" + hashId + " does not exist.");
             return null;
         }
 
         @Override
         public void deleteUserByHash(String hashId) {
             ListIterator<User> iter = users.listIterator();
             while (iter.hasNext()) {
                 User curUser = iter.next();
                 if (curUser.getHash().equals(hashId)) {
                     iter.remove();
                     return;
                 }
             }
             User.LOGGER.error("User with hash=" + hashId + " does not exist.");
         }
     }
    private class SimpleMenuManager implements MenuManager {
         private List<Order> orders;
         SimpleMenuManager() {
             // TODO read frome file
             orders = new ArrayList<>();
         }
         @Override
         public void updateOrder(Order toUpdate) {
             ListIterator<Order> iter = orders.listIterator();
             while (iter.hasNext()) {
                 Order curOrder = iter.next();
                 if (curOrder.getHash().equals(toUpdate.getHash())) {
                     iter.set(toUpdate);
                     return;
                 }
             }
         }
 
         @Override
         public void deleteOrderById(String hashId) {
             ListIterator<Order> iter = orders.listIterator();
             while (iter.hasNext()) {
                 Order curOrder = iter.next();
                 if (curOrder.getHash().equals(hashId)) {
                     iter.remove();
                     return;
                 }
             }
         }
 
         @Override
         public Order getOrderById(String hashId) {
             for (Order order : orders) {
                 if (order.getHash().equals(hashId)) {
                     return order;
                 }
             }
             return null;
         }
     }
    private class SimplePriceManager implements PriceManager {
         private List<PriceList> prices;
         SimplePriceManager() {
             // TODO read from file
             prices = new ArrayList<>();
         }
         @Override
         public PriceList getPriceListById(String hashId) {
             for (PriceList priceList : prices) {
                 if (priceList.getHash().equals(hashId)) {
                     return priceList;
                 }
             }
             return null;
         }
 
         @Override
         public void deletePriceListById(String hashId) {
             ListIterator<PriceList> iter = prices.listIterator();
             while (iter.hasNext()) {
                 PriceList curPriceList = iter.next();
                 if (curPriceList.getHash().equals(hashId)) {
                     iter.remove();
                     return;
                 }
             }
         }
 
         @Override
         public void addPriceList(PriceList toAdd) {
             prices.add(toAdd);
         }
 
         @Override
         public void updatePriceList(PriceList toUpdate) {
             ListIterator<PriceList> iter = prices.listIterator();
             while (iter.hasNext()) {
                 PriceList curPriceList = iter.next();
                 if (curPriceList.getHash().equals(toUpdate.getHash())) {
                     iter.set(toUpdate);
                     return;
                 }
             }
         }
     }
 }

 package com.exadel.borsch.managers.impl.jdbc;
 
 import com.exadel.borsch.dao.ChoicesDao;
 import com.exadel.borsch.dao.DishDao;
 import com.exadel.borsch.dao.OrderChangeDao;
 import com.exadel.borsch.dao.PriceDao;
 import com.exadel.borsch.entity.Course;
 import com.exadel.borsch.entity.Dish;
 import com.exadel.borsch.entity.PriceList;
 import com.exadel.borsch.managers.PriceManager;
 import org.joda.time.DateTime;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.util.Collections;
 import java.util.List;
 import java.util.ListIterator;
 
 /**
  * @author Vlad
  */
 @Service("jdbcPriceManager")
 @Scope(value = "singleton")
 public class JdbcPriceManager implements PriceManager {
 
     @Autowired
     private PriceDao priceDao;
 
     @Autowired
     private DishDao dishDao;
 
     @Autowired
     private ChoicesDao choicesDao;
 
     @Autowired
     private OrderChangeDao changesDao;
 
     @Override
     @Transactional(readOnly = true)
     public PriceList getPriceListById(Long id) {
         PriceList priceList = priceDao.getById(id);
 
         if (priceList != null) {
             priceList.addDishes(dishDao.getAllByPriceListId(id));
             return priceList;
         }
 
         priceList = new PriceList();
 
         priceDao.save(priceList);
 
         return priceList;
     }
 
     @Override
     @Transactional(propagation = Propagation.REQUIRED)
     public PriceList getCurrentPriceList() {
         List<PriceList> priceLists = priceDao.getAll();
         if (priceLists.isEmpty()) {
             PriceList newPriceList = new PriceList();
 
             priceDao.save(newPriceList);
 
             priceLists.add(newPriceList);
         }
 
         for (PriceList priceList : priceLists) {
             priceList.addDishes(
                     dishDao.getAllByPriceListId(priceList.getId())
             );
         }
         return priceLists.get(priceLists.size() - 1);
     }
 
     @Override
     @Transactional(readOnly = true)
     public PriceList getPriceListByCreationTime(DateTime time) {
         List<PriceList> priceLists = priceDao.getPriceListByCreationTime(time);
         if (priceLists.isEmpty()) {
             return null;
         } else {
            PriceList priceList = priceLists.get(0);

            priceList.addDishes(
                    dishDao.getAllByPriceListId(priceList.getId())
            );

            return priceList;
         }
     }
 
     @Override
     @Transactional(propagation = Propagation.REQUIRED)
     public void deletePriceListById(Long id) {
         for (Dish dish : dishDao.getAllByPriceListId(id)) {
             changesDao.deleteAllByDishId(dish.getId());
             dishDao.delete(dish.getId());
         }
 
         priceDao.delete(id);
     }
 
     @Override
     @Transactional(propagation = Propagation.REQUIRED)
     public void addPriceList(PriceList toAdd) {
         priceDao.save(toAdd);
     }
 
     @Override
     @Transactional(propagation = Propagation.REQUIRED)
     public void updatePriceList(PriceList toUpdate) {
         priceDao.update(toUpdate);
     }
 
     @Override
     @Transactional(propagation = Propagation.REQUIRED)
     public void addDishToPriceList(Dish dish, PriceList priceList) {
         dishDao.save(dish);
         dishDao.setPriceList(dish.getId(), priceList.getId());
     }
 
     @Override
     @Transactional(propagation = Propagation.REQUIRED)
     public void updateDishInPriceList(Dish dish, PriceList priceList) {
         dishDao.update(dish);
     }
 
     @Override
     @Transactional(propagation = Propagation.REQUIRED)
     public void removeDishFromPriceList(Dish dish, PriceList priceList) {
         choicesDao.delete(dish.getId());
 
         dishDao.delete(dish.getId());
     }
 
     @Override
     @Transactional(readOnly = true, propagation = Propagation.NEVER)
     public List<PriceList> getAllPriceLists() {
         List<PriceList> priceLists = priceDao.getAll();
 
         /*if (priceLists.isEmpty()) {
             priceLists.add(init());
         }*/
         ListIterator<PriceList> it = priceLists.listIterator();
 
         while (it.hasNext()) {
             PriceList curPriceList = it.next();
 
             curPriceList.addDishes(dishDao.getAllByPriceListId(curPriceList.getId()));
         }
 
         return Collections.unmodifiableList(priceLists);
     }
 
     @Transactional(propagation = Propagation.REQUIRED)
     private PriceList init() {
         PriceList newList = new PriceList();
 
         priceDao.save(newList);
         //CHECKSTYLE:OFF
         Object[][] listData = {
                 {Course.DESSERT, "Торт", "Отличный старый добрый торт! Прямо из пекарни! (столовая)", "cake.jpg.to", 59600},
                 {Course.DESSERT, "Блинчики с джемом", "Очень вкусные, и в отличие от блинчиков с ветчиной - не содержат кошек!", "pancakes.jpg.to", 10000},
                 {Course.FIRST_COURSE, "Боорщъ", "Просто борщ", "borsch.jpg.to", 7950},
                 {Course.FIRST_COURSE, "Суп", "Это не борщ. Не заказывайте.", "soup.jpg.to", 7000},
                 {Course.SECOND_COURSE, "Макароны", "Длинные такие, твердые.", "spaghetti.jpg.to", 3700},
                 {Course.SECOND_COURSE, "Картофель фри", "Mega edition. Специально от нашей столовой! Почти как в макдональдсе!", "potatoes.jpg.to", 4200},
                 {Course.SECOND_COURSE, "Драники", "Настоящие белорусские! Без пояснений", "draniki.jpg.to", 5400},
                 {Course.SECOND_COURSE, "Котлеты", "Из них делают блины. В том числе.", "http://котлет.jpg.to/", 19000}
         };
         for (int i = 0; i < listData.length; i++) {
             Dish dish = new Dish();
             dish.setCourse((Course) listData[i][0]);
             dish.setName((String) listData[i][1]);
             dish.setDescription((String) listData[i][2]);
             dish.setPhotoUrl((String) listData[i][3]);
             dish.setPrice((int) listData[i][4]);
 
             dishDao.save(dish);
             dishDao.setPriceList(dish.getId(), newList.getId());
 
             newList.addDish(dish);
         }
         //CHECKSTYLE:ON
         return newList;
     }
 }

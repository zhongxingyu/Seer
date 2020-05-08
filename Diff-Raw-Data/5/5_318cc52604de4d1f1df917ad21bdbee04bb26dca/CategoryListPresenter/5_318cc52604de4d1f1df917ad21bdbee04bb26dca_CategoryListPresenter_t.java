 package com.bulbview.recipeplanner.ui.presenter;
 
 import java.util.Collection;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.config.BeanDefinition;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Component;
 
 import com.bulbview.recipeplanner.datamodel.Item;
 import com.bulbview.recipeplanner.datamodel.ItemCategory;
 import com.bulbview.recipeplanner.persistence.DaoException;
 import com.bulbview.recipeplanner.persistence.EntityDao;
 import com.bulbview.recipeplanner.service.ItemService;
 import com.bulbview.recipeplanner.ui.manager.CategorisedItemList;
 
 /**
  * Created per view category.
  */
 @Component(value = "underlyingCategoryListPresenter")
 @Scope(BeanDefinition.SCOPE_PROTOTYPE)
 public class CategoryListPresenter extends Presenter<CategorisedItemList> implements ICategoryListPresenter {
     
     private ItemCategory            category;
     @Autowired
     private EntityDao<ItemCategory> categoryDao;
     @Autowired
     private ItemService             itemService;
     
     @Override
     public void addItem(final Item item) {
         getView().addListItem(item);
     }
     
     @Override
     public void addItemByName(final String itemName) {
         Item savedItem;
         try {
             savedItem = itemService.save(createItem(itemName));
         }
         catch (final DaoException e) {
             getView().showErrorMessage(e.getMessage());
         }
     }
     
     @Override
     public ItemCategory getCategory() {
         return category;
     }
     
     @Override
     public void init() {
         logger.debug("Registering for new item events with itemService...");
     }
     
     @Override
     public void setCategory(final String categoryName) {
         this.category = categoryDao.getByName(categoryName);
         addItemsToView(retrievePersistedItems());
     }
     
     private void addItemsToView(final Collection<Item> categoryItems) {
         for (final Item item : categoryItems) {
             addItem(item);
         }
     }
     
     private Item createItem(final String itemName) {
         logger.debug("Adding item: {}, category: {}", itemName, category.getName());
         final Item item = new Item();
         item.setName(itemName);
         item.setCategory(category);
         return item;
     }
     
     private Collection<Item> retrievePersistedItems() {
         final Collection<Item> categoryItems = itemService.getAllFor(category);
         logger.debug("{} items retrieved: {}", category, categoryItems.size());
         return categoryItems;
     }
 }

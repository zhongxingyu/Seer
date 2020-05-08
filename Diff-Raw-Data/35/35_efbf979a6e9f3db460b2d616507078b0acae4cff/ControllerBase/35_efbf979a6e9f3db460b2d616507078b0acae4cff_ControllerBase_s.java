 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.xpresstek.roster2.web;
 
 import java.io.Serializable;
 import java.util.ResourceBundle;
import javax.ejb.EJB;
 import javax.faces.model.DataModel;
 import javax.faces.model.ListDataModel;
 import javax.faces.model.SelectItem;
 import net.xpresstek.roster2.web.util.JsfUtil;
 import net.xpresstek.roster2.web.util.PaginationHelper;
 
 /**
  *
  * @author apavlune
  */
 public abstract class ControllerBase implements Serializable {
 
     protected DataModel items = null;
     protected PaginationHelper pagination;
     protected int selectedItemIndex;
     private String status;
 
     public void create() {
         try {
             getFacade().create(getCurrent());
             recreatePagination();
             recreateModel();
             status = "Item Created!";
             JsfUtil.addSuccessMessage(status);
         } catch (Exception e) {
             status = e + ResourceBundle.getBundle("/Bundle").
                     getString("PersistenceErrorOccured");
             JsfUtil.addErrorMessage(e, status);
         }
     }
 
     public Object getSelected() {
         if (getCurrent() == null) {
             createNewCurrent();
             selectedItemIndex = -1;
         }
         return getCurrent();
     }
 
     public String getStatus() {
         return status;
     }
 
     public void destroy() {
         if (getCurrent() != null) {
             performDestroy();
             recreatePagination();
             recreateModel();
         }
     }
 
     public void destroyAndView() {
         performDestroy();
         recreateModel();
         updateCurrentItem();
         recreateModel();
     }
 
     abstract AbstractFacade getFacade();
 
     abstract Object getCurrent();
 
     abstract void setCurrent(Object obj);
 
     abstract void createNewCurrent();
 
     public DataModel getItems() {
         if (items == null) {
             items = getPagination().createPageDataModel();
         }
         return items;
     }
 
     public SelectItem[] getItemsAvailableSelectMany() {
         return JsfUtil.getSelectItems(getFacade().findAll(), false);
     }
 
     public SelectItem[] getItemsAvailableSelectOne() {
         return JsfUtil.getSelectItems(getFacade().findAll(), true);
     }
 
     public PaginationHelper getPagination() {
         if (pagination == null) {
             pagination = new PaginationHelper(10) {
                 @Override
                 public int getItemsCount() {
                     return getFacade().count();
                 }
 
                 @Override
                 public DataModel createPageDataModel() {
                     return new ListDataModel(getFacade().findRange(new int[]{getPageFirstItem(), getPageFirstItem() + getPageSize()}));
                 }
             };
         }
         return pagination;
     }
 
     public Object getObject(Integer id) {
         return getFacade().find(id);
     }
     
     public Object getObject(String id) {
         return getFacade().find(id);
     }
 
     public String next() {
         getPagination().nextPage();
         recreateModel();
         return "List";
     }
 
     protected void performDestroy() {
         try {
             getFacade().remove(getCurrent());
             status = "Item Deleted";
             JsfUtil.addSuccessMessage(status);
         } catch (Exception e) {
             status = e + ResourceBundle.getBundle("/Bundle").
                     getString("PersistenceErrorOccured");
             JsfUtil.addErrorMessage(e, status);
         }
     }
 
     public void prepareCreate() {
         createNewCurrent();
         selectedItemIndex = -1;
     }
 
     public void prepareEdit() {
         setCurrent(getItems().getRowData());
         selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
     }
 
     public String prepareList() {
         recreateModel();
         return "List";
     }
 
     public String prepareView() {
         setCurrent(getItems().getRowData());
         selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
         return "View";
     }
 
     public String previous() {
         getPagination().previousPage();
         recreateModel();
         return "List";
     }
 
     protected void recreateModel() {
         items = null;
     }
 
     protected void recreatePagination() {
         pagination = null;
     }
 
     public void update() {
         try {
             getFacade().edit(getCurrent());
 
             status = "Item updated";
             JsfUtil.addSuccessMessage(status);
         } catch (Exception e) {
             status = e + ResourceBundle.getBundle("/Bundle").
                     getString("PersistenceErrorOccured");
             JsfUtil.addErrorMessage(e, status);
         }
     }
 
     protected void updateCurrentItem() {
         int count = getFacade().count();
         if (selectedItemIndex >= count) {
             // selected index cannot be bigger than number of items:
             selectedItemIndex = count - 1;
             // go to previous page if last page disappeared:
             if (pagination.getPageFirstItem() >= count) {
                 pagination.previousPage();
             }
         }
         if (selectedItemIndex >= 0) {
             setCurrent(getFacade().findRange(new int[]{selectedItemIndex, selectedItemIndex + 1}).
                     get(0));
         }
     }
 }

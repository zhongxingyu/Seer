 package backingbeans;
 
 import java.io.Serializable;
 import java.util.ResourceBundle;
 import javax.ejb.EJB;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.bean.RequestScoped;
 import javax.faces.bean.ViewScoped;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.convert.Converter;
 import javax.faces.convert.FacesConverter;
 import javax.faces.model.DataModel;
 import javax.faces.model.ListDataModel;
 import javax.faces.model.SelectItem;
 import javax.faces.bean.ManagedProperty;
 import javax.faces.application.FacesMessage;                                                                                                                
 
 import java.util.logging.Logger;
 import java.util.List;
 import java.util.Arrays;
 import java.util.ArrayList;
 import javax.faces.event.ActionEvent;
 import javax.annotation.PostConstruct;
 import javax.naming.InitialContext;
 import java.util.Map;
 import java.util.HashMap;
 import javax.faces.event.ComponentSystemEvent;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 import javax.servlet.http.HttpSession;
 
 import facades.ICarFacade;
 import entities.*;
 
 
 import mutil.base.ListUtil;
 
 @ManagedBean
 @ViewScoped // change that to @ViewScoped and watch it fail - this is an interesting case because broadening the scope cause the program to fail.
 public class CarsCELVController implements Serializable {
 
     private static final String CLASSNAME=CarsCELVController.class.getName();
     private static final Logger l = Logger.getLogger(CLASSNAME);
 
     @EJB(beanName = "CarFacade")
     private ICarFacade.ILocal carFacade;
 
     private CELVControllerEnum state = CELVControllerEnum.LIST;
     public CELVControllerEnum getState() { 
         return state;
     }
 
     private boolean loadDatabase = true;
 
     private String newItemModel = new String();
     
     public String getNewItemModel() {
         return newItemModel;
     }
     
     public void setNewItemModel(String newItemModel) {
         this.newItemModel = newItemModel;
     }
 
     private Integer newItemPrice = new Integer(0);
     
     public Integer getNewItemPrice() {
         return newItemPrice;
     }
     
     public void setNewItemPrice(Integer newItemPrice) {
         this.newItemPrice = newItemPrice;
     }
 
 
     private Car current;                                                                                                                                    
     public Car getCurrent() {
         l.info("***************>> get CURRENT called");
         if (current==null) {
             current = getSafe(items, 0);
         }
         return current;
     }
     public void setCurrent(Car current) {
         Car oldCurrent = this.current;
         this.current = current;
         l.info("*******************>> set CURRENT called "+String.format("current changed : %d -> %d", System.identityHashCode(oldCurrent), System.identityHashCode(current)));
         l.info("****>>    new current now is: "+current.toStringShort());
     }
 
     private CarInfo currentDetail;
     public CarInfo getCurrentDetail() {
         return currentDetail;
     }
     public void setCurrentDetail(CarInfo currentDetail) {
        this.currentDetail = currentDetail;
     }
 
 
     private CarFactory currentFactory;
     public CarFactory getCurrentFactory() {
         return currentFactory;
     }
     public void setCurrentFactory(CarFactory currentFactory) {
         this.currentFactory = currentFactory;
     }
 
 
     public String triggerLifeCycle() {
         return null;
     }
 
     public void backstepCurrent() {
         int i = 0 ; if (i==0) throw new RuntimeException("function deprecated");
         if (current==null)
             current = getCurrent();
         int _i = 0;
         for (Car car : items) {
             _i ++;
             if (car.equals(current)) break;
         }
         if (_i == 1)
             current = getSafe(items, items.size()-1); // go round back at the end
         else
             current = getSafe(items, _i-2);
     }
 
 
     public void advanceCurrent() {
         int i = 0 ; if (i==0) throw new RuntimeException("function deprecated");
         if (current==null)
             current = getCurrent();
         int _i = 0;
         for (Car car : items) {
             _i ++;
             if (car.equals(current)) break;
         }
         if (_i == items.size())
             current = getSafe(items, 0); // go round back at the beginning
         else
             current = getSafe(items, _i);
     }
 
 
     List<Car> removedItems  = new ArrayList<Car>();
     List<Car> createdItems  = new ArrayList<Car>();
     List<Car> items;
 
     private Car getSafe(List<Car> items, int i) {
         if ((items==null)||(i>(items.size()-1))) return null;
         return items.get(i);
     }
 
 
     public void synchItemsFromDB() {
         items = carFacade.findAll();
         l.info("**************** car facade returned the following items: ****************");
         int i = 0 ;
         for (Car item : items)
             l.info("car "+(i++)+" : "+item);
         removedItems  = new ArrayList();
         createdItems  = new ArrayList();
     }
 
     public List<Car> getItems() { 
         if (loadDatabase) {
             synchItemsFromDB();
             loadDatabase = false;
         }
         return items;
     }
 
     public Collection<CarInfo> getItemDetails() {
         // l.info("current is: "+System.identityHashCode(current));
         if (current != null) {
             // l.info("current's collection is: "+current.stringifyCollection());
             return current.getCarInfoCollection();
         }
         else return null;
     }
 
     public Collection<CarInfoReview> getCarInfoReviews() {
         l.info("%%%% getCarInfoReviews() called");
         if (currentDetail != null) {
             l.info("%%%%% A ");
             return currentDetail.getCarInfoReviewCollection();
         }
         else {
             l.info("%%%%%%%% B");
             return null;
         }
     }
 
     private CarInfoReview currentCarInfoReview;
     public void setCurrentCarInfoReview(CarInfoReview currentCarInfoReview) {
         this.currentCarInfoReview = currentCarInfoReview;
     }
     public CarInfoReview getCurrentCarInfoReview() {
         return currentCarInfoReview;
     }
 
     public void setItemDetails(Collection<CarInfo> carInfoCollection) {
         int i =  0 ;  if (i == 0) throw new RuntimeException("I wasn't expecting this to be called - no reason to panic -just wanted to hear about this");
     }
 
     public Collection<CarFactory> getItemFactories() {
         if (current != null) {
             return current.getCarFactoryCollection();
         }
         else return null;
     }
 
     public void setItemFactories(Collection<CarFactory> carFactoryCollection) {
         int i =  0 ;  if (i == 0) throw new RuntimeException("I wasn't expecting this to be called - no reason to panic -just wanted to hear about this");
     }
 
 
 
     public void remove() {
         ListUtil.remove(items, current);
         removedItems.add(current);
         l.info("just added: "+current+" to (tentatively) removed items");
     }
 
     public void restoreFromDB() {
         synchItemsFromDB();
     }
     
     private void removeFromDB(Car car) {
         try {
             carFacade.remove(car);     
             FacesContext.getCurrentInstance().addMessage("foo", new FacesMessage(FacesMessage.SEVERITY_INFO, "row deleted","row deleted"));
         } catch (Exception e) {
             FacesContext.getCurrentInstance().addMessage("CAR-form:messagePanel", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                                                                                    "row could not be deleted","row could not be deleted"));
         }  
     }
 
     public void commitToDB() {
         for (Car car : removedItems) {
             removeFromDB(car); 
         }
         removedItems = new ArrayList();
         for (Car car : createdItems) {
             createInDB(car);
         }
         createdItems = new ArrayList();
         for (Car car : items) {
             editInDB(car);
         }
     }
 
     public String add() {
         state = CELVControllerEnum.OPEN_FOR_CREATION;
         return null;
     }
 
     public void newItemDone() {
         Car newItem = new Car(newItemModel, newItemPrice);
         items.add(newItem);
         createdItems.add(newItem);
         newItemModel = new String();
         newItemPrice = new Integer(0);
         state = CELVControllerEnum.LIST;
     }
 
     public String newItemCancel() {
         state = CELVControllerEnum.LIST;
         return null;
     }
 
     private void createInDB(Car car) {
         try {
             carFacade.create(car);
             FacesContext.getCurrentInstance().addMessage("foo", new FacesMessage(FacesMessage.SEVERITY_INFO, "row added","row added"));
         } catch (Exception e) {
             FacesContext.getCurrentInstance().addMessage("CAR-form:messagePanel", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                                                                                    "row could not be created","row could not be created"));
         }
     }
 
     private void editInDB(Car car) {
         try {
             carFacade.edit(car);
             FacesContext.getCurrentInstance().addMessage("foo", new FacesMessage(FacesMessage.SEVERITY_INFO, "row edited","row edited"));
         } catch (Exception e) {
             FacesContext.getCurrentInstance().addMessage("CAR-form:messagePanel", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                                                                                    "row could not be edited","row could not be edited"));
         }
     }
 }
